package com.kshitijpatil.tazabazar.data.mapper

import com.kshitijpatil.tazabazar.api.dto.ProductCategoryDto
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.data.local.TazaBazarTypeConverters
import com.kshitijpatil.tazabazar.data.local.entity.InventoryEntity
import com.kshitijpatil.tazabazar.data.local.entity.ProductCategoryEntity
import com.kshitijpatil.tazabazar.data.local.entity.ProductEntity
import com.kshitijpatil.tazabazar.data.local.entity.ProductWithInventories
import com.kshitijpatil.tazabazar.model.Inventory
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory

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

class ProductWithInventoriesToProduct(private val inventoryMapper: InventoryEntityToInventory) :
    Mapper<ProductWithInventories, Product> {
    override fun map(from: ProductWithInventories): Product {
        val inventories = from.inventories.map(inventoryMapper::map)
        val entity = from.product
        return Product(
            sku = entity.sku,
            name = entity.name,
            category = entity.category,
            imageUri = entity.imageUri,
            inventories = inventories
        )
    }

}

class ProductCategoryToProductCategoryEntity : Mapper<ProductCategory, ProductCategoryEntity> {
    override fun map(from: ProductCategory): ProductCategoryEntity {
        return ProductCategoryEntity(
            label = from.label,
            name = from.name,
            skuPrefix = from.skuPrefix
        )
    }
}

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

class ProductCategoryEntityToProductCategory : Mapper<ProductCategoryEntity, ProductCategory> {
    override fun map(from: ProductCategoryEntity): ProductCategory {
        return ProductCategory(
            label = from.label,
            name = from.name,
            skuPrefix = from.skuPrefix
        )
    }
}

class ProductCategoryDtoToProductCategory : Mapper<ProductCategoryDto, ProductCategory> {
    override fun map(from: ProductCategoryDto): ProductCategory {
        return ProductCategory(
            label = from.label,
            name = from.name,
            skuPrefix = from.skuPrefix
        )
    }
}