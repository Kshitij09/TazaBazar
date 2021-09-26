package com.kshitijpatil.tazabazar.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.kshitijpatil.tazabazar.data.local.InventoryEntity

@Dao
interface InventoryDao : ReplacingDao<InventoryEntity> {
    @Query("SELECT * FROM inventory WHERE id = :id")
    suspend fun getInventoryById(id: Int): InventoryEntity?

    @Query("SELECT * FROM inventory")
    suspend fun getAllInventories(): List<InventoryEntity>
}
