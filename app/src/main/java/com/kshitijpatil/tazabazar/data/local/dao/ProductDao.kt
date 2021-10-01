package com.kshitijpatil.tazabazar.data.local.dao

import androidx.room.*
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteEntity
import com.kshitijpatil.tazabazar.data.local.entity.InventoryEntity
import com.kshitijpatil.tazabazar.data.local.entity.ProductEntity
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

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductWithInventoriesAndFavorites(
        product: ProductEntity,
        inventories: List<InventoryEntity>,
        favorites: List<FavoriteEntity>
    )

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductWithFavorites(
        product: ProductEntity,
        favorites: List<FavoriteEntity>
    )

    @Query("DELETE FROM product WHERE sku= :sku")
    suspend fun deleteBySku(sku: String)

    @Query("DELETE FROM product")
    suspend fun deleteAll()

    @Query("SELECT * FROM product")
    suspend fun getAllProducts(): List<ProductEntity>

    @Query("SELECT * FROM product")
    fun observeAllProducts(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM product WHERE sku = :sku")
    suspend fun getProductBySku(sku: String): ProductEntity?

    @Query("SELECT * FROM product WHERE name LIKE :name")
    suspend fun getProductsByName(name: String): List<ProductEntity>

    @Query("SELECT * FROM product WHERE sku IN (:productSkus)")
    suspend fun getProductsBySkus(productSkus: List<String>): List<ProductEntity>

    @Query("SELECT * FROM product WHERE category = :category")
    suspend fun getProductsByCategory(category: String): List<ProductEntity>
}