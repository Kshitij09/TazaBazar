package com.kshitijpatil.tazabazar.ui.favorite

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.data.ProductRepository
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import com.kshitijpatil.tazabazar.di.AppModule
import com.kshitijpatil.tazabazar.di.DomainModule
import com.kshitijpatil.tazabazar.di.RepositoryModule
import com.kshitijpatil.tazabazar.domain.AddOrUpdateCartItemUseCase
import com.kshitijpatil.tazabazar.domain.Result
import com.kshitijpatil.tazabazar.domain.data
import com.kshitijpatil.tazabazar.domain.succeeded
import com.kshitijpatil.tazabazar.model.Inventory
import com.kshitijpatil.tazabazar.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavoriteProductsViewModel(
    private val favoriteType: FavoriteType,
    private val productRepository: ProductRepository,
    private val addOrUpdateCartItemUseCase: AddOrUpdateCartItemUseCase
) : ViewModel() {
    private val _productList = MutableStateFlow<List<Product>>(emptyList())
    val productList: StateFlow<List<Product>>
        get() = _productList.asStateFlow()

    init {
        loadFavoriteProducts()
    }

    fun loadFavoriteProducts() {
        viewModelScope.launch {
            val favoriteProducts = productRepository.getProductListBy(favoriteType)
            _productList.emit(favoriteProducts)
        }
    }

    fun removeFavorites(product: Product) {
        viewModelScope.launch {
            productRepository.updateFavorites(product.sku, emptySet())
        }
        loadFavoriteProducts()
    }

    suspend fun addToCart(inventory: Inventory): Result<Boolean> {
        return addOrUpdateCartItemUseCase(AddOrUpdateCartItemUseCase.Params(inventory.id, 1))
    }

    /**
     * Adds all the products from [_productList] to cart
     * @return [Result<Int>] - if all succeed - No. of items newly added
     * else - exception wrapped within [Result]
     */
    suspend fun addAllFavoritesToCart(): Result<Int> {
        var newEntriesCount = 0
        val favoriteProducts = _productList.value
        var latestResult: Result<Boolean> = Result.Success(false)
        for (product in favoriteProducts) {
            if (product.defaultInventory != null) {
                latestResult = addToCart(product.defaultInventory!!)
                if (latestResult.succeeded) {
                    if (latestResult.data == true)
                        newEntriesCount++
                } else break
            }
        }
        return if (latestResult.succeeded) {
            Result.Success(newEntriesCount)
        } else {
            latestResult as Result<Int>
        }
    }

    fun searchProductsBy(query: String) {
        viewModelScope.launch {
            val q = if (query.isBlank()) null else query
            val newList = productRepository.getProductListBy(favoriteType, q)
            _productList.emit(newList)
        }
    }
}

class FavoriteProductsViewModelFactory(
    appContext: Context,
    private val favoriteType: FavoriteType
) : ViewModelProvider.Factory {
    private val productRepository =
        RepositoryModule.provideProductRepository(appContext)
    private val dispatchers = AppModule.provideAppCoroutineDispatchers()
    private val addToCartUseCase = DomainModule.provideAddToCartUseCase(appContext, dispatchers.io)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteProductsViewModel::class.java)) {
            return FavoriteProductsViewModel(favoriteType, productRepository, addToCartUseCase) as T
        }
        throw IllegalArgumentException()
    }

}