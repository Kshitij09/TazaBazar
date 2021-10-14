package com.kshitijpatil.tazabazar.di

import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.Dispatchers

object AppModule {
    private val appDispatchers = AppCoroutineDispatchers(
        Dispatchers.IO,
        Dispatchers.Default,
        Dispatchers.Main
    )

    fun provideAppCoroutineDispatchers() = appDispatchers
    fun provideIoDispatcher() = appDispatchers.io
    fun provideComputationDispatcher() = appDispatchers.computation
    fun provideMainDispatcher() = appDispatchers.main
}