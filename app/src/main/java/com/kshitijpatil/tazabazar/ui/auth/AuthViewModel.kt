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
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import com.kshitijpatil.tazabazar.data.*
import com.kshitijpatil.tazabazar.di.AppModule
import com.kshitijpatil.tazabazar.di.RepositoryModule
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.ui.common.ResourceMessage
import com.kshitijpatil.tazabazar.ui.common.SnackbarMessage
import com.kshitijpatil.tazabazar.util.UiState
import com.kshitijpatil.tazabazar.util.launchWithMutex
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
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
        const val KEY_PHONE = "com.kshitijpatil.tazabazar.ui.auth.AuthViewModel.phone"
    }

    private val _viewState = MutableStateFlow(
        AuthViewState(
            username = savedStateHandle[KEY_USERNAME],
            password = savedStateHandle[KEY_PASSWORD],
            fullName = savedStateHandle[KEY_FULL_NAME],
            phone = savedStateHandle[KEY_PHONE]
        )
    )
    val viewState: StateFlow<AuthViewState>
        get() = _viewState.asStateFlow()

    private val _snackbarMessages = MutableSharedFlow<SnackbarMessage>()

    val snackbarMessages: Flow<SnackbarMessage> =
        _snackbarMessages.shareIn(viewModelScope, WhileSubscribed(5000))

    private var _lastLoggedInUsername: String? = null
    private val mutex = Mutex()
    val lastLoggedInUsername: String? get() = _lastLoggedInUsername

    init {
        loadLastLoggedInUsername()
    }

    private fun loadLastLoggedInUsername() {
        viewModelScope.launchWithMutex(mutex) {
            _lastLoggedInUsername = repository.getLastLoggedInUsername()
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

    fun updatePhone(phone: String?) {
        savedStateHandle[KEY_PHONE] = phone
        setState { copy(phone = phone) }
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
        sendResourceMessageToSnackbar(msgId)
    }

    fun register(): Job {
        setState { copy(registerState = UiState.Idle) }
        val registerJob = viewModelScope.launch {
            val currentState = viewState.value
            if (currentState.fullName == null) return@launch
            if (currentState.username == null) return@launch
            if (currentState.phone == null) return@launch
            if (currentState.password == null) return@launch
            setState { copy(registerState = UiState.Loading(R.string.progress_register)) }
            val request = RegisterRequest(
                currentState.username,
                currentState.password,
                currentState.fullName,
                currentState.phone
            )
            val result = repository.register(request)
            handleRegisterResult(result)
        }
        registerJob.invokeOnCompletion {
            if (it is CancellationException) {
                Timber.d("register: Job Canceled")
            }
            setState { copy(registerState = UiState.Idle) }
        }

        return registerJob
    }

    private fun handleRegisterResult(result: Either<RegisterException, LoggedInUser>) {
        when (result) {
            is Either.Left -> {
                setState { copy(registerState = UiState.Error) }
                notifyRegisterExceptions(result.value)
            }
            is Either.Right -> {
                setState { copy(registerState = UiState.Success(result.value)) }
            }
        }
    }

    private fun notifyRegisterExceptions(exception: RegisterException) {
        val errorMsgId = when (exception) {
            PhoneExistsException -> R.string.error_phone_exists
            UsernameExistsException -> R.string.error_email_exists
            UnknownRegisterException -> R.string.error_something_went_wrong
        }
        sendResourceMessageToSnackbar(errorMsgId)
    }

    private fun sendResourceMessageToSnackbar(@StringRes resId: Int) {
        viewModelScope.launch {
            _snackbarMessages.emit(ResourceMessage(resId))
        }
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