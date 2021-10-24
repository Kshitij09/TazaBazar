package com.kshitijpatil.tazabazar.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentOrdersBinding
import com.kshitijpatil.tazabazar.di.OrdersViewModelFactory
import com.kshitijpatil.tazabazar.util.UiState
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import com.kshitijpatil.tazabazar.util.setLoginToPerformActionPrompt
import com.kshitijpatil.tazabazar.util.tazabazarApplication
import com.kshitijpatil.tazabazar.widget.FadingSnackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class OrdersFragment : Fragment(), OrderHistoryItemViewHolder.OnDetailsClickedListener {
    private var _binding: FragmentOrdersBinding? = null
    private val binding: FragmentOrdersBinding get() = _binding!!
    private val orderHistoryAdapter = OrderHistoryAdapter()
    private var snackbar: FadingSnackbar? = null
    private val viewModel: OrdersViewModel by activityViewModels {
        OrdersViewModelFactory(tazabazarApplication)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        binding.rvOrdersHistory.adapter = orderHistoryAdapter
        binding.txtPromptLogin.setLoginToPerformActionPrompt(
            promptStringResId = R.string.info_to_view_order_history,
            loginTextModifier = { textSize = 48f },
            onLogin = { onLoginClicked() }
        )
        snackbar = binding.snackbar
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchAndRepeatWithViewLifecycle {
            launch { updateVisibilityForStateChanges() }
        }
        binding.swipeRefreshOrdersHistory.setOnRefreshListener {
            viewModel.refreshOrdersList()
        }
    }

    private suspend fun updateVisibilityForStateChanges() {
        combine(viewModel.isLoggedIn, viewModel.userOrdersState, ::Pair)
            .collect { (loggedIn, ordersState) ->
                binding.swipeRefreshOrdersHistory.isRefreshing =
                    loggedIn && ordersState is UiState.Loading
                val ordersListVisibleState = ordersState is UiState.Success
                if (ordersState is UiState.Success) {
                    orderHistoryAdapter.submitList(ordersState.value)
                    binding.txtOrderHistoryEmpty.isVisible = ordersState.value.isEmpty()
                    binding.rvOrdersHistory.isVisible = ordersListVisibleState
                            && loggedIn
                            && ordersState.value.isNotEmpty()
                }
                binding.txtPromptLogin.isVisible = !loggedIn
                binding.swipeRefreshOrdersHistory.isVisible = loggedIn
            }
    }

    override fun onStart() {
        super.onStart()
        orderHistoryAdapter.onDetailsClickedListener = this
    }

    override fun onPause() {
        orderHistoryAdapter.onDetailsClickedListener = null
        super.onPause()
    }

    override fun onDetailsClicked(orderId: String) {
        val orderDetailsFound = viewModel.updateSelectedOrderById(orderId)
        if (orderDetailsFound) showBottomSheetWithOrderDetails()
        else snackbar?.show(R.string.error_order_details_not_found)
    }

    private fun showBottomSheetWithOrderDetails() {
        findNavController().navigate(R.id.action_navigation_orders_to_bottom_sheet_order_details)
    }

    private fun onLoginClicked() {
        requireActivity().findNavController(R.id.main_activity_nav_host_fragment)
            .navigate(R.id.action_fragment_dashboard_to_navigation_auth)
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val ORDER_DATE_FORMAT = "EEE, dd LLL, yyyy"
    }
}