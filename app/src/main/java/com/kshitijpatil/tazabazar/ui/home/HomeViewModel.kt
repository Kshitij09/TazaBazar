package com.kshitijpatil.tazabazar.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    private val _selectedCategories = mutableSetOf<Int>()
    val selectedCategories: Set<Int>
        get() = _selectedCategories

    private val _productCategories = MutableStateFlow<Map<String, Int>>(emptyMap())
    val productCategories: StateFlow<Map<String, Int>>
        get() = _productCategories

    fun getProductCategories() {
        viewModelScope.launch {
            _productCategories.emit(productRepository.getProductCategoryMap())
        }
    }

    fun addCategoryFilter(categoryId: Int) {
        Timber.i("Adding categoryId='$categoryId' filter")
        _selectedCategories.add(categoryId)
        getFilteredProductList()
    }

    fun removeCategoryFilter(categoryId: Int) {
        Timber.i("Removing categoryId='$categoryId' filter")
        _selectedCategories.remove(categoryId)
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