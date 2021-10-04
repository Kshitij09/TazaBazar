package com.kshitijpatil.tazabazar.ui.cart

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.model.CartCost
import com.kshitijpatil.tazabazar.model.CartItem
import com.kshitijpatil.tazabazar.ui.common.CoilProductLoadImageDelegate
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.flow.collect

class CartFragment : Fragment(R.layout.fragment_cart), CartItemViewHolder.OnItemActionCallback {
    private val loadImageDelegate = CoilProductLoadImageDelegate()
    private val cartItemListAdapter = CartItemListAdapter(loadImageDelegate)
    private val cartCostFooterAdapter = CartCostFooterAdapter()
    private val viewModel: CartViewModel by viewModels {
        CartViewModelFactory(requireContext().applicationContext)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.reloadCartItems()
        cartItemListAdapter.itemActionCallback = this
        val rvCartItems = view.findViewById<RecyclerView>(R.id.rv_cart_items)
        rvCartItems.adapter = ConcatAdapter(cartItemListAdapter, cartCostFooterAdapter)

        launchAndRepeatWithViewLifecycle {
            viewModel.cartItems.collect { cartItems ->
                cartItemListAdapter.submitList(cartItems)
                if (cartItems.isNotEmpty()) {
                    cartCostFooterAdapter.isVisible = true
                    val subTotal = cartItems.fold(0f) { acc, item -> acc + item.price }
                    cartCostFooterAdapter.costing = CartCost(subTotal = subTotal)
                } else {
                    cartCostFooterAdapter.isVisible = false
                }
            }
        }
    }

    override fun onDestroyView() {
        cartItemListAdapter.itemActionCallback = null
        super.onDestroyView()
    }

    override fun onQuantityIncrement(item: CartItem) {
        TODO("Not yet implemented")
    }

    override fun onQuantityDecrement(item: CartItem) {
        TODO("Not yet implemented")
    }
}