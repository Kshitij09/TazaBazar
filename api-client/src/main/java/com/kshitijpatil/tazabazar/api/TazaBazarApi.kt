package com.kshitijpatil.tazabazar.api

import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TazaBazarApi {
    @GET("/api/products")
    suspend fun getAllProducts(@Query("category_id") categoryId: Int? = null): List<ProductResponse>

    // Since this might return 404 when product does not exist
    @GET("/api/products/{product_id}")
    suspend fun getProduct(@Path("product_id") productId: Int): Response<ProductResponse>
}