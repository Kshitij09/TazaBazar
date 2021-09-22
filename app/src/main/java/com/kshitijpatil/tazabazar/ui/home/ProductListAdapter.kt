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
import com.kshitijpatil.tazabazar.api.dto.ProductResponse
import com.kshitijpatil.tazabazar.databinding.ProductItemViewBinding
import timber.log.Timber
import java.lang.ref.WeakReference

class ProductListAdapter :
    ListAdapter<ProductResponse, ProductListAdapter.ProductViewHolder>(ProductResponseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ProductItemViewBinding.inflate(layoutInflater, parent, false)
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCurrentListChanged(
        previousList: MutableList<ProductResponse>,
        currentList: MutableList<ProductResponse>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        Timber.i("Product List changed")
    }

    class ProductResponseDiffCallback : DiffUtil.ItemCallback<ProductResponse>() {
        override fun areItemsTheSame(oldItem: ProductResponse, newItem: ProductResponse): Boolean {
            return oldItem.sku == newItem.sku
        }

        override fun areContentsTheSame(
            oldItem: ProductResponse,
            newItem: ProductResponse
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

        fun bind(item: ProductResponse) {
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