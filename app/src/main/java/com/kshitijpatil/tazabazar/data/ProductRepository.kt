package com.kshitijpatil.tazabazar.data

import com.kshitijpatil.tazabazar.api.ProductApi
import com.kshitijpatil.tazabazar.api.dto.ProductCategoryDto
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext

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
    suspend fun getProductListByCategories(category: String? = null): List<ProductResponse> {
        return withContext(dispatchers.io) {
            productApi.getAllProducts(category)
        }
    }
}