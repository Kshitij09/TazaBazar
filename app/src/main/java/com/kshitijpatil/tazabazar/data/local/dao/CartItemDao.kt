package com.kshitijpatil.tazabazar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.kshitijpatil.tazabazar.data.local.entity.CartItemDetailView
import com.kshitijpatil.tazabazar.data.local.entity.CartItemEntity

@Dao
interface CartItemDao : UpsertDao<CartItemEntity> {

    @Query("SELECT * FROM cart_item")
    suspend fun getAllCartItems(): List<CartItemEntity>

    @Query("SELECT * FROM cart_item_detail_view")
    suspend fun getAllCartDetailViews(): List<CartItemDetailView>

    @Query("SELECT * FROM cart_item WHERE inventory_id = :inventoryId")
    suspend fun getCartItemById(inventoryId: Int): CartItemEntity?

    @Query("SELECT * FROM cart_item_detail_view WHERE inventory_id = :inventoryId")
    suspend fun getCartItemDetailViewById(inventoryId: Int): CartItemDetailView?
}