package com.kshitijpatil.tazabazar.data.local

import com.kshitijpatil.tazabazar.data.ProductDataSource
import com.kshitijpatil.tazabazar.data.local.dao.InventoryDao
import com.kshitijpatil.tazabazar.data.local.dao.ProductCategoryDao
import com.kshitijpatil.tazabazar.data.mapper.ProductWithInventoriesToProduct
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory

class ProductLocalDataSource(
    private val inventoryDao: InventoryDao,
    private val productMapper: ProductWithInventoriesToProduct,
    private val productCategoryDao: ProductCategoryDao
) : ProductDataSource {
    override suspend fun getProductCategories(): List<ProductCategory> {
        return productCategoryDao.getAllCategories().map {
            ProductCategory(
                label = it.label,
                name = it.name,
                skuPrefix = it.skuPrefix
            )
        }
    }

    override suspend fun getAllProducts(): List<Product> {
        return inventoryDao.getAllProductWithInventories()
            .map(productMapper::map)
    }

    override suspend fun getProductsBy(category: String?, query: String?): List<Product> {
        val productEntities = if (category != null && query != null) {
            inventoryDao.getProductsByCategoryAndName(category, "%$query%")
        } else if (category != null) {
            inventoryDao.getProductWithInventoriesByCategory(category)
        } else if (query != null) {
            inventoryDao.getProductWithInventoriesByName("%$query%")
        } else {
            inventoryDao.getAllProductWithInventories()
        }
        return productEntities.map(productMapper::map)
    }
}