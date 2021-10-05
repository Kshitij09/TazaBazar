package com.kshitijpatil.tazabazar.model

data class CartConfiguration(
    val maxQuantityPerItem: Int = Int.MAX_VALUE,
    val deliveryCharges: Float = 0f,
)