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
    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?>
        get() = _categoryFilter

    private val _productCategories = MutableStateFlow<List<ProductCategoryDto>>(emptyList())
    val productCategories: StateFlow<List<ProductCategoryDto>>
        get() = _productCategories

    fun fetchProductCategories() {
        viewModelScope.launch {
            _productCategories.emit(productRepository.getProductCategoryMap())
        }
    }

    fun setCategoryFilter(category: String) {
        Timber.i("Category filter changed to $category")
        _categoryFilter.value = category
        refreshProductList()
    }

    fun clearCategoryFilter() {
        _categoryFilter.value = null
        refreshProductList()
    }

    fun refreshProductList() {
        viewModelScope.launch {
            val productList =
                productRepository.getProductListByCategories(_categoryFilter.value)
            _productList.emit(productList)
        }
    }
}