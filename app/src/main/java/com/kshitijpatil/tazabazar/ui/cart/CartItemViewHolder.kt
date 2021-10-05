package com.kshitijpatil.tazabazar.ui.cart

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.CartCostViewBinding
import com.kshitijpatil.tazabazar.databinding.CartItemViewBinding
import com.kshitijpatil.tazabazar.model.CartCost
import com.kshitijpatil.tazabazar.model.CartItem
import com.kshitijpatil.tazabazar.ui.common.LoadImageDelegate

class CartItemViewHolder(
    private val binding: CartItemViewBinding,
    private val loadImageDelegate: LoadImageDelegate,
    var onItemActionCallback: OnItemActionCallback? = null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: CartItem) {
        val context = binding.root.context
        binding.tvName.text = item.name
        binding.tvPrice.text = context.getString(R.string.info_price_rupee_template, item.price)
        binding.txtQuantity.text = item.quantity.toString()
        binding.tvQuantityLabel.text = item.quantityLabel
        binding.btnIncrementQuantity.setOnClickListener {
            onItemActionCallback?.onQuantityIncrement(item)
        }
        binding.btnDecrementQuantity.setOnClickListener {
            onItemActionCallback?.onQuantityDecrement(item)
        }
        loadImageDelegate.load(binding.ivImage, item.imageUri)
    }

    interface OnItemActionCallback {
        fun onQuantityIncrement(item: CartItem)
        fun onQuantityDecrement(item: CartItem)
    }

    companion object {
        fun create(
            parent: ViewGroup,
            imageDelegate: LoadImageDelegate,
            itemActionCallback: OnItemActionCallback? = null
        ): CartItemViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = CartItemViewBinding.inflate(inflater, parent, false)
            return CartItemViewHolder(binding, imageDelegate, itemActionCallback)
        }
    }
}

class CartCostViewHolder(
    private val binding: CartCostViewBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(costing: CartCost) {
        val context = binding.root.context
        binding.tvSubtotal.text = getCostString(context, costing.subTotal)
        binding.tvDelivery.text = getCostString(context, costing.delivery)
        binding.tvDiscount.text = getCostString(context, costing.discount)
        binding.tvTotal.text = getCostString(context, costing.total)
    }

    private fun getCostString(context: Context, price: Float): String {
        return context.getString(R.string.info_price_rupee_template, price)
    }

    companion object {
        fun create(parent: ViewGroup): CartCostViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = CartCostViewBinding.inflate(inflater, parent, false)
            return CartCostViewHolder(binding)
        }
    }
}