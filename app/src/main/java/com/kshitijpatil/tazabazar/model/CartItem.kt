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

data class CartCost(
    val subTotal: Float = 0f,
    val delivery: Float = 0f,
    val discount: Float = 0f
) {
    val total: Float
        get() = subTotal + delivery - discount
}