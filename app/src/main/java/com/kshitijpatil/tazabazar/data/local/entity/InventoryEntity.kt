package com.kshitijpatil.tazabazar.data.local.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import org.threeten.bp.OffsetDateTime

@Entity(
    tableName = "inventory",
    foreignKeys = [ForeignKey(
        entity = ProductEntity::class,
        parentColumns = ["sku"],
        childColumns = ["product_sku"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["product_sku"])]
)
data class InventoryEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "product_sku")
    val productSku: String,
    val price: Float,
    @ColumnInfo(name = "quantity_label")
    val quantityLabel: String,
    @ColumnInfo(name = "stock_available")
    val stockAvailable: Int,
    @ColumnInfo(name = "updated_at")
    val updatedAt: OffsetDateTime
)