package com.kshitijpatil.tazabazar.data

import androidx.room.withTransaction
import com.kshitijpatil.tazabazar.data.local.AppDatabase
import com.kshitijpatil.tazabazar.data.mapper.ProductCategoryToProductCategoryEntity
import com.kshitijpatil.tazabazar.data.mapper.ProductToProductWithInventories
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import com.kshitijpatil.tazabazar.util.NetworkUtils
import kotlinx.coroutines.withContext
import timber.log.Timber

interface ProductRepository {
    /** Get product categories, if something goes wrong, return an empty list */
    suspend fun getProductCategories(): List<ProductCategory>

    /** Get all products */
    suspend fun getAllProducts(): List<Product>

    /** Get products filtered by [category] and/or [query] */
    suspend fun getProductListBy(category: String?, query: String?): List<Product>

    suspend fun refreshProductData()
}

class ProductRepositoryImpl(
    private val productRemoteDataSource: ProductDataSource,
    private val productLocalDataSource: ProductDataSource,
    private val networkUtils: NetworkUtils,
    private val appDatabase: AppDatabase,
    private val dispatchers: AppCoroutineDispatchers,
    private val productEntityMapper: ProductToProductWithInventories,
    private val categoryEntityMapper: ProductCategoryToProductCategoryEntity
) : ProductRepository {
    override suspend fun getProductCategories(): List<ProductCategory> {
        return productLocalDataSource.getProductCategories()
    }

    override suspend fun getAllProducts(): List<Product> {
        return withContext(dispatchers.io) {
            productLocalDataSource.getAllProducts()
        }
    }

    override suspend fun getProductListBy(
        category: String?,
        query: String?
    ): List<Product> {
        return withContext(dispatchers.io) {
            Timber.d("Retrieving products for category: $category , query: $query")
            productLocalDataSource.getProductsBy(category, query)
        }
    }

    override suspend fun refreshProductData() {
        withContext(dispatchers.io) {
            if (!networkUtils.hasNetworkConnection()) {
                Timber.d("Failed to refresh! No internet connection")
                return@withContext
            }
            Timber.d("Synchronising Product Categories")
            val remoteData = productRemoteDataSource.getProductCategories()
                .map(categoryEntityMapper::map)
            Timber.d("Received ${remoteData.size} categories from the remote source")
            appDatabase.productCategoryDao.deleteAll()
            appDatabase.productCategoryDao.insertAll(remoteData)

            Timber.d("Synchronising Product and Inventories")
            val remoteProducts = productRemoteDataSource.getAllProducts()
                .map(productEntityMapper::map)
            Timber.d("Received ${remoteProducts.size} products from the remote source")
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
        }
    }
}