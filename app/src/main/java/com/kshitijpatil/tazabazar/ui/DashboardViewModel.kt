package com.kshitijpatil.tazabazar.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kshitijpatil.tazabazar.di.DomainModule
import com.kshitijpatil.tazabazar.domain.ObserveCartItemCountUseCase
import kotlinx.coroutines.flow.Flow

class DashboardViewModel(private val observeCartItemCountUseCase: ObserveCartItemCountUseCase) :
    ViewModel() {
    fun observeCartItemCount(): Flow<Int> = observeCartItemCountUseCase()
}

class DashboardViewModelFactory(appContext: Context) : ViewModelProvider.Factory {
    private val observeCartItemCountUseCase = DomainModule.provideObserveCartItemCountUseCase(
        appContext,
        null // should be decided later
    )

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            return DashboardViewModel(observeCartItemCountUseCase) as T
        }
        throw IllegalArgumentException("ViewModel not found")
    }

}