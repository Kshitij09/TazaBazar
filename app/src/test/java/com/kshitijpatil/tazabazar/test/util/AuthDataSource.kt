package com.kshitijpatil.tazabazar.test.util

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.LoginResponse
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import com.kshitijpatil.tazabazar.data.ApiException
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.UnknownException
import com.kshitijpatil.tazabazar.data.network.AuthRemoteDataSource
import okhttp3.ResponseBody

class SucceedingAuthDataSource(
    private val loginResponse: LoginResponse? = null,
    private val registerResponse: LoginResponse.User? = null,
    private val accessToken: String? = null
) : AuthRemoteDataSource {
    override suspend fun login(request: LoginRequest): Either<DataSourceException, LoginResponse> {
        return loginResponse?.right() ?: UnknownException(NotImplementedError()).left()
    }

    override suspend fun logout(accessToken: String): Either<DataSourceException, Unit> {
        return Either.Right(Unit)
    }

    override suspend fun register(request: RegisterRequest): Either<DataSourceException, LoginResponse.User> {
        return registerResponse?.right() ?: UnknownException(NotImplementedError()).left()
    }

    override suspend fun refreshToken(token: String): Either<DataSourceException, String> {
        return accessToken?.right() ?: UnknownException(NotImplementedError()).left()
    }

}

class HttpFailureAuthDataSource(
    private val statusCode: Int,
    private val errorBody: ResponseBody? = null
) : AuthRemoteDataSource {
    override suspend fun login(request: LoginRequest): Either<DataSourceException, LoginResponse> {
        return ApiException(statusCode, errorBody).left()
    }

    override suspend fun logout(accessToken: String): Either<DataSourceException, Unit> {
        return ApiException(statusCode, errorBody).left()
    }

    override suspend fun register(request: RegisterRequest): Either<DataSourceException, LoginResponse.User> {
        return ApiException(statusCode, errorBody).left()
    }

    override suspend fun refreshToken(token: String): Either<DataSourceException, String> {
        return ApiException(statusCode, errorBody).left()
    }
}

