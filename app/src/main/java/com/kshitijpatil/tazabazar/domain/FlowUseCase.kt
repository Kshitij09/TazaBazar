package com.kshitijpatil.tazabazar.domain

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*

abstract class FlowUseCase<P, R>(private val dispatcher: CoroutineDispatcher? = null) {
    private val paramState = MutableSharedFlow<P>()

    private val flow: Flow<R> = paramState
        .distinctUntilChanged()
        .flatMapLatest { createObservable(it) }
        .distinctUntilChanged()
        .apply { dispatcher?.let { flowOn(it) } }

    open fun observe() = flow

    suspend operator fun invoke(params: P) {
        paramState.emit(params)
    }

    protected abstract fun createObservable(params: P): Flow<R>
}

abstract class FlowProducerUseCase<R>(private val dispatcher: CoroutineDispatcher?) {
    operator fun invoke() = createObservable()
        .apply { dispatcher?.let { flowOn(it) } }

    protected abstract fun createObservable(): Flow<R>
}