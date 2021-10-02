package com.kshitijpatil.tazabazar.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import com.kshitijpatil.tazabazar.databinding.BottomsheetFavoriteOptionsBinding
import com.kshitijpatil.tazabazar.ui.home.HomeFragment

class FavoriteOptionsBottomSheet : BottomSheetDialogFragment() {
    private var _binding: BottomsheetFavoriteOptionsBinding? = null
    private val binding: BottomsheetFavoriteOptionsBinding get() = _binding!!
    private val args: FavoriteOptionsBottomSheetArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = BottomsheetFavoriteOptionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initFavoriteCheckboxes()
        binding.btnDone.setOnClickListener {
            val bundle = bundleOf(
                HomeFragment.FAVORITE_OPTIONS_BUNDLE_KEY to getFavoritePreferences(),
                HomeFragment.FAVORITE_SKU_BUNDLE_KEY to args.productSku
            )
            setFragmentResult(HomeFragment.FAVORITE_OPTIONS_RESULT_KEY, bundle)
            dismiss()
        }
        binding.layoutWeeklyChoice.setOnClickListener {
            binding.cbWeekly.isChecked = !binding.cbWeekly.isChecked
        }
        binding.layoutMonthlyChoice.setOnClickListener {
            binding.cbMonthly.isChecked = !binding.cbMonthly.isChecked
        }
    }

    private fun initFavoriteCheckboxes() {
        val favTypes = enumValues<FavoriteType>()
        val favorites = args.currentFavorites?.map { favTypes[it] }
        favorites?.let {
            binding.cbWeekly.isChecked = favorites.contains(FavoriteType.WEEKLY)
            binding.cbMonthly.isChecked = favorites.contains(FavoriteType.MONTHLY)
        }
    }

    /**
     * Return IntArray based on preference checkboxes' states
     */
    private fun getFavoritePreferences(): IntArray {
        val checkedTypes = mutableListOf<Int>()
        if (binding.cbWeekly.isChecked)
            checkedTypes.add(FavoriteType.WEEKLY.ordinal)
        if (binding.cbMonthly.isChecked)
            checkedTypes.add(FavoriteType.MONTHLY.ordinal)
        return checkedTypes.toIntArray()
    }
}