package com.kshitijpatil.tazabazar.data

import com.kshitijpatil.tazabazar.api.ProductApi
import com.kshitijpatil.tazabazar.api.dto.ProductCategoryDto
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext

interface ProductRepository {
    suspend fun getProductCategories(): List<ProductCategoryDto>

    /** Get productList, optionally filter by [category] */
    suspend fun getProductListBy(category: String?): List<ProductResponse>
}

class ProductRepositoryImpl(
    private val productApi: ProductApi,
    private val dispatchers: AppCoroutineDispatchers
) : ProductRepository {
    override suspend fun getProductCategories(): List<ProductCategoryDto> {
        return withContext(dispatchers.io) {
            productApi.getProductCategories()
        }
    }

    override suspend fun getProductListBy(category: String?): List<ProductResponse> {
        return withContext(dispatchers.io) {
            productApi.getAllProducts(category)
        }
    }
}