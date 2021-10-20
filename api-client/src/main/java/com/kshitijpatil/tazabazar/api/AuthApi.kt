package com.kshitijpatil.tazabazar.api

import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.LoginResponse
import com.kshitijpatil.tazabazar.api.dto.RefreshTokenResponse
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    @POST("/api/v2/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("/api/v2/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse.User>

    @GET("/api/v2/auth/token")
    suspend fun refreshToken(@Header("refresh-token") token: String): Response<RefreshTokenResponse>
}