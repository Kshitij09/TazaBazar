package com.kshitijpatil.tazabazar.domain

import com.kshitijpatil.tazabazar.data.AuthRepository
import com.kshitijpatil.tazabazar.model.AuthConfiguration
import kotlinx.coroutines.CoroutineDispatcher

class GetAuthConfigurationUseCase(
    dispatcher: CoroutineDispatcher,
    private val repo: AuthRepository
) : CoroutineUseCase<Unit, AuthConfiguration>(dispatcher) {
    override suspend fun execute(parameters: Unit): AuthConfiguration {
        return repo.getAuthConfiguration()
    }
}