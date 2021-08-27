package com.kshitijpatil.tazabazar.di

import com.kshitijpatil.tazabazar.api.ApiModule
import com.kshitijpatil.tazabazar.data.ProductRepository
import okhttp3.OkHttpClient

object RepositoryModule {
    private val appDispatchers = AppModule.provideAppCoroutineDispatchers()
    fun provideProductRepository(okhttpClient: OkHttpClient): ProductRepository {
        return ProductRepository(ApiModule.provideTazaBazarApi(okhttpClient), appDispatchers)
    }
}