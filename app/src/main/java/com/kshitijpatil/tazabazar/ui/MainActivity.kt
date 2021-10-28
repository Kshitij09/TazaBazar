package com.kshitijpatil.tazabazar.ui

import android.content.Context
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.kshitijpatil.tazabazar.R

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (viewModel.clearFocus.value) {
            clearFocusOnOutSideClick();
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun clearFocusOnOutSideClick() {
        currentFocus?.apply {
            if (this is EditText)
                clearFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}