package com.kshitijpatil.tazabazar.api

import com.kshitijpatil.tazabazar.api.dto.OrderLine
import com.kshitijpatil.tazabazar.api.dto.OrderResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApi {
    @POST("/api/v2/orders")
    suspend fun placeOrder(@Body orderLines: List<OrderLine>): OrderResponse

    @GET("/api/v2/orders/{order_id}")
    suspend fun getOrderById(@Path("order_id") orderId: String): OrderResponse

    @GET("/api/v2/users/{username}/orders")
    suspend fun getOrdersByUsername(@Path("username") username: String): List<OrderResponse>
}