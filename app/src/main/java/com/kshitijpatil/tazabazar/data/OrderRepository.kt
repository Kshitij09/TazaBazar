package com.kshitijpatil.tazabazar.data

import com.kshitijpatil.tazabazar.api.ApiModule
import com.kshitijpatil.tazabazar.api.OrderApi
import com.kshitijpatil.tazabazar.api.dto.OrderLine
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStore
import com.kshitijpatil.tazabazar.model.CartItem
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import timber.log.Timber

abstract class OrderRepository {
    protected abstract var orderApi: OrderApi?
    abstract suspend fun placeOrder(cartItems: List<CartItem>)
}

class OrderRepositoryImpl(
    externalScope: CoroutineScope,
    private val dispatchers: AppCoroutineDispatchers,
    private val client: OkHttpClient,
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
                createOrderApi(it)
            }
        }
    }

    private fun createOrderApi(accessToken: String): OrderApi {
        return ApiModule.provideOrderApi(client, accessToken)
    }

    override suspend fun placeOrder(cartItems: List<CartItem>) {
        val api = checkNotNull(orderApi) { "OrderApi was null, can't place an order" }
        Timber.d("Placing an order with ${cartItems.size} items")
        val orderLines = cartItems.map(CartItem::makeOrderLine)
        withContext(dispatchers.io) {
            api.placeOrder(orderLines)
        }
    }
}

private fun CartItem.makeOrderLine(): OrderLine {
    return OrderLine(
        inventoryId = inventoryId,
        quantity = quantity
    )
}