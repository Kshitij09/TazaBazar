package com.kshitijpatil.tazabazar.di

import com.kshitijpatil.tazabazar.api.ApiModule
import com.kshitijpatil.tazabazar.api.OrderApi
import okhttp3.OkHttpClient

fun interface OrderApiFactory {
    fun create(accessToken: String): OrderApi
}

class DefaultOrderApiFactory(private val client: OkHttpClient) : OrderApiFactory {
    override fun create(accessToken: String): OrderApi {
        return ApiModule.provideOrderApi(client, accessToken)
    }
}