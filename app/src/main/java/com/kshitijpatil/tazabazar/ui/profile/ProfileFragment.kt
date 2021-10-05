package com.kshitijpatil.tazabazar.ui.profile

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.ui.DashboardFragmentDirections

class ProfileFragment : Fragment(R.layout.fragment_profile) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val btnLogin = view.findViewById<Button>(R.id.btn_login)
        btnLogin.setOnClickListener { openAuthNavigation() }
    }

    private fun openAuthNavigation() {
        val direction = DashboardFragmentDirections.actionFragmentDashboardToNavigationAuth()
        requireActivity().findNavController(R.id.main_activity_nav_host_fragment)
            .navigate(direction)
    }
}