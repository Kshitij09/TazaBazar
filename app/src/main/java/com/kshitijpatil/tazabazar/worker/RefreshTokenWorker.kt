package com.kshitijpatil.tazabazar.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kshitijpatil.tazabazar.domain.RefreshTokenUseCase
import com.kshitijpatil.tazabazar.domain.succeeded
import timber.log.Timber

class RefreshTokenWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val refreshTokenUseCase: RefreshTokenUseCase
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val result = refreshTokenUseCase(Unit)
        return if (result.succeeded) {
            Timber.d("refresh-token-worker: Token Refreshed Successfully")
            Result.success()
        } else {
            Result.retry()
        }
    }
}
