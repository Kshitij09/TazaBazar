package com.kshitijpatil.tazabazar.div2

import android.content.Context
import androidx.room.Room
import com.kshitijpatil.tazabazar.data.local.RoomTransactionRunner
import com.kshitijpatil.tazabazar.data.local.TazaBazarDatabase
import com.kshitijpatil.tazabazar.data.local.TazaBazarRoomDatabase
import com.kshitijpatil.tazabazar.data.local.TransactionRunner

interface DatabaseModule {
    val tazaBazarDatabase: TazaBazarDatabase
    val transactionRunner: TransactionRunner
}

class DatabaseModuleImpl(appContext: Context) : DatabaseModule {

    private val _roomDatabase: TazaBazarRoomDatabase by lazy {
        createRoomDatabase(appContext)
    }
    override val tazaBazarDatabase: TazaBazarDatabase
        get() = _roomDatabase

    private fun createRoomDatabase(context: Context): TazaBazarRoomDatabase {
        return Room.databaseBuilder(
            context,
            TazaBazarRoomDatabase::class.java,
            TazaBazarRoomDatabase.databaseName
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    override val transactionRunner: TransactionRunner by lazy {
        RoomTransactionRunner(_roomDatabase)
    }
}