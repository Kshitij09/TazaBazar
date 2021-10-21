package com.kshitijpatil.tazabazar.domain

import arrow.core.Either
import com.kshitijpatil.tazabazar.data.AuthRepository
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.NoDataFoundException
import com.kshitijpatil.tazabazar.model.AuthConfiguration
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import com.kshitijpatil.tazabazar.util.retryIO
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
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
    private val dispatchers: AppCoroutineDispatchers,
    private val authRepository: AuthRepository,
) : FlowProducerUseCase<SessionState>(dispatchers.io) {
    private val downstreamFlow = MutableStateFlow<SessionState>(SessionState.Undefined)
    private var sessionExpiredEmitter: Job? = null
    private val authConfiguration = MutableStateFlow<AuthConfiguration?>(null)

    init {
        externalScope.launch {
            loadAuthConfigurationWithExponentialBackoff()
        }

        externalScope.launch(dispatchers.computation) { updateSessionStateOnDependantsChange() }
    }

    private suspend fun loadAuthConfigurationWithExponentialBackoff() {
        withContext(dispatchers.io) {
            val loadConfigResult = runCatching {
                retryIO(
                    times = LOAD_CONFIG_MAX_RETRIES,
                    maxDelay = LOAD_CONFIG_MAX_DELAY,
                    onError = { Timber.e(it, "Failed to load AuthConfiguration, retrying...") },
                    block = { authRepository.getAuthConfiguration() }
                )
            }
            loadConfigResult
                .onFailure {
                    Timber.e(it, "Even the last attempt failed, terminating...")
                    downstreamFlow.value = SessionState.Undefined
                }
                .onSuccess { authConfiguration.value = it }
        }
    }

    private suspend fun updateSessionStateOnDependantsChange() {
        // if config was null, there's nothing we can do
        val configNonNullFlow = authConfiguration.filterNotNull()

        authRepository.observeLoggedInAt()
            .flowOn(dispatchers.io)
            .combine(configNonNullFlow, ::Pair)
            .collect { (loggedInAt, config) ->
                Timber.d("Logged-in time changed to $loggedInAt")
                val sessionState = when (loggedInAt) {
                    is Either.Left -> getSessionStateFor(loggedInAt.value)
                    is Either.Right -> {
                        val now = LocalDateTime.now()
                        val elapsedMinutes = ChronoUnit.MINUTES.between(loggedInAt.value, now)
                        val sessionExpiryMinutes =
                            (config.tokenExpiryMinutes - BUFFER_MINUTES - elapsedMinutes).coerceAtLeast(
                                0
                            )
                        if (sessionExpiryMinutes > 0) {
                            // session not yet expired
                            Timber.d("Session will expire in $sessionExpiryMinutes minutes")
                            launchSessionExpiredEmitterWithDelay(sessionExpiryMinutes)
                            getSessionStateFromLoggedInUser()
                        } else {
                            SessionState.SessionExpired
                        }
                    }
                }
                downstreamFlow.emit(sessionState)
            }
    }

    private suspend fun getSessionStateFromLoggedInUser(): SessionState {
        return when (val user = authRepository.getLoggedInUser()) {
            is Either.Left -> getSessionStateFor(user.value)
            is Either.Right -> SessionState.LoggedIn(user.value)
        }
    }

    private fun getSessionStateFor(exception: DataSourceException): SessionState {
        return if (exception is NoDataFoundException) {
            // session was cleared or empty
            SessionState.LoggedOut
        } else {
            // some unhandled exception occurred
            SessionState.Undefined
        }
    }

    private fun launchSessionExpiredEmitterWithDelay(delayMinutes: Long) {
        sessionExpiredEmitter?.cancel()
        sessionExpiredEmitter = externalScope.launch {
            Timber.d("Session Expired Emitter was launched")
            delay(delayMinutes * 60 * 1000)
            downstreamFlow.emit(SessionState.SessionExpired)
        }
    }

    override fun createObservable(): Flow<SessionState> {
        return downstreamFlow.asStateFlow()
    }

    companion object {
        /** Expire Session in advance by [BUFFER_MINUTES] */
        const val BUFFER_MINUTES = 1L
        const val LOAD_CONFIG_MAX_RETRIES = 100
        const val LOAD_CONFIG_MAX_DELAY = 2_000L
    }
}