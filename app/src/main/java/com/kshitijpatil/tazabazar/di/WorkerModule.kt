package com.kshitijpatil.tazabazar.di

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.kshitijpatil.tazabazar.worker.RefreshTokenWorker

object WorkerModule {
    private fun createRefreshTokenWorker(
        appContext: Context,
        workerParams: WorkerParameters
    ): RefreshTokenWorker {
        val ioDispatcher = AppModule.provideAppCoroutineDispatchers().io
        val refreshTokenUseCase = DomainModule.provideRefreshTokenUseCase(ioDispatcher, appContext)
        return RefreshTokenWorker(appContext, workerParams, refreshTokenUseCase)
    }

    private inline fun <reified T : ListenableWorker?> workerFactoryOf(
        crossinline workerProducer: (Context, WorkerParameters) -> T
    ): WorkerFactory {
        return object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker? {
                return if (workerClassName == T::class.java.name)
                    return workerProducer(appContext, workerParameters)
                else null
            }

        }
    }

    fun provideRefreshTokenWorkerFactory() = workerFactoryOf(WorkerModule::createRefreshTokenWorker)
}