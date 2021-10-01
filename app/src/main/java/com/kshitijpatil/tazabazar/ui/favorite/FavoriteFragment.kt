package com.kshitijpatil.tazabazar.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import com.kshitijpatil.tazabazar.databinding.FragmentFavoriteBinding

class FavoriteFragment : Fragment() {
    private var _binding: FragmentFavoriteBinding? = null
    private val binding: FragmentFavoriteBinding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        binding.cardviewWeekly.setOnClickListener { navigateToFavoriteProducts(FavoriteType.WEEKLY) }
        binding.cardviewMonthly.setOnClickListener { navigateToFavoriteProducts(FavoriteType.MONTHLY) }
        return binding.root
    }

    private fun favoriteTypeToTitleRes(favoriteType: FavoriteType): Int {
        return when (favoriteType) {
            FavoriteType.WEEKLY -> R.string.title_my_weekly_list
            FavoriteType.MONTHLY -> R.string.title_my_monthly_list
        }
    }

    private fun navigateToFavoriteProducts(favoriteType: FavoriteType) {
        val titleRes = favoriteTypeToTitleRes(favoriteType)
        val direction =
            FavoriteFragmentDirections.actionNavigationFavoriteToFragmentFavoriteProducts(titleRes)
        requireActivity().findNavController(R.id.nav_host_fragment).navigate(direction)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}