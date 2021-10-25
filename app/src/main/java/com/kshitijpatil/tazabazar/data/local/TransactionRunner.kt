package com.kshitijpatil.tazabazar.data.local

interface TransactionRunner {
    suspend operator fun <R> invoke(block: suspend () -> R): R
}

