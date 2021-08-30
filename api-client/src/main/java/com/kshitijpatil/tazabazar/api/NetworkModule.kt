package com.kshitijpatil.tazabazar.api

object NetworkModule {
    fun provideCacheInterceptor(expiryDurationInMinutes: Int): CacheInterceptor {
        return CacheInterceptor(expiryDurationInMinutes)
    }
}