package com.kshitijpatil.tazabazar.data.local.dao

import androidx.room.*
import com.kshitijpatil.tazabazar.data.local.InventoryEntity
import com.kshitijpatil.tazabazar.data.local.ProductEntity
import com.kshitijpatil.tazabazar.data.local.ProductWithInventories
import kotlinx.coroutines.flow.Flow

/**
 * The Data Access Object for [ProductEntity] class
 */
@Dao
interface ProductDao : ReplacingDao<ProductEntity> {
    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductAndInventories(
        product: ProductEntity,
        inventories: List<InventoryEntity>
    )

    @Query("DELETE FROM product WHERE sku= :sku")
    suspend fun deleteBySku(sku: String)

    @Query("DELETE FROM product")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM product")
    suspend fun getAllProductWithInventories(): List<ProductWithInventories>

    @Transaction
    @Query("SELECT * FROM product")
    fun observeAllProductWithInventories(): Flow<List<ProductWithInventories>>

    @Query("SELECT * FROM product")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM product")
    fun observeAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product WHERE sku = :sku")
    suspend fun getProductBySku(sku: String): ProductEntity?

    @Query("SELECT * FROM product WHERE name LIKE :name")
    suspend fun getProductsByName(name: String): List<ProductEntity>

    @Transaction
    @Query("SELECT * FROM product WHERE name LIKE :name")
    suspend fun getProductWithInventoriesByName(name: String): List<ProductWithInventories>

    @Query("SELECT * FROM product WHERE sku IN (:productSkus)")
    suspend fun getProductsBySkus(productSkus: List<String>): List<ProductEntity>

    @Query("SELECT * FROM product WHERE category = :category")
    suspend fun getProductsByCategory(category: String): List<ProductEntity>

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