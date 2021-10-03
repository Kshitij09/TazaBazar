package com.kshitijpatil.tazabazar.model

import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import org.threeten.bp.OffsetDateTime

data class Product(
    val sku: String,
    val name: String,
    val category: String,
    val imageUri: String,
    val inventories: List<Inventory> = emptyList(),
    val favorites: Set<FavoriteType> = emptySet()
) {
    /** Creates a copy of this instance with favorites now having [FavoriteType.WEEKLY] */
    fun withToWeeklyFavorites(): Product {
        val newFavorites = favorites.toMutableSet()
        newFavorites.add(FavoriteType.WEEKLY)
        return this.copy(favorites = newFavorites)
    }

    val defaultInventory: Inventory?
        get() = inventories.getOrNull(0)
}

data class Inventory(
    val id: Int,
    val productSku: String,
    val price: Float,
    val quantityLabel: String,
    val stockAvailable: Int,
    val updatedAt: OffsetDateTime
)