package com.kshitijpatil.tazabazar.domain

import com.kshitijpatil.tazabazar.data.CartRepository
import kotlinx.coroutines.CoroutineDispatcher

class AddOrUpdateCartItemUseCase(
    private val cartRepository: CartRepository,
    dispatcher: CoroutineDispatcher
) : CoroutineUseCase<AddOrUpdateCartItemUseCase.Params, Boolean>(dispatcher) {
    data class Params(val inventoryId: Int, val quantity: Int)

    /**
     * Add or Update Cart Item with given [Params.inventoryId]
     * @return (Boolean) - Whether item was added to cart
     */
    override suspend fun execute(parameters: Params): Boolean {
        return cartRepository.addOrUpdateCartItem(parameters.inventoryId, parameters.quantity)
    }
}