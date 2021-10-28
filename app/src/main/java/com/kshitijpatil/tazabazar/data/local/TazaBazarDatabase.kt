package com.kshitijpatil.tazabazar.data.local

import com.kshitijpatil.tazabazar.data.local.dao.*

interface TazaBazarDatabase {
    val productDao: ProductDao
    val inventoryDao: InventoryDao
    val productCategoryDao: ProductCategoryDao
    val favoriteDao: FavoriteDao
    val cartItemDao: CartItemDao
}