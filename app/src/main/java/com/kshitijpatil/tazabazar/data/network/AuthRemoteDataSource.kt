package com.kshitijpatil.tazabazar.data.network

import arrow.core.Either
import arrow.core.left
import arrow.core.rightIfNotNull
import com.kshitijpatil.tazabazar.api.AuthApi
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.LoginResponse
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import com.kshitijpatil.tazabazar.data.ApiException
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.EmptyBodyException
import com.kshitijpatil.tazabazar.data.UnknownException
import retrofit2.Response
import timber.log.Timber

interface AuthRemoteDataSource {
    suspend fun login(request: LoginRequest): Either<DataSourceException, LoginResponse>
    suspend fun register(request: RegisterRequest): Either<DataSourceException, LoginResponse.User>
    suspend fun refreshToken(token: String): Either<DataSourceException, String>
}

class AuthRemoteDataSourceImpl(private val api: AuthApi) : AuthRemoteDataSource {

    override suspend fun login(request: LoginRequest) = getResponseBody { api.login(request) }

    override suspend fun register(request: RegisterRequest) =
        getResponseBody { api.register(request) }

    override suspend fun refreshToken(token: String): Either<DataSourceException, String> {
        return getResponseBody { api.refreshToken(token) }.map { it.accessToken }
    }

    private suspend fun <T> getResponseBody(func: suspend () -> Response<T>): Either<DataSourceException, T> {
        return Either.catch {
            val response = func()
            return if (response.isSuccessful) {
                response.body().rightIfNotNull {
                    Timber.d("Response body was null")
                    EmptyBodyException
                }
            } else {
                ApiException(response.code(), response.errorBody()).left()
            }
        }.mapLeft {
            val ex = mapCommonNetworkExceptions(it)
            if (ex != null) ex
            else {
                Timber.e(it)
                UnknownException(it)
            }
        }
    }
}