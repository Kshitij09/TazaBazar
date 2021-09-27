package com.kshitijpatil.tazabazar.model

import org.threeten.bp.OffsetDateTime

data class Product(
    val sku: String,
    val name: String,
    val category: String,
    val imageUri: String,
    val inventories: List<Inventory> = emptyList()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product

        if (sku != other.sku) return false

        return true
    }

    override fun hashCode(): Int {
        return sku.hashCode()
    }
}

data class Inventory(
    val id: Int,
    val productSku: String,
    val price: Float,
    val quantityLabel: String,
    val stockAvailable: Int,
    val updatedAt: OffsetDateTime
)