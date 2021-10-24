package com.kshitijpatil.tazabazar.domain

import com.kshitijpatil.tazabazar.data.OrderRepository
import com.kshitijpatil.tazabazar.model.Order
import kotlinx.coroutines.CoroutineDispatcher

class GetUserOrdersUseCase(
    ioDispatcher: CoroutineDispatcher,
    private val orderRepository: OrderRepository
) : CoroutineUseCase<Unit, List<Order>>(ioDispatcher) {
    override suspend fun execute(parameters: Unit): List<Order> {
        return orderRepository.getOrdersOfCurrentUser()
    }
}