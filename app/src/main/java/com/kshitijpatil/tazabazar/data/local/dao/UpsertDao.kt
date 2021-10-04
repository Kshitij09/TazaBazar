package com.kshitijpatil.tazabazar.data.local.dao

import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Transaction

/**
 * Base Dao for entities using [OnConflictStrategy.IGNORE]
 * and need default implementation for upsert operation
 */
interface UpsertDao<E> : BaseDao<E> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entity: List<E>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg entity: E): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: E): Long
}

suspend inline fun <E> UpsertDao<E>.upsert(entity: E): Boolean {
    val rowid = insert(entity)
    // item was not inserted
    return if (rowid == -1L) {
        update(entity)
        false
    } else {
        true
    }
}

@Transaction
suspend inline fun <E> UpsertDao<E>.upsertAll(entities: List<E>) {
    val insertResult = insertAll(entities)
    val updateList = mutableListOf<E>()
    for (i in entities.indices) {
        // insert didn't occur
        if (insertResult[i] == -1L)
            updateList.add(entities[i])
    }
    if (updateList.isNotEmpty())
        updateAll(updateList)
}