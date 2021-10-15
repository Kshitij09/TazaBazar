package com.kshitijpatil.tazabazar.domain

import com.kshitijpatil.tazabazar.data.AuthRepository
import com.kshitijpatil.tazabazar.data.mapper.LocalDateTimeSerializer
import com.kshitijpatil.tazabazar.model.AuthConfiguration
import com.kshitijpatil.tazabazar.util.LocalDateTimeConverter
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.threeten.bp.LocalDateTime

class IsSessionExpiredUseCaseTest {
    private val testDispatcher = TestCoroutineDispatcher()
    private val localDateTimeSerializer = LocalDateTimeSerializer()
    private lateinit var isSessionExpiredUseCase: IsSessionExpiredUseCase

    object FakeSession {
        val expiryMinutes = 5L
        val config = AuthConfiguration(expiryMinutes.toInt())
        val expiredLoginTime = LocalDateTime.now().minusMinutes(expiryMinutes)
        val expiredLoginTimeRaw = LocalDateTimeConverter.toString(expiredLoginTime)
        val validLoginTime = LocalDateTime.now()
        val validLoginTimeRaw = LocalDateTimeConverter.toString(validLoginTime)
    }

    @Test
    fun sessionExpired() {
        val repo: AuthRepository = mock {
            onBlocking { getAuthConfiguration() } doReturn FakeSession.config
            onBlocking { getLoggedInAt() } doReturn FakeSession.expiredLoginTimeRaw
        }
        isSessionExpiredUseCase =
            IsSessionExpiredUseCase(testDispatcher, repo, localDateTimeSerializer)

        testDispatcher.runBlockingTest {
            val sessionsExpired = isSessionExpiredUseCase(Unit)
            assert(sessionsExpired is Result.Success)
            sessionsExpired as Result.Success
            assertTrue(sessionsExpired.data)
        }
    }

    @Test
    fun sessionValid() {
        val repo: AuthRepository = mock {
            onBlocking { getAuthConfiguration() } doReturn FakeSession.config
            onBlocking { getLoggedInAt() } doReturn FakeSession.validLoginTimeRaw
        }
        isSessionExpiredUseCase =
            IsSessionExpiredUseCase(testDispatcher, repo, localDateTimeSerializer)

        testDispatcher.runBlockingTest {
            val sessionsExpired = isSessionExpiredUseCase(Unit)
            assert(sessionsExpired is Result.Success)
            sessionsExpired as Result.Success
            assertFalse(sessionsExpired.data)
        }
    }

    @Test
    fun checkSessionExpired_whenStoredMalformedDateTimeString_ShouldReturnError() {
        val repo: AuthRepository = mock {
            onBlocking { getAuthConfiguration() } doReturn FakeSession.config
            onBlocking { getLoggedInAt() } doReturn "sahgoawlgwl"
        }
        isSessionExpiredUseCase =
            IsSessionExpiredUseCase(testDispatcher, repo, localDateTimeSerializer)

        testDispatcher.runBlockingTest {
            val sessionsExpired = isSessionExpiredUseCase(Unit)
            assert(sessionsExpired is Result.Error)
        }
    }

    @Test
    fun checkSessionExpired_whenLoggedInAtIsNull_ShouldReturnFalse() {
        val repo: AuthRepository = mock {
            onBlocking { getAuthConfiguration() } doReturn FakeSession.config
            onBlocking { getLoggedInAt() } doReturn null
        }
        isSessionExpiredUseCase =
            IsSessionExpiredUseCase(testDispatcher, repo, localDateTimeSerializer)

        testDispatcher.runBlockingTest {
            val sessionsExpired = isSessionExpiredUseCase(Unit)
            assert(sessionsExpired is Result.Success)
            sessionsExpired as Result.Success
            assertTrue(sessionsExpired.data)
        }
    }
}