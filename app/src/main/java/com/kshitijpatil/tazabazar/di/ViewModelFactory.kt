package com.kshitijpatil.tazabazar.di

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import com.kshitijpatil.tazabazar.TazaBazarApplication
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import com.kshitijpatil.tazabazar.ui.DashboardViewModel
import com.kshitijpatil.tazabazar.ui.auth.AuthViewModel
import com.kshitijpatil.tazabazar.ui.cart.CartViewModel
import com.kshitijpatil.tazabazar.ui.favorite.FavoriteProductsViewModel
import com.kshitijpatil.tazabazar.ui.home.HomeViewModel
import com.kshitijpatil.tazabazar.ui.orders.OrdersViewModel
import com.kshitijpatil.tazabazar.ui.profile.ProfileViewModel
import java.lang.ref.WeakReference

class HomeViewModelFactory(
    owner: SavedStateRegistryOwner,
    appContext: Context,
    defaultArgs: Bundle?
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    private val productRepository =
        RepositoryModule.provideProductRepository(appContext)
    private val ioDispatcher = AppModule.provideIoDispatcher()
    private val addToCartUseCase = DomainModule.provideAddToCartUseCase(appContext, ioDispatcher)
    private val searchProductsUseCase =
        DomainModule.provideSearchProductsUseCase(ioDispatcher, appContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(
                savedStateHandle = handle,
                productRepository = productRepository,
                searchProductsUseCase = searchProductsUseCase,
                addOrUpdateCartItemUseCase = addToCartUseCase
            ) as T
        }
        throw IllegalArgumentException()
    }
}

class AuthViewModelFactory(
    owner: SavedStateRegistryOwner,
    appContext: Context,
    defaultArgs: Bundle?
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    private val repository = RepositoryModule.provideAuthRepository(appContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        return AuthViewModel(handle, repository) as T
    }
}

class CartViewModelFactory(application: TazaBazarApplication) : ViewModelProvider.Factory {
    private val cartRepository =
        RepositoryModule.provideCartItemRepository(application.applicationContext)
    val dispatchers = AppModule.provideAppCoroutineDispatchers()
    private val placeOrderUseCase = DomainModule.providePlaceOrderUseCase(
        application.applicationContext,
        application.coroutineScope,
        dispatchers
    )
    private val observeSessionStateUseCase = DomainModule.provideObserveSessionStateUseCase(
        dispatchers, application.coroutineScope, application.applicationContext
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CartViewModel::class.java)) {
            return CartViewModel(cartRepository, placeOrderUseCase, observeSessionStateUseCase) as T
        }
        throw IllegalArgumentException("ViewModel not found")
    }

}

class FavoriteProductsViewModelFactory(
    appContext: Context,
    private val favoriteType: FavoriteType
) : ViewModelProvider.Factory {
    private val productRepository =
        RepositoryModule.provideProductRepository(appContext)
    private val ioDispatcher = AppModule.provideIoDispatcher()
    private val addToCartUseCase = DomainModule.provideAddToCartUseCase(appContext, ioDispatcher)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteProductsViewModel::class.java)) {
            return FavoriteProductsViewModel(favoriteType, productRepository, addToCartUseCase) as T
        }
        throw IllegalArgumentException()
    }

}

class ProfileViewModelFactory(application: TazaBazarApplication) : ViewModelProvider.Factory {
    private val dispatchers = AppModule.provideAppCoroutineDispatchers()
    private val observeSessionStateUseCase = DomainModule.provideObserveSessionStateUseCase(
        dispatchers, application.coroutineScope, application.applicationContext
    )
    private val logoutUseCase = DomainModule.provideLogoutUseCase(
        dispatchers.io,
        application.applicationContext
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(observeSessionStateUseCase, logoutUseCase) as T
        }
        throw IllegalArgumentException("Invalid ViewModel")
    }

}

class DashboardViewModelFactory(application: TazaBazarApplication) : ViewModelProvider.Factory {
    private val observeCartItemCountUseCase = DomainModule.provideObserveCartItemCountUseCase(
        application.applicationContext,
        null // should be decided later
    )
    private val dispatchers = AppModule.provideAppCoroutineDispatchers()

    private val observeSessionStateUseCase = DomainModule.provideObserveSessionStateUseCase(
        dispatchers, application.coroutineScope, application.applicationContext
    )
    private val contextRef = WeakReference(application.applicationContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (contextRef.get() == null)
            throw IllegalStateException("No Context available to initialize the ViewModel")
        val context = contextRef.get()!!
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(
                context,
                observeCartItemCountUseCase,
                observeSessionStateUseCase
            ) as T
        }
        throw IllegalArgumentException("ViewModel not found")
    }

}

class OrdersViewModelFactory(application: TazaBazarApplication) : ViewModelProvider.Factory {
    private val dispatchers = AppModule.provideAppCoroutineDispatchers()
    private val getUserOrdersUseCase = DomainModule.provideGetUseOrdersUseCase(
        application.coroutineScope, dispatchers, application.applicationContext
    )
    private val observeSessionStateUseCase = DomainModule.provideObserveSessionStateUseCase(
        dispatchers, application.coroutineScope, application.applicationContext
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return OrdersViewModel(getUserOrdersUseCase, observeSessionStateUseCase) as T
    }

}