package com.kshitijpatil.tazabazar.ui.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.data.ProductRepository
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import com.kshitijpatil.tazabazar.domain.AddOrUpdateCartItemUseCase
import com.kshitijpatil.tazabazar.domain.MediatorResult
import com.kshitijpatil.tazabazar.domain.Result
import com.kshitijpatil.tazabazar.domain.SearchProductsUseCase
import com.kshitijpatil.tazabazar.model.Inventory
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.model.ProductCategory
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch

class HomeViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val addOrUpdateCartItemUseCase: AddOrUpdateCartItemUseCase
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

    private val _uiEvents = MutableSharedFlow<UiEvent>()
    val uiEvents: SharedFlow<UiEvent>
        get() = _uiEvents.shareIn(viewModelScope, WhileSubscribed(5000))

    init {
        viewModelScope.launch { observeProductSearchResults() }
        viewModelScope.launch { loadProductCategories(forceRefresh = true) }
        viewModelScope.launch {
            loadProducts(
                query = null,
                category = null,
                forceRefresh = true
            )
        }
        viewModelScope.launch { observeProductFilters() }
        /*var refreshJob: Job? = null
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
        }*/
    }

    private suspend fun observeProductFilters() {
        _filter.collect {
            val params = SearchProductsUseCase.Params(it.query, it.category)
            searchProductsUseCase(params)
        }
    }

    private suspend fun loadProducts(
        query: String?,
        category: String?,
        forceRefresh: Boolean = false
    ) {
        // SearchProductsUseCase will take care of emitting
        // local items first followed by remote items if succeed
        val params = SearchProductsUseCase.Params(query, category, forceRefresh)
        searchProductsUseCase(params)
    }

    private suspend fun loadProductWithCurrentFilters(forceRefresh: Boolean = false) {
        val currentFilters = _filter.value
        loadProducts(currentFilters.query, currentFilters.category, forceRefresh)
    }

    private suspend fun loadProductCategories(forceRefresh: Boolean) {
        // get local first
        _productCategories.emit(productRepository.getProductCategories())
        // try remote
        runCatching {
            productRepository.getProductCategories(forceRefresh = forceRefresh)
        }.onSuccess { _productCategories.emit(it) }
    }

    private suspend fun observeProductSearchResults() {
        searchProductsUseCase.observe().collect {
            when (it) {
                is MediatorResult.Error -> {
                    if (it.isRemoteOrigin()) {
                        _uiEvents.emit(UiEvent.FetchCompleted.Failure)
                    }
                }
                is MediatorResult.Loading -> {
                    if (it.isRemoteOrigin())
                        _uiEvents.emit(UiEvent.FetchingProducts)
                }
                is MediatorResult.Success -> {
                    if (it.isRemoteOrigin())
                        _uiEvents.emit(UiEvent.FetchCompleted.Success)
                    _productList.emit(it.data)
                }
            }
        }
    }

    /**
     * Reloads product-list and product-categories from the database
     * or remote source.
     * @param forceRefresh whether to fetch data from the remote source
     */
    suspend fun reloadProductsData(forceRefresh: Boolean = false) {
        /*if (forceRefresh) {
            productRepository.refreshProductData()
        }*/
        //_productCategories.emit(productRepository.getProductCategories())
        loadProductCategories(forceRefresh)
        loadProductWithCurrentFilters(forceRefresh)
        //updateProductList(_filter.value)
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

    fun clearSearchQuery() {
        _filter.value = _filter.value.copy(query = null)
        savedStateHandle[KEY_QUERY] = null
    }

    fun setSearchQuery(query: String) {
        val q = if (query.isBlank()) null else query
        _filter.value = _filter.value.copy(query = q)
        savedStateHandle[KEY_QUERY] = query
    }

    fun updateFavorites(productSku: String, favoriteChoices: Set<FavoriteType>) {
        viewModelScope.launch {
            productRepository.updateFavorites(productSku, favoriteChoices)
            //updateProductList(_filter.value)
            loadProductWithCurrentFilters()
        }
    }

    suspend fun addToCart(inventory: Inventory): Result<Boolean> {
        return addOrUpdateCartItemUseCase(AddOrUpdateCartItemUseCase.Params(inventory.id, 1))
    }

    fun clearAllFilters() {
        clearCategoryFilter()
        clearSearchQuery()
        viewModelScope.launch { _uiEvents.emit(UiEvent.ClearFilters) }
    }

    data class FilterParams(
        val query: String? = null,
        val category: String? = null
    )

    sealed class UiEvent {
        object ClearFilters : UiEvent()
        object FetchingProducts : UiEvent()
        sealed class FetchCompleted : UiEvent() {
            object Failure : FetchCompleted()
            object Success : FetchCompleted()
        }
    }
}