package com.kshitijpatil.tazabazar.data.network

import android.content.Context
import com.kshitijpatil.tazabazar.base.SingletonHolder
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/** Provides default okHttpClient with caching enabled */
internal class OkhttpClientManager private constructor() {

    companion object : SingletonHolder<OkHttpClient, Context>({
        val cacheSize = 50 * 1024 * 1024L // 50 MB
        val httpCacheDirectory = File(it.cacheDir, "http-cache")
        val cache = Cache(httpCacheDirectory, cacheSize)
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
}