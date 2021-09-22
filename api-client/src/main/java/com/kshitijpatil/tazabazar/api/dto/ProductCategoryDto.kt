package com.kshitijpatil.tazabazar.api.dto


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ProductCategoryDto(
    @Json(name = "label")
    val label: String,
    @Json(name = "name")
    val name: String,
    @Json(name = "sku_prefix")
    val skuPrefix: String
)