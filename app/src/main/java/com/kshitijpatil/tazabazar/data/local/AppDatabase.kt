package com.kshitijpatil.tazabazar.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kshitijpatil.tazabazar.data.local.dao.FavoriteDao
import com.kshitijpatil.tazabazar.data.local.dao.InventoryDao
import com.kshitijpatil.tazabazar.data.local.dao.ProductCategoryDao
import com.kshitijpatil.tazabazar.data.local.dao.ProductDao
import com.kshitijpatil.tazabazar.data.local.entity.*

@Database(
    entities = [ProductEntity::class, InventoryEntity::class, ProductCategoryEntity::class, FavoriteEntity::class],
    views = [WeeklyFavorite::class, MonthlyFavorite::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 2, to = 3)
    ]
)
@TypeConverters(TazaBazarTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val productDao: ProductDao
    abstract val inventoryDao: InventoryDao
    abstract val productCategoryDao: ProductCategoryDao
    abstract val favoriteDao: FavoriteDao

    companion object {
        const val databaseName = "tazabazaar-db"
    }
}