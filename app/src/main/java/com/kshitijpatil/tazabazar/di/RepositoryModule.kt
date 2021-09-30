package com.kshitijpatil.tazabazar.di

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Room
import com.kshitijpatil.tazabazar.api.ApiModule
import com.kshitijpatil.tazabazar.api.ProductApi
import com.kshitijpatil.tazabazar.data.ProductDataSource
import com.kshitijpatil.tazabazar.data.ProductRepository
import com.kshitijpatil.tazabazar.data.ProductRepositoryImpl
import com.kshitijpatil.tazabazar.data.local.AppDatabase
import com.kshitijpatil.tazabazar.data.local.ProductLocalDataSource
import com.kshitijpatil.tazabazar.data.network.ProductRemoteDataSource
import com.kshitijpatil.tazabazar.util.NetworkUtils
import okhttp3.OkHttpClient

object RepositoryModule {
    private val appDispatchers = AppModule.provideAppCoroutineDispatchers()

    private val lock = Any()
    private var database: AppDatabase? = null

    @Volatile
    var productRepository: ProductRepository? = null
        @VisibleForTesting set


    fun provideProductRepository(context: Context, okhttpClient: OkHttpClient): ProductRepository {
        synchronized(lock) {
            return productRepository ?: createProductRepository(context, okhttpClient)
        }
    }

    fun createProductRepository(context: Context, okhttpClient: OkHttpClient): ProductRepository {
        val appDatabase = database ?: createDatabase(context)
        val api = ApiModule.provideProductApi(okhttpClient)
        val networkUtils = provideNetworkUtils(context)
        val newRepo = ProductRepositoryImpl(
            provideRemoteDataSource(api),
            provideLocalDataSource(appDatabase),
            networkUtils,
            appDatabase,
            appDispatchers,
            MapperModule.productToProductWithInventories,
            MapperModule.productWithInventoriesToProduct,
            MapperModule.productCategoryToProductCategoryEntity
        )
        productRepository = newRepo
        return newRepo
    }

    fun provideLocalDataSource(appDatabase: AppDatabase): ProductDataSource {
        return ProductLocalDataSource(
            favoriteDao = appDatabase.favoriteDao,
            productMapper = MapperModule.productWithInventoriesAndFavoritesToProduct,
            productCategoryDao = appDatabase.productCategoryDao
        )
    }

    fun provideRemoteDataSource(api: ProductApi): ProductDataSource {
        return ProductRemoteDataSource(
            productApi = api,
            categoryMapper = MapperModule.productCategoryDtoToProductCategory,
            productMapper = MapperModule.productResponseToProduct
        )
    }

    fun createDatabase(context: Context): AppDatabase {
        val result =
            Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.databaseName)
                .fallbackToDestructiveMigration()
                .build()
        database = result
        return result
    }

    fun provideNetworkUtils(context: Context): NetworkUtils = NetworkUtils(context)

    fun provideProductCacheExpiryMillis(): Long = 30 * 60 * 1000L // 30 minutes
}