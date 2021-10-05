package com.kshitijpatil.tazabazar.di

import android.content.Context
import com.kshitijpatil.tazabazar.BuildConfig
import com.kshitijpatil.tazabazar.base.SingletonHolder
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.LoggingEventListener
import java.io.File
import java.util.concurrent.TimeUnit

object AppModule {
    object OkhttpClientHolder : SingletonHolder<OkHttpClient, Context>({ okhttpClientFactory(it) })

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
        return OkhttpClientHolder.getInstance(context)
            .newBuilder()
            .apply {
                if (loggingInterceptor != null)
                    addInterceptor(loggingInterceptor)
                if (eventListener != null)
                    eventListenerFactory(eventListener)
            }.build()
    }

    fun okhttpClientFactory(context: Context): OkHttpClient {
        val cacheSize = 50 * 1024 * 1024L // 50 MB
        val httpCacheDirectory = File(context.cacheDir, "http-cache")
        val cache = Cache(httpCacheDirectory, cacheSize)
        return OkHttpClient.Builder()
            .cache(cache)
            .connectionPool(ConnectionPool(10, 2, TimeUnit.MINUTES))
            .dispatcher(
                Dispatcher().apply {
                    // Allow for high number of concurrent image fetches on same host.
                    maxRequestsPerHost = 15
                }
            ).build()
    }
}