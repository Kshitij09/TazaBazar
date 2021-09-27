package com.kshitijpatil.tazabazar.di

import com.kshitijpatil.tazabazar.data.mapper.*

object MapperModule {
    val productResponseToProduct = ProductResponseToProduct()
    val inventoryToInventoryEntity = InventoryToInventoryEntity()
    val inventoryEntityToInventory = InventoryEntityToInventory()
    val productToProductWithInventories =
        ProductToProductWithInventories(inventoryToInventoryEntity)
    val productWithInventoriesToProduct =
        ProductWithInventoriesToProduct(inventoryEntityToInventory)
    val productEntityToProduct = ProductEntityToProduct()
    val productCategoryEntityToProductCategory = ProductCategoryEntityToProductCategory()
    val productCategoryToProductCategoryEntity = ProductCategoryToProductCategoryEntity()
    val productCategoryDtoToProductCategory = ProductCategoryDtoToProductCategory()
}