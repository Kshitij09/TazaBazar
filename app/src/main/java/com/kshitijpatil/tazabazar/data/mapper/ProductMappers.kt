package com.kshitijpatil.tazabazar.data.mapper

import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.data.local.TazaBazarTypeConverters
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteEntity
import com.kshitijpatil.tazabazar.data.local.entity.ProductEntity
import com.kshitijpatil.tazabazar.data.local.entity.ProductWithInventories
import com.kshitijpatil.tazabazar.data.local.entity.ProductWithInventoriesAndFavorites
import com.kshitijpatil.tazabazar.model.Inventory
import com.kshitijpatil.tazabazar.model.Product

class ProductResponseToProduct : Mapper<ProductResponse, Product> {
    override fun map(from: ProductResponse): Product {
        val inventories = from.inventories.map {
            Inventory(
                id = it.id,
                productSku = from.sku,
                price = it.price,
                quantityLabel = it.quantityLabel,
                stockAvailable = it.stockAvailable,
                updatedAt = TazaBazarTypeConverters.toOffsetDateTime(it.updatedAt)
            )
        }
        return Product(
            sku = from.sku,
            name = from.name,
            category = from.category,
            imageUri = from.imageUri,
            inventories = inventories
        )
    }
}

class ProductEntityToProduct : Mapper<ProductEntity, Product> {
    override fun map(from: ProductEntity): Product {
        return Product(
            sku = from.sku,
            name = from.name,
            category = from.category,
            imageUri = from.imageUri
        )
    }
}

class ProductWithInventoriesToProduct(
    private val productMapper: ProductEntityToProduct,
    private val inventoryMapper: InventoryEntityToInventory
) : Mapper<ProductWithInventories, Product> {
    override fun map(from: ProductWithInventories): Product {
        val inventories = from.inventories.map(inventoryMapper::map)
        val product = productMapper.map(from.product)
        return product.copy(inventories = inventories)
    }
}

class ProductWithInventoriesAndFavoritesToProduct(
    private val productMapper: ProductEntityToProduct,
    private val inventoryMapper: InventoryEntityToInventory
) : Mapper<ProductWithInventoriesAndFavorites, Product> {
    override fun map(from: ProductWithInventoriesAndFavorites): Product {
        val inventories = from.inventories.map(inventoryMapper::map)
        val favorites = from.favorites.map(FavoriteEntity::type)
        val product = productMapper.map(from.product)
        return product.copy(inventories = inventories, favorites = favorites.toSet())
    }
}

class ProductToProductWithInventories(private val inventoryEntityMapper: InventoryToInventoryEntity) :
    Mapper<Product, ProductWithInventories> {
    override fun map(from: Product): ProductWithInventories {
        val productEntity = ProductEntity(
            sku = from.sku,
            name = from.name,
            category = from.category,
            imageUri = from.imageUri
        )
        val inventoryEntities = from.inventories.map(inventoryEntityMapper::map)
        return ProductWithInventories(productEntity, inventoryEntities)
    }
}

class ProductToProductWithInventoriesAndFavorites(
    private val inventoryEntityMapper: InventoryToInventoryEntity
) : Mapper<Product, ProductWithInventoriesAndFavorites> {
    override fun map(from: Product): ProductWithInventoriesAndFavorites {
        val productEntity = ProductEntity(
            sku = from.sku,
            name = from.name,
            category = from.category,
            imageUri = from.imageUri
        )
        val inventoryEntities = from.inventories.map(inventoryEntityMapper::map)
        val favoriteEntities = from.favorites.map { FavoriteEntity(it, from.sku) }
        return ProductWithInventoriesAndFavorites(
            productEntity,
            inventoryEntities,
            favoriteEntities
        )
    }
}