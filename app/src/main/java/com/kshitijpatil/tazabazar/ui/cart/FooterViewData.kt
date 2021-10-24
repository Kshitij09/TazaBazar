package com.kshitijpatil.tazabazar.ui.cart

data class CartCost(
    val subTotal: Float = 0f,
    val delivery: Float = 0f,
    val discount: Float = 0f
) {
    val total: Float
        get() = subTotal + delivery - discount
}

data class FooterViewData(
    val costing: CartCost = CartCost(),
    val placeOrderEnabled: Boolean = false,
    val userLoggedIn: Boolean = false
)