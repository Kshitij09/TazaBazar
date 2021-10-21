package com.kshitijpatil.tazabazar.di

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.kshitijpatil.tazabazar.domain.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

object DomainModule {
    @Volatile
    var observeSessionStateUseCase: ObserveSessionStateUseCase? = null
        @VisibleForTesting set

    fun provideAddToCartUseCase(
        context: Context,
        dispatcher: CoroutineDispatcher
    ): AddOrUpdateCartItemUseCase {
        val repo = RepositoryModule.provideCartItemRepository(context)
        return AddOrUpdateCartItemUseCase(repo, dispatcher)
    }

    fun provideObserveCartItemCountUseCase(
        context: Context,
        dispatcher: CoroutineDispatcher?
    ): ObserveCartItemCountUseCase {
        val repo = RepositoryModule.provideCartItemRepository(context)
        return ObserveCartItemCountUseCase(repo, dispatcher)
    }

    fun provideLogoutUseCase(
        dispatcher: CoroutineDispatcher,
        context: Context
    ): LogoutUseCase {
        val repo = RepositoryModule.provideAuthRepository(context)
        return LogoutUseCase(dispatcher, repo)
    }

    fun provideRefreshTokenUseCase(
        dispatcher: CoroutineDispatcher,
        context: Context
    ): RefreshTokenUseCase {
        val repo = RepositoryModule.provideAuthRepository(context)
        return RefreshTokenUseCase(dispatcher, repo)
    }

    fun provideObserveSessionStateUseCase(
        dispatchers: AppCoroutineDispatchers,
        applicationScope: CoroutineScope,
        context: Context
    ): ObserveSessionStateUseCase {
        return observeSessionStateUseCase
            ?: createObserveSessionStateUseCase(
                dispatchers,
                applicationScope,
                context
            )
    }

    fun createObserveSessionStateUseCase(
        dispatchers: AppCoroutineDispatchers,
        applicationScope: CoroutineScope,
        context: Context
    ): ObserveSessionStateUseCase {
        val repo = RepositoryModule.provideAuthRepository(context)
        val useCaseInstance = ObserveSessionStateUseCase(applicationScope, dispatchers, repo)
        observeSessionStateUseCase = useCaseInstance
        return useCaseInstance
    }
}