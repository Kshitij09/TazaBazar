package com.kshitijpatil.tazabazar.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.fixtures.product.fruits
import com.kshitijpatil.tazabazar.fixtures.product.leafyVegetables
import com.kshitijpatil.tazabazar.fixtures.product.vegetables
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductCategoryDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    private val scope = TestCoroutineScope()
    private val appDatabase = TestInject.appDatabase(ApplicationProvider.getApplicationContext())
    private val productCategoryDao = appDatabase.productCategoryDao

    @Test
    fun insertProductCategory() = scope.runBlockingTest {
        val categories = listOf(vegetables, leafyVegetables, fruits)
        productCategoryDao.insertAll(categories)
        val reloaded = productCategoryDao.getAllCategories()
        assertThat(reloaded).containsExactlyElementsIn(categories)
    }

    @After
    fun tearDown() {
        scope.cleanupTestCoroutines()
        appDatabase.close()
    }
}