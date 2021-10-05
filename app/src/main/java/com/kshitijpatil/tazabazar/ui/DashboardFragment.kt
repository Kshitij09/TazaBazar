package com.kshitijpatil.tazabazar.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentDashboardBinding
import com.kshitijpatil.tazabazar.di.ViewModelFactory
import com.kshitijpatil.tazabazar.ui.cart.CartViewModel
import com.kshitijpatil.tazabazar.ui.cart.CartViewModelFactory
import com.kshitijpatil.tazabazar.ui.home.HomeViewModel
import com.kshitijpatil.tazabazar.ui.home.ProductFilterFragment

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding: FragmentDashboardBinding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels {
        ViewModelFactory(requireActivity(), requireContext().applicationContext, arguments)
    }
    private val cartViewModel: CartViewModel by viewModels {
        CartViewModelFactory(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navHostFragment?.let {
            binding.bottomNavigation.setupWithNavController(it.navController)
            binding.bottomNavigation.setOnItemReselectedListener { item ->
                val reselectedDestinationId = item.itemId
                if (reselectedDestinationId == R.id.navigation_home) {
                    notifyClearFilters()
                } else {
                    it.navController.popBackStack(reselectedDestinationId, false)
                }
            }
        }
        return binding.root
    }

    /**
     * [ProductFilterFragment] is using a Activity scoped ViewModel.
     * Thus, we should explicitly notify to clear its filters
     * when Home MenuItem from the BottomNavigation is reselected
     */
    private fun notifyClearFilters() {
        homeViewModel.clearAllFilters()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}