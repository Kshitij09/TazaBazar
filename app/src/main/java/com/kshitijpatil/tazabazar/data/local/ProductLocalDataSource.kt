package com.kshitijpatil.tazabazar.data.local

import com.kshitijpatil.tazabazar.data.ProductDataSource
import com.kshitijpatil.tazabazar.data.local.dao.FavoriteDao
import com.kshitijpatil.tazabazar.data.local.dao.ProductCategoryDao
import com.kshitijpatil.tazabazar.data.mapper.ProductWithInventoriesAndFavoritesToProduct
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory

class ProductLocalDataSource(
    private val favoriteDao: FavoriteDao,
    private val productMapper: ProductWithInventoriesAndFavoritesToProduct,
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
        return favoriteDao.getAllProductWithInventoriesAndFavorites()
            .map(productMapper::map)
    }

    override suspend fun getProductsBy(category: String?, query: String?): List<Product> {
        val productEntities = if (category != null && query != null) {
            favoriteDao.getProductsWithInventoriesAndFavoritesByCategoryAndName(
                category,
                "%$query%"
            )
        } else if (category != null) {
            favoriteDao.getProductWithInventoriesAndFavoritesByCategory(category)
        } else if (query != null) {
            favoriteDao.getProductWithInventoriesAndFavoritesByName("%$query%")
        } else {
            favoriteDao.getAllProductWithInventoriesAndFavorites()
        }
        return productEntities.map(productMapper::map)
    }
}