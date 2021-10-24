package com.kshitijpatil.tazabazar.ui.cart

import android.content.Context
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.CartFooterViewBinding
import com.kshitijpatil.tazabazar.databinding.CartItemViewBinding
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

class CartFooterViewHolder(
    private val binding: CartFooterViewBinding,
    var onFooterActionCallback: OnFooterActionCallback? = null
) : RecyclerView.ViewHolder(binding.root) {
    private val loginTextColor =
        MaterialColors.getColor(binding.root, R.attr.colorPrimary, Color.BLACK)
    private val loginTextSpan = object : ClickableSpan() {
        override fun onClick(view: View) {
            onFooterActionCallback?.onLoginClicked()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = loginTextColor
            ds.textSize = 48f
        }
    }
    private val loginText = binding.root.context.getString(R.string.label_login)
    private val promptText = binding.root.context.getString(R.string.info_to_place_order_now)

    fun bind(footerViewData: FooterViewData) {
        val context = binding.root.context
        val costing = footerViewData.costing
        binding.tvSubtotal.text = getCostString(context, costing.subTotal)
        binding.tvDelivery.text = getCostString(context, costing.delivery)
        binding.tvDiscount.text = getCostString(context, costing.discount)
        binding.tvTotal.text = getCostString(context, costing.total)
        updateUiForLoggedIn(footerViewData.userLoggedIn)
        binding.btnPlaceOrder.isEnabled = footerViewData.placeOrderEnabled
        binding.btnPlaceOrder.setOnClickListener { onFooterActionCallback?.placeOrder() }

    }

    private fun updateUiForLoggedIn(userLoggedIn: Boolean) {
        binding.btnPlaceOrder.isVisible = userLoggedIn
        setLoginPromptText(userLoggedIn)
    }

    private fun setLoginPromptText(userLoggedIn: Boolean) {
        val loginStart = 0
        binding.txtPromptLogin.isVisible = !userLoggedIn
        if (!userLoggedIn) {
            val spannablePrompt = SpannableString("$loginText $promptText").apply {
                setSpan(loginTextSpan, loginStart, loginText.length, Spanned.SPAN_POINT_MARK)
            }
            binding.txtPromptLogin.apply {
                text = spannablePrompt
                movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    private fun getCostString(context: Context, price: Float): String {
        return context.getString(R.string.info_price_rupee_template, price)
    }

    companion object {
        fun create(
            parent: ViewGroup,
            onFooterActionCallback: OnFooterActionCallback? = null
        ): CartFooterViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = CartFooterViewBinding.inflate(inflater, parent, false)
            return CartFooterViewHolder(binding, onFooterActionCallback)
        }
    }

    interface OnFooterActionCallback {
        fun placeOrder()
        fun onLoginClicked()
    }
}