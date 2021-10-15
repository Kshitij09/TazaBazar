package com.kshitijpatil.tazabazar

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import androidx.work.DelegatingWorkerFactory
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kshitijpatil.tazabazar.di.WorkerModule
import kotlinx.coroutines.MainScope
import timber.log.Timber

class TazaBazarApplication : Application(), Configuration.Provider {
    val coroutineScope = MainScope()
    override fun onCreate() {
        super.onCreate()
        AndroidThreeTen.init(this)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
        val logLevel = if (BuildConfig.DEBUG) Log.DEBUG else Log.ERROR
        val delegatingWorkerFactory = DelegatingWorkerFactory().apply {
            addFactory(WorkerModule.provideRefreshTokenWorkerFactory())
        }
        return Configuration.Builder()
            .setMinimumLoggingLevel(logLevel)
            .setWorkerFactory(delegatingWorkerFactory)
            .build()
    }
}