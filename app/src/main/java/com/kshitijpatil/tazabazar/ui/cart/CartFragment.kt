package com.kshitijpatil.tazabazar.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.SimpleItemAnimator
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentCartBinding
import com.kshitijpatil.tazabazar.di.CartViewModelFactory
import com.kshitijpatil.tazabazar.model.CartConfiguration
import com.kshitijpatil.tazabazar.model.CartItem
import com.kshitijpatil.tazabazar.ui.common.CoilProductLoadImageDelegate
import com.kshitijpatil.tazabazar.util.UiState
import com.kshitijpatil.tazabazar.util.enableActionButton
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import com.kshitijpatil.tazabazar.util.tazabazarApplication
import com.kshitijpatil.tazabazar.widget.FadingSnackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class CartFragment : Fragment(), CartItemViewHolder.OnItemActionCallback,
    CartFooterViewHolder.OnPlaceOrderCallback {
    companion object {
        /** Result Key to notify cart items changed */
        const val CART_CHANGED_RESULT = "com.kshitijpatil.tazabazar.ui.cart.cart-changed-result"
    }

    private var _binding: FragmentCartBinding? = null
    private val binding: FragmentCartBinding get() = _binding!!
    private val loadImageDelegate = CoilProductLoadImageDelegate()
    private val cartItemListAdapter = CartItemListAdapter(loadImageDelegate)
    private val cartFooterAdapter = CartFooterAdapter()
    private val viewModel: CartViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { CartViewModelFactory(tazabazarApplication) }
    )
    private lateinit var cartConfiguration: CartConfiguration
    private lateinit var snackbar: FadingSnackbar
    private val activityNavController: NavController by lazy {
        requireActivity().findNavController(R.id.main_activity_nav_host_fragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listenForCartItemChanges()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        binding.rvCartItems.apply {
            adapter = ConcatAdapter(cartItemListAdapter, cartFooterAdapter)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cartItemListAdapter.itemActionCallback = this
        cartFooterAdapter.onPlaceOrderCallback = this
        launchAndRepeatWithViewLifecycle {
            launch {
                viewModel.cartConfiguration.collect {
                    cartConfiguration = it
                    cartFooterAdapter.updateDeliveryCharges(it.deliveryCharges)
                }
            }
            launch { observeCartItems() }
            launch { observePlaceOrderState() }
            launch { observeLoggedInUserState() }
        }
        snackbar = view.findViewById(R.id.snackbar)
    }

    private suspend fun observeLoggedInUserState() {
        viewModel.loggedInUser.collect {
            val placeOrderVisible = it != null
            cartFooterAdapter.setPlaceOrderVisible(placeOrderVisible)
        }
    }

    private suspend fun observePlaceOrderState() {
        viewModel.placeOrderUiState.collect {
            cartFooterAdapter.setPlaceOrderEnabled(it.enableActionButton)
            when (it) {
                UiState.Error -> showPlaceOrderFailed()
                is UiState.Success -> showPlaceOrderSucceeded()
                else -> {
                }
            }
        }
    }

    private fun showPlaceOrderSucceeded() {
        snackbar.show(R.string.info_order_successful)

        // awaiting reply on: https://stackoverflow.com/q/69681326/6738702
        /*val userFullName = viewModel.loggedInUser.value?.fullName
        if (userFullName != null) {
            activityNavController.navigate("app.tazabazar://orders/successful/$userFullName".toUri())
        } else {
            Timber.d("nav-to-success-fragment: LoggedInUser was not set, can't perform this action")
        }*/
    }

    private fun showPlaceOrderFailed() {
        snackbar.show(
            R.string.error_failed_to_place_order,
            actionId = R.string.action_retry,
            actionClick = { viewModel.placeOrder() }
        )
    }

    override fun onStop() {
        cartItemListAdapter.itemActionCallback = null
        super.onStop()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun listenForCartItemChanges() {
        setFragmentResultListener(CART_CHANGED_RESULT) { _, _ ->
            viewModel.reloadCartItems()
        }
    }

    private suspend fun observeCartItems() {
        viewModel.cartItems.collect { cartItems ->
            Timber.d("Cart Items Changed")
            cartItemListAdapter.submitList(cartItems)
            if (cartItems.isNotEmpty()) {
                cartFooterAdapter.isVisible = true
                val subTotal =
                    cartItems.fold(0f) { acc, item -> acc + (item.price * item.quantity) }
                cartFooterAdapter.updateSubTotal(subTotal)
            } else {
                cartFooterAdapter.isVisible = false
            }
        }
    }

    override fun onQuantityIncrement(item: CartItem) {
        if (item.quantity < cartConfiguration.maxQuantityPerItem)
            viewModel.incrementQuantity(item)
        else {
            val msg = requireContext().getString(
                R.string.error_max_quantity_reached,
                cartConfiguration.maxQuantityPerItem
            )
            snackbar.show(messageText = msg)
        }
    }

    override fun onQuantityDecrement(item: CartItem) {
        viewModel.decrementQuantity(item)
    }

    override fun placeOrder() {
        viewModel.placeOrder()
    }
}