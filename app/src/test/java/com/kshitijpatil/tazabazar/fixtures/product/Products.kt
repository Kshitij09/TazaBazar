package com.kshitijpatil.tazabazar.fixtures.product

import com.kshitijpatil.tazabazar.data.local.InventoryEntity
import com.kshitijpatil.tazabazar.data.local.ProductCategoryEntity
import com.kshitijpatil.tazabazar.data.local.ProductEntity
import org.threeten.bp.OffsetDateTime

val vegetables = ProductCategoryEntity("vegetables", "Vegetables", "vgt")
val leafyVegetables = ProductCategoryEntity("leafy-vegetables", "Leafy Vegetables", "lfvgt")
val fruits = ProductCategoryEntity("fruits", "Fruits", "fru")
val tomatoRed = ProductEntity("vgt-001", "Tomato Red", vegetables.label, "image-uri")
val tomatoRedInv1 = InventoryEntity(1, tomatoRed.sku, 15f, "500 gm", 31, OffsetDateTime.now())
val tomatoGreen = ProductEntity("vgt-002", "Green Tomato", vegetables.label, "image-uri")
val tomatoGreenInv1 = InventoryEntity(2, tomatoGreen.sku, 10f, "300 gm", 10, OffsetDateTime.now())
