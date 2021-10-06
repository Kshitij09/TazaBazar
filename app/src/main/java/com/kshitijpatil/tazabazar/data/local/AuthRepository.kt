package com.kshitijpatil.tazabazar.data.local

import com.kshitijpatil.tazabazar.api.AuthApi
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import com.kshitijpatil.tazabazar.data.local.prefs.PreferenceStorage
import com.kshitijpatil.tazabazar.data.mapper.LoginResponseUserToLoggedInUser
import com.kshitijpatil.tazabazar.domain.Result
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import com.kshitijpatil.tazabazar.util.LocalDateTimeConverter
import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.withContext
import org.threeten.bp.DateTimeException
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import java.net.HttpURLConnection

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoggedInUser>
    suspend fun register(request: RegisterRequest): Result<LoggedInUser>
    suspend fun refreshToken(): Result<Unit>

    class ValidationException() : Exception()
    class InvalidCredentialsException() : Exception()
    class UnknownException() : Exception()
}

class AuthRepositoryImpl(
    private val api: AuthApi,
    private val preferenceStorage: PreferenceStorage,
    private val dispatchers: AppCoroutineDispatchers,
    private val jsonAdapter: JsonAdapter<LoggedInUser>,
    private val loggedInUserMapper: LoginResponseUserToLoggedInUser
) : AuthRepository {
    override suspend fun login(request: LoginRequest): Result<LoggedInUser> {
        return withContext(dispatchers.io) {
            try {
                val apiResponse = api.login(request)
                if (apiResponse.isSuccessful) {
                    Timber.d("login: Login Successful")
                    val now = LocalDateTime.now()
                    val responseBody = apiResponse.body()
                    if (responseBody == null) {
                        Timber.e("login: Response body was null")
                        Result.Error(AuthRepository.UnknownException())
                    } else {
                        Timber.d("login: Storing user details")
                        val loggedInUser: LoggedInUser
                        with(preferenceStorage) {
                            setAccessToken(responseBody.accessToken)
                            setRefreshToken(responseBody.refreshToken)
                            setLastLoggedIn(LocalDateTimeConverter.fromLocalDateTime(now))
                            loggedInUser = loggedInUserMapper.map(responseBody.user)
                            setUserDetails(jsonAdapter.toJson(loggedInUser))
                        }
                        Result.Success(loggedInUser)
                    }
                } else {
                    when {
                        apiResponse.code() == HttpURLConnection.HTTP_UNAUTHORIZED -> {
                            Result.Error(AuthRepository.InvalidCredentialsException())
                        }
                        apiResponse.code() == HttpURLConnection.HTTP_BAD_REQUEST -> {
                            Result.Error(AuthRepository.ValidationException())
                        }
                        else -> {
                            Timber.d("login: Unknown Error with api-response: $apiResponse")
                            Result.Error(AuthRepository.UnknownException())
                        }
                    }
                }
            } catch (e: DateTimeException) {
                Timber.e(e, "login: DateTimeException")
                Result.Error(e)
            } catch (e: Throwable) {
                Timber.e(e, "login: UnhandledException")
                Result.Error(Exception(e))
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