package com.kshitijpatil.tazabazar.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.kshitijpatil.tazabazar.data.local.dao.InventoryDao
import com.kshitijpatil.tazabazar.data.local.dao.ProductCategoryDao
import com.kshitijpatil.tazabazar.data.local.dao.ProductDao

@Database(
    entities = [ProductEntity::class, InventoryEntity::class, ProductCategoryEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(TazaBazarTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val productDao: ProductDao
    abstract val inventoryDao: InventoryDao
    abstract val productCategoryDao: ProductCategoryDao

    companion object {
        const val databaseName = "tazabazaar-db"
    }
}