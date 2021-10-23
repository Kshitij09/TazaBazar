package com.kshitijpatil.tazabazar.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.domain.GetUserOrdersUseCase
import com.kshitijpatil.tazabazar.domain.ObserveSessionStateUseCase
import com.kshitijpatil.tazabazar.domain.Result
import com.kshitijpatil.tazabazar.domain.SessionState
import com.kshitijpatil.tazabazar.model.Order
import com.kshitijpatil.tazabazar.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val getUserOrdersUseCase: GetUserOrdersUseCase,
    private val observeSessionStateUseCase: ObserveSessionStateUseCase
) : ViewModel() {
    private val _userOrdersState = MutableStateFlow<UiState<List<Order>>>(UiState.Idle)
    val userOrdersState: StateFlow<UiState<List<Order>>>
        get() = _userOrdersState.asStateFlow()
    private val _isLoggedIn = MutableStateFlow(false)

    val isLoggedIn: StateFlow<Boolean>
        get() = _isLoggedIn.asStateFlow()

    private var _selectedOrder: Order? = null

    /** this field should only be accessed when updateSelectedOrder was succeeded */
    val selectedOrder: Order get() = _selectedOrder!!

    init {
        viewModelScope.launch { loadUserOrders() }
        viewModelScope.launch { observeSessionForLoggedInUser() }
    }

    private suspend fun observeSessionForLoggedInUser() {
        observeSessionStateUseCase().collect {
            _isLoggedIn.value = it is SessionState.LoggedIn
        }
    }

    /** Sets the selected order by searching [userOrdersState]'s list by [orderId]
     * @return true if the current [userOrdersState] is [UiState.Success] and [orderId]
     * was found in the available list, false otherwise.
     */
    fun updateSelectedOrderById(orderId: String): Boolean {
        val currentOrderState = _userOrdersState.value
        if (currentOrderState is UiState.Success) {
            _selectedOrder = currentOrderState.value.find { it.orderId == orderId }
            return _selectedOrder != null
        }
        return false
    }

    private suspend fun loadUserOrders() {
        _userOrdersState.emit(UiState.Loading())
        val userOrdersState = when (val result = getUserOrdersUseCase(Unit)) {
            is Result.Error -> UiState.Error
            Result.Loading -> UiState.Loading()
            is Result.Success -> UiState.Success(result.data)
        }
        _userOrdersState.emit(userOrdersState)
    }

    fun refreshOrdersList(): Job {
        return viewModelScope.launch {
            loadUserOrders()
        }
    }
}