package com.kshitijpatil.tazabazar.util

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputLayout
import com.kshitijpatil.tazabazar.TazaBazarApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

/**
 * Launches a new coroutine and repeats `block` every time the Fragment's viewLifecycleOwner
 * is in and out of `minActiveState` lifecycle state.
 */
inline fun Fragment.launchAndRepeatWithViewLifecycle(
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    viewLifecycleOwner.lifecycleScope.launch {
        viewLifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            block()
        }
    }
}

/**
 * Launches a set of Common Jobs around TextInputLayout
 * in a given scope.
 *
 * Launched Jobs For Text Changes:
 * 1) Update [fieldState]
 * 2) Set Error on the input layout if [getErrorFor] was provided
 * 3) Invoke [onTextChanged] Callback
 *
 * Also, Update [fieldState] with Focus Changes
 */
inline fun CoroutineScope.launchTextInputLayoutObservers(
    textInputLayout: TextInputLayout,
    fieldState: FieldState,
    crossinline onTextChanged: suspend (String?) -> Unit,
    textChangeDebounceInMillis: Long = 500,
    noinline getErrorFor: ((String?) -> String)? = null
) {
    textInputLayout.editText?.let { textField ->
        val textChanges = textField.textChanges()
            .debounce(textChangeDebounceInMillis)
            .shareIn(this, SharingStarted.WhileSubscribed())
        with(textChanges) {
            launch { collect { fieldState.onTextChanged(it) } }

            if (getErrorFor != null) {
                launch {
                    collect {
                        textInputLayout.error = if (fieldState.showErrors)
                            getErrorFor(it.toString())
                        else null
                    }
                }
            }
            launch { collect { onTextChanged(it.toString()) } }
        }
        launch {
            textField.setOnFocusChangeListener { _, focused ->
                fieldState.onFocusChanged(focused)
            }
        }
    }
}

val Fragment.tazabazarApplication get() = requireActivity().application as TazaBazarApplication