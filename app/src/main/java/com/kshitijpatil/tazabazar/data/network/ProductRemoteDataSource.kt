package com.kshitijpatil.tazabazar.data.network

import com.kshitijpatil.tazabazar.api.ProductApi
import com.kshitijpatil.tazabazar.data.ProductDataSource
import com.kshitijpatil.tazabazar.data.mapper.ProductCategoryDtoToProductCategory
import com.kshitijpatil.tazabazar.data.mapper.ProductResponseToProduct
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory

class ProductRemoteDataSource(
    private val productApi: ProductApi,
    private val categoryMapper: ProductCategoryDtoToProductCategory,
    private val productMapper: ProductResponseToProduct
) : ProductDataSource {
    override suspend fun getProductCategories(): List<ProductCategory> {
        return productApi.getProductCategories().map(categoryMapper::map)
    }

    override suspend fun getAllProducts(): List<Product> {
        return productApi.getAllProducts().map(productMapper::map)
    }

    override suspend fun getProductsBy(category: String?, query: String?): List<Product> {
        return productApi.getAllProducts(category, query).map(productMapper::map)
    }
}