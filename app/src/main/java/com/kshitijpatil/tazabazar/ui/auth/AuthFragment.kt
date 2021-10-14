package com.kshitijpatil.tazabazar.ui.auth

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.di.AuthViewModelFactory

class AuthFragment : Fragment(R.layout.fragment_auth) {
    private val viewModel: AuthViewModel by navGraphViewModels(R.id.navigation_auth) {
        AuthViewModelFactory(this, requireContext().applicationContext, arguments)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.btn_login).setOnClickListener { navigateSignIn() }
        view.findViewById<Button>(R.id.btn_register).setOnClickListener { navigateSignUp() }
    }

    private fun navigateSignIn() {
        findNavController().navigate(R.id.fragment_signin)
    }

    private fun navigateSignUp() {
        findNavController().navigate(R.id.fragment_signup)
    }
}