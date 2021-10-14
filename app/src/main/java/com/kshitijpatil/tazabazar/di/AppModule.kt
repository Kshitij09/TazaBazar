package com.kshitijpatil.tazabazar.di

import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.Dispatchers

object AppModule {
    fun provideAppCoroutineDispatchers(): AppCoroutineDispatchers {
        return AppCoroutineDispatchers(
            Dispatchers.IO,
            Dispatchers.Default,
            Dispatchers.Main
        )
    }
}