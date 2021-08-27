package com.kshitijpatil.tazabazar.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.data.ProductRepository
import com.kshitijpatil.tazabazar.di.RepositoryModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val productRepository: ProductRepository) : ViewModel() {
    private val _productList = MutableStateFlow<List<ProductResponse>>(emptyList())
    val productList: StateFlow<List<ProductResponse>>
        get() = _productList
    private val selectedCategories = mutableSetOf<Int>()

    private val _productCategories = MutableStateFlow<Map<String, Int>>(emptyMap())
    val productCategories: StateFlow<Map<String, Int>>
        get() = _productCategories

    /** Fetch Product list from the remote source */
    fun getAllProducts() {
        viewModelScope.launch {
            _productList.emit(productRepository.getAllProducts())
        }
    }

    fun updateProductCategories() {
        viewModelScope.launch {
            _productCategories.emit(productRepository.getProductCategoryMap())
        }
    }

    fun addCategoryFilter(categoryId: Int) {
        selectedCategories.add(categoryId)
        filterProductListByCategories()
    }

    fun removeCategoryFilter(categoryId: Int) {
        selectedCategories.remove(categoryId)
        filterProductListByCategories()
    }

    fun clearAllCategoryFilters() {
        selectedCategories.clear()
        getAllProducts()
    }

    private fun filterProductListByCategories() {
        viewModelScope.launch {
            val productList =
                productRepository.getProductListByCategories(selectedCategories.toList())
            _productList.emit(productList)
        }
    }
}

class HomeViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(RepositoryModule.provideProductRepository()) as T
        }
        throw IllegalArgumentException()
    }
}