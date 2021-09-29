package com.kshitijpatil.tazabazar.data.local.entity

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

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

data class ProductWithInventories(
    @Embedded val product: ProductEntity,
    @Relation(parentColumn = "sku", entityColumn = "product_sku")
    val inventories: List<InventoryEntity> = emptyList()
)

data class ProductWithInventoriesAndFavorites(
    @Embedded val product: ProductEntity,
    @Relation(parentColumn = "sku", entityColumn = "product_sku")
    val inventories: List<InventoryEntity> = emptyList(),
    @Relation(parentColumn = "sku", entityColumn = "product_sku")
    val favorites: List<FavoriteEntity> = emptyList()
)