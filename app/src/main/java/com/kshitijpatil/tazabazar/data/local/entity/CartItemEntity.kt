package com.kshitijpatil.tazabazar.data.local.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(
    tableName = "cart_item", foreignKeys = [
        ForeignKey(
            entity = InventoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["inventory_id"],
            deferred = true,
            onDelete = CASCADE
        )
    ]
)
data class CartItemEntity(
    @ColumnInfo(name = "inventory_id")
    @PrimaryKey
    val inventoryId: Int,
    val quantity: Int
)


@DatabaseView(
    value =
    """
        SELECT inv.id AS inventory_id,inv.stock_available,inv.quantity_label,inv.price,p.name,p.image_uri,cart.quantity
        FROM cart_item AS cart
        INNER JOIN inventory as inv ON cart.inventory_id=inv.id
        INNER JOIN product AS p ON inv.product_sku=p.sku
    """, viewName = "cart_item_detail_view"
)
data class CartItemDetailView(
    @ColumnInfo(name = "inventory_id")
    val inventoryId: Int,
    @ColumnInfo(name = "stock_available")
    val stockAvailable: Int,
    @ColumnInfo(name = "quantity_label")
    val quantityLabel: String,
    val price: Float,
    val name: String,
    @ColumnInfo(name = "image_uri")
    val imageUri: String,
    val quantity: Int,
)