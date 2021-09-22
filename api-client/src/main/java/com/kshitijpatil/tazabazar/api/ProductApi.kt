package com.kshitijpatil.tazabazar.api

import com.kshitijpatil.tazabazar.api.dto.ProductCategoryDto
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {
    @GET("/api/v2/products")
    suspend fun getAllProducts(
        @Query("category") category: String? = null,
        @Query("q") query: String? = null
    ): List<ProductResponse>

    // Since this might return 404 when product does not exist
    @GET("/api/v2/products/{product_sku}")
    suspend fun getProduct(@Path("product_sku") sku: String): Response<ProductResponse>

    @GET("/api/v2/products/categories")
    suspend fun getProductCategories(): List<ProductCategoryDto>

    @GET("/api/v2/products/{product_sku}/inventories")
    suspend fun getProductInventories(@Path("product_sku") sku: String): List<ProductResponse.Inventory>
}