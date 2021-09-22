package com.kshitijpatil.tazabazar.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

object ApiModule {
    private const val baseUrl = "http://tazabazaar.ddns.net:8080"
    private val moshiConverterFactory = MoshiConverterFactory.create()

    // defer calling `build` to share the same okHttpClient
    // across all the instances of retrofit
    private val retrofitBuilder: Retrofit.Builder by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(moshiConverterFactory)
    }

    // We decide to inject okttpClient from the app module
    // Since cache requires Context
    fun provideRetrofitWith(client: OkHttpClient): Retrofit {
        return retrofitBuilder.client(client).build()
    }

    fun provideTazaBazarApi(client: OkHttpClient): TazaBazarApi {
        return provideRetrofitWith(client).create()
    }
}

