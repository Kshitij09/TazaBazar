package com.kshitijpatil.tazabazar.data.local

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import org.threeten.bp.OffsetDateTime

@Entity(tableName = "product_category")
data class ProductCategoryEntity(
    @PrimaryKey val label: String,
    val name: String,
    @ColumnInfo(name = "sku_prefix") val skuPrefix: String
)

@Entity(
    tableName = "product",
    foreignKeys = [ForeignKey(
        entity = ProductCategoryEntity::class,
        parentColumns = ["label"],
        childColumns = ["category"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["category"])]
)
data class ProductEntity(
    @PrimaryKey val sku: String,
    val name: String,
    val category: String,
    @ColumnInfo(name = "image_uri")
    val imageUri: String
)

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

data class ProductWithInventories(
    @Embedded val product: ProductEntity,
    @Relation(parentColumn = "sku", entityColumn = "product_sku")
    val inventories: List<InventoryEntity> = emptyList()
)