package com.kshitijpatil.tazabazar.data

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.handleError
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStore
import com.kshitijpatil.tazabazar.data.network.AuthRemoteDataSource
import com.kshitijpatil.tazabazar.model.AuthConfiguration
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import java.net.HttpURLConnection

interface AuthRepository : LoginRepository, RegisterRepository, AuthPreferenceStore {
    suspend fun logout()
    suspend fun refreshToken()
    suspend fun getAuthConfiguration(): AuthConfiguration
}

class AuthRepositoryImpl(
    private val registerRepository: RegisterRepository,
    private val loginRepository: LoginRepository,
    private val authRemoteDataSource: AuthRemoteDataSource,
    private val authPreferenceStore: AuthPreferenceStore,
    private val dispatchers: AppCoroutineDispatchers
) : AuthRepository, AuthPreferenceStore by authPreferenceStore {
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

    override suspend fun refreshToken() {
        withContext(dispatchers.io) {
            either<DataSourceException, String> {
                val token = authPreferenceStore.getRefreshToken().bind()
                val accessToken = authRemoteDataSource.refreshToken(token).bind()
                val now = LocalDateTime.now()
                authPreferenceStore.storeAccessToken(accessToken).bind()
                authPreferenceStore.updateLoggedInAt(now)
                accessToken
            }.handleError {
                if (it is ApiException && it.statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    Timber.d("Stored refreshToken was invalid")
                    authPreferenceStore.clearRefreshToken()
                }
                it.logExceptionsForRefreshToken()
                // Map any exception as generic error for Presentation Layer
                throw Exception("Failed to refresh the access token")
            }
        }
    }

    override suspend fun getAuthConfiguration(): AuthConfiguration {
        // TODO: Fetch these from remote APIs once supported
        delay(500)
        return AuthConfiguration(tokenExpiryMinutes = 15)
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