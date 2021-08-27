package com.kshitijpatil.tazabazar.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kshitijpatil.tazabazar.data.network.OkhttpClientManager
import com.kshitijpatil.tazabazar.ui.home.HomeViewModel

class ViewModelFactory(appContext: Context) : ViewModelProvider.Factory {
    private val okhttpClient = OkhttpClientManager.getInstance(appContext)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            val repository = RepositoryModule.provideProductRepository(okhttpClient)
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException()
    }
}