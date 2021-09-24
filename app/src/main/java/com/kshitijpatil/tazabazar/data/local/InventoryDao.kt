package com.kshitijpatil.tazabazar.data.local

import androidx.room.Dao
import androidx.room.Query

@Dao
interface InventoryDao : UpsertBaseDao<InventoryEntity> {
    @Query("SELECT * FROM inventory WHERE id = :id")
    suspend fun getInventoryById(id: Int): InventoryEntity?
}
