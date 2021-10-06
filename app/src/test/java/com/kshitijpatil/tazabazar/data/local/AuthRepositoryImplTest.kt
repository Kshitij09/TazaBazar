package com.kshitijpatil.tazabazar.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.api.AuthApi
import com.kshitijpatil.tazabazar.api.dto.*
import com.kshitijpatil.tazabazar.data.mapper.LoginResponseUserToLoggedInUser
import com.kshitijpatil.tazabazar.domain.Result
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.test.util.FakePreferenceStorage
import com.kshitijpatil.tazabazar.test.util.MainCoroutineRule
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Rule
import org.junit.Test
import retrofit2.Response
import java.net.HttpURLConnection

@ExperimentalStdlibApi
class AuthRepositoryImplTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    // Overrides Dispatchers.Main used in coroutines
    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private val testDispatcher = coroutineRule.testDispatcher
    private val testAppDispatchers = AppCoroutineDispatchers(
        testDispatcher, testDispatcher, testDispatcher
    )
    private val loggedInUserMapper = LoginResponseUserToLoggedInUser()
    private val moshi = Moshi.Builder().build()

    private val jsonAdapter: JsonAdapter<LoggedInUser> = moshi.adapter()
    private lateinit var repo: AuthRepository

    @Test
    fun login_happyPath() {
        // setup
        val preferenceStorage = FakePreferenceStorage()
        val expectedResponse = AuthSession1.loginResponse
        val api = SucceedingAuthApi(loginResponse = expectedResponse)
        repo = provideAuthRepoImpl(api, preferenceStorage)

        testDispatcher.runBlockingTest {
            val result = repo.login(AuthSession1.loginRequest)
            assertThat(result).isInstanceOf(Result.Success::class.java)
            result as Result.Success
            assertThat(result.data).isEqualTo(AuthSession1.loggedInUser)
            assertThat(preferenceStorage.userDetails.first()).isEqualTo(
                jsonAdapter.toJson(
                    AuthSession1.loggedInUser
                )
            )
            assertThat(preferenceStorage.accessToken.first()).isEqualTo(expectedResponse.accessToken)
            assertThat(preferenceStorage.refreshToken.first()).isEqualTo(expectedResponse.refreshToken)
            assertThat(preferenceStorage.loggedInAt.first()).isNotEmpty()
        }
    }

    @Test
    fun login_InvalidCredentials() {
        repo = provideAuthRepoImpl(UnauthorizedAuthApi())
        testDispatcher.runBlockingTest {
            val result = repo.login(AuthSession1.loginRequest)
            assertThat(result).isInstanceOf(Result.Error::class.java)
            result as Result.Error
            assertThat(result.exception).isInstanceOf(AuthRepository.InvalidCredentialsException::class.java)
        }
    }

    @Test
    fun login_ValidationError() {
        repo = provideAuthRepoImpl(BadRequestAuthApi())
        testDispatcher.runBlockingTest {
            val result = repo.login(AuthSession1.loginRequest)
            assertThat(result).isInstanceOf(Result.Error::class.java)
            result as Result.Error
            assertThat(result.exception).isInstanceOf(AuthRepository.ValidationException::class.java)
        }
    }

    @Test
    fun login_unexpectedError() {
        repo = provideAuthRepoImpl(FakeAuthApi())
        testDispatcher.runBlockingTest {
            val result = repo.login(AuthSession1.loginRequest)
            assertThat(result).isInstanceOf(Result.Error::class.java)
            result as Result.Error
            assertThat(result.exception).hasCauseThat()
                .isInstanceOf(NotImplementedError::class.java)
        }
    }

    private fun provideAuthRepoImpl(
        api: AuthApi,
        _storage: FakePreferenceStorage? = null
    ): AuthRepositoryImpl {
        val preferenceStorage = _storage ?: FakePreferenceStorage()
        return AuthRepositoryImpl(
            api = api,
            preferenceStorage = preferenceStorage,
            dispatchers = testAppDispatchers,
            jsonAdapter = jsonAdapter,
            loggedInUserMapper = loggedInUserMapper
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
    val loginRequest = LoginRequest(AuthSession1.user.username, "anything")
}

open class FakeAuthApi : AuthApi {
    override suspend fun login(request: LoginRequest): Response<LoginResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun register(request: RegisterRequest): Response<RegisterResponse> {
        TODO("Not yet implemented")
    }

    override suspend fun refreshToken(token: String): Response<RefreshTokenResponse> {
        TODO("Not yet implemented")
    }

}

class SucceedingAuthApi(
    private val loginResponse: LoginResponse? = null,
    private val registerResponse: RegisterResponse? = null,
    private val tokenResponse: RefreshTokenResponse? = null
) : FakeAuthApi() {
    override suspend fun login(request: LoginRequest): Response<LoginResponse> {
        if (loginResponse != null) return Response.success(loginResponse)
        throw NotImplementedError("test")
    }

    override suspend fun register(request: RegisterRequest): Response<RegisterResponse> {
        if (registerResponse != null) return Response.success(registerResponse)
        throw NotImplementedError("test")
    }

    override suspend fun refreshToken(token: String): Response<RefreshTokenResponse> {
        if (tokenResponse != null) return Response.success(tokenResponse)
        throw NotImplementedError("test")
    }
}

class UnauthorizedAuthApi : FakeAuthApi() {
    override suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return Response.error(HttpURLConnection.HTTP_UNAUTHORIZED, "".toResponseBody())
    }

    override suspend fun register(request: RegisterRequest): Response<RegisterResponse> {
        return Response.error(HttpURLConnection.HTTP_UNAUTHORIZED, "".toResponseBody())
    }
}

class BadRequestAuthApi : FakeAuthApi() {
    override suspend fun login(request: LoginRequest): Response<LoginResponse> {
        return Response.error(HttpURLConnection.HTTP_BAD_REQUEST, "".toResponseBody())
    }

    override suspend fun register(request: RegisterRequest): Response<RegisterResponse> {
        return Response.error(HttpURLConnection.HTTP_BAD_REQUEST, "".toResponseBody())
    }
}