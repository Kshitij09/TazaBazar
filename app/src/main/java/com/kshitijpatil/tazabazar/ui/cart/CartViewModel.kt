package com.kshitijpatil.tazabazar.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.data.CartRepository
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