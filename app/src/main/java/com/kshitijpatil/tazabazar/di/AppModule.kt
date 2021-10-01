package com.kshitijpatil.tazabazar.di

import android.content.Context
import com.kshitijpatil.tazabazar.BuildConfig
import com.kshitijpatil.tazabazar.data.network.OkhttpClientManager
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.LoggingEventListener

object AppModule {
    fun provideAppCoroutineDispatchers(): AppCoroutineDispatchers {
        return AppCoroutineDispatchers(
            Dispatchers.IO,
            Dispatchers.Default,
            Dispatchers.Main
        )
    }

    /** Provide logging interceptor if using DEBUG variant */
    fun provideLoggingInterceptor(): HttpLoggingInterceptor? {
        return if (BuildConfig.DEBUG) {
            return HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
        } else null
    }

    /** Provide EventListener if using DEBUG variant  */
    fun provideLoggingEventListener(): LoggingEventListener.Factory? {
        return if (BuildConfig.DEBUG)
            LoggingEventListener.Factory()
        else null
    }

    fun provideOkHttpClient(
        context: Context,
    ): OkHttpClient {
        val loggingInterceptor = provideLoggingInterceptor()
        val eventListener = provideLoggingEventListener()
        return OkhttpClientManager.getInstance(context)
            .newBuilder()
            .apply {
                if (loggingInterceptor != null)
                    addInterceptor(loggingInterceptor)
                if (eventListener != null)
                    eventListenerFactory(eventListener)
            }.build()
    }
}