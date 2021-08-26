package com.kshitijpatil.tazabazar.util

import kotlinx.coroutines.CoroutineDispatcher

// Inspired from https://github.com/chrisbanes/tivi
data class AppCoroutineDispatchers(
    val io: CoroutineDispatcher,
    val computation: CoroutineDispatcher,
    val main: CoroutineDispatcher
)