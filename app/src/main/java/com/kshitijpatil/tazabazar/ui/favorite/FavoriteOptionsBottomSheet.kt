package com.kshitijpatil.tazabazar.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kshitijpatil.tazabazar.R

class FavoriteOptionsBottomSheet : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.bottomsheet_favorite_options, container, false)

    companion object {
        const val TAG = "FavoriteOptions"
    }
}