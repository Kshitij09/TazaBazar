package com.kshitijpatil.tazabazar.data.local

import androidx.room.withTransaction

class RoomTransactionRunner(private val db: TazaBazarRoomDatabase) : TransactionRunner {
    override suspend fun <R> invoke(block: suspend () -> R): R {
        return db.withTransaction(block)
    }
}