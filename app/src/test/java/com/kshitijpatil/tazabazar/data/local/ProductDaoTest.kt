package com.kshitijpatil.tazabazar.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.fixtures.product.tomatoGreen
import com.kshitijpatil.tazabazar.fixtures.product.tomatoRed
import com.kshitijpatil.tazabazar.fixtures.product.tomatoRedInv1
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

    @Test
    fun insertProduct() = scope.runBlockingTest {
        val product = tomatoRed
        productDao.insert(product)
        val retrieved = productDao.getAllProducts()
        assertThat(retrieved).hasSize(1)
        assertThat(retrieved[0]).isEqualTo(product)
    }

    @Test
    fun insertProductWithInventories() = scope.runBlockingTest {
        val product = tomatoRed
        val inv = tomatoRedInv1
        productDao.insertProductAndInventories(product, listOf(inv))
        val retrieved = productDao.getAllProductWithInventories()
        assertThat(retrieved).hasSize(1)
        val savedProductWithInventories = retrieved[0]
        assertThat(savedProductWithInventories.product).isEqualTo(product)
        assertThat(savedProductWithInventories.inventories).hasSize(1)
        assertThat(savedProductWithInventories.inventories[0]).isEqualTo(inv)
    }

    @Test
    fun getProductBySku() = scope.runBlockingTest {
        val product = tomatoRed
        productDao.insert(product)
        val reloaded = productDao.getProductBySku(product.sku)
        assertThat(reloaded).isEqualTo(product)
    }

    // Also covers the update operation
    @Test
    fun upsertProduct() = scope.runBlockingTest {
        var product = tomatoRed
        val rowId = productDao.insert(product)
        assertThat(rowId).isNotEqualTo(-1)
        // re-inserting same entity would be skipped
        product = product.copy(name = "Tomato Red")
        assertThat(productDao.insert(product)).isEqualTo(-1)
        // upsert should update when an entity already exists
        productDao.upsert(product)
        val reloaded = productDao.getProductBySku(product.sku)
        assertThat(reloaded).isEqualTo(product)
    }

    private suspend fun insertAndAssertProduct(product: ProductEntity) {
        productDao.insert(product)
        assertThat(productDao.getProductBySku(product.sku)).isEqualTo(product)
    }

    @Test
    fun deleteProduct() = scope.runBlockingTest {
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
        assertThat(productDao.getAllProductWithInventories()).isEmpty()
    }

    @Test
    fun getProductsByName() = scope.runBlockingTest {
        val product1 = tomatoRed;
        val product2 = tomatoGreen
        productDao.insertAll(product1, product2)
        val reloaded = productDao.getProductsByName("%tomato%")
        assertThat(reloaded).containsExactly(product1, product2)
    }

    @Test
    fun getProductsBySkus() = scope.runBlockingTest {
        val product1 = tomatoRed;
        val product2 = tomatoGreen
        productDao.insertAll(product1, product2)
        val reloaded = productDao.getProductsBySkus(listOf(product1.sku, product2.sku))
        assertThat(reloaded).containsExactly(product1, product2)
    }

    @Test
    fun observeAllProducts_whenProductUpdates_shouldEmitNewList() {
        val product1 = tomatoRed;
        val product2 = tomatoGreen
        scope.runBlockingTest {
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
            productDao.observeAllProductWithInventories().test {
                assertThat(awaitItem()).isEmpty()
                productDao.insert(product1)
                assertThat(awaitItem()).containsExactly(ProductWithInventories(product1))
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