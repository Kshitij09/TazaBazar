package com.kshitijpatil.tazabazar.ui.cart

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kshitijpatil.tazabazar.model.CartCost

class CartCostFooterAdapter : RecyclerView.Adapter<CartCostViewHolder>() {
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
    var isVisible: Boolean = false
        set(value) {
            if (field && !value) {
                notifyItemRemoved(0)
            } else if (!field && value) {
                notifyItemInserted(0)
            }
            field = value
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartCostViewHolder {
        return CartCostViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: CartCostViewHolder, position: Int) {
        holder.bind(costing)
    }

    override fun getItemCount(): Int = if (isVisible) 1 else 0
}