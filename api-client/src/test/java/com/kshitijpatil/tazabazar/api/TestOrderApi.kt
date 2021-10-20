package com.kshitijpatil.tazabazar.api

import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.OrderLine
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Test

/**
 * NOTE: These tests are just meant for development
 *  and are not ready to run in CI pipelines
 */
class TestOrderApi {
    private val client = OkHttpClient.Builder().build()
    private lateinit var api: OrderApi
    private val authApi = ApiModule.provideAuthApi(client)
    private val testLoginCredentials = LoginRequest("john.doe@test.com", "1234")
    private val testOrderLines = listOf(OrderLine(1, 3), OrderLine(2, 3))

    @Test
    fun test_placeOrder() = runBlocking {
        val response = authApi.login(testLoginCredentials)
        assertThat(response.isSuccessful).isTrue()
        val accessToken = response.body()!!.accessToken
        api = ApiModule.provideOrderApi(client, accessToken)
        val orderResponse = api.placeOrder(testOrderLines)
        assertThat(orderResponse).isNotNull()
        // TODO: Delete this order if POST call was successful
    }

    @Test
    fun test_getOrderById() = runBlocking {
        val testOrderId = "b8128611-f1e8-46bf-a934-25e719d5187a"
        val response = authApi.login(testLoginCredentials)
        assertThat(response.isSuccessful).isTrue()
        val accessToken = response.body()!!.accessToken
        api = ApiModule.provideOrderApi(client, accessToken)
        val orderResponse = api.getOrderById(testOrderId)
        assertThat(orderResponse).isNotNull()
    }

    @Test
    fun test_getOrdersByUsername() = runBlocking {
        val testOrderId = "b8128611-f1e8-46bf-a934-25e719d5187a"
        val response = authApi.login(testLoginCredentials)
        assertThat(response.isSuccessful).isTrue()
        val accessToken = response.body()!!.accessToken
        api = ApiModule.provideOrderApi(client, accessToken)
        val userOrders = api.getOrdersByUsername(testLoginCredentials.username)
        assertThat(userOrders).hasSize(1)
        assertThat(userOrders[0].id).isEqualTo(testOrderId)
    }
}