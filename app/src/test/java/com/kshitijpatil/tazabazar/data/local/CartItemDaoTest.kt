package com.kshitijpatil.tazabazar.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.data.local.dao.upsert
import com.kshitijpatil.tazabazar.data.local.entity.CartItemDetailView
import com.kshitijpatil.tazabazar.data.local.entity.CartItemEntity
import com.kshitijpatil.tazabazar.data.local.entity.InventoryEntity
import com.kshitijpatil.tazabazar.data.local.entity.ProductEntity
import com.kshitijpatil.tazabazar.fixtures.product.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartItemDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val scope = TestCoroutineScope()
    private val appDatabase = TestInject.appDatabase(ApplicationProvider.getApplicationContext())
    private val productDao = appDatabase.productDao
    private val inventoryDao = appDatabase.inventoryDao
    private val categoryDao = appDatabase.productCategoryDao
    private val cartItemDao = appDatabase.cartItemDao

    @Before
    fun setup() {
        runBlocking {
            categoryDao.insertAll(allCategoryEntities)
            productDao.insertAll(allProductEntities)
            inventoryDao.insertAll(allInventoryEntities)
        }
    }

    @Test
    fun insert() {
        val cartItem1 = CartItemEntity(tomatoRedInv1.id, 5)
        val cartItem2 = CartItemEntity(tomatoRedInv2.id, 2)

        scope.runBlockingTest {
            cartItemDao.insertAll(cartItem1, cartItem2)

            val reloaded = cartItemDao.getAllCartItems()
            assertThat(reloaded).containsExactly(cartItem1, cartItem2)
        }
    }

    @Test
    fun upsert() {
        var cartItem1 = CartItemEntity(tomatoRedInv1.id, 5)
        scope.runBlockingTest {
            cartItemDao.upsert(cartItem1)
            assertThat(cartItemDao.getCartItemById(cartItem1.inventoryId)).isEqualTo(cartItem1)

            cartItem1 = cartItem1.copy(quantity = 8)
            cartItemDao.upsert(cartItem1)
            assertThat(cartItemDao.getCartItemById(cartItem1.inventoryId)).isEqualTo(cartItem1)
        }
    }

    @Test
    fun getCartItemDetailView() {
        val cartItem1 = CartItemEntity(tomatoRedInv1.id, 5)

        scope.runBlockingTest {
            cartItemDao.insert(cartItem1)
            val expected = getCartItemDetailViewFrom(cartItem1, tomatoRedInv1, tomatoRed)

            val actual = cartItemDao.getCartItemDetailViewById(cartItem1.inventoryId)
            assertThat(actual).isEqualTo(expected)
        }
    }

    @Test
    fun getAllCartDetailViews() {
        val cartItem1 = CartItemEntity(tomatoRedInv1.id, 4)
        val cartItem1DetailView = getCartItemDetailViewFrom(cartItem1, tomatoRedInv1, tomatoRed)
        val cartItem2 = CartItemEntity(sitafalInv.id, 8)
        val cartItem2DetailView = getCartItemDetailViewFrom(cartItem2, sitafalInv, sitafal)
        scope.runBlockingTest {
            cartItemDao.insertAll(cartItem1, cartItem2)
            assertThat(cartItemDao.getAllCartDetailViews()).containsExactly(
                cartItem1DetailView, cartItem2DetailView
            )
        }
    }

    @Test
    fun deleteById() {
        val cartItem1 = CartItemEntity(tomatoRedInv1.id, 4)
        val cartItem2 = CartItemEntity(sitafalInv.id, 8)
        scope.runBlockingTest {
            cartItemDao.insertAll(cartItem1, cartItem2)
            cartItemDao.deleteById(cartItem1.inventoryId)
            val actual = cartItemDao.getAllCartItems()
            assertThat(actual).doesNotContain(cartItem1)
        }
    }

    @Test
    fun observeCartItemCount() = scope.runBlockingTest {
        cartItemDao.observeCartItemCount().test {
            assertThat(awaitItem()).isEqualTo(0)

            // insert
            val cartItem1 = CartItemEntity(tomatoRedInv1.id, 4)
            cartItemDao.insert(cartItem1)
            assertThat(awaitItem()).isEqualTo(1)

            // insert-2
            val cartItem2 = CartItemEntity(sitafalInv.id, 8)
            cartItemDao.insert(cartItem2)
            assertThat(awaitItem()).isEqualTo(2)

            // delete
            cartItemDao.deleteById(cartItem1.inventoryId)
            assertThat(awaitItem()).isEqualTo(1)
        }

    }

    @After
    fun tearDown() {
        appDatabase.clearAllTables()
        appDatabase.close()
    }

    private fun getCartItemDetailViewFrom(
        cartItem: CartItemEntity,
        inventory: InventoryEntity,
        product: ProductEntity
    ): CartItemDetailView {
        return CartItemDetailView(
            inventory.id,
            inventory.stockAvailable,
            inventory.quantityLabel,
            inventory.price,
            product.name,
            product.imageUri,
            cartItem.quantity
        )
    }
}