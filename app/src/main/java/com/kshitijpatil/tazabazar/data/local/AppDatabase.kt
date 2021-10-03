package com.kshitijpatil.tazabazar.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kshitijpatil.tazabazar.data.local.dao.*
import com.kshitijpatil.tazabazar.data.local.entity.*

@Database(
    entities = [
        ProductEntity::class,
        InventoryEntity::class,
        ProductCategoryEntity::class,
        FavoriteEntity::class,
        CartItemEntity::class
    ],
    views = [
        WeeklyFavorite::class,
        MonthlyFavorite::class,
        CartItemDetailView::class
    ],
    version = 5,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 4, to = 5)
    ]
)
@TypeConverters(TazaBazarTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract val productDao: ProductDao
    abstract val inventoryDao: InventoryDao
    abstract val productCategoryDao: ProductCategoryDao
    abstract val favoriteDao: FavoriteDao
    abstract val cartItemDao: CartItemDao

    companion object {
        const val databaseName = "tazabazaar-db"
    }
}