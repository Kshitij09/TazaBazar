package com.kshitijpatil.tazabazar.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.SimpleItemAnimator
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentCartBinding
import com.kshitijpatil.tazabazar.model.CartConfiguration
import com.kshitijpatil.tazabazar.model.CartCost
import com.kshitijpatil.tazabazar.model.CartItem
import com.kshitijpatil.tazabazar.ui.common.CoilProductLoadImageDelegate
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import com.kshitijpatil.tazabazar.widget.FadingSnackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class CartFragment : Fragment(), CartItemViewHolder.OnItemActionCallback {
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
            launch { viewModel.cartConfiguration.collect { cartConfiguration = it } }
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

    private suspend fun observeCartItems() {
        viewModel.cartItems.collect { cartItems ->
            cartItemListAdapter.submitList(cartItems)
            if (cartItems.isNotEmpty()) {
                cartCostFooterAdapter.isVisible = true
                val subTotal =
                    cartItems.fold(0f) { acc, item -> acc + (item.price * item.quantity) }
                cartCostFooterAdapter.costing = CartCost(subTotal = subTotal)
            } else {
                cartCostFooterAdapter.isVisible = false
            }
        }
    }

    override fun onQuantityIncrement(item: CartItem) {
        Timber.d("CartFragment: Increment event received")
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
        Timber.d("CartFragment: Decrement event received")
        viewModel.decrementQuantity(item)
    }
}