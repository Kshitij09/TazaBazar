package com.kshitijpatil.tazabazar.data.local

import android.database.sqlite.SQLiteConstraintException
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.data.local.entity.*
import com.kshitijpatil.tazabazar.fixtures.product.*
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val scope = TestCoroutineScope()
    private val appDatabase = TestInject.appDatabase(ApplicationProvider.getApplicationContext())
    private val productDao = appDatabase.productDao
    private val inventoryDao = appDatabase.inventoryDao
    private val productCategoryDao = appDatabase.productCategoryDao
    private val favoriteDao = appDatabase.favoriteDao

    private suspend fun insertVegetablesCategory() {
        productCategoryDao.insert(vegetables)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertProduct_withoutCategory_shouldRaiseSQLConstraintException() {
        scope.runBlockingTest {
            val product = tomatoRed
            productDao.insert(product)
        }
    }

    @Test
    fun insertProduct() = scope.runBlockingTest {
        insertVegetablesCategory()
        val product = tomatoRed
        productDao.insert(product)
        val retrieved = productDao.getAllProducts()
        assertThat(retrieved).hasSize(1)
        assertThat(retrieved[0]).isEqualTo(product)
    }

    @Test
    fun insertProductWithInventories() = scope.runBlockingTest {
        insertVegetablesCategory()
        val product = tomatoRed
        val inv = tomatoRedInv1
        productDao.insertProductAndInventories(product, listOf(inv))
        val retrieved = inventoryDao.getAllProductWithInventories()
        assertThat(retrieved).hasSize(1)
        val savedProductWithInventories = retrieved[0]
        assertThat(savedProductWithInventories.product).isEqualTo(product)
        assertThat(savedProductWithInventories.inventories).hasSize(1)
        assertThat(savedProductWithInventories.inventories[0]).isEqualTo(inv)
    }

    @Test
    fun insertProductWithFavorites() = scope.runBlockingTest {
        insertVegetablesCategory()
        val product = tomatoRed;
        val product2 = tomatoGreen
        val weeklyFavorite = FavoriteEntity(FavoriteType.WEEKLY, product.sku)
        productDao.insertProductWithFavorites(product, listOf(weeklyFavorite))
        productDao.insert(product2)

        val expectedEntities = listOf(
            ProductWithInventoriesAndFavorites(product, favorites = listOf(weeklyFavorite)),
            ProductWithInventoriesAndFavorites(product2)
        )
        var retrieved = favoriteDao.getAllProductWithInventoriesAndFavorites()
        assertThat(retrieved).containsExactlyElementsIn(expectedEntities)

        // since all products are vegetables only
        retrieved = favoriteDao.getProductWithInventoriesAndFavoritesByCategory(vegetables.label)
        assertThat(retrieved).containsExactlyElementsIn(expectedEntities)

        // again, both contain 'tomato' in their names
        retrieved = favoriteDao.getProductWithInventoriesAndFavoritesByName("%tomato%")
        assertThat(retrieved).containsExactlyElementsIn(expectedEntities)

        retrieved = favoriteDao.getProductsWithInventoriesAndFavoritesByCategoryAndName(
            vegetables.label,
            tomatoGreen.name
        )
        assertThat(retrieved).containsExactly(ProductWithInventoriesAndFavorites(tomatoGreen))
    }

    @Test
    fun getProductBySku() = scope.runBlockingTest {
        insertVegetablesCategory()
        val product = tomatoRed
        productDao.insert(product)
        val reloaded = productDao.getProductBySku(product.sku)
        assertThat(reloaded).isEqualTo(product)
    }

    // Also covers the update operation
    @Test
    fun insertProduct_whenExists_shouldGetReplaced() = scope.runBlockingTest {
        insertVegetablesCategory()
        var product = tomatoRed
        val inv = tomatoRedInv1
        val rowId = productDao.insertProductAndInventories(product, listOf(inv))
        assertThat(rowId).isNotEqualTo(-1)
        // re-inserting same entity would be skipped
        product = product.copy(name = "Tomato Red")
        // insert should replace when an entity already exists
        productDao.insert(product)
        val reloaded = productDao.getProductBySku(product.sku)
        assertThat(reloaded).isEqualTo(product)
        // replacing a product will also delete the associated inventories
        // this is expected behaviour assuming entities will be replaced
        // altogether most the times
        assertThat(inventoryDao.getInventoryById(inv.id)).isNull()
    }

    private suspend fun insertAndAssertProduct(product: ProductEntity) {
        insertVegetablesCategory()
        productDao.insert(product)
        assertThat(productDao.getProductBySku(product.sku)).isEqualTo(product)
    }

    @Test
    fun deleteProduct() = scope.runBlockingTest {
        insertVegetablesCategory()
        // Given
        val product = tomatoRed
        insertAndAssertProduct(product)
        // Test
        productDao.delete(product)
        assertThat(productDao.getProductBySku(product.sku)).isNull()
        // Given
        insertAndAssertProduct(product)
        // Test
        productDao.deleteBySku(product.sku)
        assertThat(productDao.getProductBySku(product.sku)).isNull()
        // Given
        insertAndAssertProduct(product)
        // Test
        productDao.deleteAll()
        assertThat(inventoryDao.getAllProductWithInventories()).isEmpty()
    }

    @Test
    fun deleteAllProducts_shouldCascadeInventories() = scope.runBlockingTest {
        insertVegetablesCategory()
        productDao.insertProductAndInventories(tomatoRed, listOf(tomatoRedInv1, tomatoRedInv2))
        productDao.insertProductAndInventories(tomatoGreen, listOf(tomatoGreenInv1))
        assertThat(inventoryDao.getAllInventories()).isNotEmpty()
        productDao.deleteAll()
        assertThat(productDao.getAllProducts()).isEmpty()
        assertThat(inventoryDao.getAllInventories()).isEmpty()
    }

    @Test
    fun getProductsByName() = scope.runBlockingTest {
        insertVegetablesCategory()
        val product1 = tomatoRed
        val product2 = tomatoGreen
        productDao.insertAll(product1, product2)
        val reloaded = productDao.getProductsByName("%tomato%")
        assertThat(reloaded).containsExactly(product1, product2)
    }

    @Test
    fun getProductsBySkus() = scope.runBlockingTest {
        insertVegetablesCategory()
        val product1 = tomatoRed
        val product2 = tomatoGreen
        productDao.insertAll(product1, product2)
        val reloaded = productDao.getProductsBySkus(listOf(product1.sku, product2.sku))
        assertThat(reloaded).containsExactly(product1, product2)
    }

    @Test
    fun getProductsByCategory() = scope.runBlockingTest {
        insertVegetablesCategory()
        val products = listOf(tomatoRed, tomatoGreen)
        productDao.insertAll(products)
        val reloaded = productDao.getProductsByCategory(vegetables.label)
        assertThat(reloaded).containsExactlyElementsIn(products)
    }

    @Test
    fun getProductsByCategoryAndName() = scope.runBlockingTest {
        insertVegetablesCategory()
        val products = listOf(tomatoRed, tomatoGreen)
        productDao.insertAll(products)
        var reloaded = inventoryDao.getProductsByCategoryAndName(vegetables.label, "%red%")
        assertThat(reloaded).containsExactly(ProductWithInventories(tomatoRed))
        reloaded = inventoryDao.getProductsByCategoryAndName(vegetables.label, "%green%")
        assertThat(reloaded).containsExactly(ProductWithInventories(tomatoGreen))
    }

    @Test
    fun observeAllProducts_whenProductUpdates_shouldEmitNewList() {
        val product1 = tomatoRed
        val product2 = tomatoGreen
        scope.runBlockingTest {
            insertVegetablesCategory()
            productDao.observeAllProducts().test {
                assertThat(awaitItem()).isEmpty()
                productDao.insert(product1)
                assertThat(awaitItem()).containsExactly(product1)
                productDao.insert(product2)
                assertThat(awaitItem()).containsExactly(product1, product2)
                val product1Updated = product1.copy(name = "Red Tomato")
                productDao.update(product1Updated)
                assertThat(awaitItem()).containsExactly(product1Updated, product2)
            }
        }
    }

    @Test
    fun observeAllProductWithInventories_whenProductOrInventoryUpdates_shouldEmitNewList() {
        val product1 = tomatoRed
        val inv1 = tomatoRedInv1
        scope.runBlockingTest {
            insertVegetablesCategory()
            inventoryDao.observeAllProductWithInventories().test {
                assertThat(awaitItem()).isEmpty()
                productDao.insert(product1)
                var actual = awaitItem()
                assertThat(actual).containsExactly(ProductWithInventories(product1))
                inventoryDao.insert(inv1)
                assertThat(awaitItem()).containsExactly(
                    ProductWithInventories(
                        product1,
                        listOf(inv1)
                    )
                )
                val updatedInventory = inv1.copy(stockAvailable = 0)
                inventoryDao.update(updatedInventory)
                assertThat(awaitItem()).containsExactly(
                    ProductWithInventories(
                        product1,
                        listOf(updatedInventory)
                    )
                )
            }
        }
    }

    @After
    fun tearDown() {
        scope.cleanupTestCoroutines()
        appDatabase.close()
    }
}