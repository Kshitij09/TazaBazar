package com.kshitijpatil.tazabazar.data.network

import android.content.Context
import com.kshitijpatil.tazabazar.BuildConfig
import com.kshitijpatil.tazabazar.base.SingletonHolder
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

internal class OkhttpClientManager private constructor() {

    companion object : SingletonHolder<OkHttpClient, Context>({
        val cacheDurationInMinutes = 15
        val cacheSize = 50 * 1024 * 1024L // 50 MB
        val httpCacheDirectory = File(it.cacheDir, "http-cache")
        val cache = Cache(httpCacheDirectory, cacheSize)
        val okhttpBuilder = OkHttpClient.Builder()
            .addNetworkInterceptor(CacheInterceptor(cacheDurationInMinutes))
            .cache(cache)
            .connectionPool(ConnectionPool(10, 2, TimeUnit.MINUTES))
            .dispatcher(
                Dispatcher().apply {
                    // Allow for high number of concurrent image fetches on same host.
                    maxRequestsPerHost = 15
                }
            )
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            okhttpBuilder.addInterceptor(loggingInterceptor)
        }
        okhttpBuilder.build()
    })
}