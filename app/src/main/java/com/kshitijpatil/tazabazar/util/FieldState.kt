package com.kshitijpatil.tazabazar.util

import android.widget.EditText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * State Holder for [EditText]
 */
class FieldState(val validator: (CharSequence?) -> Boolean) {
    /** Whether EditText was focused at least once */
    private var isFocusedDirty: Boolean = false
    private var text: CharSequence? = null
    val currentText: CharSequence? get() = text
    private val _isValid = MutableStateFlow(false)
    val isValid: StateFlow<Boolean>
        get() = _isValid.asStateFlow()
    val showErrors: Boolean
        get() = !isValid.value && isFocusedDirty

    fun onFocusChanged(focused: Boolean) {
        isFocusedDirty = isFocusedDirty || focused
    }

    fun onTextChanged(newText: CharSequence?) {
        _isValid.value = validator(newText)
        text = newText
    }
}
