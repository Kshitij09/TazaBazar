package com.kshitijpatil.tazabazar.data.local.prefs

import arrow.core.Either
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.PreferenceStorageException
import com.kshitijpatil.tazabazar.data.UnknownException
import java.io.IOException

interface AuthPreferenceStore {
    suspend fun storeLoginDetails(
        accessToken: String,
        refreshToken: String,
        serializedLoginTime: String,
        serializedUser: String
    ): Either<DataSourceException, Unit>
}

class AuthPreferenceStoreImpl(private val preferenceStorage: PreferenceStorage) :
    AuthPreferenceStore {
    override suspend fun storeLoginDetails(
        accessToken: String,
        refreshToken: String,
        serializedLoginTime: String,
        serializedUser: String
    ): Either<DataSourceException, Unit> {
        return Either.catch {
            with(preferenceStorage) {
                setAccessToken(accessToken)
                setRefreshToken(refreshToken)
                setLastLoggedIn(serializedLoginTime)
                setUserDetails(serializedUser)
            }
        }.mapLeft {
            if (it is IOException) PreferenceStorageException
            else UnknownException(it)
        }
    }
}