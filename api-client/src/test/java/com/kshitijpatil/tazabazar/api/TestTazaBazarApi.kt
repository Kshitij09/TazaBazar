package com.kshitijpatil.tazabazar.api

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.Test

class TestTazaBazarApi {
    private val api = ApiModule.tazaBazarApi

    @Test
    fun getAllProducts_should_not_return_empty_list() = runBlocking {
        val products = api.getAllProducts()
        assertThat(products).isNotEmpty()
    }

    @Test
    fun test_getProductsByCategory() = runBlocking {
        val products = api.getAllProducts(2)
        assertThat(products).isNotEmpty()
    }

    @Test
    fun getProduct_works() = runBlocking {
        val productResponse = api.getProduct(1)
        if (productResponse.isSuccessful) {
            assertThat(productResponse.body()).isNotNull()
        } else {
            assertThat(productResponse.code()).isEqualTo(404)
        }
    }
}