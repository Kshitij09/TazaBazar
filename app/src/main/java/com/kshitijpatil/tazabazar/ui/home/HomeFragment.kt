package com.kshitijpatil.tazabazar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import com.kshitijpatil.tazabazar.databinding.FragmentHomeBinding
import com.kshitijpatil.tazabazar.di.ViewModelFactory
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.ui.SwipeRefreshHandler
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import com.kshitijpatil.tazabazar.ui.favorite.FavoriteOptionsBottomSheet
import com.kshitijpatil.tazabazar.widget.FadingSnackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeFragment : Fragment(), ProductViewHolder.OnItemActionCallback {

    companion object {
        /** An array [FavoriteType] ordinals should be passed along this key */
        const val FAVORITE_PREFERENCES_KEY =
            "com.kshitijpatil.tazabazar.ui.home.favorite-preferences"

        /** Use this key to pass product-sku back and forth */
        const val FAVORITE_SKU_KEY = "com.kshitijpatil.tazabazar.ui.home.favorite-product-sku"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels {
        ViewModelFactory(this, requireContext().applicationContext, arguments)
    }
    private val productListAdapter = ProductListAdapter()
    private var snackbar: FadingSnackbar? = null
    private val favoriteTypeValues = enumValues<FavoriteType>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listenFavoriteOptionsResult()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        snackbar = requireActivity().findViewById(R.id.snackbar)
        productListAdapter.onItemActionCallback = this
        binding.rvProducts.adapter = productListAdapter
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("On View Created called")
        observeProductList(productListAdapter)
        setupSwipeRefreshUI()
    }

    override fun onStop() {
        super.onStop()
        // To prevent any memory leaks
        productListAdapter.onItemActionCallback = null
    }

    // ====================
    //  UI Business Logic
    // ====================

    private fun showFavoriteSnackbarFor(product: Product) {
        snackbar?.show(
            messageId = R.string.info_added_to_weekly_favorites,
            actionId = R.string.action_change,
            actionClick = {
                launchFavoriteOptionsBottomSheet(product)
            }
        )
    }

    private fun launchFavoriteOptionsBottomSheet(product: Product) {
        val favoriteOrdinals = product.favorites.map(FavoriteType::ordinal).toIntArray()
        findNavController().navigate(
            HomeFragmentDirections.actionNavigationHomeToBottomSheetFavoriteOptions(
                product.sku,
                favoriteOrdinals
            )
        )
    }

    private fun listenFavoriteOptionsResult() {
        setFragmentResultListener(FAVORITE_PREFERENCES_KEY) { _, bundle ->
            val favoriteOrdinals = bundle.getIntArray(FAVORITE_PREFERENCES_KEY)
            val productSku = bundle.getString(FAVORITE_SKU_KEY)
            val favoriteChoices = favoriteOrdinals?.map { favoriteTypeValues[it] }
            if (favoriteChoices != null && productSku != null) {
                viewModel.updateFavorites(productSku, favoriteChoices.toSet())
            } else {
                Timber.e("Unable to update favorites with sku=$productSku and favoriteChoices=$favoriteChoices")
            }
        }
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

    override fun onFavoriteClicked(product: Product) {
        // Already part of some favorites, launch Favorite Options
        if (product.favorites.isNotEmpty()) {
            launchFavoriteOptionsBottomSheet(product)
        } else {
            // Add to weekly favorites and allow user to change their choice
            // by Snackbar actions
            viewModel.updateFavorites(product.sku, setOf(FavoriteType.WEEKLY))

            showFavoriteSnackbarFor(product.withToWeeklyFavorites())
        }
    }

}