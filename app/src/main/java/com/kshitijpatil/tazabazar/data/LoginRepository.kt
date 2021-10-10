package com.kshitijpatil.tazabazar.data

import arrow.core.Either
import arrow.core.computations.either
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStore
import com.kshitijpatil.tazabazar.data.mapper.LoginResponseUserToLoggedInUser
import com.kshitijpatil.tazabazar.data.network.AuthRemoteDataSource
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import java.net.HttpURLConnection

interface LoginRepository {
    suspend fun login(request: LoginRequest): Either<LoginException, LoggedInUser>
}

sealed class LoginException
object InvalidCredentialsException : LoginException()
object ValidationException : LoginException()
object UnknownLoginException : LoginException()


class LoginRepositoryImpl(
    private val authRemoteSource: AuthRemoteDataSource,
    private val dispatchers: AppCoroutineDispatchers,
    private val loggedInUserMapper: LoginResponseUserToLoggedInUser,
    private val authPreferenceStore: AuthPreferenceStore,
) : LoginRepository {

    private fun mapIfApiException(ex: DataSourceException): LoginException? {
        if (ex !is ApiException) return null
        return when (ex.statusCode) {
            HttpURLConnection.HTTP_UNAUTHORIZED -> InvalidCredentialsException
            HttpURLConnection.HTTP_BAD_REQUEST -> ValidationException
            else -> null
        }
    }

    override suspend fun login(request: LoginRequest): Either<LoginException, LoggedInUser> {
        return withContext(dispatchers.io) {
            either<DataSourceException, LoggedInUser> {
                val response = authRemoteSource.login(request).bind()
                val now = LocalDateTime.now()
                val loggedInUser = loggedInUserMapper.map(response.user)
                authPreferenceStore.storeLoginDetails(
                    response.accessToken,
                    response.refreshToken,
                    now,
                    loggedInUser
                )
                loggedInUser
            }.mapLeft {
                mapIfApiException(it) ?: UnknownLoginException
            }
        }
    }
}