package com.kshitijpatil.tazabazar.ui.favorite

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.data.ProductRepository
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import com.kshitijpatil.tazabazar.di.AppModule
import com.kshitijpatil.tazabazar.di.RepositoryModule
import com.kshitijpatil.tazabazar.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteProductsViewModel(
    private val favoriteType: FavoriteType,
    private val productRepository: ProductRepository
) : ViewModel() {
    private val _productList = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>>
        get() = _productList.asStateFlow()

    init {
        loadFavoriteProducts()
    }

    fun loadFavoriteProducts() {
        viewModelScope.launch {
            val favoriteProducts = productRepository.getProductsByFavoriteType(favoriteType)
            _productList.emit(favoriteProducts)
        }
    }
}

class FavoriteProductsViewModelFactory(
    appContext: Context,
    private val favoriteType: FavoriteType
) : ViewModelProvider.Factory {
    private val okhttpClient = AppModule.provideOkHttpClient(appContext)
    private val productRepository =
        RepositoryModule.provideProductRepository(appContext, okhttpClient)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteProductsViewModel::class.java)) {
            return FavoriteProductsViewModel(favoriteType, productRepository) as T
        }
        throw IllegalArgumentException()
    }

}