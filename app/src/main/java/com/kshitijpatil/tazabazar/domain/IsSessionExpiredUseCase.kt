package com.kshitijpatil.tazabazar.domain

import arrow.core.getOrHandle
import com.kshitijpatil.tazabazar.data.AuthRepository
import com.kshitijpatil.tazabazar.data.mapper.EitherStringSerializer
import kotlinx.coroutines.CoroutineDispatcher
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit


/** Returns `true` if the session was expired, `false` otherwise */
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
        val lastLoggedInTime = authRepository.getLoggedInAt().getOrHandle {
            // if there was any exception while getting login-time
            // we treat it as session expired
            return true
        }
        val now = LocalDateTime.now()
        return ChronoUnit.MINUTES.between(
            lastLoggedInTime,
            now
        ) > (config.tokenExpiryMinutes - BUFFER_MINUTES)
    }

}