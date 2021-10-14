package com.kshitijpatil.tazabazar.di

import android.content.Context
import com.kshitijpatil.tazabazar.domain.AddOrUpdateCartItemUseCase
import com.kshitijpatil.tazabazar.domain.LogoutUseCase
import com.kshitijpatil.tazabazar.domain.ObserveCartItemCountUseCase
import com.kshitijpatil.tazabazar.domain.ObserveLoggedInUserUseCase
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
}