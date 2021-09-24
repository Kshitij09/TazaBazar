package com.kshitijpatil.tazabazar.data.local

import androidx.room.*

/**
 * Base Dao for entities using [OnConflictStrategy.IGNORE]
 * and need default implementation for upsert operation
 */
interface UpsertBaseDao<E> {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(entity: List<E>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(vararg entity: E)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: E): Long

    @Update
    suspend fun update(entity: E)

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
    entities.forEach { upsert(it) }
}