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
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.databinding.FragmentProductFilterBinding
import com.kshitijpatil.tazabazar.di.ViewModelFactory
import com.kshitijpatil.tazabazar.util.textChanges
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce

class ProductFilterFragment : Fragment() {

    companion object {
        const val SEARCH_DEBOUNCE_MILLIS = 1500L
    }

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
        observeSearchQuery()
        return binding.root
    }

    private fun observeSearchQuery() {
        lifecycleScope.launchWhenCreated {
            binding.textFieldSearch.editText?.let { searchField ->
                searchField.textChanges()
                    .debounce(SEARCH_DEBOUNCE_MILLIS)
                    .collect { viewModel.setSearchQuery(it.toString()) }
            }
        }
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
                        val chip = createChoiceChipFrom(context)
                        chip.text = category.name
                        chip.tag = category.label
                        chip.isChecked = category.label == viewModel.categoryFilter.value
                        chip.setOnCheckedChangeListener { chipView, checked ->
                            if (checked) viewModel.setCategoryFilter(chipView.tag as String)
                        }
                        binding.cgProductCategories.addView(chip)
                    }
                    binding.progressCategories.isVisible = false
                }
            }
        }
    }

    private fun createChoiceChipFrom(context: Context): Chip {
        return Chip(context, null, R.attr.CategoryChipChoiceStyle)
    }

    private fun createActionChipFrom(context: Context): Chip {
        return Chip(context, null, R.attr.CategoryChipActionStyle)
    }
}