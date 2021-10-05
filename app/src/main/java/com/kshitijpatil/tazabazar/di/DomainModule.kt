package com.kshitijpatil.tazabazar.di

import android.content.Context
import com.kshitijpatil.tazabazar.domain.AddOrUpdateCartItemUseCase
import com.kshitijpatil.tazabazar.domain.ObserveCartItemCountUseCase
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
}