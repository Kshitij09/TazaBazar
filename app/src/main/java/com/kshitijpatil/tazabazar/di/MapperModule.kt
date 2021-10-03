package com.kshitijpatil.tazabazar.di

import com.kshitijpatil.tazabazar.data.mapper.*

object MapperModule {
    val productResponseToProduct = ProductResponseToProduct()
    val inventoryToInventoryEntity = InventoryToInventoryEntity()
    val inventoryEntityToInventory = InventoryEntityToInventory()
    val productToProductWithInventories =
        ProductToProductWithInventories(inventoryToInventoryEntity)
    val productToProductWithInventoriesAndFavorites = ProductToProductWithInventoriesAndFavorites(
        inventoryToInventoryEntity
    )
    val productEntityToProduct = ProductEntityToProduct()
    val productWithInventoriesToProduct =
        ProductWithInventoriesToProduct(productEntityToProduct, inventoryEntityToInventory)
    val productWithInventoriesAndFavoritesToProduct = ProductWithInventoriesAndFavoritesToProduct(
        productEntityToProduct, inventoryEntityToInventory
    )
    val productCategoryEntityToProductCategory = ProductCategoryEntityToProductCategory()
    val productCategoryToProductCategoryEntity = ProductCategoryToProductCategoryEntity()
    val productCategoryDtoToProductCategory = ProductCategoryDtoToProductCategory()

    val cartItemDetailViewToCartItem = CartItemDetailViewToCartItem()
}