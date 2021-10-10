package com.kshitijpatil.tazabazar.data

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.data.local.prefs.AuthPreferenceStoreImpl
import com.kshitijpatil.tazabazar.data.local.prefs.PreferenceStorage
import com.kshitijpatil.tazabazar.data.network.AuthRemoteDataSource
import com.kshitijpatil.tazabazar.domain.Result
import com.kshitijpatil.tazabazar.test.util.FakePreferenceStorage
import com.kshitijpatil.tazabazar.test.util.HttpFailureAuthDataSource
import com.kshitijpatil.tazabazar.test.util.MainCoroutineRule
import com.kshitijpatil.tazabazar.test.util.SucceedingAuthDataSource
import com.kshitijpatil.tazabazar.util.AppCoroutineDispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
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

    @Test
    fun refreshToken_happyPath() {
        // setup
        val preferenceStorage = FakePreferenceStorage()
        runBlocking { preferenceStorage.setRefreshToken(AuthSession.refreshToken) }
        val authRemoteDataSource =
            SucceedingAuthDataSource(accessToken = AuthSession.fetchedAccessToken)
        repo = provideRepo(authRemoteDataSource, preferenceStorage)

        // test
        testDispatcher.runBlockingTest {
            val result = repo.refreshToken()
            assertThat(result).isInstanceOf(Result.Success::class.java)
            assertThat(preferenceStorage.accessToken.first()).isEqualTo(AuthSession.fetchedAccessToken)
        }
    }

    @Test
    fun refreshToken_whenNoStoredToken_shouldReturnError() {
        val authRemoteDataSource =
            SucceedingAuthDataSource(accessToken = AuthSession.fetchedAccessToken)
        repo = provideRepo(authRemoteDataSource)

        testDispatcher.runBlockingTest {
            val result = repo.refreshToken()
            assertThat(result).isInstanceOf(Result.Error::class.java)
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
            val result = repo.refreshToken()
            assertThat(result).isInstanceOf(Result.Error::class.java)
            assertThat(preferenceStorage.refreshToken.first()).isNull()
        }
    }

    private fun provideRepo(
        remoteDataSource: AuthRemoteDataSource,
        preferenceStorage: PreferenceStorage = FakePreferenceStorage()
    ): AuthRepository {
        val authPreferenceStore = AuthPreferenceStoreImpl(
            preferenceStorage,
            mock(),
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
    const val fetchedAccessToken = "brand-new-access-token"
}