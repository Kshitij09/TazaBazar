package com.kshitijpatil.tazabazar.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkInfo
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.domain.*
import com.kshitijpatil.tazabazar.model.AuthConfiguration
import com.kshitijpatil.tazabazar.ui.DashboardViewModel.Companion.REFRESH_TOKEN_WORK
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class DashboardViewModelTest {
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @get:Rule
    var wmRule = WorkManagerTestRule()

    private lateinit var viewModel: DashboardViewModel
    private val testDispatcher = TestCoroutineDispatcher()
    private val mockGetAuthConfigurationUseCase: GetAuthConfigurationUseCase = mock {
        onBlocking { invoke(any()) } doReturn Result.Success(DashboardSession.config)
    }

    @Test
    fun whenSessionExpired_RefreshTokenWorkShouldBeScheduled() {
        val mockIsSessionExpiredUseCase = getMockSessionExpiredUseCase(expired = true)
        val mockObserveAccessTokenChangedUseCase: ObserveAccessTokenChangedUseCase = mock {
            on { invoke() } doReturn emptyFlow()
        }

        viewModel =
            provideViewModel(mockIsSessionExpiredUseCase, mockObserveAccessTokenChangedUseCase)
        val workInfos = wmRule.workManager.getWorkInfosForUniqueWork(REFRESH_TOKEN_WORK).get()
        assertThat(workInfos).isNotNull()
        assertThat(workInfos).hasSize(1)
        assertThat(workInfos[0].state).isAtLeast(WorkInfo.State.ENQUEUED)
    }

    @Test
    fun whenAccessTokenChanges_RefreshTokenWorkShouldBeScheduled() {
        // given session was not expired initially
        val mockIsSessionExpiredUseCase = getMockSessionExpiredUseCase(expired = false)
        val tokenChangesChannel = Channel<AccessTokenChanged>()
        val mockObserveAccessTokenChangedUseCase: ObserveAccessTokenChangedUseCase = mock {
            on { invoke() } doReturn tokenChangesChannel.consumeAsFlow().flowOn(testDispatcher)
        }

        viewModel =
            provideViewModel(mockIsSessionExpiredUseCase, mockObserveAccessTokenChangedUseCase)
        var workInfos = wmRule.workManager.getWorkInfosForUniqueWork(REFRESH_TOKEN_WORK).get()
        // no work should be scheduled at ViewModel initialization
        assertThat(workInfos).isEmpty()
        testDispatcher.runBlockingTest {
            // when token changes
            tokenChangesChannel.send(AccessTokenChanged)

            // RefreshTokenWorker should be scheduled
            workInfos = wmRule.workManager.getWorkInfosForUniqueWork(REFRESH_TOKEN_WORK).get()
            assertThat(workInfos).hasSize(1)
            assertThat(workInfos[0].state).isAtLeast(WorkInfo.State.ENQUEUED)
        }
    }

    private fun provideViewModel(
        isSessionExpiredUseCase: IsSessionExpiredUseCase,
        observeAccessTokenChangedUseCase: ObserveAccessTokenChangedUseCase
    ): DashboardViewModel {
        return DashboardViewModel(
            wmRule.targetContext,
            mock(),
            isSessionExpiredUseCase,
            mockGetAuthConfigurationUseCase,
            observeAccessTokenChangedUseCase
        )
    }

    private fun getMockSessionExpiredUseCase(expired: Boolean): IsSessionExpiredUseCase {
        return mock {
            onBlocking { invoke(any()) } doReturn Result.Success(expired)
        }
    }

    object DashboardSession {
        val config = AuthConfiguration(1)
    }
}