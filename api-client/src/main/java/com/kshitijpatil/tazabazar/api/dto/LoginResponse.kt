package com.kshitijpatil.tazabazar.api.dto


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "access_token")
    val accessToken: String,
    @Json(name = "authorities")
    val authorities: List<String>,
    @Json(name = "refresh_token")
    val refreshToken: String,
    @Json(name = "user")
    val user: User
) {
    @JsonClass(generateAdapter = true)
    data class User(
        @Json(name = "username")
        val username: String,
        @Json(name = "phone")
        val phone: String,
        @Json(name = "full_name")
        val fullName: String,
        @Json(name = "email_verified")
        val emailVerified: Boolean,
        @Json(name = "phone_verified")
        val phoneVerified: Boolean
    )
}