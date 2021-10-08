package com.kshitijpatil.tazabazar.data.local

import arrow.core.Either
import arrow.core.computations.either
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import com.kshitijpatil.tazabazar.data.*
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStore
import com.kshitijpatil.tazabazar.data.mapper.LoginResponseUserToLoggedInUser
import com.kshitijpatil.tazabazar.data.network.AuthRemoteDataSource
import com.kshitijpatil.tazabazar.data.util.LocalDateTimeSerializer
import com.kshitijpatil.tazabazar.data.util.LoggedInUserSerializer
import com.kshitijpatil.tazabazar.domain.Result
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import java.net.HttpURLConnection

interface AuthRepository {
    suspend fun login(request: LoginRequest): Either<LoginException, LoggedInUser>
    suspend fun register(request: RegisterRequest): Result<LoggedInUser>
    suspend fun refreshToken(): Result<Unit>
}

class AuthRepositoryImpl(
    private val authRemoteSource: AuthRemoteDataSource,
    private val localDateTimeSerializer: LocalDateTimeSerializer,
    private val authPreferenceStore: AuthPreferenceStore,
    private val dispatchers: AppCoroutineDispatchers,
    private val loggedInUserSerializer: LoggedInUserSerializer,
    private val loggedInUserMapper: LoginResponseUserToLoggedInUser
) : AuthRepository {
    companion object {
        private const val EMAIL_EXISTS_ERROR_CODE = "userdetail-001"
        private const val PHONE_EXISTS_ERROR_CODE = "userdetail-002"
    }

    private suspend fun performLoginWithDataSource(request: LoginRequest): Either<DataSourceException, LoggedInUser> {
        return withContext(dispatchers.io) {
            either {
                val response = authRemoteSource.login(request).bind()
                val now = LocalDateTime.now()
                val serializedLoginTime = localDateTimeSerializer.serialize(now).bind()
                val loggedInUser = loggedInUserMapper.map(response.user)
                val serializedUser = loggedInUserSerializer(loggedInUser).bind()
                authPreferenceStore.storeLoginDetails(
                    response.accessToken,
                    response.refreshToken,
                    serializedLoginTime,
                    serializedUser
                )
                loggedInUser
            }
        }
    }

    override suspend fun login(request: LoginRequest): Either<LoginException, LoggedInUser> {
        return performLoginWithDataSource(request).mapLeft {
            when (it) {
                is ApiException -> {
                    when (it.statusCode) {
                        HttpURLConnection.HTTP_UNAUTHORIZED -> InvalidCredentialsException
                        HttpURLConnection.HTTP_BAD_REQUEST -> ValidationException
                        else -> UnknownLoginException
                    }
                }
                else -> UnknownLoginException
            }
        }
    }


    override suspend fun register(request: RegisterRequest): Result<LoggedInUser> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshToken(): Result<Unit> {
        TODO("Not yet implemented")
    }
}