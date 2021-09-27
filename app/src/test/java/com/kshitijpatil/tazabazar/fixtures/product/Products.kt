package com.kshitijpatil.tazabazar.fixtures.product

import com.kshitijpatil.tazabazar.data.local.InventoryEntity
import com.kshitijpatil.tazabazar.data.local.ProductCategoryEntity
import com.kshitijpatil.tazabazar.data.local.ProductEntity
import com.kshitijpatil.tazabazar.data.local.ProductWithInventories
import org.threeten.bp.OffsetDateTime

val vegetables = ProductCategoryEntity("vegetables", "Vegetables", "vgt")
val leafyVegetables = ProductCategoryEntity("leafy-vegetables", "Leafy Vegetables", "lfvgt")
val fruits = ProductCategoryEntity("fruits", "Fruits", "fru")
val tomatoRed = ProductEntity("vgt-001", "Tomato Red", vegetables.label, "image-uri")
val tomatoRedInv1 = InventoryEntity(1, tomatoRed.sku, 15f, "500 gm", 31, OffsetDateTime.now())
val tomatoRedInv2 = InventoryEntity(2, tomatoRed.sku, 25f, "1 kg", 15, OffsetDateTime.now())
val tomatoGreen = ProductEntity("vgt-002", "Green Tomato", vegetables.label, "image-uri")
val tomatoGreenInv1 = InventoryEntity(3, tomatoGreen.sku, 10f, "300 gm", 10, OffsetDateTime.now())
val sitafal = ProductEntity("fru-001", "Sitafal", fruits.label, "image-uri")
val sitafalInv = InventoryEntity(4, sitafal.sku, 50f, "500 gm", 5, OffsetDateTime.now())

val allProductEntities = listOf(tomatoRed, tomatoGreen, sitafal)
val allCategoryEntities = listOf(vegetables, leafyVegetables, fruits)
val allInventoryEntities = listOf(tomatoRedInv1, tomatoRedInv2, tomatoGreenInv1, sitafalInv)
val tomatoRedProductWithInventories =
    ProductWithInventories(tomatoRed, listOf(tomatoRedInv1, tomatoRedInv2))
val tomatoGreenProductWithInventories = ProductWithInventories(tomatoGreen, listOf(tomatoGreenInv1))
val sitafalProductWithInventories = ProductWithInventories(sitafal, listOf(sitafalInv))
val allProductWithInventories = listOf(
    tomatoRedProductWithInventories,
    tomatoGreenProductWithInventories,
    sitafalProductWithInventories
)