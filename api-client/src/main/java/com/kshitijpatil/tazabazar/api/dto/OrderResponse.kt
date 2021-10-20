package com.kshitijpatil.tazabazar.api.dto


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrderResponse(
    @Json(name = "created_at")
    val createdAt: String,
    @Json(name = "id")
    val id: String,
    @Json(name = "order_lines")
    val orderLines: List<OrderLine> = emptyList(),
    @Json(name = "status")
    val status: String,
    @Json(name = "username")
    val username: String
)