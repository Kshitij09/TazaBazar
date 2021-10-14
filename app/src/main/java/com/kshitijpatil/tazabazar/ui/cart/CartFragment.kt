package com.kshitijpatil.tazabazar.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.SimpleItemAnimator
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentCartBinding
import com.kshitijpatil.tazabazar.di.CartViewModelFactory
import com.kshitijpatil.tazabazar.model.CartConfiguration
import com.kshitijpatil.tazabazar.model.CartItem
import com.kshitijpatil.tazabazar.ui.common.CoilProductLoadImageDelegate
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import com.kshitijpatil.tazabazar.widget.FadingSnackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CartFragment : Fragment(), CartItemViewHolder.OnItemActionCallback {
    companion object {
        /** Result Key to notify cart items changed */
        const val CART_CHANGED_RESULT = "com.kshitijpatil.tazabazar.ui.cart.cart-changed-result"
    }

    private var _binding: FragmentCartBinding? = null
    private val binding: FragmentCartBinding get() = _binding!!
    private val loadImageDelegate = CoilProductLoadImageDelegate()
    private val cartItemListAdapter = CartItemListAdapter(loadImageDelegate)
    private val cartCostFooterAdapter = CartCostFooterAdapter()
    private val viewModel: CartViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { CartViewModelFactory(requireContext().applicationContext) }
    )
    private lateinit var cartConfiguration: CartConfiguration
    private lateinit var snackbar: FadingSnackbar

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
            adapter = ConcatAdapter(cartItemListAdapter, cartCostFooterAdapter)
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cartItemListAdapter.itemActionCallback = this
        launchAndRepeatWithViewLifecycle {
            launch {
                viewModel.cartConfiguration.collect {
                    cartConfiguration = it
                    cartCostFooterAdapter.updateDeliveryCharges(it.deliveryCharges)
                }
            }
            launch { observeCartItems() }
        }
        snackbar = view.findViewById(R.id.snackbar)
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
            cartItemListAdapter.submitList(cartItems)
            if (cartItems.isNotEmpty()) {
                cartCostFooterAdapter.isVisible = true
                val subTotal =
                    cartItems.fold(0f) { acc, item -> acc + (item.price * item.quantity) }
                cartCostFooterAdapter.updateSubTotal(subTotal)
            } else {
                cartCostFooterAdapter.isVisible = false
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
}