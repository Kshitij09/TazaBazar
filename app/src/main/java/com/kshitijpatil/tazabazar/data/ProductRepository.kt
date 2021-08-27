package com.kshitijpatil.tazabazar.data

import com.kshitijpatil.tazabazar.api.TazaBazarApi
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ProductRepository(
    private val tazaBazarApi: TazaBazarApi,
    private val dispatchers: AppCoroutineDispatchers
) {
    private val categoryMap = mapOf(
        "Vegetables" to 0,
        "Leafy Vegetables" to 1,
        "Fruits" to 2,
        "Rice Wheat Atta" to 3,
        "Dals and Pulses" to 4,
    )

    suspend fun getAllProducts(): List<ProductResponse> {
        return withContext(dispatchers.io) {
            getProductListByCategories()
        }
    }

    suspend fun getProductCategoryMap(): Map<String, Int> {
        return withContext(dispatchers.io) {
            delay(700)
            categoryMap
        }
    }

    // TODO: Update when list of query parameters is supported by the server
    suspend fun getProductListByCategories(categoryIds: List<Int> = emptyList()): List<ProductResponse> {
        return withContext(dispatchers.io) {
            if (categoryIds.isNotEmpty())
                tazaBazarApi.getAllProducts(categoryIds[0])
            else
                tazaBazarApi.getAllProducts()
        }
    }
}