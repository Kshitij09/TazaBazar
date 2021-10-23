package com.kshitijpatil.tazabazar.data

import arrow.core.left
import arrow.core.right
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.api.OrderApi
import com.kshitijpatil.tazabazar.api.dto.OrderLineDto
import com.kshitijpatil.tazabazar.api.dto.OrderResponse
import com.kshitijpatil.tazabazar.data.local.TazaBazarTypeConverters
import com.kshitijpatil.tazabazar.data.local.dao.InventoryDao
import com.kshitijpatil.tazabazar.data.local.entity.InventoryEntity
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStore
import com.kshitijpatil.tazabazar.di.MapperModule
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.model.Order
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.threeten.bp.OffsetDateTime
import java.io.IOException


class OrderRepositoryImplTest {
    private lateinit var repo: OrderRepository
    private val testDispatcher = TestCoroutineDispatcher()
    private val testScope = TestCoroutineScope(testDispatcher)
    private val appDispatchers = AppCoroutineDispatchers(
        testDispatcher, testDispatcher, testDispatcher
    )
    private val orderMapper = MapperModule.orderMapper

    @Test
    fun getOrdersOfCurrentUser_happyPath() {
        val orderApi = FakeOrderApi(OrderSession.validOrderDtos)
        val authPrefs: AuthPreferenceStore = mock {
            onBlocking { observeAccessToken() } doReturn flowOf(OrderSession.accessToken)
            onBlocking { getLoggedInUser() } doReturn OrderSession.user.right()
        }
        val mockInventoryDao = getMockInventoryDaoFor(OrderSession.inventoryEntities)
        repo = provideOrderRepo(orderApi, mockInventoryDao, authPrefs)
        testDispatcher.runBlockingTest {
            val expectedOrders = OrderSession.validOrderDtos
                .map(orderMapper::map)
                .map { order ->
                    val invIds = order.orderLines
                        .map { it.inventoryId }
                    val orderTotal = mockInventoryDao
                        .getInventoriesByIds(invIds)
                        .zip(order.orderLines)
                        .map { (inv, ol) -> inv.price * ol.quantity }
                        .sum()
                    order.copy(total = orderTotal)
                }

            val actualOrders = repo.getOrdersOfCurrentUser()
            assertThat(actualOrders).isNotEmpty()
            assertThat(actualOrders).containsExactlyElementsIn(expectedOrders)
        }
    }

    @Test
    fun getOrdersOfCurrentUser_whenOrderHasNonLocalInventories_shouldBeSkippedFromReturnList() {
        val inconsistentOrder = OrderSession.order3
        val orderApi = FakeOrderApi(OrderSession.validOrderDtos + inconsistentOrder)
        val authPrefs: AuthPreferenceStore = mock {
            onBlocking { observeAccessToken() } doReturn flowOf(OrderSession.accessToken)
            onBlocking { getLoggedInUser() } doReturn OrderSession.user.right()
        }
        val mockInventoryDao = getMockInventoryDaoFor(OrderSession.inventoryEntities)
        repo = provideOrderRepo(orderApi, mockInventoryDao, authPrefs)
        testDispatcher.runBlockingTest {
            val orderWithDefaultTotals = OrderSession.validOrderDtos.map(orderMapper::map)
            val expectedOrders = updateOrderWithTotals(orderWithDefaultTotals, mockInventoryDao)
            val actualOrders = repo.getOrdersOfCurrentUser()
            assertThat(actualOrders).isNotEmpty()
            assertThat(actualOrders).doesNotContain(inconsistentOrder)
            assertThat(actualOrders).containsExactlyElementsIn(expectedOrders)
        }
    }

    private suspend fun updateOrderWithTotals(
        orders: List<Order>,
        inventoryDao: InventoryDao
    ): List<Order> {
        return orders.map { order ->
            val invIds = order.orderLines
                .map { it.inventoryId }
            val orderTotal = inventoryDao
                .getInventoriesByIds(invIds)
                .zip(order.orderLines)
                .map { (inv, ol) -> inv.price * ol.quantity }
                .sum()
            order.copy(total = orderTotal)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun getOrdersOfCurrentUser_whenNotLoggedIn_throwsIllegalStateException() {
        val orderApi = FakeOrderApi(OrderSession.validOrderDtos)
        val authPrefs: AuthPreferenceStore = mock {
            onBlocking { observeAccessToken() } doReturn flowOf(OrderSession.accessToken)
            onBlocking { getLoggedInUser() } doReturn NoDataFoundException.left()
        }
        val mockInventoryDao = getMockInventoryDaoFor(OrderSession.inventoryEntities)
        repo = provideOrderRepo(orderApi, mockInventoryDao, authPrefs)
        runBlocking { repo.getOrdersOfCurrentUser() }
    }

    @Test(expected = IllegalStateException::class)
    fun getOrdersOfCurrentUser_whenAccessTokenIsNull_throwsIllegalStateException() {
        val orderApi = FakeOrderApi(OrderSession.validOrderDtos)
        val authPrefs: AuthPreferenceStore = mock {
            onBlocking { observeAccessToken() } doReturn flowOf(null)
            onBlocking { getLoggedInUser() } doReturn OrderSession.user.right()
        }
        val mockInventoryDao = getMockInventoryDaoFor(OrderSession.inventoryEntities)
        repo = provideOrderRepo(orderApi, mockInventoryDao, authPrefs)
        runBlocking { repo.getOrdersOfCurrentUser() }
    }

    fun provideOrderRepo(
        orderApi: OrderApi,
        inventoryDao: InventoryDao,
        authPreferenceStore: AuthPreferenceStore
    ): OrderRepository {
        return OrderRepositoryImpl(
            externalScope = testScope,
            dispatchers = appDispatchers,
            orderApiFactory = { orderApi },
            orderMapper = orderMapper,
            inventoryDao = inventoryDao,
            authPreferenceStore = authPreferenceStore
        )
    }

    private fun getMockInventoryDaoFor(invEntries: List<InventoryEntity>): InventoryDao {
        return mock {
            onBlocking { getInventoriesByIds(anyList()) } doAnswer {
                val invIds = it.arguments[0] as List<Int>
                invEntries.filter { inv -> invIds.contains(inv.id) }
            }
        }
    }

    object OrderSession {
        private val nowOffsetDatetime = OffsetDateTime.now()
        private val validOffsetDateTimeRaw =
            TazaBazarTypeConverters.fromOffsetDateTime(nowOffsetDatetime)!!
        private const val validStatusString = "ACCEPTED"
        const val accessToken = "sakasfj-sajbmas-sajfkaf"
        val user = LoggedInUser(
            email = "user1@test.com",
            fullName = "User 1",
            phone = "00000",
            emailVerified = false,
            phoneVerified = false
        )
        val inv1 = InventoryEntity(
            id = 1,
            productSku = "AAA",
            price = 15f,
            quantityLabel = "",
            stockAvailable = 5,
            updatedAt = OffsetDateTime.now()
        )
        val orderLine1 = OrderLineDto(inv1.id, 5)
        val inv2 = InventoryEntity(
            id = 2,
            productSku = "BBB",
            price = 30f,
            quantityLabel = "",
            stockAvailable = 5,
            updatedAt = OffsetDateTime.now()
        )
        val orderLine2 = OrderLineDto(inv2.id, 4)
        val inv3 = InventoryEntity(
            id = 3,
            productSku = "CCC",
            price = 18f,
            quantityLabel = "",
            stockAvailable = 5,
            updatedAt = OffsetDateTime.now()
        )
        val orderLine3 = OrderLineDto(inv3.id, 2)

        // NonLocal inventories
        val orderLine4 = OrderLineDto(4, 1)
        val orderLine5 = OrderLineDto(5, 1)

        val order1 = OrderResponse(
            validOffsetDateTimeRaw,
            "order-01",
            listOf(orderLine1, orderLine2),
            validStatusString,
            user.email
        )
        val order2 = OrderResponse(
            validOffsetDateTimeRaw,
            "order-02",
            listOf(orderLine2, orderLine3),
            validStatusString,
            user.email
        )

        // order with non-local inventories
        val order3 = OrderResponse(
            validOffsetDateTimeRaw,
            "order-03",
            listOf(orderLine4, orderLine5, orderLine1),
            validStatusString,
            user.email
        )
        val validOrderDtos = listOf(order1, order2)
        val inventoryEntities = listOf(inv1, inv2, inv3)
    }
}

class FakeOrderApi(private val bootstrapOrders: List<OrderResponse>? = null) : OrderApi {
    override suspend fun placeOrder(orderLines: List<OrderLineDto>): OrderResponse {
        TODO("Not yet implemented")
    }

    override suspend fun getOrderById(orderId: String): OrderResponse {
        TODO("Not yet implemented")
    }

    override suspend fun getOrdersByUsername(username: String): List<OrderResponse> {
        return bootstrapOrders ?: throw IOException("Failed to retrieve orders")
    }
}