package com.kshitijpatil.tazabazar.ui.orders

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kshitijpatil.tazabazar.model.Order

class OrderHistoryAdapter(
    var onDetailsClickedListener: OrderHistoryItemViewHolder.OnDetailsClickedListener? = null
) : ListAdapter<Order, OrderHistoryItemViewHolder>(OrderHistoryDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryItemViewHolder {
        return OrderHistoryItemViewHolder.create(parent, onDetailsClickedListener)
    }

    override fun onBindViewHolder(holder: OrderHistoryItemViewHolder, position: Int) {
        val orderItem = getItem(position)
        holder.bind(orderItem)
    }

    object OrderHistoryDiffCallback : DiffUtil.ItemCallback<Order>() {
        override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem.orderId == newItem.orderId
        }

        override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
            return oldItem == newItem
        }

    }

}