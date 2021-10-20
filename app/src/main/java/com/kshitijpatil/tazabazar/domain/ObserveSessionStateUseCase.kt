package com.kshitijpatil.tazabazar.domain

import arrow.core.*
import com.kshitijpatil.tazabazar.data.AuthRepository
import com.kshitijpatil.tazabazar.model.AuthConfiguration
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.getOrNull
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit
import timber.log.Timber


sealed class SessionState {
    data class LoggedIn(val user: LoggedInUser) : SessionState()
    object LoggedOut : SessionState()
    object SessionExpired : SessionState()
    object Undefined : SessionState()
}

class ObserveSessionStateUseCase(
    private val externalScope: CoroutineScope,
    ioDispatcher: CoroutineDispatcher,
    private val authRepository: AuthRepository,
) : FlowProducerUseCase<SessionState>(ioDispatcher) {
    private val downstreamFlow = MutableStateFlow<SessionState>(SessionState.Undefined)
    private var sessionExpiredEmitter: Job? = null
    private var authConfiguration: AuthConfiguration? = null
    private val mutex = Mutex()

    init {
        externalScope.launch {
            loadAuthConfiguration()
            val initialState = getInitialState()
            downstreamFlow.emit(initialState)
        }
        externalScope.launch { updateSessionOnAccessTokenChanged() }
    }

    private suspend fun loadAuthConfiguration() {
        val fetchedConfig = runCatching { authRepository.getAuthConfiguration() }.getOrNull()
        mutex.withLock {
            authConfiguration = fetchedConfig
        }
    }

    private suspend fun updateSessionOnAccessTokenChanged() {
        authRepository.observeAccessToken()
            .collect { token ->
                if (token == null)
                    downstreamFlow.emit(SessionState.LoggedOut)
                else {
                    val config = authConfiguration ?: return@collect
                    val sessionExpiryMinutes = config.tokenExpiryMinutes - BUFFER_MINUTES
                    launchSessionExpiredEmitterWithDelay(sessionExpiryMinutes)
                }
            }
    }

    private fun launchSessionExpiredEmitterWithDelay(delayMinutes: Long) {
        sessionExpiredEmitter?.cancel()
        sessionExpiredEmitter = externalScope.launch {
            delay(delayMinutes * 60 * 1000)
            downstreamFlow.emit(SessionState.SessionExpired)
        }
    }

    private suspend fun getInitialState(): SessionState {
        val loggedInAt = authRepository.getLoggedInAt().orNull()
            ?: return SessionState.LoggedOut

        val config = authConfiguration
        if (config == null) {
            Timber.e("Failed to retrieve auth configuration, can't decide Session State")
            return SessionState.Undefined
        }

        val now = LocalDateTime.now()
        val sessionExpiryMinutes = config.tokenExpiryMinutes - BUFFER_MINUTES
        val sessionExpired = ChronoUnit.MINUTES.between(loggedInAt, now) > sessionExpiryMinutes
        return if (sessionExpired) {
            SessionState.SessionExpired
        } else {
            val user = authRepository.getLoggedInUser().getOrNull()
            if (user == null) {
                Timber.e("Session wasn't cleared/set properly, failed to get LoggedInUser")
                SessionState.Undefined
            } else {
                SessionState.LoggedIn(user)
            }
        }
    }

    override fun createObservable(): Flow<SessionState> {
        return downstreamFlow.asStateFlow()
    }

    companion object {
        /** Expire Session in advance by [BUFFER_MINUTES] */
        const val BUFFER_MINUTES = 1L
    }
}