package com.kshitijpatil.tazabazar.data

import androidx.room.withTransaction
import com.kshitijpatil.tazabazar.data.local.AppDatabase
import com.kshitijpatil.tazabazar.data.local.entity.*
import com.kshitijpatil.tazabazar.data.mapper.InventoryToInventoryEntity
import com.kshitijpatil.tazabazar.data.mapper.ProductCategoryToProductCategoryEntity
import com.kshitijpatil.tazabazar.data.mapper.ProductToProductWithInventories
import com.kshitijpatil.tazabazar.data.mapper.ProductWithInventoriesToProduct
import com.kshitijpatil.tazabazar.model.Inventory
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

interface ProductRepository {
    /** Get product categories, if something goes wrong, return an empty list */
    suspend fun getProductCategories(forceRefresh: Boolean = false): List<ProductCategory>

    /** Get all products */
    suspend fun getAllProducts(forceRefresh: Boolean = false): List<Product>

    /** Get products filtered by [favoriteType] and/or [query] */
    suspend fun getProductListBy(favoriteType: FavoriteType, query: String? = null): List<Product>

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
    private val productRemoteSource: ProductDataSource,
    private val productLocalDataSource: ProductDataSource,
    private val appDatabase: AppDatabase,
    private val dispatchers: AppCoroutineDispatchers,
    private val productEntityMapper: ProductToProductWithInventories,
    private val productMapper: ProductWithInventoriesToProduct,
    private val inventoryEntityMapper: InventoryToInventoryEntity,
    private val categoryEntityMapper: ProductCategoryToProductCategoryEntity
) : ProductRepository {
    private val productDao = appDatabase.productDao
    private val productCategoryDao = appDatabase.productCategoryDao
    private val inventoryDao = appDatabase.inventoryDao

    private val productSyncer = ItemSyncer<ProductEntity, Product, String>(
        insertEntity = productDao::insert,
        updateEntity = productDao::update,
        deleteEntity = productDao::delete,
        localEntityToKey = { it.sku },
        networkEntityToKey = { it.sku },
        networkEntityToLocalEntity = { entity, _ -> productEntityMapper.map(entity).product }
    )

    private val productCategorySyncer = ItemSyncer<ProductCategoryEntity, ProductCategory, String>(
        insertEntity = productCategoryDao::insert,
        updateEntity = productCategoryDao::update,
        deleteEntity = productCategoryDao::delete,
        localEntityToKey = { it.label },
        networkEntityToKey = { it.label },
        networkEntityToLocalEntity = { entity, _ -> categoryEntityMapper.map(entity) }
    )

    private val inventorySyncer = ItemSyncer<InventoryEntity, Inventory, Int>(
        insertEntity = inventoryDao::insert,
        updateEntity = inventoryDao::update,
        deleteEntity = inventoryDao::delete,
        localEntityToKey = { it.id },
        networkEntityToKey = { it.id },
        networkEntityToLocalEntity = { entity, _ -> inventoryEntityMapper.map(entity) }
    )

    override suspend fun getProductCategories(forceRefresh: Boolean): List<ProductCategory> {
        if (forceRefresh) refreshProductCategories()
        return withContext(dispatchers.io) {
            productLocalDataSource.getProductCategories()
        }
    }

    override suspend fun getAllProducts(forceRefresh: Boolean): List<Product> {
        if (forceRefresh) refreshProducts()
        return withContext(dispatchers.io) {
            productLocalDataSource.getAllProducts()
        }
    }

    // TODO: Extract into an use-case
    override suspend fun getProductListBy(
        favoriteType: FavoriteType,
        query: String?
    ): List<Product> {
        Timber.d("Retrieving products for favoriteType: $favoriteType , query: $query")
        val productEntities = withContext(dispatchers.io) {
            if (query == null) {
                when (favoriteType) {
                    FavoriteType.WEEKLY -> appDatabase.favoriteDao.getWeeklyFavoriteProductWithInventories()
                    FavoriteType.MONTHLY -> appDatabase.favoriteDao.getMonthlyFavoriteProductWithInventories()
                }
            } else {
                when (favoriteType) {
                    FavoriteType.WEEKLY -> appDatabase.favoriteDao.getWeeklyFavoriteProductWithInventoriesByName(
                        "%$query%"
                    )
                    FavoriteType.MONTHLY -> appDatabase.favoriteDao.getMonthlyFavoriteProductWithInventoriesByName(
                        "%$query%"
                    )
                }
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

    private suspend fun <T> runCatchingRemoteSource(getRemoteData: suspend ProductDataSource.() -> T): T? {
        return withContext(dispatchers.io) {
            runCatching { getRemoteData(productRemoteSource) }
        }.getOrNull()
    }

    override suspend fun getProductListBy(
        category: String?,
        query: String?,
        forceRefresh: Boolean
    ): List<Product> {
        if (forceRefresh) {
            val remoteProducts = runCatchingRemoteSource { getProductsBy(category, query) }
            if (remoteProducts != null) {
                selectiveSyncProductAndInventories(remoteProducts)
                return remoteProducts
            } else {
                Timber.d("Failed retrieving products from the remote source, defaulting to local source")
            }
        }
        return withContext(dispatchers.io) {
            Timber.d("Retrieving products for category: $category , query: $query")
            productLocalDataSource.getProductsBy(category, query)
        }
    }

    private suspend fun selectiveSyncProductAndInventories(remoteProducts: List<Product>) {
        withContext(dispatchers.io) {
            val mappedProductWithInventories = remoteProducts.map(productEntityMapper::map)
            val allInventories = mappedProductWithInventories
                .map { it.inventories }
                .flatten()
                .toList()

            // REPLACE strategy will make sure to delete the
            // the inventories of changed products due to CASCADE
            // behaviour on the InventoryEntity
            appDatabase.withTransaction {
                appDatabase.productDao.insertAll(mappedProductWithInventories.map { it.product })
                appDatabase.inventoryDao.insertAll(allInventories)
            }
        }
    }

    private suspend fun refreshProductCategories() {
        withContext(dispatchers.io) {
            Timber.d("Synchronising Product Categories")
            val remoteCategories = runCatchingRemoteSource { getProductCategories() }
            if (remoteCategories != null) {
                Timber.d("Received ${remoteCategories.size} categories from the remote source")
                val localCategories = appDatabase.productCategoryDao.getAllCategories()
                productCategorySyncer.sync(
                    localCategories,
                    remoteCategories,
                    removeNotMatched = true
                )
            } else {
                Timber.d("Failed retrieving product categories from the remote source, skipping the sync operation")
            }
        }
    }

    private suspend fun refreshProducts() {
        withContext(dispatchers.io) {
            Timber.d("Synchronising Products")
            val remoteProducts = runCatchingRemoteSource { getAllProducts() }
            if (remoteProducts != null) {
                Timber.d("Received ${remoteProducts.size} products from the remote source")
                val localProducts = productDao.getAllProducts()
                productSyncer.sync(localProducts, remoteProducts, removeNotMatched = true)
                val remoteInventories = remoteProducts.map { it.inventories }.flatten()
                refreshInventories(remoteInventories)
            } else {
                Timber.d("Failed retrieving products from the remote source, skipping the sync operation")
            }
        }
    }

    private suspend fun refreshInventories(remoteInventories: List<Inventory>) {
        withContext(dispatchers.io) {
            Timber.d("Synchronising Product Inventories")
            val localInventories = appDatabase.inventoryDao.getAllInventories()
            Timber.d("Received ${remoteInventories.size} inventories from the remote source")
            inventorySyncer.sync(localInventories, remoteInventories, removeNotMatched = true)
        }
    }

    override suspend fun refreshProductData() {
        refreshProductCategories()
        refreshProducts()
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