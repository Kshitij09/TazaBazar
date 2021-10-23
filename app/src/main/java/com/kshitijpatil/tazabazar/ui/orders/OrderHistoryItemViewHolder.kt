package com.kshitijpatil.tazabazar.ui.orders

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.OrderHistoryItemViewBinding
import com.kshitijpatil.tazabazar.model.Order
import org.threeten.bp.format.DateTimeFormatter

class OrderHistoryItemViewHolder(
    private val binding: OrderHistoryItemViewBinding,
    var onDetailsClickedListener: OnDetailsClickedListener? = null
) : RecyclerView.ViewHolder(binding.root) {
    private val customDateTimeFormatter =
        DateTimeFormatter.ofPattern(OrdersFragment.ORDER_DATE_FORMAT)

    fun bind(order: Order) {
        val context = binding.root.context
        binding.txtDate.text = customDateTimeFormatter.format(order.createdAt)
        val strippedOrderId = stripOrderIdToFirstSegment(order.orderId)
        binding.txtOrderId.text =
            context.getString(R.string.info_order_id_template, strippedOrderId)
        binding.txtTotalCost.text =
            context.getString(R.string.info_price_rupee_template, order.total)
        binding.btnDetails.setOnClickListener { onDetailsClickedListener?.onDetailsClicked(order.orderId) }
    }

    private fun stripOrderIdToFirstSegment(orderId: String): String {
        return orderId.takeWhile { it != '-' }
    }

    fun interface OnDetailsClickedListener {
        fun onDetailsClicked(orderId: String)
    }

    companion object {
        fun create(
            parent: ViewGroup,
            onDetailsClickedListener: OnDetailsClickedListener? = null
        ): OrderHistoryItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = OrderHistoryItemViewBinding.inflate(inflater, parent, false)
            return OrderHistoryItemViewHolder(binding, onDetailsClickedListener)
        }
    }
}