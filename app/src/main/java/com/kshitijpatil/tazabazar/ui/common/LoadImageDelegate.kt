package com.kshitijpatil.tazabazar.ui.common

import android.widget.ImageView
import coil.load
import coil.request.CachePolicy
import com.kshitijpatil.tazabazar.R

interface LoadImageDelegate {
    fun load(imageView: ImageView, imageUri: String)
}

class CoilProductLoadImageDelegate : LoadImageDelegate {
    override fun load(imageView: ImageView, imageUri: String) {
        imageView.load(imageUri) {
            placeholder(R.drawable.product_preview_placeholder)
            error(R.drawable.product_preview_placeholder)
            memoryCachePolicy(CachePolicy.ENABLED)
            memoryCacheKey(imageUri)
        }
    }
}
