package com.kshitijpatil.tazabazar.data

import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory

interface ProductDataSource {
    suspend fun getProductCategories(): List<ProductCategory>
    suspend fun getAllProducts(): List<Product>
    suspend fun getProductsBy(category: String?, query: String?): List<Product>
}