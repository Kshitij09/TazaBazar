package com.kshitijpatil.tazabazar.data

import com.kshitijpatil.tazabazar.api.OrderApi
import com.kshitijpatil.tazabazar.api.dto.OrderLineDto
import com.kshitijpatil.tazabazar.data.local.dao.InventoryDao
import com.kshitijpatil.tazabazar.data.local.entity.InventoryEntity
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStore
import com.kshitijpatil.tazabazar.data.mapper.OrderResponseToOrderMapper
import com.kshitijpatil.tazabazar.di.OrderApiFactory
import com.kshitijpatil.tazabazar.model.CartItem
import com.kshitijpatil.tazabazar.model.Order
import com.kshitijpatil.tazabazar.model.OrderLine
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import org.threeten.bp.DateTimeException
import timber.log.Timber

abstract class OrderRepository {
    protected abstract var orderApi: OrderApi?
    abstract suspend fun placeOrder(cartItems: List<CartItem>)
    abstract suspend fun getOrdersOfCurrentUser(): List<Order>
}

class OrderRepositoryImpl(
    externalScope: CoroutineScope,
    private val dispatchers: AppCoroutineDispatchers,
    private val orderApiFactory: OrderApiFactory,
    private val orderMapper: OrderResponseToOrderMapper,
    private val inventoryDao: InventoryDao,
    private val authPreferenceStore: AuthPreferenceStore,
) : OrderRepository() {
    override var orderApi: OrderApi? = null

    init {
        externalScope.launch(dispatchers.io) { observeAccessTokenToUpdateApi() }
    }

    private suspend fun observeAccessTokenToUpdateApi() {
        authPreferenceStore.observeAccessToken().collect {
            orderApi = if (it == null) {
                Timber.d("Access Token not found, resetting OrderApi")
                null
            } else {
                orderApiFactory.create(it)
            }
        }
    }

    override suspend fun placeOrder(cartItems: List<CartItem>) {
        val api = checkNotNull(orderApi) { "OrderApi was null, can't place an order" }
        Timber.d("Placing an order with ${cartItems.size} items")
        val orderLines = cartItems.map(CartItem::makeOrderLine)
        withContext(dispatchers.io) {
            api.placeOrder(orderLines)
        }
    }

    /**
     * Returns the orders of current user, with total calculated using
     * local inventory details. Note: Orders with non-local inventories
     * are skipped from the final result
     */
    @Throws(
        IllegalStateException::class,
        IOException::class,
        DateTimeException::class,
        IllegalArgumentException::class
    )
    override suspend fun getOrdersOfCurrentUser(): List<Order> {
        val loggedInUser = authPreferenceStore.getLoggedInUser().orNull()
        checkNotNull(loggedInUser) { "LoggedInUser must not be null to fetch orders" }
        val api = checkNotNull(orderApi) { "OrderApi was null, make sure user is Logged-In" }
        val fetchedOrders =
            withContext(dispatchers.io) { api.getOrdersByUsername(loggedInUser.email) }
        val computedOrders = withContext(dispatchers.computation) {
            fetchedOrders.map(orderMapper::map)
                .map { associateWithInventories(it) }
                .filter(::skipOrderWithMissingInventories)
                .map(::updateOrderTotal)
        }
        return computedOrders
    }

    private suspend fun associateWithInventories(order: Order): Pair<Order, List<InventoryEntity>> {
        val invIds = order.orderLines.map(OrderLine::inventoryId)
        val inventories = inventoryDao.getInventoriesByIds(invIds)
        return Pair(order, inventories)
    }

    private fun skipOrderWithMissingInventories(orderInvPair: Pair<Order, List<InventoryEntity>>): Boolean {
        val (order, inventories) = orderInvPair
        if (order.orderLines.size != inventories.size) {
            Timber.d("Skipping Order: ${order.orderId} due to missing inventories")
            return false
        }
        return true
    }

    private fun updateOrderTotal(orderInvPair: Pair<Order, List<InventoryEntity>>): Order {
        val (order, inventories) = orderInvPair
        val orderTotal = order.orderLines.zip(inventories)
            .map { (ol, inv) -> ol.quantity * inv.price }
            .sum()
        return order.copy(total = orderTotal)
    }
}

private fun CartItem.makeOrderLine(): OrderLineDto {
    return OrderLineDto(
        inventoryId = inventoryId,
        quantity = quantity
    )
}