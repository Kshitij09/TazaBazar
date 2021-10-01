package com.kshitijpatil.tazabazar.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kshitijpatil.tazabazar.R
import kotlinx.coroutines.delay

class SplashFragment : Fragment(R.layout.fragment_splash) {
    companion object {
        private const val SPLASH_DURATION_IN_MILLIS = 3000L
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val direction = SplashFragmentDirections.actionFragmentSplashToFragmentDashboard()
        lifecycleScope.launchWhenCreated {
            delay(SPLASH_DURATION_IN_MILLIS)
            findNavController().navigate(direction)
        }
    }
}