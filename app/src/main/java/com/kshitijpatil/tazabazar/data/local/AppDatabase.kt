package com.kshitijpatil.tazabazar.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [ProductEntity::class, InventoryEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(TazaBazarTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract val productDao: ProductDao
    abstract val inventoryDao: InventoryDao

    companion object {
        private const val databaseName = "tazabazaar-db"

        fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}