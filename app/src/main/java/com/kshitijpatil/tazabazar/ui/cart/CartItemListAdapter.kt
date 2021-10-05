package com.kshitijpatil.tazabazar.ui.cart

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kshitijpatil.tazabazar.model.CartItem
import com.kshitijpatil.tazabazar.ui.common.LoadImageDelegate


class CartItemListAdapter(
    private val loadImageDelegate: LoadImageDelegate,
    var itemActionCallback: CartItemViewHolder.OnItemActionCallback? = null
) : ListAdapter<CartItem, CartItemViewHolder>(CartItemDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemViewHolder {
        return CartItemViewHolder.create(parent, loadImageDelegate, itemActionCallback)
    }

    override fun onBindViewHolder(holder: CartItemViewHolder, position: Int) {
        val cartItem = getItem(position)
        holder.bind(cartItem)
    }

    object CartItemDiffCallback : DiffUtil.ItemCallback<CartItem>() {
        override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem.inventoryId == newItem.inventoryId
        }

        override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
            return oldItem == newItem
        }
    }
}