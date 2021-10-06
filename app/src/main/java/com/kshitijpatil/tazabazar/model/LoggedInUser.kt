package com.kshitijpatil.tazabazar.model

data class LoggedInUser(
    val email: String,
    val fullName: String,
    val phone: String,
    val emailVerified: Boolean,
    val phoneVerified: Boolean
)