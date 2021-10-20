package com.kshitijpatil.tazabazar.api

import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.api.dto.ApiError
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.coroutines.runBlocking
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Test
import java.net.HttpURLConnection

@ExperimentalStdlibApi
class TestAuthApi {
    private val loggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    private val client = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()
    private val api = ApiModule.provideAuthApi(client)
    private val moshi = Moshi.Builder().build()
    private val apiErrorJsonAdapter: JsonAdapter<ApiError> = moshi.adapter()

    // this is a test account provided by the APIs by default
    private val testLoginCredentials = LoginRequest("john.doe@test.com", "1234")

    // test admin account
    private val adminCredentials = LoginRequest("ashok.kumar@test.com", "0000")

    @Test
    fun login_whenValidCredentials_shouldReturn200() = runBlocking {
        val response = api.login(testLoginCredentials)
        assertThat(response.isSuccessful).isTrue()
        assertThat(response.body()).isNotNull()
    }

    @Test
    fun login_whenInvalidCredentials_shouldReturn401() = runBlocking {
        val credentials = LoginRequest("john.doe@test.com", "asfgbkagk")
        val response = api.login(credentials)
        assertThat(response.code()).isEqualTo(HttpURLConnection.HTTP_UNAUTHORIZED)
    }

    @Test
    fun register() = runBlocking {
        val request = RegisterRequest("user1@test.com", "abcd", "User 2", "8080676745")
        val response = api.register(request)
        assertThat(response.isSuccessful).isTrue()

        deleteAccount(request.username) // delete immediately after you know account was created

        // rest of the assertions
        assertThat(response.body()).isNotNull()
    }

    @Test
    fun register_whenEmailExists_shouldReturn400AndErrorCode() = runBlocking {
        // user with this email address exists but phone is unique
        val request = RegisterRequest("john.doe@test.com", "abcd", "User 1", "1289651472")
        val response = api.register(request)
        if (response.isSuccessful) {
            deleteAccount(request.username) // delete immediately after you know account was created
        }
        assertThat(response.code()).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST)
        val apiError = response.errorBody()?.let {
            apiErrorJsonAdapter.fromJson(it.source())
        }
        assertThat(apiError?.error).isEqualTo("userdetail-001")
    }

    @Test
    fun register_whenPhoneExists_shouldReturn400AndErrorCode() = runBlocking {
        // user with this phone exists but email address is unique
        val request = RegisterRequest("user-unique-email@test.com", "abcd", "User 1", "1234567890")
        val response = api.register(request)
        if (response.isSuccessful) {
            deleteAccount(request.username) // delete immediately after you know account was created
        }
        assertThat(response.code()).isEqualTo(HttpURLConnection.HTTP_BAD_REQUEST)
        val apiError = response.errorBody()?.let {
            apiErrorJsonAdapter.fromJson(it.source())
        }
        assertThat(apiError?.error).isEqualTo("userdetail-002")
    }

    @Test
    fun refreshToken_whenValidAccessToken_shouldReturn200() = runBlocking {
        val loginResponse = api.login(testLoginCredentials)
        assertThat(loginResponse.isSuccessful)
        assertThat(loginResponse.body()).isNotNull()
        val refreshToken = loginResponse.body()!!.refreshToken
        val response = api.refreshToken(refreshToken)
        assertThat(response.isSuccessful).isTrue()
        assertThat(response.body()?.accessToken).isNotEmpty()
    }

    private suspend fun deleteAccount(username: String) {
        val response = api.login(adminCredentials)

        if (!response.isSuccessful || response.body() == null) {
            println("Failed to login as an admin, skipping cleanup")
        }
        val accessToken = response.body()!!.accessToken

        val deleteUrl = "${ApiModule.baseUrl}/api/v2/users".toHttpUrl().newBuilder()
            .addPathSegment(username)
            .build()
        val deleteAccountRequest = Request.Builder()
            .addHeader("Authorization", "Bearer $accessToken")
            .url(deleteUrl)
            .delete()
            .build()
        client.newCall(deleteAccountRequest).execute().use {
            if (!it.isSuccessful) {
                println("Failed deleting the account with username: $username")
            }
        }
    }
}