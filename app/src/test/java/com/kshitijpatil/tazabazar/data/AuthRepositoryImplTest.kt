package com.kshitijpatil.tazabazar.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStoreImpl
import com.kshitijpatil.tazabazar.data.local.prefs.PreferenceStorage
import com.kshitijpatil.tazabazar.data.mapper.LocalDateTimeSerializer
import com.kshitijpatil.tazabazar.data.network.AuthRemoteDataSource
import com.kshitijpatil.tazabazar.test.util.FakePreferenceStorage
import com.kshitijpatil.tazabazar.test.util.HttpFailureAuthDataSource
import com.kshitijpatil.tazabazar.test.util.MainCoroutineRule
import com.kshitijpatil.tazabazar.test.util.SucceedingAuthDataSource
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import com.kshitijpatil.tazabazar.util.LocalDateTimeConverter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.threeten.bp.LocalDateTime
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
    private lateinit var repo: AuthRepository

    private val localDateTimeSerializer = LocalDateTimeSerializer()

    @Test
    fun refreshToken_happyPath_shouldUpdateAccessTokenAndLoggedInAt() {
        // setup
        val preferenceStorage = FakePreferenceStorage()
        runBlocking {
            preferenceStorage.setRefreshToken(AuthSession.refreshToken)
            preferenceStorage.setLastLoggedIn(AuthSession.initialLoginTimeRaw)
        }
        val authRemoteDataSource =
            SucceedingAuthDataSource(accessToken = AuthSession.fetchedAccessToken)
        repo = provideRepo(authRemoteDataSource, preferenceStorage)

        // test
        testDispatcher.runBlockingTest {
            assertThat(repo.getLoggedInAt()).isEqualTo(AuthSession.initialLoginTimeRaw)
            repo.refreshToken()
            assertThat(preferenceStorage.accessToken.first()).isEqualTo(AuthSession.fetchedAccessToken)
            assertThat(preferenceStorage.loggedInAt.first()).isNotEqualTo(AuthSession.initialLoginTimeRaw)
        }
    }

    @Test
    fun refreshToken_whenNoStoredToken_shouldReturnError() {
        val authRemoteDataSource =
            SucceedingAuthDataSource(accessToken = AuthSession.fetchedAccessToken)
        repo = provideRepo(authRemoteDataSource)

        testDispatcher.runBlockingTest {
            val e = assertThrows(Exception::class.java) { runBlocking { repo.refreshToken() } }
            assertThat(e).hasMessageThat().contains("token not found")
        }
    }

    @Test
    fun refreshToken_whenApiReturns401_shouldResetTokenAndReturnError() {
        val preferenceStorage = FakePreferenceStorage()
        runBlocking { preferenceStorage.setRefreshToken(AuthSession.refreshToken) }
        val authRemoteDataSource =
            HttpFailureAuthDataSource(statusCode = HttpURLConnection.HTTP_UNAUTHORIZED)
        repo = provideRepo(authRemoteDataSource, preferenceStorage)

        testDispatcher.runBlockingTest {
            val e = assertThrows(Exception::class.java) { runBlocking { repo.refreshToken() } }
            assertThat(e).hasMessageThat().contains("Failed to refresh")
            assertThat(preferenceStorage.refreshToken.first()).isNull()
        }
    }

    private fun provideRepo(
        remoteDataSource: AuthRemoteDataSource,
        preferenceStorage: PreferenceStorage = FakePreferenceStorage()
    ): AuthRepository {
        val authPreferenceStore = AuthPreferenceStoreImpl(
            preferenceStorage,
            localDateTimeSerializer,
            mock(),
            testDispatcher
        )
        return AuthRepositoryImpl(
            mock(),
            mock(),
            remoteDataSource,
            authPreferenceStore,
            testAppDispatchers
        )
    }
}

object AuthSession {
    const val refreshToken = "secret-token"
    val initialLoginTime = LocalDateTime.now().minusHours(4)
    val initialLoginTimeRaw = LocalDateTimeConverter.toString(initialLoginTime)
    const val fetchedAccessToken = "brand-new-access-token"
}