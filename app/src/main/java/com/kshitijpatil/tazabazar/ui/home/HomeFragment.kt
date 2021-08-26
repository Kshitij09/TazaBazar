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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        observeProductList()
        populateProductNames()
        return binding.root
    }

    private fun populateProductNames() {
        viewModel.refreshProductList()
    }

    private fun observeProductList() {
        lifecycleScope.launchWhenCreated {
            viewModel.productList.collect { products ->
                val productString = StringBuilder()
                products.forEach {
                    productString.appendLine(it.name)
                }
                binding.txtNames.text = productString.toString()
            }
        }
    }

}