package com.kshitijpatil.tazabazar.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kshitijpatil.tazabazar.R
import kotlinx.coroutines.delay

class SplashFragment : Fragment(R.layout.fragment_splash) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        lifecycleScope.launchWhenCreated {
            delay(SPLASH_DURATION_IN_MILLIS)
            findNavController().navigate(SplashFragmentDirections.actionFragmentSplashToFragmentMain())
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    companion object {
        private const val SPLASH_DURATION_IN_MILLIS = 3000L
    }
}