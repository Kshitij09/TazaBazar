package com.kshitijpatil.tazabazar.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoggedInUser(
    val email: String,
    val fullName: String,
    val phone: String,
    val emailVerified: Boolean,
    val phoneVerified: Boolean
)