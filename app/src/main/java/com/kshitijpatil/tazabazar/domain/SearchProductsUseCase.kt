package com.kshitijpatil.tazabazar.domain

import com.kshitijpatil.tazabazar.data.ProductRepository
import com.kshitijpatil.tazabazar.model.Product
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SearchProductsUseCase(
    ioDispatcher: CoroutineDispatcher,
    private val productRepository: ProductRepository
) : FlowUseCase<SearchProductsUseCase.Params, MediatorResult<List<Product>>>(
    ioDispatcher,
    conflateParams = false
) {
    data class Params(
        val query: String? = null,
        val category: String? = null,
        val forceRefresh: Boolean = false
    )

    override fun createObservable(params: Params): Flow<MediatorResult<List<Product>>> {
        return flow {
            emit(MediatorResult.Loading(ResponseOrigin.LOCAL))
            val localProducts = productRepository.getProductListBy(params.category, params.query)
            emit(MediatorResult.Success(localProducts, ResponseOrigin.LOCAL))
            // refresh if asked for or couldn't find any results
            // in the local source
            val refresh = params.forceRefresh || localProducts.isEmpty()
            if (refresh) {
                emit(MediatorResult.Loading(ResponseOrigin.REMOTE))
                runCatching {
                    productRepository.getProductListBy(
                        category = params.category,
                        query = params.query,
                        forceRefresh = refresh
                    )
                }.onSuccess {
                    emit(MediatorResult.Success(it, ResponseOrigin.REMOTE))
                }.onFailure {
                    emit(MediatorResult.Error(it, ResponseOrigin.REMOTE))
                }
            }
        }
    }
}