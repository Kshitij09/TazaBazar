package com.kshitijpatil.tazabazar.data

import androidx.room.withTransaction
import com.kshitijpatil.tazabazar.data.local.AppDatabase
import com.kshitijpatil.tazabazar.data.mapper.ProductCategoryToProductCategoryEntity
import com.kshitijpatil.tazabazar.data.mapper.ProductToProductWithInventories
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import com.kshitijpatil.tazabazar.util.NetworkUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.System.currentTimeMillis

interface ProductRepository {
    /** Get product categories, if something goes wrong, return an empty list */
    suspend fun getProductCategories(): List<ProductCategory>

    /** Get all products */
    suspend fun getAllProducts(): List<Product>

    /** Get products filtered by [category] and/or [query] */
    suspend fun getProductListBy(category: String?, query: String?): List<Product>

    suspend fun refreshProductCache()
}

class ProductRepositoryImpl(
    private val productRemoteDataSource: ProductDataSource,
    private val productLocalDataSource: ProductDataSource,
    private val networkUtils: NetworkUtils,
    private val appDatabase: AppDatabase,
    private val dispatchers: AppCoroutineDispatchers,
    private val productEntityMapper: ProductToProductWithInventories,
    private val categoryEntityMapper: ProductCategoryToProductCategoryEntity,
    private val productCacheExpiryMillis: Long
) : ProductRepository {
    private var categoryFirstRun = true
    private val mutex = Mutex()
    override suspend fun getProductCategories(): List<ProductCategory> {
        return if (!networkUtils.hasNetworkConnection()) {
            Timber.d("Network not connected")
            withContext(dispatchers.io) {
                productLocalDataSource.getProductCategories()
            }
        } else {
            if (categoryFirstRun) {
                Timber.d("Caching Product categories...")
                withContext(dispatchers.io) {
                    val remoteData = productRemoteDataSource.getProductCategories()
                        .map(categoryEntityMapper::map)
                    appDatabase.productCategoryDao.insertAll(remoteData)
                    categoryFirstRun = false
                }
            }
            productLocalDataSource.getProductCategories()
        }
    }

    // TODO: Persist this in stored preferences
    private var productsLastSynced: Long? = null

    override suspend fun getAllProducts(): List<Product> {
        if (networkUtils.hasNetworkConnection()) {
            refreshProductCacheIfExpired()
        }
        return withContext(dispatchers.io) { productLocalDataSource.getAllProducts() }
    }

    private suspend fun refreshProductCacheIfExpired() {
        val cacheExpired =
            productsLastSynced == null || currentTimeMillis() - productsLastSynced!! > productCacheExpiryMillis
        if (cacheExpired) {
            Timber.d("Product Cache expired! fetching products from the remote source")
            refreshProductCache()
        }
    }

    override suspend fun getProductListBy(
        category: String?,
        query: String?
    ): List<Product> {
        return withContext(dispatchers.io) {
            if (networkUtils.hasNetworkConnection()) {
                refreshProductCacheIfExpired()
            }
            Timber.d("Retrieving products for category: $category , query: $query")
            productLocalDataSource.getProductsBy(category, query)
        }
    }

    override suspend fun refreshProductCache() = withContext(dispatchers.io) {
        val remoteProducts = productRemoteDataSource.getAllProducts()
            .map(productEntityMapper::map)
        val allInventories = remoteProducts
            .map { it.inventories }
            .flatten()
            .toList()
        appDatabase.withTransaction {
            appDatabase.productDao.deleteAll() // To avoid any inconsistencies
            // NO for insert in for-loop
            appDatabase.productDao.insertAll(remoteProducts.map { it.product })
            appDatabase.inventoryDao.insertAll(allInventories)
        }
        mutex.withLock {
            productsLastSynced = currentTimeMillis()
        }
    }
}