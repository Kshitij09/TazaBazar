package com.kshitijpatil.tazabazar.di

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.kshitijpatil.tazabazar.domain.*
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
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

    fun providePlaceOrderUseCase(
        context: Context,
        externalScope: CoroutineScope,
        dispatchers: AppCoroutineDispatchers
    ): PlaceOrderUseCase {
        val repo = RepositoryModule.provideOrderRepository(context, externalScope, dispatchers)
        return PlaceOrderUseCase(dispatchers.io, repo)
    }

    fun provideGetUseOrdersUseCase(
        applicationScope: CoroutineScope,
        dispatchers: AppCoroutineDispatchers,
        context: Context
    ): GetUserOrdersUseCase {
        val repo = RepositoryModule.provideOrderRepository(context, applicationScope, dispatchers)
        return GetUserOrdersUseCase(dispatchers.io, repo)
    }


    fun provideSearchProductsUseCase(
        ioDispatcher: CoroutineDispatcher,
        context: Context
    ): SearchProductsUseCase {
        val repo = RepositoryModule.provideProductRepository(context)
        return SearchProductsUseCase(ioDispatcher, repo)
    }
}