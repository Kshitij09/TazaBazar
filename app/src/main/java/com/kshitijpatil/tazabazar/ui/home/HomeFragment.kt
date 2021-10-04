package com.kshitijpatil.tazabazar.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import com.kshitijpatil.tazabazar.databinding.FragmentHomeBinding
import com.kshitijpatil.tazabazar.di.ViewModelFactory
import com.kshitijpatil.tazabazar.domain.data
import com.kshitijpatil.tazabazar.domain.succeeded
import com.kshitijpatil.tazabazar.model.Inventory
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.ui.SwipeRefreshHandler
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import com.kshitijpatil.tazabazar.widget.FadingSnackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeFragment : Fragment(), ProductViewHolder.OnItemActionCallback {

    companion object {

        const val FAVORITE_OPTIONS_RESULT_KEY =
            "com.kshitijpatil.tazabazar.ui.home.favorite-options-result"

        /** Use this key with bundle to return an array of [FavoriteType] ordinals */
        const val FAVORITE_OPTIONS_BUNDLE_KEY =
            "com.kshitijpatil.tazabazar.ui.home.favorite-options-bundle"

        /** Use this key with bundle to return product-sku */
        const val FAVORITE_SKU_BUNDLE_KEY =
            "com.kshitijpatil.tazabazar.ui.home.favorite-product-sku-bundle"

        /** Use this key to notify [HomeFragment] about product-list has been updated */
        const val PRODUCTS_UPDATED_RESULT_KEY =
            "com.kshitijpatil.tazabazar.ui.home.products-updated"
    }

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by activityViewModels {
        ViewModelFactory(requireActivity(), requireContext().applicationContext, arguments)
    }
    private val productListAdapter = ProductListAdapter()
    private lateinit var snackbar: FadingSnackbar
    private val favoriteTypeValues = enumValues<FavoriteType>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listenFavoriteOptionsResult()
        listenProductsUpdatedResult()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        snackbar = binding.snackbar
        productListAdapter.onItemActionCallback = this
        binding.rvProducts.adapter = productListAdapter
        binding.rvProducts.layoutManager = GridLayoutManager(requireContext(), 2)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        snackbar.show(
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
        setFragmentResultListener(FAVORITE_OPTIONS_RESULT_KEY) { _, bundle ->
            val favoriteOrdinals = bundle.getIntArray(FAVORITE_OPTIONS_BUNDLE_KEY)
            val productSku = bundle.getString(FAVORITE_SKU_BUNDLE_KEY)
            val favoriteChoices = favoriteOrdinals?.map { favoriteTypeValues[it] }
            if (favoriteChoices != null && productSku != null) {
                viewModel.updateFavorites(productSku, favoriteChoices.toSet())
            } else {
                Timber.e("Unable to update favorites with sku=$productSku and favoriteChoices=$favoriteChoices")
            }
        }
    }

    private fun listenProductsUpdatedResult() {
        setFragmentResultListener(PRODUCTS_UPDATED_RESULT_KEY) { _, _ ->
            Timber.d("Received a result indicating product list updated")
            lifecycleScope.launch { viewModel.reloadProductsData() }
        }
    }

    private fun setupSwipeRefreshUI() {
        val swipeRefreshHandler = SwipeRefreshHandler(
            scope = viewLifecycleOwner.lifecycle.coroutineScope,
            swipeRefreshLayout = binding.swipeRefreshProducts,
            action = { viewModel.reloadProductsData(forceRefresh = true) }
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

    override fun onCartClicked(productName: String, inventory: Inventory) {
        lifecycleScope.launch {
            val result = viewModel.addToCart(inventory)
            if (result.succeeded) {
                val cartMessage = if (result.data == true) {
                    requireContext().getString(
                        R.string.info_inventory_added_to_cart,
                        productName,
                        inventory.quantityLabel
                    )
                } else {
                    requireContext().getString(R.string.info_already_carted_single)
                }
                snackbar.show(messageText = cartMessage)
            }
        }
    }

}