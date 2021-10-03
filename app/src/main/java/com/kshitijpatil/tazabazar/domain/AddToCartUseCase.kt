package com.kshitijpatil.tazabazar.domain

import com.kshitijpatil.tazabazar.data.CartRepository
import kotlinx.coroutines.CoroutineDispatcher

class AddToCartUseCase(
    private val cartRepository: CartRepository,
    dispatcher: CoroutineDispatcher
) : CoroutineUseCase<AddToCartUseCase.Params, Unit>(dispatcher) {
    data class Params(val inventoryId: Int, val quantity: Int)

    override suspend fun execute(parameters: Params) {
        cartRepository.addToCart(parameters.inventoryId, parameters.quantity)
    }
}