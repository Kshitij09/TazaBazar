package com.kshitijpatil.tazabazar.api.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String, // This should be a valid email Address
    val password: String
)