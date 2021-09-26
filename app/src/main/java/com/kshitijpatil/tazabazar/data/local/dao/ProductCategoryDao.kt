package com.kshitijpatil.tazabazar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.kshitijpatil.tazabazar.data.local.ProductCategoryEntity

@Dao
interface ProductCategoryDao : ReplacingDao<ProductCategoryEntity> {
    @Query("SELECT * FROM product_category")
    suspend fun getAllCategories(): List<ProductCategoryEntity>

    @Query("SELECT * FROM product_category WHERE label = :label")
    suspend fun getCategoryByLabel(label: String): ProductCategoryEntity?
}