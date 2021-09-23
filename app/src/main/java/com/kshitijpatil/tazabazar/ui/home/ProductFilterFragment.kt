package com.kshitijpatil.tazabazar.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentProductFilterBinding
import com.kshitijpatil.tazabazar.di.ViewModelFactory
import kotlinx.coroutines.flow.collect

class ProductFilterFragment : Fragment() {
    private var _binding: FragmentProductFilterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels {
        ViewModelFactory(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProductFilterBinding.inflate(inflater, container, false)
        observeCategoryFilters()
        return binding.root
    }

    private fun observeCategoryFilters() {
        val context = binding.root.context
        lifecycleScope.launchWhenCreated {
            viewModel.productCategories.collect { categories ->
                if (categories.isNotEmpty()) {
                    binding.cgProductCategories.removeAllViews()
                    binding.progressCategories.isVisible = true

                    val allChip = createActionChipFrom(context)
                    allChip.text = context.getString(R.string.label_all)
                    allChip.setOnClickListener {
                        binding.cgProductCategories.clearCheck()
                        viewModel.clearCategoryFilter()
                    }
                    binding.cgProductCategories.addView(allChip)

                    categories.forEach { category ->
                        val chip = createFilterChipFrom(context)
                        chip.text = category.name
                        chip.tag = category.label
                        chip.isChecked = viewModel.categoryFilter.value == category.label
                        chip.setOnCheckedChangeListener { chipView, checked ->
                            if (checked)
                                viewModel.setCategoryFilter(chipView.tag as String)
                            else
                                viewModel.clearCategoryFilter()
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
            R.style.Widget_App_Chip_Choice
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