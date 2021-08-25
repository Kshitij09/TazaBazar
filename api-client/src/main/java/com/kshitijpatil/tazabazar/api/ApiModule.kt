package com.kshitijpatil.tazabazar.api

import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

object ApiModule {
    private const val baseUrl = "http://tazabazar.ddns.net:8080"
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
    val tazaBazarApi: TazaBazarApi by lazy { retrofit.create() }
}