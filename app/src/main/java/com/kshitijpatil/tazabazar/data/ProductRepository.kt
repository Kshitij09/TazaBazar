package com.kshitijpatil.tazabazar.data

import androidx.room.withTransaction
import com.kshitijpatil.tazabazar.data.local.AppDatabase
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteEntity
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import com.kshitijpatil.tazabazar.data.mapper.ProductCategoryToProductCategoryEntity
import com.kshitijpatil.tazabazar.data.mapper.ProductToProductWithInventories
import com.kshitijpatil.tazabazar.data.mapper.ProductWithInventoriesToProduct
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import com.kshitijpatil.tazabazar.util.NetworkUtils
import kotlinx.coroutines.withContext
import timber.log.Timber

interface ProductRepository {
    /** Get product categories, if something goes wrong, return an empty list */
    suspend fun getProductCategories(forceRefresh: Boolean = false): List<ProductCategory>

    /** Get all products */
    suspend fun getAllProducts(forceRefresh: Boolean = false): List<Product>

    suspend fun getProductsByFavoriteType(favoriteType: FavoriteType): List<Product>

    /** Get products filtered by [category] and/or [query] */
    suspend fun getProductListBy(
        category: String?,
        query: String?,
        forceRefresh: Boolean = false
    ): List<Product>

    suspend fun refreshProductData()

    /** Update Favorite Choices of a Product with given sku */
    suspend fun updateFavorites(productSku: String, favoriteChoices: Set<FavoriteType>)
}

class ProductRepositoryImpl(
    private val productRemoteDataSource: ProductDataSource,
    private val productLocalDataSource: ProductDataSource,
    private val networkUtils: NetworkUtils,
    private val appDatabase: AppDatabase,
    private val dispatchers: AppCoroutineDispatchers,
    private val productEntityMapper: ProductToProductWithInventories,
    private val productMapper: ProductWithInventoriesToProduct,
    private val categoryEntityMapper: ProductCategoryToProductCategoryEntity
) : ProductRepository {
    override suspend fun getProductCategories(forceRefresh: Boolean): List<ProductCategory> {
        if (forceRefresh) refreshProductData()
        return withContext(dispatchers.io) {
            productLocalDataSource.getProductCategories()
        }
    }

    override suspend fun getAllProducts(forceRefresh: Boolean): List<Product> {
        if (forceRefresh) refreshProductData()
        return withContext(dispatchers.io) {
            productLocalDataSource.getAllProducts()
        }
    }

    override suspend fun getProductsByFavoriteType(favoriteType: FavoriteType): List<Product> {
        val productEntities = withContext(dispatchers.io) {
            when (favoriteType) {
                FavoriteType.WEEKLY -> appDatabase.favoriteDao.getWeeklyFavoriteProductWithInventories()
                FavoriteType.MONTHLY -> appDatabase.favoriteDao.getMonthlyFavoriteProductWithInventories()
            }
        }
        return productEntities
            .map(productMapper::map)
            .map {
                when (favoriteType) {
                    FavoriteType.WEEKLY -> it.copy(favorites = setOf(FavoriteType.WEEKLY))
                    FavoriteType.MONTHLY -> it.copy(favorites = setOf(FavoriteType.MONTHLY))
                }
            }
    }

    override suspend fun getProductListBy(
        category: String?,
        query: String?,
        forceRefresh: Boolean
    ): List<Product> {
        if (forceRefresh) refreshProductData()
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

            Timber.d("Synchronising Product and Inventories")
            val remoteProducts = productRemoteDataSource.getAllProducts()
                .map(productEntityMapper::map)
            Timber.d("Received ${remoteProducts.size} products from the remote source")
            val allInventories = remoteProducts
                .map { it.inventories }
                .flatten()
                .toList()
            appDatabase.withTransaction {
                // NOTE: Cascading
                appDatabase.productCategoryDao.deleteAll() // To avoid any inconsistencies
                appDatabase.productCategoryDao.insertAll(remoteData)
                // NO for insert in for-loop
                appDatabase.productDao.insertAll(remoteProducts.map { it.product })
                appDatabase.inventoryDao.insertAll(allInventories)
            }
        }
    }

    override suspend fun updateFavorites(productSku: String, favoriteChoices: Set<FavoriteType>) {
        Timber.d("Updating favorites for productSku=$productSku to $favoriteChoices")
        withContext(dispatchers.io) {
            appDatabase.withTransaction {
                appDatabase.favoriteDao.deleteFavoritesBySku(productSku)
                val favoriteEntities = favoriteChoices.map { FavoriteEntity(it, productSku) }
                appDatabase.favoriteDao.insertAll(favoriteEntities)
            }
        }
    }
}