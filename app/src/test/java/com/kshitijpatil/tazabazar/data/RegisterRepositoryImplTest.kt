package com.kshitijpatil.tazabazar.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.api.dto.ApiError
import com.kshitijpatil.tazabazar.api.dto.LoginResponse
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import com.kshitijpatil.tazabazar.data.mapper.ErrorBodyDecoder
import com.kshitijpatil.tazabazar.data.mapper.LoginResponseUserToLoggedInUser
import com.kshitijpatil.tazabazar.data.mapper.ResponseBodyToApiErrorMapper
import com.kshitijpatil.tazabazar.data.network.AuthRemoteDataSource
import com.kshitijpatil.tazabazar.model.LoggedInUser
import com.kshitijpatil.tazabazar.test.util.MainCoroutineRule
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Rule
import org.junit.Test
import java.net.HttpURLConnection

class RegisterRepositoryImplTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineRule = MainCoroutineRule()

    private val testDispatcher = coroutineRule.testDispatcher
    private val testAppDispatchers = AppCoroutineDispatchers(
        testDispatcher, testDispatcher, testDispatcher
    )

    private val loggedInUserMapper = LoginResponseUserToLoggedInUser()
    private lateinit var repo: RegisterRepository
    private val moshi = Moshi.Builder().build()
    private val apiErrorAdapter: JsonAdapter<ApiError> = moshi.adapter(ApiError::class.java)
    private val errorBodyDecoder = ErrorBodyDecoder(apiErrorAdapter)

    @Test
    fun register_happyPath() {
        repo = provideRepo(SucceedingAuthDataSource(registerResponse = RegisterSession.user))
        testDispatcher.runBlockingTest {
            val response = repo.register(RegisterSession.request)
            assert(response.isRight())
            response as Either.Right
            assertThat(response.value).isEqualTo(RegisterSession.loggedInUser)
        }
    }

    @Test
    fun register_usernameExists() {
        val usernameExistsJson = apiErrorAdapter.toJson(RegisterSession.usernameExistsError)
        val userExistsBody = usernameExistsJson.toResponseBody("application/json".toMediaType())
        repo = provideRepo(
            HttpFailureAuthDataSource(
                HttpURLConnection.HTTP_BAD_REQUEST,
                userExistsBody
            )
        )

        testDispatcher.runBlockingTest {
            val response = repo.register(RegisterSession.request)
            assertThat(response).isInstanceOf(Either.Left::class.java)
            response as Either.Left
            assertThat(response.value).isEqualTo(UsernameExistsException)
        }
    }

    @Test
    fun register_phoneExists() {
        val phoneExistsJson = apiErrorAdapter.toJson(RegisterSession.phoneExistsError)
        val phoneExistsBody = phoneExistsJson.toResponseBody("application/json".toMediaType())
        repo = provideRepo(
            HttpFailureAuthDataSource(
                HttpURLConnection.HTTP_BAD_REQUEST,
                phoneExistsBody
            )
        )

        testDispatcher.runBlockingTest {
            val response = repo.register(RegisterSession.request)
            assertThat(response).isInstanceOf(Either.Left::class.java)
            response as Either.Left
            assertThat(response.value).isEqualTo(PhoneExistsException)
        }
    }

    private fun provideRepo(authDataSource: AuthRemoteDataSource): RegisterRepository {
        return RegisterRepositoryImpl(
            authDataSource,
            errorBodyDecoder,
            loggedInUserMapper,
            testAppDispatchers
        )
    }
}

object RegisterSession {
    val request = RegisterRequest("username", "1234", "Full Name", "9999009976")
    val user = LoginResponse.User(
        username = request.username,
        phone = request.phone,
        fullName = request.fullName,
        emailVerified = false,
        phoneVerified = false
    )
    val loggedInUser = LoggedInUser(
        user.username,
        user.fullName,
        user.phone,
        user.emailVerified,
        user.phoneVerified
    )
    val usernameExistsError = ApiError("userdetail-001", "Username exists", null, 400, "timestamp")
    val phoneExistsError = ApiError("userdetail-002", "Phone exists", null, 400, "timestamp")
}

class FakeErrorBodyDecoder(private val feedApiError: ApiError? = null) :
    ResponseBodyToApiErrorMapper {
    override fun map(from: ResponseBody): Either<SerializationException, ApiError> {
        return feedApiError?.right() ?: SerializationException.left()
    }
}