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
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.threeten.bp.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger


class ObserveSessionStateUseCaseTest {
    private val testDispatcher = TestCoroutineDispatcher()
    private val scope = TestCoroutineScope(testDispatcher)
    private val dispatchers =
        AppCoroutineDispatchers(testDispatcher, testDispatcher, testDispatcher)
    private lateinit var observeSessionStateUseCase: ObserveSessionStateUseCase

    @Test
    fun happyPath_shouldEmitLoggedInFollowedBySessionExpired() {
        val repo = FakeAuthRepository(
            authConfiguration = FakeSession.config,
            loggedInAtFlow = flowOf(FakeSession.validLoginTime),
            loggedInUser = FakeSession.user,
            accessToken = FakeSession.accessToken
        )
        observeSessionStateUseCase = provideUseCase(repo)
        scope.runBlockingTest {
            observeSessionStateUseCase().test {
                val expectedState = awaitItem()
                assertThat(expectedState).isInstanceOf(SessionState.LoggedIn::class.java)
                expectedState as SessionState.LoggedIn
                assertThat(expectedState.user).isEqualTo(FakeSession.user)
                advanceUntilIdle()
                assertThat(awaitItem()).isInstanceOf(SessionState.SessionExpired::class.java)
            }
        }
    }

    @Test
    fun observeSessionState_whenLoggedInAtIsNull_shouldEmitLoggedOut() {
        val repo = FakeAuthRepository(
            authConfiguration = FakeSession.config,
            loggedInAtFlow = flowOf(null),
            loggedInUser = null,
            accessToken = null
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
            loggedInAtFlow = flowOf(FakeSession.validLoginTime),
            loggedInUser = FakeSession.user,
            accessToken = FakeSession.accessToken
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
    fun observeSessionState_whenAuthConfigurationChangesToNonNull_shouldUpdateSessionState() {
        val repo = FakeAuthRepository(
            authConfiguration = null,
            loggedInAtFlow = flowOf(FakeSession.validLoginTime),
            loggedInUser = FakeSession.user,
            accessToken = FakeSession.accessToken
        )
        scope.runBlockingTest {
            observeSessionStateUseCase = ObserveSessionStateUseCase(scope, dispatchers, repo)
            observeSessionStateUseCase().test {
                assertThat(awaitItem()).isInstanceOf(SessionState.Undefined::class.java)
                advanceTimeBy(5000)
                // 1--(100)--2--(200)--3--(400)--4--(800)--5--(1600)--6--(2000)
                val failedAttempts = 6
                repo.authConfiguration = FakeSession.config
                advanceUntilIdle()
                val expectedState = awaitItem()
                assertThat(expectedState).isInstanceOf(SessionState.LoggedIn::class.java)
                val expectedCallCount = failedAttempts + 1 // +1 for success
                assertThat(repo.authConfigCallCount.get()).isEqualTo(expectedCallCount)
                expectedState as SessionState.LoggedIn
                assertThat(expectedState.user).isEqualTo(FakeSession.user)
                assertThat(awaitItem()).isInstanceOf(SessionState.SessionExpired::class.java)
            }
        }
    }

    @Test
    fun observeSessionState_whenLoggedInAtChangesToNonNull_shouldUpdateSessionState() {
        val loggedInAtFlow = flowOf(null, FakeSession.validLoginTime)
            .onEach { delay(100) }
        val repo = FakeAuthRepository(
            authConfiguration = FakeSession.config,
            loggedInAtFlow = loggedInAtFlow,
            loggedInUser = FakeSession.user,
            accessToken = FakeSession.accessToken
        )
        scope.runBlockingTest {
            observeSessionStateUseCase = ObserveSessionStateUseCase(scope, dispatchers, repo)
            observeSessionStateUseCase().test {
                // initially undefined
                assertThat(awaitItem()).isInstanceOf(SessionState.Undefined::class.java)
                // logged-out for null logged-in time
                assertThat(awaitItem()).isInstanceOf(SessionState.LoggedOut::class.java)
                // when logged-in time changes to non-null
                advanceUntilIdle()
                val expectedState = awaitItem()
                // we get LoggedIn Session State
                assertThat(expectedState).isInstanceOf(SessionState.LoggedIn::class.java)
                expectedState as SessionState.LoggedIn
                assertThat(expectedState.user).isEqualTo(FakeSession.user)
                assertThat(awaitItem()).isInstanceOf(SessionState.SessionExpired::class.java)
            }
        }
    }

    @Test
    fun observeSessionState_whenAuthConfigurationRetriesExceed_shouldRemainUndefined() {
        val repo = FakeAuthRepository(
            authConfiguration = null,
            loggedInAtFlow = flowOf(FakeSession.validLoginTime),
            loggedInUser = FakeSession.user,
            accessToken = FakeSession.accessToken
        )
        scope.runBlockingTest {
            observeSessionStateUseCase = ObserveSessionStateUseCase(scope, dispatchers, repo)
            observeSessionStateUseCase().test {
                assertThat(awaitItem()).isInstanceOf(SessionState.Undefined::class.java)
                // It takes 6 attempts to reach 2000 delay (5100 millis)
                // assuming max-attempts are 100
                // it'll take 94 * 2000 = 188000 millis finish all attempts
                advanceTimeBy(188000 + 5100L + 1000)
                // at this point, it's too late to get the configuration
                // thus, Session State won't update anymore
                repo.authConfiguration = FakeSession.config
                expectNoEvents()
            }
        }
    }

    @Test
    fun observeSessionState_whenSessionTimedOut_shouldEmitSessionExpired() {
        val repo = FakeAuthRepository(
            authConfiguration = FakeSession.config,
            loggedInAtFlow = flowOf(FakeSession.expiredLoginTime),
            loggedInUser = FakeSession.user,
            accessToken = FakeSession.accessToken
        )
        observeSessionStateUseCase = provideUseCase(repo)
        scope.runBlockingTest {
            val currentState = observeSessionStateUseCase().first()
            assertThat(currentState).isInstanceOf(SessionState.SessionExpired::class.java)
        }
    }

    @Test
    fun observeSessionState_whenSessionUnexpiredAndLoggedInUserIsNull_shouldEmitLoggedOut() {
        val repo = FakeAuthRepository(
            authConfiguration = FakeSession.config,
            loggedInAtFlow = flowOf(FakeSession.validLoginTime),
            loggedInUser = null,
            accessToken = FakeSession.accessToken
        )
        observeSessionStateUseCase = provideUseCase(repo)
        scope.runBlockingTest {
            val currentState = observeSessionStateUseCase().first()
            assertThat(currentState).isInstanceOf(SessionState.LoggedOut::class.java)
        }
    }

    @Test
    fun observeSessionState_loggedInAtChangesToNull_shouldEmitLoggedOut() {
        val repo = FakeAuthRepository(
            authConfiguration = FakeSession.config,
            loggedInAtFlow = flowOf(FakeSession.validLoginTime, null),
            loggedInUser = FakeSession.user,
            accessToken = FakeSession.accessToken
        )
        observeSessionStateUseCase = provideUseCase(repo)
        scope.runBlockingTest {
            observeSessionStateUseCase().test {
                // Initial State
                assertThat(awaitItem()).isInstanceOf(SessionState.LoggedIn::class.java)
                // when logged-in time changes to null
                assertThat(awaitItem()).isInstanceOf(SessionState.LoggedOut::class.java)
            }
        }
    }

    private fun provideUseCase(repo: AuthRepository): ObserveSessionStateUseCase {
        return ObserveSessionStateUseCase(scope, dispatchers, repo)
    }

    object FakeSession {
        val config = AuthConfiguration(4)
        val validLoginTime: LocalDateTime = LocalDateTime.now()
        const val accessToken = "fake-token"
        val expiredLoginTime: LocalDateTime =
            LocalDateTime.now().minusMinutes(config.tokenExpiryMinutes.toLong())
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
    var authConfiguration: AuthConfiguration?,
    private val loggedInAtFlow: Flow<LocalDateTime?>,
    private val loggedInUser: LoggedInUser?,
    private val accessToken: String?
) : AuthRepository {
    val authConfigCallCount = AtomicInteger(0)
    override suspend fun logout() {
        TODO("Not yet implemented")
    }

    override suspend fun refreshToken() {
        TODO("Not yet implemented")
    }

    override suspend fun getAuthConfiguration(): AuthConfiguration {
        authConfigCallCount.getAndIncrement()
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
        return loggedInAtFlow.first().rightIfNotNull { NoDataFoundException }
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

    override fun observeAccessToken() = flowOf(accessToken)

    override fun observeLoggedInAt(): Flow<Either<DataSourceException, LocalDateTime>> {
        return loggedInAtFlow.map { it.rightIfNotNull { NoDataFoundException } }
    }

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