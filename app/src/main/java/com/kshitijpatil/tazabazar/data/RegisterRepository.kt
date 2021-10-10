package com.kshitijpatil.tazabazar.data

import arrow.core.Either
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import com.kshitijpatil.tazabazar.data.mapper.LoginResponseUserToLoggedInUser
import com.kshitijpatil.tazabazar.data.mapper.ResponseBodyToApiErrorMapper
import com.kshitijpatil.tazabazar.data.network.AuthRemoteDataSource
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.net.HttpURLConnection

interface RegisterRepository {
    suspend fun register(request: RegisterRequest): Either<RegisterException, LoggedInUser>
}

sealed class RegisterException
object PhoneExistsException : RegisterException()
object UsernameExistsException : RegisterException()
object UnknownRegisterException : RegisterException()

class RegisterRepositoryImpl(
    private val authRemoteSource: AuthRemoteDataSource,
    private val errorBodyDecoder: ResponseBodyToApiErrorMapper,
    private val loggedInUserMapper: LoginResponseUserToLoggedInUser,
    private val dispatchers: AppCoroutineDispatchers
) : RegisterRepository {
    companion object {
        private const val EMAIL_EXISTS_ERROR_CODE = "userdetail-001"
        private const val PHONE_EXISTS_ERROR_CODE = "userdetail-002"
    }

    override suspend fun register(request: RegisterRequest): Either<RegisterException, LoggedInUser> {
        return withContext(dispatchers.io) {
            authRemoteSource.register(request).bimap(
                leftOperation = { mapIfApiException(it) ?: UnknownRegisterException },
                rightOperation = loggedInUserMapper::map
            )
        }
    }

    private fun mapIfApiException(ex: DataSourceException): RegisterException? {
        if (ex !is ApiException) return null
        if (ex.statusCode != HttpURLConnection.HTTP_BAD_REQUEST) return null
        if (ex.errorBody == null) {
            Timber.d("register: Error body was null")
            return null
        }
        val apiError = errorBodyDecoder.map(ex.errorBody)
        if (apiError.isLeft()) return null
        apiError as Either.Right
        return mapRegisterExceptionBy(apiError.value.error)
    }

    private fun mapRegisterExceptionBy(errorCode: String): RegisterException? {
        return when (errorCode) {
            PHONE_EXISTS_ERROR_CODE -> PhoneExistsException
            EMAIL_EXISTS_ERROR_CODE -> UsernameExistsException
            else -> null
        }
    }
}