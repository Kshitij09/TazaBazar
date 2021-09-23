package com.kshitijpatil.tazabazar.di

import com.kshitijpatil.tazabazar.api.ApiModule
import com.kshitijpatil.tazabazar.data.ProductRepository
import com.kshitijpatil.tazabazar.data.ProductRepositoryImpl
import okhttp3.OkHttpClient

object RepositoryModule {
    private val appDispatchers = AppModule.provideAppCoroutineDispatchers()
    fun provideProductRepository(okhttpClient: OkHttpClient): ProductRepository {
        val api = ApiModule.provideProductApi(okhttpClient)
        return ProductRepositoryImpl(api, appDispatchers)
    }
}