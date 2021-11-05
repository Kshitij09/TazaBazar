package com.kshitijpatil.tazabazar.di

import android.content.Context
import com.kshitijpatil.tazabazar.base.SingletonHolder
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.LoggingEventListener
import java.io.File
import java.util.concurrent.TimeUnit

interface OkHttpModule {
    val okHttpClient: OkHttpClient
    val httpLoggingInterceptor: HttpLoggingInterceptor?
    val loggingEventListenerFactory: LoggingEventListener.Factory?
}

open class OkhttpClientWithCacheProviderMixin(appContext: Context) {
    private object OkhttpClientHolder : SingletonHolder<OkHttpClient, Context>({
        val cacheSize = 50 * 1024 * 1024L // 50 MB
        val httpCacheDirectory = File(it.cacheDir, "http-cache")
        val cache = Cache(httpCacheDirectory, cacheSize)

        println("OkHttpClient created")
        OkHttpClient.Builder()
            .cache(cache)
            .connectionPool(ConnectionPool(10, 2, TimeUnit.MINUTES))
            .dispatcher(
                Dispatcher().apply {
                    // Allow for high number of concurrent image fetches on same host.
                    maxRequestsPerHost = 15
                }
            ).build()
    })

    protected val clientWithCache = OkhttpClientHolder.getInstance(appContext)
}

class DebugOkHttpClientModuleImpl(appContext: Context) : OkHttpModule,
    OkhttpClientWithCacheProviderMixin(appContext) {
    override val httpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
    }
    override val loggingEventListenerFactory by lazy {
        LoggingEventListener.Factory()
    }
    override val okHttpClient: OkHttpClient
        get() = clientWithCache
            .newBuilder()
            .addInterceptor(httpLoggingInterceptor)
            .eventListenerFactory(loggingEventListenerFactory)
            .build()
}

class ProdOkhttpClientModuleImpl(appContext: Context) : OkHttpModule,
    OkhttpClientWithCacheProviderMixin(appContext) {
    override val httpLoggingInterceptor: HttpLoggingInterceptor? = null
    override val loggingEventListenerFactory: LoggingEventListener.Factory? = null
    override val okHttpClient = clientWithCache
}