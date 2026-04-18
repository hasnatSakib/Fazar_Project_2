package com.example.fazarproject2.ui.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.fazarproject2.domain.model.AlarmSettings
import com.example.fazarproject2.domain.repository.AlarmRepository
import com.example.fazarproject2.domain.usecase.GetSunriseTimeUseCase
import com.example.fazarproject2.util.LocationTracker
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.mockkStatic
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()
    private val alarmRepository = mockk<AlarmRepository>(relaxed = true)
    private val getSunriseTimeUseCase = mockk<GetSunriseTimeUseCase>(relaxed = true)
    private val locationTracker = mockk<LocationTracker>(relaxed = true)

    private val alarmSettingsFlow = MutableStateFlow<AlarmSettings?>(null)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkStatic(Log::class)
        coEvery { Log.d(any(), any()) } returns 0
        coEvery { Log.e(any(), any()) } returns 0
        coEvery { Log.e(any(), any(), any()) } returns 0
        
        coEvery { alarmRepository.getAlarmSettings() } returns alarmSettingsFlow
        // Mock locationTracker to prevent "no answer found" exception
        coEvery { locationTracker.getCurrentLocation() } returns null
    }

    @After
    fun teardown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state - emits settings from repository`() = runTest {
        // Given
        val settings = AlarmSettings(isEnabled = true)
        alarmSettingsFlow.value = settings

        // When
        val viewModel = DashboardViewModel(alarmRepository, getSunriseTimeUseCase, locationTracker)
        
        // Then
        viewModel.alarmSettings.test {
            val item = awaitItem()
            if (item == null) {
                assertThat(awaitItem()).isEqualTo(settings)
            } else {
                assertThat(item).isEqualTo(settings)
            }
        }
    }

    @Test
    fun `toggleAlarm - updates repository`() = runTest {
        // Given
        val initialSettings = AlarmSettings(isEnabled = false)
        alarmSettingsFlow.value = initialSettings
        val viewModel = DashboardViewModel(alarmRepository, getSunriseTimeUseCase, locationTracker)
        testDispatcher.scheduler.advanceUntilIdle()
        
        // When
        viewModel.toggleAlarm(true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { alarmRepository.updateAlarmSettings(any()) }
    }

    @Test
    fun `updateOffset - updates repository`() = runTest {
        // Given
        val initialSettings = AlarmSettings(offsetMinutes = 0)
        alarmSettingsFlow.value = initialSettings
        val viewModel = DashboardViewModel(alarmRepository, getSunriseTimeUseCase, locationTracker)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.updateOffset(15)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        coVerify { alarmRepository.updateAlarmSettings(any()) }
    }
}
