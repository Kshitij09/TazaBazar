package com.kshitijpatil.tazabazar.model

data class CartItem(
    val inventoryId: Int,
    val stockAvailable: Int,
    val quantityLabel: String,
    val price: Float,
    val name: String,
    val imageUri: String,
    val quantity: Int,
)