package com.kshitijpatil.tazabazar.api.dto


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductResponse(
    @Json(name = "category")
    val category: String,
    @Json(name = "image_uri")
    val imageUri: String,
    @Json(name = "inventories")
    val inventories: List<Inventory>,
    @Json(name = "name")
    val name: String,
    @Json(name = "sku")
    val sku: String
) {
    @JsonClass(generateAdapter = true)
    data class Inventory(
        @Json(name = "id")
        val id: Int,
        @Json(name = "price")
        val price: Int,
        @Json(name = "quantity_label")
        val quantityLabel: String,
        @Json(name = "stock_available")
        val stockAvailable: Int,
        @Json(name = "updated_at")
        val updatedAt: String
    )
}