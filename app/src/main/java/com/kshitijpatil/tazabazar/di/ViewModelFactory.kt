package com.kshitijpatil.tazabazar.di

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.kshitijpatil.tazabazar.ui.home.HomeViewModel

class ViewModelFactory(
    owner: SavedStateRegistryOwner,
    appContext: Context,
    defaultArgs: Bundle?
) : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
    private val loggingInterceptor = AppModule.provideLoggingInterceptor()
    private val httpEventListener = AppModule.provideLoggingEventListener()
    private val okhttpClient = AppModule.provideOkHttpClient(
        appContext,
        loggingInterceptor,
        httpEventListener
    )
    private val productRepository =
        RepositoryModule.provideProductRepository(appContext, okhttpClient)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        key: String,
        modelClass: Class<T>,
        handle: SavedStateHandle
    ): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(handle, productRepository) as T
        }
        throw IllegalArgumentException()
    }
}