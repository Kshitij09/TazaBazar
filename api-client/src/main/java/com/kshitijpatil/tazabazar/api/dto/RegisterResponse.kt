package com.kshitijpatil.tazabazar.api.dto


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class RegisterResponse(
    @Json(name = "email_verified")
    val emailVerified: Boolean,
    @Json(name = "full_name")
    val fullName: String,
    @Json(name = "phone")
    val phone: String,
    @Json(name = "phone_verified")
    val phoneVerified: Boolean,
    @Json(name = "username")
    val username: String
)