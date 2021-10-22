package com.kshitijpatil.tazabazar.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.data.CartRepository
import com.kshitijpatil.tazabazar.domain.ObserveSessionStateUseCase
import com.kshitijpatil.tazabazar.domain.PlaceOrderUseCase
import com.kshitijpatil.tazabazar.domain.SessionState
import com.kshitijpatil.tazabazar.domain.succeeded
import com.kshitijpatil.tazabazar.model.CartConfiguration
import com.kshitijpatil.tazabazar.model.CartItem
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CartViewModel(
    private val cartRepository: CartRepository,
    private val placeOrderUseCase: PlaceOrderUseCase,
    private val observeSessionStateUseCase: ObserveSessionStateUseCase
) : ViewModel() {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>>
        get() = _cartItems.asStateFlow()
    val cartConfiguration: Flow<CartConfiguration> = flow {
        emit(CartConfiguration()) // default
        emit(cartRepository.getCartConfiguration())
    }

    private val _placeOrderUiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val placeOrderUiState: StateFlow<UiState<Unit>>
        get() = _placeOrderUiState.asStateFlow()

    private val _loggedInUser = MutableStateFlow<LoggedInUser?>(null)
    val loggedInUser get() = _loggedInUser.asStateFlow()

    init {
        reloadCartItems()
        viewModelScope.launch { observeSessionForLoggedInUser() }
    }

    private suspend fun observeSessionForLoggedInUser() {
        observeSessionStateUseCase()
            .collect {
                _loggedInUser.value = if (it is SessionState.LoggedIn) it.user else null
            }
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

    fun placeOrder() {
        viewModelScope.launch {
            _placeOrderUiState.emit(UiState.Loading())
            val result = placeOrderUseCase(_cartItems.value)
            if (result.succeeded) {
                cartRepository.clearCart()
                reloadCartItems()
                _placeOrderUiState.emit(UiState.Success(Unit))
            } else {
                _placeOrderUiState.emit(UiState.Error)
            }
        }
    }
}