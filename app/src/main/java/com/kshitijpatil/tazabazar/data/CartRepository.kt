package com.kshitijpatil.tazabazar.data

import com.kshitijpatil.tazabazar.data.local.dao.CartItemDao
import com.kshitijpatil.tazabazar.data.local.dao.upsert
import com.kshitijpatil.tazabazar.data.local.entity.CartItemEntity
import com.kshitijpatil.tazabazar.data.mapper.CartItemDetailViewToCartItem
import com.kshitijpatil.tazabazar.model.CartConfiguration
import com.kshitijpatil.tazabazar.model.CartItem
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import timber.log.Timber

interface CartRepository {
    /**
     * Add or Update Cart Item with given [inventoryId]
     * @return (Boolean) - Whether item was added to cart
     */
    suspend fun addOrUpdateCartItem(inventoryId: Int, quantity: Int = 1): Boolean

    /** Remove [CartItem] with given [inventoryId] from the cart */
    suspend fun removeFromCart(inventoryId: Int)

    /** Get all Cart Items stored in the local store */
    suspend fun getAllCartItems(): List<CartItem>

    suspend fun getCartConfiguration(): CartConfiguration

    fun observeCartItemCount(): Flow<Int>

    suspend fun clearCart()
}

class CartRepositoryImpl(
    private val cartItemDao: CartItemDao,
    private val dispatchers: AppCoroutineDispatchers,
    private val cartItemMapper: CartItemDetailViewToCartItem
) : CartRepository {
    // TODO: Check for quantity here
    override suspend fun addOrUpdateCartItem(inventoryId: Int, quantity: Int): Boolean {
        val cartItemEntity = CartItemEntity(inventoryId, quantity)
        Timber.d("Saving CartItemEntity: $cartItemEntity")
        return withContext(dispatchers.io) {
            cartItemDao.upsert(cartItemEntity)
        }
    }

    override suspend fun removeFromCart(inventoryId: Int) {
        Timber.d("Deleting CartItemEntity with inventoryId=$inventoryId")
        return withContext(dispatchers.io) {
            cartItemDao.deleteById(inventoryId)
        }
    }

    override suspend fun getAllCartItems(): List<CartItem> {
        return withContext(dispatchers.io) {
            cartItemDao.getAllCartDetailViews().map(cartItemMapper::map)
        }
    }

    // TODO: Fetch it from the server and cache in some preference store
    override suspend fun getCartConfiguration(): CartConfiguration {
        Timber.d("Fetching cart configuration")
        return withContext(dispatchers.io) {
            delay(500) // Fake delay
            CartConfiguration(
                maxQuantityPerItem = 6,
                deliveryCharges = 15f
            )
        }
    }

    override fun observeCartItemCount(): Flow<Int> {
        return cartItemDao.observeCartItemCount().flowOn(dispatchers.io)
    }

    override suspend fun clearCart() {
        withContext(dispatchers.io) {
            cartItemDao.deleteAll()
        }
    }

}