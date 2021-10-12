package com.kshitijpatil.tazabazar.data

import arrow.core.Either
import arrow.core.computations.either
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStore
import com.kshitijpatil.tazabazar.data.network.AuthRemoteDataSource
import com.kshitijpatil.tazabazar.domain.Result
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.HttpURLConnection

interface AuthRepository {
    suspend fun login(request: LoginRequest): Either<LoginException, LoggedInUser>
    suspend fun logout()
    suspend fun register(request: RegisterRequest): Either<RegisterException, LoggedInUser>
    suspend fun refreshToken(): Result<Unit>
}

class AuthRepositoryImpl(
    private val registerRepository: RegisterRepository,
    private val loginRepository: LoginRepository,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val authPreferenceStore: AuthPreferenceStore,
    private val dispatchers: AppCoroutineDispatchers
) : AuthRepository {
    override suspend fun login(request: LoginRequest): Either<LoginException, LoggedInUser> {
        return loginRepository.login(request)
    }

    override suspend fun logout() {
        Timber.d("logout called")
        withContext(dispatchers.io) {
            val accessToken = authPreferenceStore.getAccessToken()
            if (accessToken == null) {
                Timber.d("Access token not found, returning..")
                return@withContext
            }
            when (val response = authRemoteDataSource.logout(accessToken)) {
                is Either.Left -> {
                    throw Exception("Internal DataSource Error: ${response.value}")
                }
                is Either.Right -> {
                    authPreferenceStore.clearUserDetails()
                }
            }
        }
    }

    override suspend fun register(request: RegisterRequest): Either<RegisterException, LoggedInUser> {
        return registerRepository.register(request)
    }

    override suspend fun refreshToken(): Result<Unit> {
        return withContext(dispatchers.io) {
            either<DataSourceException, String> {
                val token = authPreferenceStore.getRefreshToken().bind()
                val accessToken = authRemoteDataSource.refreshToken(token).bind()
                authPreferenceStore.storeAccessToken(accessToken)
                accessToken
            }.fold(
                ifLeft = {
                    if (it is ApiException && it.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                        Timber.d("Stored refreshToken was invalid")
                        authPreferenceStore.clearRefreshToken()
                    }
                    it.logExceptionsForRefreshToken()
                    // Map any exception as generic error for Presentation Layer
                    Result.Error(Exception("Session Expired!"))
                },
                ifRight = { Result.Success(Unit) }
            )
        }
    }

    private fun DataSourceException.logExceptionsForRefreshToken() {
        val tag = "refresh-token"
        when (this) {
            NoDataFoundException -> {
                Timber.d("$tag: No previously stored refresh-token found")
            }
            PreferenceStorageException -> {
                Timber.d("$tag: I/O Error with Preference DataStore")
            }
            is UnknownException -> {
                Timber.e(ex, "$tag: Unhandled exception")
            }
            else -> {
            }
        }
    }
}