package com.kshitijpatil.tazabazar.data.local

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ProductCategoryDao : UpsertBaseDao<ProductCategoryEntity> {
    @Query("SELECT * FROM product_category")
    suspend fun getAllCategories(): List<ProductCategoryEntity>

    @Query("SELECT * FROM product_category WHERE label = :label")
    suspend fun getCategoryByLabel(label: String): ProductCategoryEntity?
}