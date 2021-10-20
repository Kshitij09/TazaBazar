package com.kshitijpatil.tazabazar.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import com.kshitijpatil.tazabazar.databinding.FragmentFavoriteProductsBinding
import com.kshitijpatil.tazabazar.di.FavoriteProductsViewModelFactory
import com.kshitijpatil.tazabazar.domain.data
import com.kshitijpatil.tazabazar.domain.succeeded
import com.kshitijpatil.tazabazar.model.Inventory
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.ui.cart.CartFragment
import com.kshitijpatil.tazabazar.ui.common.CoilProductLoadImageDelegate
import com.kshitijpatil.tazabazar.ui.home.HomeFragment
import com.kshitijpatil.tazabazar.ui.home.ProductFilterFragment
import com.kshitijpatil.tazabazar.ui.home.ProductListAdapter
import com.kshitijpatil.tazabazar.ui.home.ProductListAdapter.ProductLayoutType
import com.kshitijpatil.tazabazar.ui.home.ProductViewHolder
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import com.kshitijpatil.tazabazar.util.textChanges
import com.kshitijpatil.tazabazar.widget.FadingSnackbar
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import timber.log.Timber

class FavoriteProductsFragment : Fragment(), ProductViewHolder.OnItemActionCallback {
    private var _binding: FragmentFavoriteProductsBinding? = null
    private val binding: FragmentFavoriteProductsBinding get() = _binding!!
    private val args: FavoriteProductsFragmentArgs by navArgs()
    private val loadImageDelegate = CoilProductLoadImageDelegate()
    private val productListAdapter = ProductListAdapter(loadImageDelegate, ProductLayoutType.ROW)
    private val viewModel: FavoriteProductsViewModel by viewModels {
        FavoriteProductsViewModelFactory(
            requireContext().applicationContext,
            titleIdToFavoriteType(args.listTitle)
        )
    }
    private lateinit var snackbar: FadingSnackbar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteProductsBinding.inflate(inflater, container, false)
        binding.tvListTitle.text = requireContext().getString(args.listTitle)
        productListAdapter.onItemActionCallback = this
        binding.rvProducts.adapter = productListAdapter
        snackbar = binding.snackbar
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // reload favorite products everytime a
        // fragment is created as favorites might
        // have changed since then
        viewModel.loadFavoriteProducts()
        setupAddAllButton()
        launchAndRepeatWithViewLifecycle {
            launch { observeProductList() }
            launch { observeSearchQuery() }
        }
    }

    private fun setupAddAllButton() {
        binding.btnAddAll.setOnClickListener {
            lifecycleScope.launch {
                val result = viewModel.addAllFavoritesToCart()
                val context = requireContext()
                val msg = if (result.succeeded) {
                    val itemsCarted = result.data!!
                    if (itemsCarted == 0) {
                        context.getString(R.string.info_already_carted_multi)
                    } else {
                        notifyCartChanged()
                        context.resources.getQuantityString(
                            R.plurals.info_no_of_items_added_to_cart,
                            itemsCarted,
                            itemsCarted
                        )
                    }
                } else {
                    context.getString(R.string.error_something_went_wrong)
                }
                snackbar.show(messageText = msg)
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        productListAdapter.onItemActionCallback = null
        super.onDestroyView()
    }

    private suspend fun observeProductList() {
        viewModel.productList.collect(productListAdapter::submitList)
    }

    private suspend fun observeSearchQuery() {
        binding.textFieldSearch.editText?.let { textField ->
            textField.textChanges()
                .debounce(ProductFilterFragment.SEARCH_DEBOUNCE_MILLIS)
                .collect { viewModel.searchProductsBy(it.toString()) }
        }
    }

    override fun onFavoriteClicked(product: Product) {
        if (product.favorites.isNotEmpty()) {
            viewModel.removeFavorites(product)
            notifyProductsUpdated()
        } else {
            val favoriteType = titleIdToFavoriteType(args.listTitle)
            Timber.e("Product: '$product' inappropriately appeared in $favoriteType list")
        }
    }

    override fun onCartClicked(productName: String, inventory: Inventory) {
        lifecycleScope.launch {
            viewModel.addToCart(inventory).data?.let { itemAdded ->
                val context = requireContext()
                val cartMessage = if (itemAdded) {
                    notifyCartChanged()
                    context.getString(
                        R.string.info_inventory_added_to_cart,
                        productName,
                        inventory.quantityLabel
                    )
                } else {
                    context.getString(R.string.info_already_carted_single)
                }
                snackbar.show(messageText = cartMessage)
            }
        }
    }

    private fun titleIdToFavoriteType(@StringRes titleId: Int): FavoriteType {
        return when (titleId) {
            R.string.title_my_monthly_list -> FavoriteType.MONTHLY
            else -> FavoriteType.WEEKLY // Using weekly list by default
        }
    }

    private fun notifyProductsUpdated() {
        setFragmentResult(HomeFragment.PRODUCTS_UPDATED_RESULT_KEY, bundleOf())
    }

    private fun notifyCartChanged() {
        setFragmentResult(CartFragment.CART_CHANGED_RESULT, bundleOf())
    }
}