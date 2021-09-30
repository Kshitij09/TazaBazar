package com.kshitijpatil.tazabazar.ui.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.kshitijpatil.tazabazar.R
import com.kshitijpatil.tazabazar.data.local.entity.FavoriteType
import com.kshitijpatil.tazabazar.databinding.FragmentFavoriteProductsBinding
import com.kshitijpatil.tazabazar.model.Product
import com.kshitijpatil.tazabazar.ui.home.ProductListAdapter
import com.kshitijpatil.tazabazar.ui.home.ProductListAdapter.ProductLayoutType
import com.kshitijpatil.tazabazar.ui.home.ProductViewHolder
import com.kshitijpatil.tazabazar.util.launchAndRepeatWithViewLifecycle
import kotlinx.coroutines.flow.collect

class FavoriteProductsFragment : Fragment(), ProductViewHolder.OnItemActionCallback {
    private var _binding: FragmentFavoriteProductsBinding? = null
    private val binding: FragmentFavoriteProductsBinding get() = _binding!!
    private val args: FavoriteProductsFragmentArgs by navArgs()
    private val productListAdapter = ProductListAdapter(ProductLayoutType.ROW)
    private val viewModel: FavoriteProductsViewModel by viewModels {
        FavoriteProductsViewModelFactory(
            requireContext().applicationContext,
            titleIdToFavoriteType(args.listTitle)
        )
    }

    private fun titleIdToFavoriteType(@StringRes titleId: Int): FavoriteType {
        return when (titleId) {
            R.string.title_my_monthly_list -> FavoriteType.MONTHLY
            else -> FavoriteType.WEEKLY // Using weekly list by default
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteProductsBinding.inflate(inflater, container, false)
        binding.tvListTitle.text = requireContext().getString(args.listTitle)
        productListAdapter.onItemActionCallback = this
        binding.rvProducts.adapter = productListAdapter
        binding.rvProducts.layoutManager = LinearLayoutManager(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // reload favorite products everytime a
        // fragment is created as favorites might
        // have changed since then
        viewModel.loadFavoriteProducts()
        launchAndRepeatWithViewLifecycle {
            viewModel.productList.collect(productListAdapter::submitList)
        }
    }

    override fun onDestroyView() {
        _binding = null
        productListAdapter.onItemActionCallback = null
        super.onDestroyView()
    }

    override fun onFavoriteClicked(product: Product) {

    }
}