package com.kshitijpatil.tazabazar.model

import org.threeten.bp.OffsetDateTime

data class Product(
    val sku: String,
    val name: String,
    val category: String,
    val imageUri: String,
    val inventories: List<Inventory> = emptyList(),
    val isFavorite: Boolean = false
)

data class Inventory(
    val id: Int,
    val productSku: String,
    val price: Float,
    val quantityLabel: String,
    val stockAvailable: Int,
    val updatedAt: OffsetDateTime
)