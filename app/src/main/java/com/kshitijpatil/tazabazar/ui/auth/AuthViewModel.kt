package com.kshitijpatil.tazabazar.ui.auth

import android.content.Context
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.savedstate.SavedStateRegistryOwner
import arrow.core.Either
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.data.*
import com.kshitijpatil.tazabazar.di.AppModule
import com.kshitijpatil.tazabazar.di.RepositoryModule
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.ui.common.ResourceMessage
import com.kshitijpatil.tazabazar.ui.common.SnackbarMessage
import com.kshitijpatil.tazabazar.util.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.coroutines.cancellation.CancellationException

class AuthViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: AuthRepository
) : ViewModel() {
    companion object {
        const val KEY_USERNAME = "com.kshitijpatil.tazabazar.ui.auth.AuthViewModel.username"
        const val KEY_PASSWORD = "com.kshitijpatil.tazabazar.ui.auth.AuthViewModel.password"
        const val KEY_FULL_NAME = "com.kshitijpatil.tazabazar.ui.auth.AuthViewModel.fullName"
    }

    private val _viewState = MutableStateFlow(
        AuthViewState(
            username = savedStateHandle[KEY_USERNAME],
            password = savedStateHandle[KEY_PASSWORD],
            fullName = savedStateHandle[KEY_FULL_NAME]
        )
    )
    val viewState: StateFlow<AuthViewState>
        get() = _viewState.asStateFlow()

    private val _snackbarMessages =
        Channel<SnackbarMessage>(capacity = 3, onBufferOverflow = BufferOverflow.DROP_OLDEST)

    val snackbarMessages: Flow<SnackbarMessage> =
        _snackbarMessages.consumeAsFlow()
            .shareIn(viewModelScope, WhileSubscribed(5000))

    init {
        updateUsernameFromPreferencesIfNull()
    }

    private fun updateUsernameFromPreferencesIfNull() {
        if (viewState.value.username == null) {
            viewModelScope.launch {
                updateUsername(repository.getLastLoggedInUsername())
            }
        }
    }

    fun updateUsername(username: String?) {
        savedStateHandle[KEY_USERNAME] = username
        setState { copy(username = username) }
    }

    fun updatePassword(password: String?) {
        savedStateHandle[KEY_PASSWORD] = password
        setState { copy(password = password) }
    }

    fun updateFullName(fullName: String?) {
        savedStateHandle[KEY_FULL_NAME] = fullName
        setState { copy(fullName = fullName) }
    }

    fun login(): Job {
        // reset any previous state
        setState { copy(loginState = UiState.Idle) }
        val loginJob = viewModelScope.launch {
            val currentState = viewState.value
            if (currentState.username == null) return@launch
            if (currentState.password == null) return@launch
            setState { copy(loginState = UiState.Loading(R.string.progress_login)) }
            val result =
                repository.login(LoginRequest(currentState.username, currentState.password))
            handleLoginResult(result)
        }
        loginJob.invokeOnCompletion {
            if (it is CancellationException) {
                Timber.d("login: Job Canceled")
            }
            setState { copy(loginState = UiState.Idle) }
        }
        return loginJob
    }

    private fun handleLoginResult(result: Either<LoginException, LoggedInUser>) {
        when (result) {
            is Either.Right -> {
                setState { copy(loginState = UiState.Success(result.value)) }
            }
            is Either.Left -> {
                setState { copy(loginState = UiState.Error) }
                notifyLoginExceptions(result.value)
            }
        }
    }

    private fun notifyLoginExceptions(ex: LoginException) {
        val msgId = when (ex) {
            InvalidCredentialsException -> R.string.error_invalid_credentials
            ValidationException -> R.string.error_validation_failed
            UnknownLoginException -> R.string.error_something_went_wrong
        }
        viewModelScope.launch {
            sendResourceMessageToSnackbar(msgId)
        }
    }

    private suspend fun sendResourceMessageToSnackbar(@StringRes resId: Int) {
        _snackbarMessages.send(ResourceMessage(resId))
    }


    private fun setState(mutator: AuthViewState.() -> AuthViewState) {
        viewModelScope.launch {
            _viewState.emit(mutator(_viewState.value))
        }
    }

    fun clearPassword() {
        setState { copy(password = null) }
    }
}

class AuthViewModelFactory(
    owner: SavedStateRegistryOwner,
    appContext: Context,
    defaultArgs: Bundle?
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    private val client = AppModule.provideOkHttpClient(appContext)
    private val repository = RepositoryModule.provideAuthRepository(appContext, client)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return AuthViewModel(handle, repository) as T
    }
}