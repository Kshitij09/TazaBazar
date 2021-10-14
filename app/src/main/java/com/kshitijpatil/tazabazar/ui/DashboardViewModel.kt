package com.kshitijpatil.tazabazar.ui

import androidx.lifecycle.ViewModel
import com.kshitijpatil.tazabazar.domain.ObserveCartItemCountUseCase
import kotlinx.coroutines.flow.Flow

class DashboardViewModel(private val observeCartItemCountUseCase: ObserveCartItemCountUseCase) :
    ViewModel() {
    fun observeCartItemCount(): Flow<Int> = observeCartItemCountUseCase()
}