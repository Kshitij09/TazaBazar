package com.kshitijpatil.tazabazar.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.data.local.TazaBazarRoomDatabase
import com.kshitijpatil.tazabazar.data.local.TestInject
import com.kshitijpatil.tazabazar.di.MapperModule
import com.kshitijpatil.tazabazar.di.RepositoryModule
import com.kshitijpatil.tazabazar.fixtures.product.*
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory
import com.kshitijpatil.tazabazar.test.util.MainCoroutineRule
import com.kshitijpatil.tazabazar.test.util.runBlockingTest
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import java.io.IOException
import java.util.concurrent.Executors

/**
 * TODO: Use Fake Database to run these tests without instrumentation
 */
@RunWith(AndroidJUnit4::class)
class ProductRepositoryImplTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Overrides Dispatchers.Main used in coroutines
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var repo: ProductRepository
    private val testDispatcher = coroutineRule.testDispatcher
    private val testAppDispatchers =
        AppCoroutineDispatchers(testDispatcher, testDispatcher, testDispatcher)

    @Test
    fun getAllProducts_shouldReturnLocalDataByDefault() {
        // Given localDataSource with all Product Entities
        val appDatabase = provideAppDatabase()
        val productMapper = MapperModule.productEntityToProduct
        val dbProducts = allProductEntities
        runBlocking {
            appDatabase.productCategoryDao.insertAll(allCategoryEntities)
            appDatabase.productDao.insertAll(dbProducts)
        }
        val localDataSource = RepositoryModule.provideLocalDataSource(appDatabase)
        repo = provideProductRepoImpl(mock(), localDataSource, appDatabase)

        coroutineRule.runBlockingTest {
            // When asked for all products
            val actualProducts = repo.getAllProducts()
            // should return the same
            val expected = dbProducts.map(productMapper::map)
            assertThat(actualProducts).containsExactlyElementsIn(expected)
        }
        appDatabase.close()
    }

    @Test(timeout = 5000L)
    fun getAllProducts_whenForceRefresh_shouldUpdateLocalSourceAndReturn() {
        val appDatabase = provideAppDatabase(withTransactionExecutor = true)
        val productMapper = MapperModule.productWithInventoriesToProduct
        val categoryMapper = MapperModule.productCategoryEntityToProductCategory
        val remoteProducts = listOf(tomatoRedProductWithInventories).map(productMapper::map)
        val remoteCategories = allCategoryEntities.map(categoryMapper::map)
        val remoteSource = FakeRemoteDataSource(remoteProducts, remoteCategories)

        val localEntities = allProductWithInventories
        runBlocking {
            appDatabase.productCategoryDao.insertAll(allCategoryEntities)
            localEntities.forEach {
                appDatabase.productDao.insertProductAndInventories(it.product, it.inventories)
            }
        }
        val localDataSource = RepositoryModule.provideLocalDataSource(appDatabase)
        repo = provideProductRepoImpl(
            remoteSource,
            localDataSource,
            appDatabase
        )

        runBlocking {
            val actual = repo.getAllProducts(forceRefresh = true)
            val unexpected = localEntities
                .map { it.product }
                .toSet()
                .subtract(remoteProducts)
            assertThat(actual).containsNoneIn(unexpected)
            assertThat(actual).containsExactlyElementsIn(remoteProducts)
        }
        appDatabase.close()
    }

    @Test
    fun getProductCategories_shouldReturnLocalDataByDefault() {
        // Given
        val appDatabase = provideAppDatabase()
        val categoryMapper = MapperModule.productCategoryEntityToProductCategory
        runBlocking { appDatabase.productCategoryDao.insertAll(allCategoryEntities) }
        val localDataSource = RepositoryModule.provideLocalDataSource(appDatabase)
        repo = provideProductRepoImpl(mock(), localDataSource, appDatabase)

        coroutineRule.runBlockingTest {
            val actual = repo.getProductCategories()
            assertThat(actual).containsExactlyElementsIn(allCategoryEntities.map(categoryMapper::map))
        }
    }

    @Test(timeout = 5000L)
    fun getProductCategories_whenForceRefresh_shouldUpdateLocalSourceAndReturn() {
        // Given local source with no entries
        val appDatabase = provideAppDatabase(withTransactionExecutor = true)
        val categoryMapper = MapperModule.productCategoryEntityToProductCategory
        val localDataSource = RepositoryModule.provideLocalDataSource(appDatabase)
        val allCategories = allCategoryEntities.map(categoryMapper::map)
        // and remote source with a Category List
        val remoteSource = FakeRemoteDataSource(emptyList(), allCategories)
        repo = provideProductRepoImpl(
            remoteSource,
            localDataSource,
            appDatabase
        )

        runBlocking {
            // when forced for fresh data
            val actual = repo.getProductCategories(forceRefresh = true)
            // should update local store and return
            assertThat(actual).containsExactlyElementsIn(allCategories)
            val entriesFromDb = appDatabase.productCategoryDao.getAllCategories()
            assertThat(entriesFromDb).containsExactlyElementsIn(allCategoryEntities)
        }
    }

    @Test(timeout = 5000L)
    fun refreshProductData_shouldFetchRemoteSource_andUpdateLocalStore() {
        // Given remote source with data and empty local source
        val appDatabase = provideAppDatabase(withTransactionExecutor = true)
        val productMapper = MapperModule.productWithInventoriesToProduct
        val categoryMapper = MapperModule.productCategoryEntityToProductCategory
        val remoteProducts = listOf(tomatoRedProductWithInventories, sitafalProductWithInventories)
        val remoteCategories = listOf(vegetables, fruits)
        val remoteSource = FakeRemoteDataSource(
            remoteProducts.map(productMapper::map),
            remoteCategories.map(categoryMapper::map)
        )
        val localSource = RepositoryModule.provideLocalDataSource(appDatabase)
        val repo =
            provideProductRepoImpl(remoteSource, localSource, appDatabase)

        runBlocking {
            // when called refresh data
            repo.refreshProductData()
            // should update local data store
            val actualCategories = appDatabase.productCategoryDao.getAllCategories()
            assertThat(actualCategories).containsExactlyElementsIn(remoteCategories)
            val actualProducts = appDatabase.inventoryDao.getAllProductWithInventories()
            assertThat(actualProducts).containsExactlyElementsIn(remoteProducts)
        }
    }

    @Test
    fun whenForceRefresh_andNotConnected_shouldFallbackToLocalSource() {
        // given local and remote sources with distinct data
        val appDatabase = provideAppDatabase()
        val productMapper = MapperModule.productWithInventoriesToProduct
        val categoryMapper = MapperModule.productCategoryEntityToProductCategory
        val remoteSource = FakeRemoteDataSource(null, null)
        val localCategories = listOf(fruits)
        val localProducts = listOf(sitafalProductWithInventories)
        runBlocking {
            appDatabase.productCategoryDao.insertAll(localCategories)
            localProducts.forEach {
                appDatabase.productDao.insertProductAndInventories(it.product, it.inventories)
            }
        }
        val localSource = RepositoryModule.provideLocalDataSource(appDatabase)
        // and network not connected
        val repo =
            provideProductRepoImpl(remoteSource, localSource, appDatabase)
        coroutineRule.runBlockingTest {
            // when forced to refresh
            val actualProducts = repo.getAllProducts(forceRefresh = true)
            // should fallback to local store
            assertThat(actualProducts).containsExactlyElementsIn(localProducts.map(productMapper::map))
            // when forced to refresh
            val actualCategories = repo.getProductCategories(forceRefresh = true)
            // should fallback to local store
            assertThat(actualCategories).containsExactlyElementsIn(
                localCategories.map(
                    categoryMapper::map
                )
            )
        }
    }

    /**
     * Handy function to construct ProductRepositoryImpl with defaults
     * and accepting parameters for varying dependencies
     */
    private fun provideProductRepoImpl(
        remoteSource: ProductDataSource,
        localSource: ProductDataSource,
        appDatabase: TazaBazarRoomDatabase
    ): ProductRepositoryImpl {
        return ProductRepositoryImpl(
            remoteSource,
            localSource,
            appDatabase,
            RepositoryModule.provideRoomTransactionRunner(context),
            testAppDispatchers,
            MapperModule.productToProductWithInventories,
            MapperModule.productWithInventoriesToProduct,
            MapperModule.inventoryToInventoryEntity,
            MapperModule.productCategoryToProductCategoryEntity
        )
    }

    private val transactionExecutor = Executors.newSingleThreadExecutor()
    private fun provideAppDatabase(withTransactionExecutor: Boolean = false): TazaBazarRoomDatabase {
        return if (withTransactionExecutor)
            TestInject.appDatabase(context, transactionExecutor)
        else
            TestInject.appDatabase(context)
    }
}

/**
 * Fake ProductDataSource which returns given products
 * when asked for all or filtered lists
 */
class FakeRemoteDataSource(
    private val products: List<Product>? = null,
    private val productCategories: List<ProductCategory>? = null
) : ProductDataSource {
    override suspend fun getProductCategories(): List<ProductCategory> {
        return productCategories ?: throw IOException()
    }

    override suspend fun getAllProducts(): List<Product> {
        return products ?: throw IOException()
    }

    override suspend fun getProductsBy(category: String?, query: String?): List<Product> {
        return products ?: throw IOException()
    }
}