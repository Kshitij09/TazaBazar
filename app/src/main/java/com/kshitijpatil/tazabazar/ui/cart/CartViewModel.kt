package com.kshitijpatil.tazabazar.ui.cart

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.data.CartRepository
import com.kshitijpatil.tazabazar.di.RepositoryModule
import com.kshitijpatil.tazabazar.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>>
        get() = _cartItems.asStateFlow()

    init {
        reloadCartItems()
    }

    fun reloadCartItems() {
        viewModelScope.launch {
            val reloaded = cartRepository.getAllCartItems()
            _cartItems.emit(reloaded)
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