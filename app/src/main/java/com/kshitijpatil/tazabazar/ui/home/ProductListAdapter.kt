package com.kshitijpatil.tazabazar.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.model.Product

class ProductListAdapter(
    private val layoutType: ProductLayoutType = ProductLayoutType.GRID,
    var onItemActionCallback: ProductViewHolder.OnItemActionCallback? = null
) : ListAdapter<Product, ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val layoutResource = when (layoutType) {
            ProductLayoutType.GRID -> R.layout.product_grid_item_view
            ProductLayoutType.ROW -> R.layout.product_row_item_view
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutResource, parent, false)
        return ProductViewHolder(view, onItemActionCallback)
    }

    override fun onViewDetachedFromWindow(holder: ProductViewHolder) {
        holder.onItemActionCallback = null
        super.onViewDetachedFromWindow(holder)
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

    enum class ProductLayoutType {
        GRID, ROW
    }
}