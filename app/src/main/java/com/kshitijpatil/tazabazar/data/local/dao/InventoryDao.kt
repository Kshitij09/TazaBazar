package com.kshitijpatil.tazabazar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.kshitijpatil.tazabazar.data.local.entity.InventoryEntity
import com.kshitijpatil.tazabazar.data.local.entity.ProductWithInventories
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao : ReplacingDao<InventoryEntity> {
    @Query("SELECT * FROM inventory WHERE id = :id")
    suspend fun getInventoryById(id: Int): InventoryEntity?

    @Query("SELECT * FROM inventory WHERE id IN (:inventoryIds)")
    suspend fun getInventoriesByIds(inventoryIds: List<Int>): List<InventoryEntity>

    @Query("SELECT * FROM inventory")
    suspend fun getAllInventories(): List<InventoryEntity>

    @Transaction
    @Query("SELECT * FROM product")
    suspend fun getAllProductWithInventories(): List<ProductWithInventories>

    @Transaction
    @Query("SELECT * FROM product")
    fun observeAllProductWithInventories(): Flow<List<ProductWithInventories>>

    @Transaction
    @Query("SELECT * FROM product WHERE name LIKE :name")
    suspend fun getProductWithInventoriesByName(name: String): List<ProductWithInventories>

    @Transaction
    @Query("SELECT * FROM product WHERE category = :category")
    suspend fun getProductWithInventoriesByCategory(category: String): List<ProductWithInventories>

    @Transaction
    @Query("SELECT * FROM product WHERE category = :category AND name LIKE :name")
    suspend fun getProductsByCategoryAndName(
        category: String,
        name: String
    ): List<ProductWithInventories>
}
