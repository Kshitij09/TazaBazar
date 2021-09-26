package com.kshitijpatil.tazabazar.data.local

import androidx.room.*

/**
 * Base Dao for entities using [OnConflictStrategy.IGNORE]
 * and need default implementation for upsert operation
 */
interface UpsertBaseDao<E> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entity: List<E>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg entity: E): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: E): Long

    @Update
    suspend fun update(entity: E)

    @Update
    suspend fun updateAll(entities: List<E>)

    @Delete
    suspend fun delete(entity: E): Int

    @Delete
    suspend fun deleteAll(entities: List<E>)

}

suspend inline fun <E> UpsertBaseDao<E>.upsert(entity: E) {
    val rowid = insert(entity)
    // item was not inserted
    if (rowid == -1L) {
        update(entity)
    }
}

@Transaction
suspend inline fun <E> UpsertBaseDao<E>.upsertAll(entities: List<E>) {
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