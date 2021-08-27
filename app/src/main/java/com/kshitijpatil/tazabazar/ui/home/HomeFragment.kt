package com.kshitijpatil.tazabazar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.kshitijpatil.tazabazar.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collect

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels { HomeViewModelFactory() }
    private val productListAdapter = ProductListAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.rvProducts.adapter = productListAdapter
        observeProductList(productListAdapter)
        viewModel.getAllProducts()
        return binding.root
    }

    private fun observeProductList(productListAdapter: ProductListAdapter) {
        lifecycleScope.launchWhenCreated {
            viewModel.productList.collect(productListAdapter::submitList)
        }
    }

}