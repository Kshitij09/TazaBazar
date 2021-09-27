package com.kshitijpatil.tazabazar.data.local.dao

import androidx.room.Delete
import androidx.room.Update

/**
 * Base Dao for all the invariant operations
 */
interface BaseDao<E> {
    @Update
    suspend fun update(entity: E)

    @Update
    suspend fun updateAll(entities: List<E>)

    @Delete
    suspend fun delete(entity: E): Int

    @Delete
    suspend fun deleteAll(entities: List<E>)
}