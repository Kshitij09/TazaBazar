package com.kshitijpatil.tazabazar.data.mapper

import com.kshitijpatil.tazabazar.data.local.entity.CartItemDetailView
import com.kshitijpatil.tazabazar.model.CartItem

class CartItemDetailViewToCartItem : Mapper<CartItemDetailView, CartItem> {
    override fun map(from: CartItemDetailView): CartItem {
        return CartItem(
            from.inventoryId,
            from.stockAvailable,
            from.quantityLabel,
            from.price,
            from.name,
            from.imageUri,
            from.quantity
        )
    }

}