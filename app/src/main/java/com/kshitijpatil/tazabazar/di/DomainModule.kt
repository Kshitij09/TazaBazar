package com.kshitijpatil.tazabazar.di

import android.content.Context
import com.kshitijpatil.tazabazar.domain.AddToCartUseCase
import kotlinx.coroutines.CoroutineDispatcher

object DomainModule {
    fun provideAddToCartUseCase(
        context: Context,
        dispatcher: CoroutineDispatcher
    ): AddToCartUseCase {
        val repo = RepositoryModule.provideCartItemRepository(context)
        return AddToCartUseCase(repo, dispatcher)
    }
}