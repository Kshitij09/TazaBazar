package com.kshitijpatil.tazabazar.data.network

import arrow.core.Either
import arrow.core.getOrElse
import arrow.core.left
import arrow.core.rightIfNotNull
import com.kshitijpatil.tazabazar.api.AuthApi
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.LoginResponse
import com.kshitijpatil.tazabazar.data.*
import timber.log.Timber

interface AuthRemoteDataSource {
    suspend fun login(request: LoginRequest): Either<DataSourceException, LoginResponse>
}

class AuthRemoteDataSourceImpl(private val api: AuthApi) : AuthRemoteDataSource {

    override suspend fun login(request: LoginRequest): Either<DataSourceException, LoginResponse> {
        return Either.catch {
            val response = api.login(request)
            return if (response.isSuccessful) {
                response.body().rightIfNotNull {
                    Timber.d("login: Response body was null")
                    EmptyBodyException
                }
            } else {
                ApiException(response.code(), response.errorBody()).left()
            }
        }.mapLeft {
            it.mapCommonNetworkExceptions("login").getOrElse {
                // You can continue handling more errors here
                Timber.e(it, "login: Unhandled exception")
                UnknownException(it)
            }

        }
    }
}