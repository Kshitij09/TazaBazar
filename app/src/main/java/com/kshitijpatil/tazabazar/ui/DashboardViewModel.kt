package com.kshitijpatil.tazabazar.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.kshitijpatil.tazabazar.domain.*
import com.kshitijpatil.tazabazar.model.AuthConfiguration
import com.kshitijpatil.tazabazar.worker.RefreshTokenWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class DashboardViewModel(
    context: Context,
    private val observeCartItemCountUseCase: ObserveCartItemCountUseCase,
    private val isSessionExpiredUseCase: IsSessionExpiredUseCase,
    private val getAuthConfigurationUseCase: GetAuthConfigurationUseCase,
    private val observeAccessTokenChangedUseCase: ObserveAccessTokenChangedUseCase
) : ViewModel() {
    companion object {
        const val REFRESH_TOKEN_WORK = "refresh-token-work"
    }

    private val workManager = WorkManager.getInstance(context)
    private val networkConnectedConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private val authConfiguration = MutableStateFlow<AuthConfiguration?>(null)

    fun observeCartItemCount(): Flow<Int> = observeCartItemCountUseCase()

    init {
        with(viewModelScope) {
            launch { loadAuthConfiguration() }
            launch { checkAndScheduleRefreshTokenWork() }
            launch { observeAccessTokenChanges() }
        }
    }

    private suspend fun loadAuthConfiguration() {
        authConfiguration.value = getAuthConfigurationUseCase(Unit).data
    }

    private suspend fun observeAccessTokenChanges() {
        observeAccessTokenChangedUseCase().collect {
            val tokenExpiryMinutes = authConfiguration
                .first { it != null }!!.tokenExpiryMinutes.toLong()
            val delayedWorkRequest = getRefreshTokenWorkRequest(initialDelay = tokenExpiryMinutes)
            scheduleRefreshTokenWork(delayedWorkRequest)
        }
    }

    private fun getRefreshTokenWorkRequest(initialDelay: Long? = null): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<RefreshTokenWorker>()
            .setConstraints(networkConnectedConstraint)
            .apply { initialDelay?.let { setInitialDelay(it, TimeUnit.MINUTES) } }
            .build()
    }

    private suspend fun checkAndScheduleRefreshTokenWork() {
        val immediateWorkRequest = getRefreshTokenWorkRequest()
        val sessionExpiredResult = isSessionExpiredUseCase(Unit)
        if (sessionExpiredResult is Result.Success && sessionExpiredResult.data) {
            scheduleRefreshTokenWork(immediateWorkRequest)
        }
    }

    private fun scheduleRefreshTokenWork(request: OneTimeWorkRequest) {
        workManager.enqueueUniqueWork(
            REFRESH_TOKEN_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}