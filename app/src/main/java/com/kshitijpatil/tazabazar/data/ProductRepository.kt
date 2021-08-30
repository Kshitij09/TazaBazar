package com.kshitijpatil.tazabazar.data

import com.kshitijpatil.tazabazar.api.TazaBazarApi
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber

class ProductRepository(
    private val tazaBazarApi: TazaBazarApi,
    private val dispatchers: AppCoroutineDispatchers
) {
    //TODO: Replace with network-call when supported
    private val categoryMap = mapOf(
        "Vegetables" to 0,
        "Leafy Vegetables" to 1,
        "Fruits" to 2,
        "Rice Wheat Atta" to 3,
        "Dals and Pulses" to 4,
    )

    suspend fun getProductCategoryMap(): Map<String, Int> {
        return withContext(dispatchers.io) {
            delay(700)
            categoryMap
        }
    }

    // TODO: Update when list of query parameters is supported by the server
    suspend fun getProductListByCategories(categoryIds: List<Int> = emptyList()): List<ProductResponse> {
        return withContext(dispatchers.io) {
            Timber.i("Getting products with $categoryIds from the remote source")
            if (categoryIds.isNotEmpty())
                tazaBazarApi.getAllProducts(categoryIds[0])
            else
                tazaBazarApi.getAllProducts()
        }
    }
}