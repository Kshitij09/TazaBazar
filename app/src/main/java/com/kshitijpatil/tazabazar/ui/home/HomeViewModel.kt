package com.kshitijpatil.tazabazar.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.api.dto.ProductCategoryDto
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.data.ProductRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(private val productRepository: ProductRepository) : ViewModel() {
    private val _productList = MutableStateFlow<List<ProductResponse>>(emptyList())
    val productList: StateFlow<List<ProductResponse>>
        get() = _productList
    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?>
        get() = _categoryFilter

    /** Start with empty default state, fetch from repository in background */
    val productCategories: StateFlow<List<ProductCategoryDto>> = flow {
        emit(productRepository.getProductCategories())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())


    init {
        refreshProductList()
    }

    private fun refreshProductList() {
        viewModelScope.launch {
            val productList = productRepository.getProductListBy(_categoryFilter.value)
            _productList.emit(productList)
        }
    }

    fun setCategoryFilter(category: String) {
        _categoryFilter.value = category
        refreshProductList()
    }

    fun clearCategoryFilter() {
        _categoryFilter.value = null
        refreshProductList()
    }
}