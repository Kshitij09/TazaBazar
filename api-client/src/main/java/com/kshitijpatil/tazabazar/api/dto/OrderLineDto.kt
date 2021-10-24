package com.kshitijpatil.tazabazar.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OrderLineDto(
    @Json(name = "inventoryId")
    val inventoryId: Int,
    @Json(name = "quantity")
    val quantity: Int
)
