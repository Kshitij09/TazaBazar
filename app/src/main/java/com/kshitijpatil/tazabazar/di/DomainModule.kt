package com.kshitijpatil.tazabazar.di

import android.content.Context
import com.kshitijpatil.tazabazar.domain.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope

object DomainModule {
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
        dispatcher: CoroutineDispatcher,
        applicationScope: CoroutineScope,
        context: Context
    ): ObserveSessionStateUseCase {
        val repo = RepositoryModule.provideAuthRepository(context)
        return ObserveSessionStateUseCase(applicationScope, dispatcher, repo)
    }
}