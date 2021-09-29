package com.kshitijpatil.tazabazar.data.mapper

import com.kshitijpatil.tazabazar.api.dto.ProductCategoryDto
import com.kshitijpatil.tazabazar.data.local.entity.ProductCategoryEntity
import com.kshitijpatil.tazabazar.model.ProductCategory

class ProductCategoryToProductCategoryEntity : Mapper<ProductCategory, ProductCategoryEntity> {
    override fun map(from: ProductCategory): ProductCategoryEntity {
        return ProductCategoryEntity(
            label = from.label,
            name = from.name,
            skuPrefix = from.skuPrefix
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