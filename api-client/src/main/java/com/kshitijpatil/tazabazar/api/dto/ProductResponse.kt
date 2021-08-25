package com.kshitijpatil.tazabazar.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class ProductResponse(
    @Json(name = "category_id")
    val categoryId: Int,
    @Json(name = "id")
    val id: Int,
    @Json(name = "image_uri")
    val imageUri: String,
    @Json(name = "inventory")
    val inventory: Inventory,
    @Json(name = "name")
    val name: String,
    @Json(name = "price")
    val price: Int,
    @Json(name = "quantity_label")
    val quantityLabel: String,
    @Json(name = "sku")
    val sku: String
) {
    @JsonClass(generateAdapter = true)
    data class Inventory(
        @Json(name = "quantity")
        val quantity: Int
    )
}