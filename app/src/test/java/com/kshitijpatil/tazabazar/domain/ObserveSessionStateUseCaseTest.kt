package com.kshitijpatil.tazabazar.domain

import app.cash.turbine.test
import arrow.core.Either
import arrow.core.rightIfNotNull
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.api.dto.LoginRequest
import com.kshitijpatil.tazabazar.api.dto.RegisterRequest
import com.kshitijpatil.tazabazar.data.*
import com.kshitijpatil.tazabazar.model.AuthConfiguration
import com.kshitijpatil.tazabazar.model.LoggedInUser
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.threeten.bp.LocalDateTime


class ObserveSessionStateUseCaseTest {
    private val scope = TestCoroutineScope()
    private val testDispatcher = TestCoroutineDispatcher()
    private lateinit var observeSessionStateUseCase: ObserveSessionStateUseCase

    @Test
    fun happyPath_shouldEmitLoggedInState() {
        val repo = FakeAuthRepository(
            authConfiguration = FakeSession.config,
            loggedInAt = FakeSession.validLoginTime,
            loggedInUser = FakeSession.user,
            accessTokenFlow = flowOf(FakeSession.accessToken)
        )
        observeSessionStateUseCase = provideUseCase(repo)
        scope.runBlockingTest {
            observeSessionStateUseCase().test {
                val expectedState = awaitItem()
                assertThat(expectedState).isInstanceOf(SessionState.LoggedIn::class.java)
                expectedState as SessionState.LoggedIn
                assertThat(expectedState.user).isEqualTo(FakeSession.user)
            }
        }
    }

    @Test
    fun observeSessionState_whenLoggedInAtIsNull_shouldEmitLoggedOut() {
        val repo = FakeAuthRepository(
            authConfiguration = FakeSession.config,
            loggedInAt = null,
            loggedInUser = null,
            accessTokenFlow = flowOf()
        )
        observeSessionStateUseCase = provideUseCase(repo)
        scope.runBlockingTest {
            observeSessionStateUseCase().test {
                val expectedState = awaitItem()
                assertThat(expectedState).isInstanceOf(SessionState.LoggedOut::class.java)
            }
        }
    }

    @Test
    fun observeSessionState_whenAuthConfigurationIsNull_shouldEmitUndefined() {
        val repo = FakeAuthRepository(
            authConfiguration = null,
            loggedInAt = FakeSession.validLoginTime,
            loggedInUser = FakeSession.user,
            accessTokenFlow = flowOf(FakeSession.accessToken)
        )
        observeSessionStateUseCase = provideUseCase(repo)
        scope.runBlockingTest {
            observeSessionStateUseCase().test {
                val expectedState = awaitItem()
                assertThat(expectedState).isInstanceOf(SessionState.Undefined::class.java)
            }
        }
    }

    @Test
    fun observeSessionState_whenTokenExpired_shouldEmitSessionExpired() {
        val repo = FakeAuthRepository(
            authConfiguration = FakeSession.config,
            loggedInAt = FakeSession.expiredLoginTime,
            loggedInUser = FakeSession.user,
            accessTokenFlow = flowOf(FakeSession.accessToken)
        )
        observeSessionStateUseCase = provideUseCase(repo)
        scope.runBlockingTest {
            observeSessionStateUseCase().test {
                val expectedState = awaitItem()
                assertThat(expectedState).isInstanceOf(SessionState.SessionExpired::class.java)
            }
        }
    }

    @Test
    fun observeSessionState_whenTokenValidAndLoggedInUserNull_shouldEmitUndefined() {
        val repo = FakeAuthRepository(
            authConfiguration = FakeSession.config,
            loggedInAt = FakeSession.validLoginTime,
            loggedInUser = null,
            accessTokenFlow = flowOf(FakeSession.accessToken)
        )
        observeSessionStateUseCase = provideUseCase(repo)
        scope.runBlockingTest {
            observeSessionStateUseCase().test {
                val expectedState = awaitItem()
                assertThat(expectedState).isInstanceOf(SessionState.Undefined::class.java)
            }
        }
    }

    @Test
    fun observeSessionState_whenTokenChangesToNull_shouldEmitLoggedOut() {
        val tokenDownstream = Channel<String?>(capacity = Channel.CONFLATED)
        val repo = FakeAuthRepository(
            authConfiguration = FakeSession.config,
            loggedInAt = FakeSession.validLoginTime,
            loggedInUser = FakeSession.user,
            accessTokenFlow = tokenDownstream.consumeAsFlow()
        )
        observeSessionStateUseCase = provideUseCase(repo)
        scope.runBlockingTest {
            observeSessionStateUseCase().test {
                // Initial State
                assertThat(awaitItem()).isInstanceOf(SessionState.LoggedIn::class.java)
                tokenDownstream.send(null)
                val expectedState = awaitItem()
                assertThat(expectedState).isInstanceOf(SessionState.LoggedOut::class.java)
            }
        }
    }

    @Test
    fun observeSessionState_whenTokenChangesToAnyNonNull_shouldEmitSessionExpiredAfterDelay() {
        val tokenDownstream = Channel<String?>(capacity = Channel.CONFLATED)
        val repo = FakeAuthRepository(
            authConfiguration = FakeSession.config,
            loggedInAt = FakeSession.validLoginTime,
            loggedInUser = FakeSession.user,
            accessTokenFlow = tokenDownstream.consumeAsFlow()
        )
        observeSessionStateUseCase = provideUseCase(repo)
        scope.runBlockingTest {
            observeSessionStateUseCase().test {
                // Initial State
                assertThat(awaitItem()).isInstanceOf(SessionState.LoggedIn::class.java)
                tokenDownstream.send(FakeSession.accessToken)
                advanceUntilIdle()
                val expectedState = awaitItem()
                assertThat(expectedState).isInstanceOf(SessionState.SessionExpired::class.java)
            }
        }
    }

    private fun provideUseCase(repo: AuthRepository): ObserveSessionStateUseCase {
        return ObserveSessionStateUseCase(scope, testDispatcher, repo)
    }

    object FakeSession {
        val config = AuthConfiguration(4)
        val validLoginTime = LocalDateTime.now()
        val accessToken = "fake-token"
        val expiredLoginTime = LocalDateTime.now().minusMinutes(config.tokenExpiryMinutes.toLong())
        val user = LoggedInUser(
            email = "fake-email",
            fullName = "fake-name",
            phone = "fake-phone",
            emailVerified = false,
            phoneVerified = false
        )
    }
}

class FakeAuthRepository(
    private val authConfiguration: AuthConfiguration?,
    private val loggedInAt: LocalDateTime?,
    private val loggedInUser: LoggedInUser?,
    private val accessTokenFlow: Flow<String?>
) : AuthRepository {
    override suspend fun logout() {
        TODO("Not yet implemented")
    }

    override suspend fun refreshToken() {
        TODO("Not yet implemented")
    }

    override suspend fun getAuthConfiguration(): AuthConfiguration {
        return authConfiguration ?: throw IllegalStateException("UnInitialized")
    }

    override suspend fun login(request: LoginRequest): Either<LoginException, LoggedInUser> {
        TODO("Not yet implemented")
    }

    override suspend fun register(request: RegisterRequest): Either<RegisterException, LoggedInUser> {
        TODO("Not yet implemented")
    }

    override suspend fun storeLoginDetails(
        accessToken: String,
        refreshToken: String,
        loggedInAt: LocalDateTime,
        user: LoggedInUser
    ): Either<DataSourceException, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun getLoggedInAt(): Either<DataSourceException, LocalDateTime> {
        return loggedInAt.rightIfNotNull { NoDataFoundException }
    }

    override suspend fun getLastLoggedInUsername(): Either<DataSourceException, String> {
        TODO("Not yet implemented")
    }

    override suspend fun getRefreshToken(): Either<DataSourceException, String> {
        TODO("Not yet implemented")
    }

    override suspend fun clearRefreshToken() {
        TODO("Not yet implemented")
    }

    override suspend fun clearUserDetails() {
        TODO("Not yet implemented")
    }

    override fun observeAccessToken() = accessTokenFlow

    override suspend fun getAccessToken(): Either<DataSourceException, String> {
        TODO("Not yet implemented")
    }

    override suspend fun getLoggedInUser(): Either<DataSourceException, LoggedInUser> {
        return loggedInUser.rightIfNotNull { NoDataFoundException }
    }

    override suspend fun storeAccessToken(token: String): Either<DataSourceException, Unit> {
        TODO("Not yet implemented")
    }

    override suspend fun updateLoggedInAt(loginTime: LocalDateTime): Either<DataSourceException, Unit> {
        TODO("Not yet implemented")
    }

}