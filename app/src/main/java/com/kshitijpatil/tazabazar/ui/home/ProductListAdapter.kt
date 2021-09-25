package com.kshitijpatil.tazabazar.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.request.CachePolicy
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.ProductItemViewBinding
import com.kshitijpatil.tazabazar.model.Product
import java.lang.ref.WeakReference

class ProductListAdapter :
    ListAdapter<Product, ProductListAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ProductItemViewBinding.inflate(layoutInflater, parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.sku == newItem.sku
        }

        override fun areContentsTheSame(
            oldItem: Product,
            newItem: Product
        ): Boolean {
            return oldItem == newItem
        }

    }

    // TODO: Handle out of stock
    // TODO: Handle discount details
    // TODO: Handle Cart Action
    class ProductViewHolder(
        private val binding: ProductItemViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val contextRef = WeakReference(binding.root.context)

        fun bind(item: Product) {
            binding.tvName.text = item.name
            // TODO: Make it list menu
            val inventory = item.inventories[0]
            binding.tvQuantityLabel.text = inventory.quantityLabel
            contextRef.get()?.let {
                binding.tvPrice.text =
                    it.getString(R.string.info_price_rupee_template, inventory.price)
            }
            loadImage(binding.ivImage, item.imageUri)
        }

        private fun loadImage(imageView: ImageView, imageUri: String) {
            imageView.load(imageUri) {
                crossfade(true)
                placeholder(R.drawable.product_preview_placeholder)
                error(R.drawable.product_preview_placeholder)
                memoryCachePolicy(CachePolicy.ENABLED)
                memoryCacheKey(imageUri)
            }
        }
    }
}