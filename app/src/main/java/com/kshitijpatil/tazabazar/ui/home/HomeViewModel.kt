package com.kshitijpatil.tazabazar.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.data.ProductRepository
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository
) : ViewModel() {
    companion object {
        private const val KEY_QUERY = "query"
        private const val KEY_CATEGORY = "category"
    }

    private val _productList = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>>
        get() = _productList

    private val _filter = MutableStateFlow(
        FilterParams(
            query = savedStateHandle[KEY_QUERY],
            category = savedStateHandle[KEY_CATEGORY]
        )
    )
    val searchQuery: StateFlow<String?>
        get() = _filter
            .map { it.query }
            .stateIn(viewModelScope, WhileSubscribed(), _filter.value.query)

    val searchCategory: StateFlow<String?>
        get() = _filter
            .map { it.category }
            .stateIn(viewModelScope, WhileSubscribed(), _filter.value.category)

    private val _productCategories = MutableStateFlow<List<ProductCategory>>(emptyList())
    val productCategories: StateFlow<List<ProductCategory>>
        get() = _productCategories

    // TODO: update this
    private val cacheExpired: Boolean = true

    init {
        var refreshJob: Job? = null
        if (cacheExpired) {
            refreshJob = viewModelScope.launch { productRepository.refreshProductData() }
        }
        viewModelScope.launch {
            refreshJob?.join() // wait till cache is updated (if required)
            _productCategories.emit(productRepository.getProductCategories())
            // NOTE: We're initializing `_filter` with the values from savedInstanceState
            // this will inherently call `updateProductList`, either for
            // 1) Last saved filters or
            // 2) no filters (null values)
            _filter.collect {
                Timber.d("Filter updated: $it")
                updateProductList(it)
            }
        }
    }

    fun refreshData() = viewModelScope.launch {
        productRepository.refreshProductData()
        _productCategories.emit(productRepository.getProductCategories())
        updateProductList(_filter.value)
    }

    private fun updateProductList(filterParams: FilterParams) {
        viewModelScope.launch {
            val productList = productRepository.getProductListBy(
                category = filterParams.category,
                query = filterParams.query
            )
            _productList.emit(productList)
        }
    }

    fun setCategoryFilter(category: String) {
        _filter.value = _filter.value.copy(category = category)
        savedStateHandle[KEY_CATEGORY] = category
    }

    fun clearCategoryFilter() {
        _filter.value = _filter.value.copy(category = null)
        savedStateHandle[KEY_CATEGORY] = null
    }

    fun setSearchQuery(query: String) {
        val q = if (query.isBlank()) null else query
        _filter.value = _filter.value.copy(query = q)
        savedStateHandle[KEY_QUERY] = query
    }

    data class FilterParams(
        val query: String? = null,
        val category: String? = null
    )
}