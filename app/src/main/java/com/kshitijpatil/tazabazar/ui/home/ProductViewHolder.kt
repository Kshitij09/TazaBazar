package com.kshitijpatil.tazabazar.ui.home

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.google.android.material.button.MaterialButton
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.model.Inventory
import com.kshitijpatil.tazabazar.model.Product
import java.lang.ref.WeakReference

// TODO: Handle out of stock
// TODO: Handle discount details
// TODO: Handle Cart Action
class ProductViewHolder(
    view: View,
    var onItemActionCallback: OnItemActionCallback? = null
) : RecyclerView.ViewHolder(view) {
    private val viewRef = WeakReference(view)
    private val tvName: TextView = view.findViewById(R.id.tv_name)
    private val tvPrice: TextView = view.findViewById(R.id.tv_price)
    private val tvQuantityLabel: TextView = view.findViewById(R.id.tv_quantity_label)
    private val btnFavorite: MaterialButton = view.findViewById(R.id.btn_favorite)
    private val btnCart: MaterialButton = view.findViewById(R.id.btn_cart)
    private val ivImage: ImageView = view.findViewById(R.id.iv_image)
    fun bind(item: Product) {
        tvName.text = item.name
        // TODO: Make it list menu
        val inventory = item.inventories[0]
        tvQuantityLabel.text = inventory.quantityLabel
        viewRef.get()?.context?.let {
            tvPrice.text = it.getString(R.string.info_price_rupee_template, inventory.price)
        }
        updateFavoriteButtonColors(item.favorites.isNotEmpty())
        btnFavorite.setOnClickListener {
            // Update favorite colors in advance
            // since item will be added in weekly list
            // by default
            if (item.favorites.isEmpty())
                updateFavoriteButtonColors(true)
            onItemActionCallback?.onFavoriteClicked(item)
        }
        btnCart.isEnabled = item.inventories.isNotEmpty()
        btnCart.setOnClickListener {
            // we can assure that defaultInventory is not null here
            // since we would've disabled the button otherwise
            onItemActionCallback?.onCartClicked(item.name, item.defaultInventory!!)
        }
        loadImage(item.imageUri)
    }

    private fun updateFavoriteButtonColors(favorite: Boolean) {
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

    private fun loadImage(imageUri: String) {
        ivImage.load(imageUri) {
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

        /**
         * Called whenever user clicks the cart button
         * @param productName Name of the Product added to cart
         * @param inventory Currently selected inventory
         */
        fun onCartClicked(productName: String, inventory: Inventory)
    }
}