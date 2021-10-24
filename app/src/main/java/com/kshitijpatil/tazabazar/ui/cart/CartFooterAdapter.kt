package com.kshitijpatil.tazabazar.ui.cart

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class CartFooterAdapter(
    var onFooterActionCallback: CartFooterViewHolder.OnFooterActionCallback? = null
) : RecyclerView.Adapter<CartFooterViewHolder>(), FooterViewDataDelegate {
    override var footerViewData: FooterViewData = FooterViewData()

    override fun onDataChanged() {
        notifyItemChanged(0)
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
        return CartFooterViewHolder.create(parent, onFooterActionCallback)
    }

    override fun onBindViewHolder(holder: CartFooterViewHolder, position: Int) {
        holder.bind(footerViewData)
    }

    override fun getItemCount(): Int = if (isVisible) 1 else 0
}