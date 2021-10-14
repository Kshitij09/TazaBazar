package com.kshitijpatil.tazabazar.domain

import arrow.core.getOrHandle
import com.kshitijpatil.tazabazar.data.AuthRepository
import com.kshitijpatil.tazabazar.data.mapper.EitherStringSerializer
import kotlinx.coroutines.CoroutineDispatcher
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit


class IsSessionExpiredUseCase(
    dispatcher: CoroutineDispatcher,
    private val authRepository: AuthRepository,
    private val dateTimeSerializer: EitherStringSerializer<LocalDateTime>
) : CoroutineUseCase<Unit, Boolean>(dispatcher) {
    companion object {
        /** Expire Session in advance by [BUFFER_MINUTES] */
        const val BUFFER_MINUTES = 1
    }

    override suspend fun execute(parameters: Unit): Boolean {
        val config = authRepository.getAuthConfiguration()
        val lastLoggedInRaw = authRepository.getLoggedInAt() ?: return false
        val lastLoggedInTime = dateTimeSerializer.deserialize(lastLoggedInRaw).getOrHandle {
            throw Exception("Failed to deserialize stored time")
        }
        val now = LocalDateTime.now()
        return ChronoUnit.MINUTES.between(
            lastLoggedInTime,
            now
        ) > (config.tokenExpiryMinutes - BUFFER_MINUTES)
    }

}