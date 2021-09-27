package com.kshitijpatil.tazabazar.data.local

import android.content.Context
import androidx.room.Room
import java.util.concurrent.Executor

object TestInject {
    // In case transaction tests start causing issue with runBlockingTest
    // setTransactionExecutor(Executors.newSingleThreadExecutor())
    fun appDatabase(context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    fun appDatabase(context: Context, transactionExecutor: Executor): AppDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .setTransactionExecutor(transactionExecutor)
            .build()
    }
}