package com.kshitijpatil.tazabazar.ui.orders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.BottomsheetOrderDetailsBinding
import com.kshitijpatil.tazabazar.di.OrdersViewModelFactory
import com.kshitijpatil.tazabazar.model.OrderStatus
import com.kshitijpatil.tazabazar.util.tazabazarApplication
import org.threeten.bp.format.DateTimeFormatter

class OrderDetailsBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomsheetOrderDetailsBinding? = null
    private val binding: BottomsheetOrderDetailsBinding get() = _binding!!
    private val ordersViewModel: OrdersViewModel by activityViewModels {
        OrdersViewModelFactory(tazabazarApplication)
    }
    private val orderDateTimeFormatter =
        DateTimeFormatter.ofPattern(OrdersFragment.ORDER_DATE_FORMAT)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomsheetOrderDetailsBinding.inflate(inflater, container, false)
        val selectedOrder = ordersViewModel.selectedOrder
        binding.txtOrderDateTime.text = orderDateTimeFormatter.format(selectedOrder.createdAt)
        binding.txtOrderId.text = selectedOrder.orderId
        binding.txtStatus.text = getString(selectedOrder.status.displayTextResId)
        binding.txtTotal.text = getString(R.string.info_price_rupee_template, selectedOrder.total)
        return binding.root
    }

    private val OrderStatus.displayTextResId: Int
        get() {
            return when (this) {
                OrderStatus.ACCEPTED -> R.string.label_order_status_accepted
                OrderStatus.PENDING -> R.string.label_order_status_pending
                OrderStatus.DISPATCHED -> R.string.label_order_status_dispatched
                OrderStatus.DELIVERED -> R.string.label_order_status_delivered
                OrderStatus.CANCELLED -> R.string.label_order_status_canceled
            }
        }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}