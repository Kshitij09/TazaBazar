package com.kshitijpatil.tazabazar.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.create

object ApiModule {
    const val baseUrl = "http://tazabazaar.ddns.net:8080"
    private val moshiConverterFactory = MoshiConverterFactory.create()

    private val defaultRetrofitBuilder = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(moshiConverterFactory)

    private fun getAuthInterceptorFor(accessToken: String): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
                .newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            chain.proceed(request)
        }
    }

    fun provideRetrofitWith(client: OkHttpClient): Retrofit {
        return defaultRetrofitBuilder.client(client).build()
    }

    fun provideProductApi(client: OkHttpClient): ProductApi {
        return provideRetrofitWith(client).create()
    }

    fun provideAuthApi(client: OkHttpClient): AuthApi {
        return provideRetrofitWith(client).create()
    }

    fun provideOrderApi(baseClient: OkHttpClient, accessToken: String): OrderApi {
        val authorizedClient = baseClient.newBuilder()
            .addInterceptor(getAuthInterceptorFor(accessToken))
            .build()
        return provideRetrofitWith(authorizedClient).create()
    }
}

