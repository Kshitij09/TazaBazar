package com.kshitijpatil.tazabazar.ui.home

import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.google.android.material.button.MaterialButton
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.ProductItemViewBinding
import com.kshitijpatil.tazabazar.model.Product

// TODO: Handle out of stock
// TODO: Handle discount details
// TODO: Handle Cart Action
class ProductViewHolder(
    private val binding: ProductItemViewBinding,
    var onItemActionCallback: OnItemActionCallback? = null
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: Product) {
        binding.tvName.text = item.name
        // TODO: Make it list menu
        val inventory = item.inventories[0]
        binding.tvQuantityLabel.text = inventory.quantityLabel
        binding.tvPrice.text = getPriceStringFor(inventory.price)
        updateFavoriteButtonColors(binding.btnFavorite, item.favorites.isNotEmpty())
        binding.btnFavorite.setOnClickListener {
            onItemActionCallback?.onFavoriteClicked(item)
        }
        loadImage(binding.ivImage, item.imageUri)
    }

    private fun getPriceStringFor(price: Float): String {
        return binding.root.context.getString(R.string.info_price_rupee_template, price)
    }

    private fun updateFavoriteButtonColors(btnFavorite: MaterialButton, favorite: Boolean) {
        if (favorite) {
            btnFavorite.setIconTintResource(R.color.tzb_green)
            btnFavorite.backgroundTintList =
                ContextCompat.getColorStateList(btnFavorite.context, R.color.tzb_green_light)
        } else {
            btnFavorite.setIconTintResource(R.color.tzb_gray_200)
            btnFavorite.backgroundTintList =
                ContextCompat.getColorStateList(btnFavorite.context, R.color.tzb_gray_100)
        }
    }

    private fun loadImage(imageView: ImageView, imageUri: String) {
        imageView.load(imageUri) {
            placeholder(R.drawable.product_preview_placeholder)
            error(R.drawable.product_preview_placeholder)
            memoryCachePolicy(CachePolicy.ENABLED)
            memoryCacheKey(imageUri)
        }
    }

    interface OnItemActionCallback {
        /**
         * Called whenever user clicks on the favorite button
         * @param product whose favorite was clicked
         */
        fun onFavoriteClicked(product: Product)
    }
}