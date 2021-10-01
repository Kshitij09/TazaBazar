package com.kshitijpatil.tazabazar.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteEntity
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import com.kshitijpatil.tazabazar.data.local.entity.ProductWithInventories
import com.kshitijpatil.tazabazar.fixtures.product.*
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavoriteDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val scope = TestCoroutineScope()
    private val appDatabase = TestInject.appDatabase(ApplicationProvider.getApplicationContext())
    private val productDao = appDatabase.productDao
    private val inventoryDao = appDatabase.inventoryDao
    private val productCategoryDao = appDatabase.productCategoryDao
    private val favoriteDao = appDatabase.favoriteDao

    @Test
    fun getAllFavorites() = scope.runBlockingTest {
        productCategoryDao.insertAll(vegetables, fruits)
        productDao.insertAll(tomatoRed, sitafal)
        val allFavorites = listOf(
            FavoriteEntity(FavoriteType.WEEKLY, tomatoRed.sku),
            FavoriteEntity(FavoriteType.MONTHLY, tomatoRed.sku),
            FavoriteEntity(FavoriteType.WEEKLY, sitafal.sku),
            FavoriteEntity(FavoriteType.MONTHLY, sitafal.sku),
        )
        favoriteDao.insertAll(allFavorites)
        val reloaded = favoriteDao.getAllFavorites()
        assertThat(reloaded).containsExactlyElementsIn(allFavorites)
    }

    @Test
    fun getAllFavoritesBySku() = scope.runBlockingTest {
        productCategoryDao.insert(vegetables)
        productDao.insertAll(tomatoRed)
        val tomatoRedFavorites = listOf(
            FavoriteEntity(FavoriteType.WEEKLY, tomatoRed.sku),
            FavoriteEntity(FavoriteType.MONTHLY, tomatoRed.sku)
        )
        val allFavorites = listOf(
            *tomatoRedFavorites.toTypedArray(),
            FavoriteEntity(FavoriteType.WEEKLY, sitafal.sku),
            FavoriteEntity(FavoriteType.MONTHLY, sitafal.sku),
        )
        favoriteDao.insertAll(allFavorites)
        val actual = favoriteDao.getAllFavoritesBySku(tomatoRed.sku)
        assertThat(actual).containsExactlyElementsIn(tomatoRedFavorites)
    }

    /**
     * weekly - tomatoRed, tomatoGreen
     * monthly - tomatoRed
     */
    private suspend fun loadFavoriteFixtures() {
        productCategoryDao.insertAll(vegetables, fruits)
        productDao.insertAll(tomatoRed, tomatoGreen, sitafal)
        inventoryDao.insertAll(tomatoGreenInv1, tomatoRedInv1, tomatoRedInv2, sitafalInv)
        val favorites = listOf(
            FavoriteEntity(FavoriteType.WEEKLY, tomatoRed.sku),
            FavoriteEntity(FavoriteType.WEEKLY, tomatoGreen.sku),
            FavoriteEntity(FavoriteType.MONTHLY, tomatoRed.sku),
            FavoriteEntity(FavoriteType.MONTHLY, sitafal.sku),
        )
        favoriteDao.insertAll(favorites)
    }

    @Test
    fun getWeeklyFavoriteProductWithInventories() = scope.runBlockingTest {
        loadFavoriteFixtures()
        val actual = favoriteDao.getWeeklyFavoriteProductWithInventories()
        assertThat(actual).containsExactly(
            ProductWithInventories(tomatoRed, listOf(tomatoRedInv1, tomatoRedInv2)),
            ProductWithInventories(tomatoGreen, listOf(tomatoGreenInv1))
        )
    }

    @Test
    fun getMonthlyFavoriteProductWithInventories() = scope.runBlockingTest {
        loadFavoriteFixtures()
        val actual = favoriteDao.getMonthlyFavoriteProductWithInventories()
        assertThat(actual).containsExactly(
            ProductWithInventories(tomatoRed, listOf(tomatoRedInv1, tomatoRedInv2)),
            ProductWithInventories(sitafal, listOf(sitafalInv))
        )
    }


    @After
    fun tearDown() {
        appDatabase.clearAllTables()
        appDatabase.close()
    }
}