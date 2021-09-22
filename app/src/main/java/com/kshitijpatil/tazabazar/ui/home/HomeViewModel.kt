package com.kshitijpatil.tazabazar.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.api.dto.ProductCategoryDto
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(private val productRepository: ProductRepository) : ViewModel() {
    private val _productList = MutableStateFlow<List<ProductResponse>>(emptyList())
    val productList: StateFlow<List<ProductResponse>>
        get() = _productList
    private val _selectedCategories = mutableSetOf<String>()
    val selectedCategories: Set<String>
        get() = _selectedCategories

    private val _productCategories = MutableStateFlow<List<ProductCategoryDto>>(emptyList())
    val productCategories: StateFlow<List<ProductCategoryDto>>
        get() = _productCategories

    fun getProductCategories() {
        viewModelScope.launch {
            _productCategories.emit(productRepository.getProductCategoryMap())
        }
    }

    fun addCategoryFilter(category: String) {
        Timber.i("Adding categoryId='$category' filter")
        _selectedCategories.add(category)
        getFilteredProductList()
    }

    fun removeCategoryFilter(categories: String) {
        Timber.i("Removing categoryId='$categories' filter")
        _selectedCategories.remove(categories)
        getFilteredProductList()
    }

    fun clearAllCategoryFilters() {
        _selectedCategories.clear()
        getFilteredProductList()
    }

    fun getFilteredProductList() {
        viewModelScope.launch {
            val productList =
                productRepository.getProductListByCategories(_selectedCategories.toList())
            _productList.emit(productList)
        }
    }
}