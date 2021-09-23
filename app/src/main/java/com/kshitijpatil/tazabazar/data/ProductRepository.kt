package com.kshitijpatil.tazabazar.data

import com.kshitijpatil.tazabazar.api.ProductApi
import com.kshitijpatil.tazabazar.api.dto.ProductCategoryDto
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

interface ProductRepository {
    /** Get product categories, if something goes wrong, return an empty list */
    suspend fun getProductCategories(): List<ProductCategoryDto>

    /** Get all products, optionally filter by [category] */
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
            Timber.d("fetching products for category: $category ")
            productApi.getAllProducts(category)
        }
    }
}