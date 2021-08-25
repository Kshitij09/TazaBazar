package com.kshitijpatil.tazabazar.api

import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TazaBazarApi {
    @GET("/api/products")
    suspend fun getAllProducts(): List<ProductResponse>

    // Since this might return 404 when product does not exist
    @GET("/api/products/{product_id}")
    suspend fun getProduct(@Path("product_id") productId: Int): Response<ProductResponse>
}