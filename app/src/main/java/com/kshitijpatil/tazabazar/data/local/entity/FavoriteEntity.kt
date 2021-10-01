package com.kshitijpatil.tazabazar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.DatabaseView
import androidx.room.Entity

enum class FavoriteType {
    WEEKLY, MONTHLY
}

@Entity(primaryKeys = ["type", "product_sku"], tableName = "favorite")
data class FavoriteEntity(
    val type: FavoriteType,
    @ColumnInfo(name = "product_sku")
    val productSku: String
)

@DatabaseView(
    "SELECT product_sku FROM favorite WHERE type = 'WEEKLY'",
    viewName = "weekly_favorite"
)
data class WeeklyFavorite(
    @ColumnInfo(name = "product_sku")
    val productSku: String
)

@DatabaseView(
    "SELECT product_sku FROM favorite WHERE type = 'MONTHLY'",
    viewName = "monthly_favorite"
)
data class MonthlyFavorite(
    @ColumnInfo(name = "product_sku")
    val productSku: String
)