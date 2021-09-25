package com.kshitijpatil.tazabazar.di

import android.content.Context
import com.kshitijpatil.tazabazar.api.ApiModule
import com.kshitijpatil.tazabazar.data.ProductRepository
import com.kshitijpatil.tazabazar.data.ProductRepositoryImpl
import com.kshitijpatil.tazabazar.data.local.AppDatabase
import com.kshitijpatil.tazabazar.data.local.ProductLocalDataSource
import com.kshitijpatil.tazabazar.data.network.ProductRemoteDataSource
import com.kshitijpatil.tazabazar.util.NetworkUtils
import okhttp3.OkHttpClient

object RepositoryModule {
    private val appDispatchers = AppModule.provideAppCoroutineDispatchers()

    fun provideProductRepository(context: Context, okhttpClient: OkHttpClient): ProductRepository {
        val api = ApiModule.provideProductApi(okhttpClient)
        val remoteDataSource = ProductRemoteDataSource(
            productApi = api,
            categoryMapper = MapperModule.productCategoryDtoToProductCategory,
            productMapper = MapperModule.productResponseToProduct
        )
        val appDatabase = AppDatabase.buildDatabase(context)
        val localDataSource = ProductLocalDataSource(
            productDao = appDatabase.productDao,
            productMapper = MapperModule.ProductWithInventoriesToProduct,
            productCategoryDao = appDatabase.productCategoryDao
        )
        val networkUtils = provideNetworkUtils(context)
        return ProductRepositoryImpl(
            remoteDataSource,
            localDataSource,
            networkUtils,
            appDatabase,
            appDispatchers,
            MapperModule.productToProductWithInventories,
            MapperModule.productCategoryToProductCategoryEntity,
            provideProductCacheExpiryMillis()
        )
    }

    fun provideNetworkUtils(context: Context): NetworkUtils = NetworkUtils(context)

    fun provideProductCacheExpiryMillis(): Long = 30 * 60 * 1000L // 30 minutes
}