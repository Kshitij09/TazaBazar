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
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentDashboardBinding
import com.kshitijpatil.tazabazar.di.DashboardViewModelFactory
import com.kshitijpatil.tazabazar.di.HomeViewModelFactory
import com.kshitijpatil.tazabazar.ui.home.HomeViewModel
import com.kshitijpatil.tazabazar.ui.home.ProductFilterFragment
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding: FragmentDashboardBinding get() = _binding!!
    private val homeViewModel: HomeViewModel by activityViewModels {
        HomeViewModelFactory(requireActivity(), requireContext().applicationContext, arguments)
    }
    private val dashboardViewModel: DashboardViewModel by viewModels {
        DashboardViewModelFactory(requireContext())
    }
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var cartItemCountBadge: BadgeDrawable

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        bottomNavigation = binding.bottomNavigation
        cartItemCountBadge = bottomNavigation.getOrCreateBadge(R.id.navigation_cart)
        cartItemCountBadge.isVisible = false
        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navHostFragment?.let {
            bottomNavigation.setupWithNavController(it.navController)
            bottomNavigation.setOnItemReselectedListener { item ->
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchAndRepeatWithViewLifecycle {
            launch { updateCartItemCount() }
        }
    }

    private suspend fun updateCartItemCount() {
        dashboardViewModel.observeCartItemCount().collect { count ->
            Timber.d("Cart Item Count Updated: $count")
            if (count != 0) {
                cartItemCountBadge.isVisible = true
                cartItemCountBadge.number = count
            } else {
                cartItemCountBadge.isVisible = false
                cartItemCountBadge.clearNumber()
            }
        }
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