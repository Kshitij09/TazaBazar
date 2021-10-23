package com.kshitijpatil.tazabazar.ui.orders

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentOrdersBinding
import com.kshitijpatil.tazabazar.di.OrdersViewModelFactory
import com.kshitijpatil.tazabazar.util.UiState
import com.kshitijpatil.tazabazar.util.getLoginToPerformActionSpannableString
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import com.kshitijpatil.tazabazar.util.tazabazarApplication
import com.kshitijpatil.tazabazar.widget.FadingSnackbar
import kotlinx.coroutines.flow.collect
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
        binding.txtPromptLogin.apply {
            text = requireContext().getLoginToPerformActionSpannableString(
                promptStringResId = R.string.info_to_view_order_history,
                onLoginClicked = { onLoginClicked() }
            )
            movementMethod = LinkMovementMethod.getInstance()
        }
        snackbar = binding.snackbar
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        launchAndRepeatWithViewLifecycle {
            launch { updateVisibilityForLoggedInState() }
            launch { updateVisibilityForUserOrdersState() }
        }
        binding.swipeRefreshOrdersHistory.setOnRefreshListener {
            viewModel.refreshOrdersList().invokeOnCompletion {
                binding.swipeRefreshOrdersHistory.isRefreshing = false
            }
        }
    }

    private suspend fun updateVisibilityForUserOrdersState() {
        viewModel.userOrdersState.collect { userOrdersState ->
            binding.swipeRefreshOrdersHistory.isRefreshing = userOrdersState is UiState.Loading
            val ordersListVisibleState =
                userOrdersState is UiState.Success || userOrdersState is UiState.Error
            val userLoggedIn = viewModel.isLoggedIn.value
            if (userOrdersState is UiState.Success) {
                orderHistoryAdapter.submitList(userOrdersState.value)
                binding.rvOrdersHistory.isVisible = ordersListVisibleState
                        && userLoggedIn
                        && userOrdersState.value.isNotEmpty()
                binding.txtOrderHistoryEmpty.isVisible = userOrdersState.value.isEmpty()
            }
        }
    }

    private suspend fun updateVisibilityForLoggedInState() {
        viewModel.isLoggedIn.collect { loggedIn ->
            binding.txtPromptLogin.isVisible = !loggedIn
            binding.rvOrdersHistory.isVisible = loggedIn
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