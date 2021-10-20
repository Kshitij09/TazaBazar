package com.kshitijpatil.tazabazar.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kshitijpatil.tazabazar.domain.LogoutUseCase
import com.kshitijpatil.tazabazar.domain.ObserveSessionStateUseCase
import com.kshitijpatil.tazabazar.domain.SessionState
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
    private val observeSessionStateUseCase: ObserveSessionStateUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {
    private val _viewState = MutableStateFlow(ProfileViewState())
    val viewState: StateFlow<ProfileViewState>
        get() = _viewState.asStateFlow()

    init {
        viewModelScope.launch { observeSessionForLoggedInUser() }
    }

    private suspend fun observeSessionForLoggedInUser() {
        observeSessionStateUseCase()
            .map { if (it is SessionState.LoggedIn) it.user else null }
            .collect { setState { copy(loggedInUser = it) } }
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