package com.kshitijpatil.tazabazar.di

import android.content.Context
import com.kshitijpatil.tazabazar.domain.*
import kotlinx.coroutines.CoroutineDispatcher

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

    fun provideObserveLoggedInUserUseCase(
        context: Context,
        dispatcher: CoroutineDispatcher
    ): ObserveLoggedInUserUseCase {
        val preferenceStorage = PreferenceStorageModule.providePreferenceStorage(context)
        val serializer = RepositoryModule.provideLoggedInUserSerializer()
        return ObserveLoggedInUserUseCase(dispatcher, preferenceStorage, serializer)
    }

    fun provideLogoutUseCase(
        dispatcher: CoroutineDispatcher,
        context: Context
    ): LogoutUseCase {
        val repo = RepositoryModule.provideAuthRepository(context)
        return LogoutUseCase(dispatcher, repo)
    }

    fun provideIsSessionExpiredUseCase(
        dispatcher: CoroutineDispatcher,
        context: Context
    ): IsSessionExpiredUseCase {
        val repo = RepositoryModule.provideAuthRepository(context)
        val serializer = RepositoryModule.provideLocalDateTimeSerializer()
        return IsSessionExpiredUseCase(dispatcher, repo, serializer)
    }

    fun provideRefreshTokenUseCase(
        dispatcher: CoroutineDispatcher,
        context: Context
    ): RefreshTokenUseCase {
        val repo = RepositoryModule.provideAuthRepository(context)
        return RefreshTokenUseCase(dispatcher, repo)
    }

    fun provideGetAuthConfigurationUseCase(
        dispatcher: CoroutineDispatcher,
        context: Context
    ): GetAuthConfigurationUseCase {
        val repo = RepositoryModule.provideAuthRepository(context)
        return GetAuthConfigurationUseCase(dispatcher, repo)
    }

    fun provideObserveAccessTokenChangedUseCase(
        dispatcher: CoroutineDispatcher,
        context: Context
    ): ObserveAccessTokenChangedUseCase {
        val preferenceStorage = PreferenceStorageModule.providePreferenceStorage(context)
        return ObserveAccessTokenChangedUseCase(dispatcher, preferenceStorage)
    }
}