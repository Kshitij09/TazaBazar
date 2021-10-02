package com.kshitijpatil.tazabazar.data.local.dao

import androidx.room.*
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteEntity
import com.kshitijpatil.tazabazar.data.local.entity.ProductWithInventories
import com.kshitijpatil.tazabazar.data.local.entity.ProductWithInventoriesAndFavorites
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(favoriteEntity: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg favoriteEntities: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(favoriteEntities: List<FavoriteEntity>)

    @Delete
    suspend fun delete(favoriteEntity: FavoriteEntity)

    @Query("DELETE FROM favorite WHERE product_sku = :productSku")
    suspend fun deleteFavoritesBySku(productSku: String)

    @Query("SELECT * FROM favorite")
    suspend fun getAllFavorites(): List<FavoriteEntity>

    @Transaction
    @Query("SELECT * FROM product")
    suspend fun getAllProductWithInventoriesAndFavorites(): List<ProductWithInventoriesAndFavorites>

    @Transaction
    @Query("SELECT * FROM product")
    fun observeAllProductWithInventoriesAndFavorites(): Flow<List<ProductWithInventoriesAndFavorites>>

    @Query("SELECT * FROM favorite WHERE product_sku = :productSku")
    suspend fun getAllFavoritesBySku(productSku: String): List<FavoriteEntity>

    @Transaction
    @Query("SELECT * FROM product WHERE category = :category")
    suspend fun getProductWithInventoriesAndFavoritesByCategory(category: String): List<ProductWithInventoriesAndFavorites>

    @Transaction
    @Query("SELECT * FROM product WHERE name LIKE :name")
    suspend fun getProductWithInventoriesAndFavoritesByName(name: String): List<ProductWithInventoriesAndFavorites>

    @Transaction
    @Query("SELECT * FROM product WHERE category = :category AND name LIKE :name")
    suspend fun getProductsWithInventoriesAndFavoritesByCategoryAndName(
        category: String,
        name: String
    ): List<ProductWithInventoriesAndFavorites>

    @Transaction
    @Query("SELECT * FROM product INNER JOIN weekly_favorite ON product.sku=weekly_favorite.product_sku")
    suspend fun getWeeklyFavoriteProductWithInventories(): List<ProductWithInventories>

    @Transaction
    @Query(
        """
        SELECT * FROM product
        INNER JOIN weekly_favorite ON product.sku=weekly_favorite.product_sku
        WHERE name LIKE :name
    """
    )
    suspend fun getWeeklyFavoriteProductWithInventoriesByName(name: String): List<ProductWithInventories>

    @Transaction
    @Query("SELECT * FROM product INNER JOIN monthly_favorite ON product.sku=monthly_favorite.product_sku")
    suspend fun getMonthlyFavoriteProductWithInventories(): List<ProductWithInventories>

    @Transaction
    @Query(
        """
        SELECT * FROM product
        INNER JOIN monthly_favorite ON product.sku=monthly_favorite.product_sku
        WHERE name LIKE :name
    """
    )
    suspend fun getMonthlyFavoriteProductWithInventoriesByName(name: String): List<ProductWithInventories>
}