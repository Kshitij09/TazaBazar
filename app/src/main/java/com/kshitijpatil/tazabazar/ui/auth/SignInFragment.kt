package com.kshitijpatil.tazabazar.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.ui.MainActivityViewModel

class SignInFragment : Fragment(R.layout.fragment_signin_signup) {
    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivityViewModel.disableClearFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mainActivityViewModel.resetClearFocusToDefault()
    }

}