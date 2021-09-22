package com.kshitijpatil.tazabazar.data

import com.kshitijpatil.tazabazar.api.ProductApi
import com.kshitijpatil.tazabazar.api.dto.ProductCategoryDto
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class ProductRepository(
    private val productApi: ProductApi,
    private val dispatchers: AppCoroutineDispatchers
) {
    suspend fun getProductCategoryMap(): List<ProductCategoryDto> {
        return withContext(dispatchers.io) {
            productApi.getProductCategories()
        }
    }

    // TODO: Update when list of query parameters is supported by the server
    suspend fun getProductListByCategories(categories: List<String> = emptyList()): List<ProductResponse> {
        return withContext(dispatchers.io) {
            Timber.i("Getting products with $categories from the remote source")
            if (categories.isNotEmpty())
                productApi.getAllProducts(categories[0])
            else
                productApi.getAllProducts()
        }
    }
}