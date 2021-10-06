package com.kshitijpatil.tazabazar.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainActivityViewModel : ViewModel() {
    private val defaultClearFocus = true
    private val _clearFocus = MutableStateFlow(defaultClearFocus)
    val clearFocus: StateFlow<Boolean>
        get() = _clearFocus.asStateFlow()

    fun enableClearFocus() {
        _clearFocus.value = true
    }

    fun disableClearFocus() {
        _clearFocus.value = false
    }

    fun resetClearFocusToDefault() {
        _clearFocus.value = defaultClearFocus
    }
}