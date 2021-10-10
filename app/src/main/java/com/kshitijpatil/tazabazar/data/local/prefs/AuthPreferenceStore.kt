package com.kshitijpatil.tazabazar.data.local.prefs

import arrow.core.Either
import arrow.core.computations.either
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.PreferenceStorageException
import com.kshitijpatil.tazabazar.data.UnknownException
import com.kshitijpatil.tazabazar.data.mapper.EitherStringSerializer
import com.kshitijpatil.tazabazar.model.LoggedInUser
import kotlinx.coroutines.CoroutineDispatcher
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
        serializedLoginTime: String,
        serializedUser: String
    ): Either<DataSourceException, Unit> {
        return withContext(dispatcher) {
            Either.catch {
                with(preferenceStorage) {
                    setAccessToken(accessToken)
                    setRefreshToken(refreshToken)
                    setLastLoggedIn(serializedLoginTime)
                    setUserDetails(serializedUser)
                }
            }.mapLeft {
                if (it is IOException) {
                    Timber.d(it, "I/O Error while storing the details")
                    PreferenceStorageException
                } else {
                    Timber.e(it)
                    UnknownException(it)
                }
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
                serializedLoginTime,
                serializedUser
            )
        }
    }
}