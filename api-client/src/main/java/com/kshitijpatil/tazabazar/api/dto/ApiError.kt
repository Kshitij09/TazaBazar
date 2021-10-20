package com.kshitijpatil.tazabazar.api.dto


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "error")
    val error: String,
    @Json(name = "message")
    val message: String?,
    @Json(name = "path")
    val path: String?,
    @Json(name = "status")
    val status: Int?,
    @Json(name = "timestamp")
    val timestamp: String
)