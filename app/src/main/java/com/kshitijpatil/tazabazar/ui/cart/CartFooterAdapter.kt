package com.kshitijpatil.tazabazar.ui.cart

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kshitijpatil.tazabazar.model.CartCost
import com.kshitijpatil.tazabazar.util.UiState

class CartFooterAdapter(
    var onPlaceOrderCallback: CartFooterViewHolder.OnPlaceOrderCallback? = null
) : RecyclerView.Adapter<CartFooterViewHolder>() {
    /**
     * Cost details to present in the Adapter
     * Changing this property will immediately notify the adapter to change
     * the item it's presenting
     */
    var costing: CartCost = CartCost()
        set(newCost) {
            if (field != newCost) {
                notifyItemChanged(0)
                field = newCost
            }
        }

    var placeOrderState: UiState<Unit> = UiState.Idle
        set(newState) {
            if (field != newState) {
                notifyItemChanged(0)
                field = newState
            }
        }

    fun updateSubTotal(subTotal: Float) {
        costing = costing.copy(subTotal = subTotal)
    }

    fun updateDeliveryCharges(deliveryCharges: Float) {
        costing = costing.copy(delivery = deliveryCharges)
    }

    fun updateDiscount(discount: Float) {
        costing = costing.copy(discount = discount)
    }

    var isVisible: Boolean = false
        set(value) {
            if (field && !value) {
                notifyItemRemoved(0)
            } else if (!field && value) {
                notifyItemInserted(0)
            }
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartFooterViewHolder {
        return CartFooterViewHolder.create(parent, onPlaceOrderCallback)
    }

    override fun onBindViewHolder(holder: CartFooterViewHolder, position: Int) {
        holder.bind(costing, placeOrderState)
    }

    override fun getItemCount(): Int = if (isVisible) 1 else 0
}