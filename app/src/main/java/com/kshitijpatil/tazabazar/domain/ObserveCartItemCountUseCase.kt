package com.kshitijpatil.tazabazar.domain

import com.kshitijpatil.tazabazar.data.CartRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow

class ObserveCartItemCountUseCase(
    private val repository: CartRepository,
    dispatcher: CoroutineDispatcher?
) : FlowProducerUseCase<Int>(dispatcher) {
    override fun createObservable(): Flow<Int> {
        return repository.observeCartItemCount()
    }
}