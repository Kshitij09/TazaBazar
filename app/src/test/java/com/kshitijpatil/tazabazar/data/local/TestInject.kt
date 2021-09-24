package com.kshitijpatil.tazabazar.data.local

import android.content.Context
import androidx.room.Room

object TestInject {
    // In case transaction tests start causing issue with runBlockingTest
    // setTransactionExecutor(Executors.newSingleThreadExecutor())
    fun appDatabase(context: Context): AppDatabase {
        return Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }
}