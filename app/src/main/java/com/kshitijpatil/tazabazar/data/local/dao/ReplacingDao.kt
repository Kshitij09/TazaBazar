package com.kshitijpatil.tazabazar.data.local.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy

/**
 * Base Dao for entities using [OnConflictStrategy.REPLACE]
 */
interface ReplacingDao<E> : BaseDao<E> {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entity: List<E>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg entity: E): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: E): Long
}