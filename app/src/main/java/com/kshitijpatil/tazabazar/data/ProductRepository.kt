package com.kshitijpatil.tazabazar.data

import com.kshitijpatil.tazabazar.api.TazaBazarApi
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext

class ProductRepository(
    private val tazaBazarApi: TazaBazarApi,
    private val dispatchers: AppCoroutineDispatchers
) {
    suspend fun getAllProducts(): List<ProductResponse> {
        return withContext(dispatchers.io) {
            tazaBazarApi.getAllProducts()
        }
    }
}