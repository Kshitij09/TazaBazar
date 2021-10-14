package com.kshitijpatil.tazabazar.ui.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.di.AppModule
import com.kshitijpatil.tazabazar.di.DomainModule
import com.kshitijpatil.tazabazar.domain.LogoutUseCase
import com.kshitijpatil.tazabazar.domain.ObserveLoggedInUserUseCase
import com.kshitijpatil.tazabazar.domain.succeeded
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.UiState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.reflect.KProperty1

data class ProfileViewState(
    val loggedInUser: LoggedInUser? = null,
    val logoutState: UiState<Unit> = UiState.Idle
)

class ProfileViewModel(
    observeLoggedInUserUseCase: ObserveLoggedInUserUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _viewState = MutableStateFlow(ProfileViewState())
    val viewState: StateFlow<ProfileViewState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch { collectLoggedInState(observeLoggedInUserUseCase()) }
    }

    private suspend fun collectLoggedInState(upstream: Flow<LoggedInUser?>) {
        upstream.collect { setState { copy(loggedInUser = it) } }
    }

    private fun setState(mutator: ProfileViewState.() -> ProfileViewState) {
        viewModelScope.launch {
            _viewState.emit(mutator(_viewState.value))
        }
    }

    fun logout(): Job {
        setState { copy(logoutState = UiState.Loading()) }
        val logoutJob = viewModelScope.launch {
            val result = logoutUseCase(Unit)
            val logoutState = if (result.succeeded)
                UiState.Success(Unit)
            else
                UiState.Error
            setState { copy(logoutState = logoutState) }
        }
        logoutJob.invokeOnCompletion {
            if (it is CancellationException) {
                Timber.d("logout: Job Canceled")
            }
            setState { copy(logoutState = UiState.Idle) }
        }
        return logoutJob
    }

    fun <A> selectSubscribe(prop: KProperty1<ProfileViewState, A>): Flow<A> {
        return _viewState.map { prop.get(it) }.distinctUntilChanged()
    }
}

class ProfileViewModelFactory(appContext: Context) : ViewModelProvider.Factory {
    private val dispatchers = AppModule.provideAppCoroutineDispatchers()
    private val observeLoggedInUserUseCase = DomainModule.provideObserveLoggedInUserUseCase(
        appContext,
        dispatchers.io
    )
    private val logoutUseCase = DomainModule.provideLogoutUseCase(dispatchers.io, appContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(observeLoggedInUserUseCase, logoutUseCase) as T
        }
        throw IllegalArgumentException("Invalid ViewModel")
    }

}