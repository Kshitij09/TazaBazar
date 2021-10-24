package com.kshitijpatil.tazabazar.util

import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.textfield.TextInputLayout
import com.kshitijpatil.tazabazar.R
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

/**
 * Will set a Spannable String on TextView in the following format
 *      Login <your prompt>
 *      for instance, 'Login to place your Order'
 * Where the word 'Login' will be clickable and invoke the [onLogin] callback
 * @param promptStringResId String Resource Id of your prompt
 * @param loginTextModifier Define your customization for Login's Text Appearance here
 * @param onLogin A callback method to be invoked when 'Login' gets clicked
 */
internal fun TextView.setLoginToPerformActionPrompt(
    promptStringResId: Int,
    loginTextModifier: TextPaint.() -> Unit = {},
    onLogin: () -> Unit
) {
    val loginTextSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            onLogin()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.apply(loginTextModifier)
        }
    }
    val loginText = context.getString(R.string.label_login)
    val promptText = context.getString(promptStringResId)
    val loginStart = 0
    val targetSpannableString = SpannableString("$loginText $promptText").apply {
        setSpan(loginTextSpan, loginStart, loginText.length, Spanned.SPAN_POINT_MARK)
    }
    text = targetSpannableString
    movementMethod = LinkMovementMethod.getInstance()
}