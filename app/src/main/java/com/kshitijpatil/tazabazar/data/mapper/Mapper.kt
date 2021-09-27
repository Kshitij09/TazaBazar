package com.kshitijpatil.tazabazar.data.mapper

interface Mapper<F, T> {
    fun map(from: F): T
}