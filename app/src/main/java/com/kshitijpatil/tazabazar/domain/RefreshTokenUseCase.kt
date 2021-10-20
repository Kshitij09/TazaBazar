package com.kshitijpatil.tazabazar.domain

import com.kshitijpatil.tazabazar.data.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher

class RefreshTokenUseCase(
    dispatcher: CoroutineDispatcher,
    private val repository: AuthRepository
) : CoroutineUseCase<Unit, Unit>(dispatcher) {
    override suspend fun execute(parameters: Unit) {
        repository.refreshToken()
    }
}