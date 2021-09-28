package com.kshitijpatil.tazabazar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import com.kshitijpatil.tazabazar.databinding.FragmentHomeBinding
import com.kshitijpatil.tazabazar.di.ViewModelFactory
import com.kshitijpatil.tazabazar.ui.SwipeRefreshHandler
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 *
 * adapter.setOnFavoriteToggledListener { sku, isFavorite ->
 *      viewModel.submitFavoriteAction(sku, isFavorite)
 * }
 * -------------
 * HomeViewModel
 *  init {
 *      favoritePendingActions.collect{ action ->
 *          val productMap = productList.value
 *          var product = productMap[action.sku]
 *          productMap[actions.sku] = product.copy(isFavorite = action.isFavorite)
 *          productList.emit(productMap)
 *      }
 *  }
 *
 *   fun submitFavoriteAction(sku: String, isFavorite: Boolean) {
 *      favoritePendingActions.emit(FavoriteAction(sku,isFavorite)
 *   }
 */

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels {
        ViewModelFactory(this, requireContext().applicationContext, arguments)
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
        Timber.d("On View Created called")
        observeProductList(productListAdapter)
        setupSwipeRefreshUI()
    }

    private fun setupSwipeRefreshUI() {
        val swipeRefreshHandler = SwipeRefreshHandler(
            scope = viewLifecycleOwner.lifecycle.coroutineScope,
            swipeRefreshLayout = binding.swipeRefreshProducts,
            action = { viewModel.refreshData() }
        )
        lifecycle.addObserver(swipeRefreshHandler)
        binding.swipeRefreshProducts.setOnRefreshListener(swipeRefreshHandler)
    }

    private fun observeProductList(productListAdapter: ProductListAdapter) {
        // Collect viewModel Flows in a confined viewLifecycle scope
        launchAndRepeatWithViewLifecycle {
            launch { viewModel.productList.collect(productListAdapter::submitList) }
        }
    }

}