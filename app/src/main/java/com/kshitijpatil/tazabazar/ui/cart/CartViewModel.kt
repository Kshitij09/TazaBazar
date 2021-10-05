package com.kshitijpatil.tazabazar.ui.cart

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.data.CartRepository
import com.kshitijpatil.tazabazar.di.RepositoryModule
import com.kshitijpatil.tazabazar.model.CartConfiguration
import com.kshitijpatil.tazabazar.model.CartItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>>
        get() = _cartItems.asStateFlow()
    val cartConfiguration: Flow<CartConfiguration> = flow {
        emit(CartConfiguration()) // default
        emit(cartRepository.getCartConfiguration())
    }

    init {
        reloadCartItems()
    }

    fun reloadCartItems() {
        viewModelScope.launch {
            val reloaded = cartRepository.getAllCartItems()
            _cartItems.emit(reloaded)
        }
    }

    fun incrementQuantity(item: CartItem) {
        viewModelScope.launch {
            cartRepository.addOrUpdateCartItem(item.inventoryId, item.quantity + 1)
            reloadCartItems()
        }
    }

    fun decrementQuantity(item: CartItem) {
        viewModelScope.launch {
            if (item.quantity != 1) {
                cartRepository.addOrUpdateCartItem(item.inventoryId, item.quantity - 1)
            } else {
                cartRepository.removeFromCart(item.inventoryId)
            }
            reloadCartItems()
        }
    }
}

class CartViewModelFactory(appContext: Context) : ViewModelProvider.Factory {
    private val cartRepository = RepositoryModule.provideCartItemRepository(appContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            return CartViewModel(cartRepository) as T
        }
        throw IllegalArgumentException("ViewModel not found")
    }

}