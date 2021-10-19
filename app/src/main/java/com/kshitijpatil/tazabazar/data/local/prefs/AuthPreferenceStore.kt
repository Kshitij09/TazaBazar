package com.kshitijpatil.tazabazar.data.local.prefs

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.flatten
import arrow.core.leftIfNull
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.NoDataFoundException
import com.kshitijpatil.tazabazar.data.PreferenceStorageException
import com.kshitijpatil.tazabazar.data.UnknownException
import com.kshitijpatil.tazabazar.data.mapper.EitherStringSerializer
import com.kshitijpatil.tazabazar.model.LoggedInUser
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.threeten.bp.LocalDateTime
import timber.log.Timber
import java.io.IOException

interface AuthPreferenceStore {
    suspend fun storeLoginDetails(
        accessToken: String,
        refreshToken: String,
        loggedInAt: LocalDateTime,
        user: LoggedInUser
    ): Either<DataSourceException, Unit>

    suspend fun getLoggedInAt(): Either<DataSourceException, LocalDateTime>
    suspend fun getLastLoggedInUsername(): Either<DataSourceException, String>
    suspend fun getRefreshToken(): Either<DataSourceException, String>
    suspend fun clearRefreshToken()
    suspend fun clearUserDetails()
    suspend fun getAccessToken(): Either<DataSourceException, String>
    suspend fun getLoggedInUser(): Either<DataSourceException, LoggedInUser>
    suspend fun storeAccessToken(token: String): Either<DataSourceException, Unit>
    suspend fun updateLoggedInAt(loginTime: LocalDateTime): Either<DataSourceException, Unit>
}

class AuthPreferenceStoreImpl(
    private val preferenceStorage: PreferenceStorage,
    private val localDateTimeSerializer: EitherStringSerializer<LocalDateTime>,
    private val loggedInUserSerializer: EitherStringSerializer<LoggedInUser>,
    private val dispatcher: CoroutineDispatcher
) : AuthPreferenceStore {

    private suspend fun storeDetailsToPreferenceStorage(
        accessToken: String,
        refreshToken: String,
        username: String,
        serializedLoginTime: String,
        serializedUser: String
    ): Either<DataSourceException, Unit> {
        return withContext(dispatcher) {
            storeCatching {
                setAccessToken(accessToken)
                setRefreshToken(refreshToken)
                setLastLoggedIn(serializedLoginTime)
                setUserDetails(serializedUser)
                setLastLoggedInUsername(username)
            }
        }
    }

    override suspend fun storeLoginDetails(
        accessToken: String,
        refreshToken: String,
        loggedInAt: LocalDateTime,
        user: LoggedInUser
    ): Either<DataSourceException, Unit> {
        return either {
            val serializedLoginTime = localDateTimeSerializer.serialize(loggedInAt).bind()
            val serializedUser = loggedInUserSerializer.serialize(user).bind()
            storeDetailsToPreferenceStorage(
                accessToken,
                refreshToken,
                user.email,
                serializedLoginTime,
                serializedUser,
            )
        }
    }

    override suspend fun getRefreshToken(): Either<DataSourceException, String> {
        return withContext(dispatcher) {
            getNullableCatching { refreshToken.first() }
        }
    }

    override suspend fun clearRefreshToken() {
        preferenceStorage.setRefreshToken(null)
    }

    override suspend fun clearUserDetails() {
        clearRefreshToken()
        preferenceStorage.setAccessToken(null)
        preferenceStorage.setUserDetails(null)
        preferenceStorage.setLastLoggedIn(null)
    }

    override suspend fun getAccessToken(): Either<DataSourceException, String> {
        return withContext(dispatcher) {
            getNullableCatching { accessToken.first() }
        }
    }

    override suspend fun getLoggedInAt(): Either<DataSourceException, LocalDateTime> {
        return withContext(dispatcher) {
            getNullableCatching { loggedInAt.first() }
                .map { localDateTimeSerializer.deserialize(it) }
                .flatten()
        }
    }

    override suspend fun getLoggedInUser(): Either<DataSourceException, LoggedInUser> {
        return withContext(dispatcher) {
            getNullableCatching { userDetails.first() }
                .map { loggedInUserSerializer.deserialize(it) }
                .flatten()
        }
    }

    override suspend fun storeAccessToken(token: String): Either<DataSourceException, Unit> {
        return storeCatching { setAccessToken(token) }
    }

    override suspend fun updateLoggedInAt(loginTime: LocalDateTime): Either<DataSourceException, Unit> {
        return either {
            val serializedLoginTime = localDateTimeSerializer.serialize(loginTime).bind()
            storeCatching { setLastLoggedIn(serializedLoginTime) }.bind()
        }
    }

    override suspend fun getLastLoggedInUsername(): Either<DataSourceException, String> {
        return withContext(dispatcher) {
            getNullableCatching { lastLoggedInUsername.first() }
        }
    }

    private fun Throwable.toDataSourceException(): DataSourceException {
        return if (this is IOException) {
            Timber.d(this, "I/O Error while storing the details")
            PreferenceStorageException
        } else {
            Timber.e(this)
            UnknownException(this)
        }
    }

    /**
     * Maps any nullable getter when returns null to [NoDataFoundException]
     * while mapping caught exceptions to [DataSourceException]
     * */
    private suspend fun <T> getNullableCatching(
        getter: suspend PreferenceStorage.() -> T?
    ): Either<DataSourceException, T> {
        return Either.catch { getter(preferenceStorage) }
            .mapLeft { it.toDataSourceException() }
            .leftIfNull { NoDataFoundException }
    }

    private suspend fun storeCatching(setter: suspend PreferenceStorage.() -> Unit): Either<DataSourceException, Unit> {
        return withContext(dispatcher) {
            Either.catch {
                setter(preferenceStorage)
            }.mapLeft { it.toDataSourceException() }
        }
    }
}