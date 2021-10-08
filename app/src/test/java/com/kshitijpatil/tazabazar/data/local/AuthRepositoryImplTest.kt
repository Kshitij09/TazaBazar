package com.kshitijpatil.tazabazar.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.LoginResponse
import com.kshitijpatil.tazabazar.data.ApiException
import com.kshitijpatil.tazabazar.data.DataSourceException
import com.kshitijpatil.tazabazar.data.InvalidCredentialsException
import com.kshitijpatil.tazabazar.data.ValidationException
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStoreImpl
import com.kshitijpatil.tazabazar.data.local.prefs.PreferenceStorage
import com.kshitijpatil.tazabazar.data.mapper.LoginResponseUserToLoggedInUser
import com.kshitijpatil.tazabazar.data.network.AuthRemoteDataSource
import com.kshitijpatil.tazabazar.data.util.DefaultLocalDateTimeSerializer
import com.kshitijpatil.tazabazar.data.util.LoggedInUserSerializer
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.test.util.FakePreferenceStorage
import com.kshitijpatil.tazabazar.test.util.MainCoroutineRule
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody
import org.junit.Rule
import org.junit.Test
import java.net.HttpURLConnection

class AuthRepositoryImplTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private val testDispatcher = coroutineRule.testDispatcher
    private val testAppDispatchers = AppCoroutineDispatchers(
        testDispatcher, testDispatcher, testDispatcher
    )
    private val loggedInUserMapper = LoginResponseUserToLoggedInUser()
    private val dateTimeSerializer = DefaultLocalDateTimeSerializer()
    private val loggedInUserSerializer = FakeLoggedInUserSerializer()
    private lateinit var repo: AuthRepository

    @Test
    fun login_happyPath() {
        val preferenceStorage = FakePreferenceStorage()
        val expectedResponse = AuthSession1.loginResponse
        val expectedSerializedUser = loggedInUserSerializer.serialize(AuthSession1.loggedInUser)
        assert(expectedSerializedUser.isRight())
        expectedSerializedUser as Either.Right
        repo = provideRepo(
            SucceedingAuthDataSource(expectedResponse),
            preferenceStorage
        )

        testDispatcher.runBlockingTest {
            val result = repo.login(AuthSession1.loginRequest)
            assert(result is Either.Right)
            result as Either.Right
            assertThat(result.value).isEqualTo(AuthSession1.loggedInUser)
            assertThat(preferenceStorage.loggedInAt.first()).isNotEmpty()
            assertThat(preferenceStorage.userDetails.first()).isEqualTo(expectedSerializedUser.value)
            assertThat(preferenceStorage.refreshToken.first()).isEqualTo(expectedResponse.refreshToken)
            assertThat(preferenceStorage.accessToken.first()).isEqualTo(expectedResponse.accessToken)
        }
    }

    @Test
    fun login_invalidCredentials() {
        repo = provideRepo(HttpFailureAuthDataSource(HttpURLConnection.HTTP_UNAUTHORIZED))

        testDispatcher.runBlockingTest {
            val result = repo.login(AuthSession1.loginRequest)
            assert(result.isLeft())
            result as Either.Left
            assert(result.value is InvalidCredentialsException)
        }
    }

    @Test
    fun login_ValidationError() {
        repo = provideRepo(HttpFailureAuthDataSource(HttpURLConnection.HTTP_BAD_REQUEST))

        testDispatcher.runBlockingTest {
            val result = repo.login(AuthSession1.loginRequest)
            assert(result.isLeft())
            result as Either.Left
            assert(result.value is ValidationException)
        }
    }

    private fun provideRepo(
        authDataSource: AuthRemoteDataSource,
        preferenceStorage: PreferenceStorage = FakePreferenceStorage()
    ): AuthRepositoryImpl {
        return AuthRepositoryImpl(
            authDataSource,
            dateTimeSerializer,
            AuthPreferenceStoreImpl(preferenceStorage),
            testAppDispatchers,
            loggedInUserSerializer,
            loggedInUserMapper
        )
    }
}

object AuthSession1 {
    val user = LoginResponse.User("user1@test.com", "1111111111", "User 1", false, false)
    val loggedInUser = LoggedInUser(
        user.username,
        user.fullName,
        user.phone,
        user.emailVerified,
        user.phoneVerified
    )
    val loginResponse = LoginResponse("access-token", emptyList(), "refresh-token", user)
    val loginRequest = LoginRequest(user.username, "anything")
}

class SucceedingAuthDataSource(private val loginResponse: LoginResponse) : AuthRemoteDataSource {
    override suspend fun login(request: LoginRequest): Either<DataSourceException, LoginResponse> {
        return loginResponse.right()
    }
}

class HttpFailureAuthDataSource(
    private val statusCode: Int,
    private val errorBody: ResponseBody? = null
) : AuthRemoteDataSource {
    override suspend fun login(request: LoginRequest): Either<DataSourceException, LoginResponse> {
        return ApiException(statusCode, errorBody).left()
    }

}

class FakeLoggedInUserSerializer : LoggedInUserSerializer() {
    override fun serialize(user: LoggedInUser): Either<DataSourceException, String> {
        return user.toString().right()
    }
}