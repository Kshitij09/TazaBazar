package com.kshitijpatil.tazabazar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kshitijpatil.tazabazar.databinding.FragmentHomeBinding
import com.kshitijpatil.tazabazar.di.ViewModelFactory
import kotlinx.coroutines.flow.collect

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels {
        ViewModelFactory(requireContext().applicationContext)
    }
    private val productListAdapter = ProductListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.rvProducts.adapter = productListAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeProductList(productListAdapter)
    }

    private fun observeProductList(productListAdapter: ProductListAdapter) {
        lifecycleScope.launchWhenCreated {
            viewModel.productList.collect(productListAdapter::submitList)
        }
    }

}