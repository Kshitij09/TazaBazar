package com.kshitijpatil.tazabazar.api

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Test
import java.net.HttpURLConnection

class TestProductApi {
    private val client = OkHttpClient.Builder().build()
    private val api = ApiModule.provideTazaBazarApi(client)

    @Test
    fun test_getAllProducts() = runBlocking {
        val products = api.getAllProducts()
        assertThat(products).isNotEmpty()
    }

    @Test
    fun test_getProductsByCategory() = runBlocking {
        val products = api.getAllProducts("fruits")
        assertThat(products).isNotEmpty()
    }

    @Test
    fun test_getProductBySku() = runBlocking {
        // valid sku
        var productResponse = api.getProduct("vgt-004")
        assertThat(productResponse.body()).isNotNull()
        // invalid sku
        productResponse = api.getProduct("vgt-10001")
        assertThat(productResponse.isSuccessful).isFalse()
        assertThat(productResponse.code()).isEqualTo(HttpURLConnection.HTTP_NOT_FOUND)
    }

    @Test
    fun test_getProductCategories() = runBlocking {
        val categories = api.getProductCategories()
        assertThat(categories).isNotEmpty()
    }

    @Test
    fun test_getProductInventories() = runBlocking {
        val inventories = api.getProductInventories("fru-002")
        assertThat(inventories).isNotEmpty()
    }
}