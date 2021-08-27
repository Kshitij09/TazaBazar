package com.kshitijpatil.tazabazar.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentProductFilterBinding
import kotlinx.coroutines.flow.collect

class ProductFilterFragment : Fragment() {
    private var _binding: FragmentProductFilterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { HomeViewModelFactory() }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductFilterBinding.inflate(inflater, container, false)
        observeCategoryFilters()
        viewModel.updateProductCategories()
        return binding.root
    }

    private fun observeCategoryFilters() {
        val context = binding.root.context
        lifecycleScope.launchWhenCreated {
            viewModel.productCategories.collect { categoryMap ->
                if (categoryMap.isNotEmpty()) {
                    binding.cgProductCategories.removeAllViews()
                    binding.progressCategories.isVisible = true

                    val allChip = createActionChipFrom(context)
                    allChip.text = context.getString(R.string.label_all)
                    allChip.setOnClickListener {
                        binding.cgProductCategories.clearCheck()
                        viewModel.clearAllCategoryFilters()
                    }
                    binding.cgProductCategories.addView(allChip)

                    categoryMap.forEach { (categoryLabel, categoryId) ->
                        val chip = createFilterChipFrom(context)
                        chip.text = categoryLabel
                        chip.tag = categoryId
                        chip.setOnCheckedChangeListener { chipView, checked ->
                            if (checked)
                                viewModel.addCategoryFilter(chipView.tag as Int)
                            else
                                viewModel.removeCategoryFilter(chipView.tag as Int)
                        }
                        binding.cgProductCategories.addView(chip)
                    }
                    binding.progressCategories.isVisible = false
                }
            }
        }
    }

    private fun createFilterChipFrom(context: Context): Chip {
        val drawable = ChipDrawable.createFromAttributes(
            context,
            null,
            0,
            R.style.Widget_App_Chip_Filter
        )
        return Chip(context).apply { setChipDrawable(drawable) }
    }

    private fun createActionChipFrom(context: Context): Chip {
        val drawable = ChipDrawable.createFromAttributes(
            context,
            null,
            0,
            R.style.Widget_App_Chip_Action
        )
        return Chip(context).apply { setChipDrawable(drawable) }
    }
}