package com.kshitijpatil.tazabazar.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.data.ProductRepository
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory
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

    /** Start with empty default state, fetch from the repository in background */
    val productCategories: StateFlow<List<ProductCategory>> = flow {
        emit(productRepository.getProductCategories())
    }.stateIn(viewModelScope, WhileSubscribed(), emptyList())


    init {
        _filter.onEach {
            Timber.d("Filter updated: $it")
            refreshProductList(it)
        }.launchIn(viewModelScope)
    }

    private fun refreshProductList(filterParams: FilterParams) {
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