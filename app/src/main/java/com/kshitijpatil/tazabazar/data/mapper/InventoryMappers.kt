package com.kshitijpatil.tazabazar.data.mapper

import com.kshitijpatil.tazabazar.data.local.entity.InventoryEntity
import com.kshitijpatil.tazabazar.model.Inventory

class InventoryToInventoryEntity : Mapper<Inventory, InventoryEntity> {
    override fun map(from: Inventory): InventoryEntity {
        return InventoryEntity(
            id = from.id,
            productSku = from.productSku,
            price = from.price,
            quantityLabel = from.quantityLabel,
            stockAvailable = from.stockAvailable,
            updatedAt = from.updatedAt
        )
    }

}

class InventoryEntityToInventory : Mapper<InventoryEntity, Inventory> {
    override fun map(from: InventoryEntity): Inventory {
        return Inventory(
            id = from.id,
            productSku = from.productSku,
            price = from.price,
            quantityLabel = from.quantityLabel,
            stockAvailable = from.stockAvailable,
            updatedAt = from.updatedAt
        )
    }
}