package com.kshitijpatil.tazabazar.di

import com.kshitijpatil.tazabazar.api.ApiModule
import com.kshitijpatil.tazabazar.data.ProductRepository

object RepositoryModule {
    private val appDispatchers = AppModule.provideAppCoroutineDispatchers()
    fun provideProductRepository(): ProductRepository {
        return ProductRepository(ApiModule.tazaBazarApi, appDispatchers)
    }
}