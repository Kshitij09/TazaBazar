package com.kshitijpatil.tazabazar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_category")
data class ProductCategoryEntity(
    @PrimaryKey val label: String,
    val name: String,
    @ColumnInfo(name = "sku_prefix") val skuPrefix: String
)