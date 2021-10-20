package com.kshitijpatil.tazabazar.domain

import com.kshitijpatil.tazabazar.data.OrderRepository
import com.kshitijpatil.tazabazar.model.CartItem
import kotlinx.coroutines.CoroutineDispatcher

class PlaceOrderUseCase(
    dispatcher: CoroutineDispatcher,
    private val orderRepository: OrderRepository
) : CoroutineUseCase<List<CartItem>, Unit>(dispatcher) {
    override suspend fun execute(parameters: List<CartItem>) {
        orderRepository.placeOrder(parameters)
    }
}