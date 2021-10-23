package com.kshitijpatil.tazabazar.data

import arrow.core.left
import arrow.core.right
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.api.OrderApi
import com.kshitijpatil.tazabazar.api.dto.OrderLineDto
import com.kshitijpatil.tazabazar.api.dto.OrderResponse
import com.kshitijpatil.tazabazar.data.local.TazaBazarTypeConverters
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStore
import com.kshitijpatil.tazabazar.di.MapperModule
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
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
        repo = provideOrderRepo(orderApi, authPrefs)
        val expectedOrders = OrderSession.validOrderDtos.map(orderMapper::map)
        testDispatcher.runBlockingTest {
            val actualOrders = repo.getOrdersOfCurrentUser()
            assertThat(actualOrders).isNotEmpty()
            assertThat(actualOrders).containsExactlyElementsIn(expectedOrders)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun getOrdersOfCurrentUser_whenNotLoggedIn_throwsIllegalStateException() {
        val orderApi = FakeOrderApi(OrderSession.validOrderDtos)
        val authPrefs: AuthPreferenceStore = mock {
            onBlocking { observeAccessToken() } doReturn flowOf(OrderSession.accessToken)
            onBlocking { getLoggedInUser() } doReturn NoDataFoundException.left()
        }
        repo = provideOrderRepo(orderApi, authPrefs)
        runBlocking { repo.getOrdersOfCurrentUser() }
    }

    @Test(expected = IllegalStateException::class)
    fun getOrdersOfCurrentUser_whenAccessTokenIsNull_throwsIllegalStateException() {
        val orderApi = FakeOrderApi(OrderSession.validOrderDtos)
        val authPrefs: AuthPreferenceStore = mock {
            onBlocking { observeAccessToken() } doReturn flowOf(null)
            onBlocking { getLoggedInUser() } doReturn OrderSession.user.right()
        }
        repo = provideOrderRepo(orderApi, authPrefs)
        runBlocking { repo.getOrdersOfCurrentUser() }
    }

    fun provideOrderRepo(
        orderApi: OrderApi,
        authPreferenceStore: AuthPreferenceStore
    ): OrderRepository {
        return OrderRepositoryImpl(
            externalScope = testScope,
            dispatchers = appDispatchers,
            orderApiFactory = { orderApi },
            orderMapper = orderMapper,
            authPreferenceStore = authPreferenceStore
        )
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
        val order1 = OrderResponse(
            validOffsetDateTimeRaw,
            "order-01",
            emptyList(),
            validStatusString,
            user.email
        )
        val order2 = OrderResponse(
            validOffsetDateTimeRaw,
            "order-02",
            emptyList(),
            validStatusString,
            user.email
        )

        val validOrderDtos = listOf(order1, order2)
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