package com.kshitijpatil.tazabazar.api.dto


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "username")
    val username: String,
    @Json(name = "password")
    val password: String,
    @Json(name = "full_name")
    val fullName: String,
    @Json(name = "phone")
    val phone: String,
    @Json(name = "authorities")
    val authorities: List<String> = emptyList(),
)