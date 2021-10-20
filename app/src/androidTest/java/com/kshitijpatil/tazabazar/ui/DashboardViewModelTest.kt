package com.kshitijpatil.tazabazar.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkInfo
import com.google.common.truth.Truth.assertThat
import com.kshitijpatil.tazabazar.domain.ObserveSessionStateUseCase
import com.kshitijpatil.tazabazar.domain.SessionState
import com.kshitijpatil.tazabazar.ui.DashboardViewModel.Companion.REFRESH_TOKEN_WORK
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
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
    private val downstreamFlow = MutableStateFlow<SessionState>(SessionState.Undefined)

    @Before
    fun setup() {
        val observeSessionStateUseCase: ObserveSessionStateUseCase = mock {
            on { invoke() } doReturn downstreamFlow
        }
        viewModel = DashboardViewModel(
            wmRule.targetContext,
            mock(),
            observeSessionStateUseCase
        )
    }

    @Test
    fun whenSessionExpired_RefreshTokenWorkShouldBeScheduled() {
        val workInfosBeforeExpiration =
            wmRule.workManager.getWorkInfosForUniqueWork(REFRESH_TOKEN_WORK).get()
        assertThat(workInfosBeforeExpiration).isEmpty()

        downstreamFlow.value = SessionState.SessionExpired
        val workInfos = wmRule.workManager.getWorkInfosForUniqueWork(REFRESH_TOKEN_WORK).get()
        assertThat(workInfos).isNotNull()
        assertThat(workInfos).hasSize(1)
        assertThat(workInfos[0].state).isAtLeast(WorkInfo.State.ENQUEUED)
    }
}