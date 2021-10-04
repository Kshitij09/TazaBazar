package com.kshitijpatil.tazabazar.data

import com.kshitijpatil.tazabazar.data.local.dao.CartItemDao
import com.kshitijpatil.tazabazar.data.local.dao.upsert
import com.kshitijpatil.tazabazar.data.local.entity.CartItemEntity
import com.kshitijpatil.tazabazar.data.mapper.CartItemDetailViewToCartItem
import com.kshitijpatil.tazabazar.model.CartItem
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
}

class CartRepositoryImpl(
    private val cartItemDao: CartItemDao,
    private val cartItemMapper: CartItemDetailViewToCartItem
) : CartRepository {
    // TODO: Check for quantity here
    override suspend fun addOrUpdateCartItem(inventoryId: Int, quantity: Int): Boolean {
        val cartItemEntity = CartItemEntity(inventoryId, quantity)
        Timber.d("Saving CartItemEntity: $cartItemEntity")
        return cartItemDao.upsert(cartItemEntity)
    }

    override suspend fun removeFromCart(inventoryId: Int) {
        Timber.d("Deleting CartItemEntity with inventoryId=$inventoryId")
        cartItemDao.deleteById(inventoryId)
    }

    override suspend fun getAllCartItems(): List<CartItem> {
        return cartItemDao.getAllCartDetailViews().map(cartItemMapper::map)
    }

}