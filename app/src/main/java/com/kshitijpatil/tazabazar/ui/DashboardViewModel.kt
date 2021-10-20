package com.kshitijpatil.tazabazar.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.kshitijpatil.tazabazar.domain.ObserveCartItemCountUseCase
import com.kshitijpatil.tazabazar.domain.ObserveSessionStateUseCase
import com.kshitijpatil.tazabazar.domain.SessionState
import com.kshitijpatil.tazabazar.worker.RefreshTokenWorker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

class DashboardViewModel(
    context: Context,
    private val observeCartItemCountUseCase: ObserveCartItemCountUseCase,
    private val observeSessionStateUseCase: ObserveSessionStateUseCase
) : ViewModel() {
    companion object {
        const val REFRESH_TOKEN_WORK = "refresh-token-work"
    }

    private val workManager = WorkManager.getInstance(context)
    private val networkConnectedConstraint = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private val refreshTokenWorkRequest = OneTimeWorkRequestBuilder<RefreshTokenWorker>()
        .setConstraints(networkConnectedConstraint)
        .build()

    fun observeCartItemCount(): Flow<Int> = observeCartItemCountUseCase()

    init {
        with(viewModelScope) {
            launch { observeSessionExpired() }
        }
    }

    private suspend fun observeSessionExpired() {
        observeSessionStateUseCase()
            .filter { it is SessionState.SessionExpired }
            .collect { scheduleRefreshTokenWork() }
    }


    private fun scheduleRefreshTokenWork() {
        workManager.enqueueUniqueWork(
            REFRESH_TOKEN_WORK,
            ExistingWorkPolicy.REPLACE,
            refreshTokenWorkRequest
        )
    }
}