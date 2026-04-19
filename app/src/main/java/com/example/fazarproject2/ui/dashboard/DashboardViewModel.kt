package com.example.fazarproject2.ui.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fazarproject2.domain.model.AlarmSettings
import com.example.fazarproject2.domain.model.SunriseResults
import com.example.fazarproject2.domain.repository.AlarmRepository
import com.example.fazarproject2.domain.usecase.GetSunriseTimeUseCase
import com.example.fazarproject2.util.LocationTracker
import com.example.fazarproject2.util.SunriseAlarmCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    private val getSunriseTimeUseCase: GetSunriseTimeUseCase,
    private val locationTracker: LocationTracker
) : ViewModel() {

    private val TAG = "DashboardViewModel"

    val alarmSettings: StateFlow<AlarmSettings?> = alarmRepository.getAlarmSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _latestSunriseResults = MutableStateFlow<SunriseResults?>(null)
    val latestSunriseResults: StateFlow<SunriseResults?> = _latestSunriseResults.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
    }

    fun toggleAlarm(enabled: Boolean) {
        Log.d(TAG, "toggleAlarm: enabled = $enabled")
        println("$TAG: toggleAlarm: enabled = $enabled")
        viewModelScope.launch {
            val current = alarmSettings.value ?: AlarmSettings()
            val updated = current.copy(isEnabled = enabled)
            alarmRepository.updateAlarmSettings(updated)
            if (enabled) {
                Log.d(TAG, "toggleAlarm: Refreshing sunrise and scheduling")
                println("$TAG: toggleAlarm: Refreshing sunrise and scheduling")
                refreshSunriseAndSchedule(showToast = true)
            } else {
                Log.d(TAG, "toggleAlarm: Cancelling alarm")
                println("$TAG: toggleAlarm: Cancelling alarm")
                alarmRepository.cancelAlarm()
                _uiEvent.emit(UiEvent.ShowToast("Alarm Cancelled"))
            }
        }
    }

    fun updateOffset(offset: Int) {
        Log.d(TAG, "updateOffset: offset = $offset")
        println("$TAG: updateOffset: offset = $offset")
        viewModelScope.launch {
            val current = alarmSettings.value ?: AlarmSettings()
            val updated = current.copy(offsetMinutes = offset)
            alarmRepository.updateAlarmSettings(updated)
            if (updated.isEnabled) {
                Log.d(TAG, "updateOffset: Alarm is enabled, refreshing sunrise")
                println("$TAG: updateOffset: Alarm is enabled, refreshing sunrise")
                refreshSunriseAndSchedule(showToast = true)
            }
        }
    }

    fun updateAudio(uri: String, fileName: String) {
        Log.d(TAG, "updateAudio: uri = $uri, fileName = $fileName")
        println("$TAG: updateAudio: uri = $uri, fileName = $fileName")
        viewModelScope.launch {
            val current = alarmSettings.value ?: AlarmSettings()
            val updated = current.copy(customAudioUri = uri, customAudioFileName = fileName)
            alarmRepository.updateAlarmSettings(updated)
        }
    }

    fun refreshSunriseAndSchedule(showToast: Boolean = false) {
        Log.d(TAG, "refreshSunriseAndSchedule: Starting, showToast=$showToast")
        println("$TAG: refreshSunriseAndSchedule: Starting, showToast=$showToast")
        viewModelScope.launch {
            val location = locationTracker.getCurrentLocation()
            if (location == null) {
                Log.e(TAG, "refreshSunriseAndSchedule: Failed to get location")
                println("$TAG ERROR: refreshSunriseAndSchedule: Failed to get location")
                return@launch
            }
            Log.d(
                TAG,
                "refreshSunriseAndSchedule: Location obtained: ${location.latitude}, ${location.longitude}"
            )
            println("$TAG: refreshSunriseAndSchedule: Location obtained: ${location.latitude}, ${location.longitude}")

            val result = getSunriseTimeUseCase(location.latitude, location.longitude)

            result.onSuccess { response ->
               // val sunriseStr = response.results.sunrise // e.g. "6:15:23 AM"
                //val dateStr = response.results.date
                // MOCK DATA for testing: Overriding API response
                // val sunriseStr = response.results.sunrise // e.g. "6:15:23 AM"
                // val dateStr = response.results.date
                val sunriseStr = "03:21:00 PM"
                val dateStr = "2026-04-19"
                // END MOCK DATA
                if (sunriseStr == null) {
                    Log.e(TAG, "refreshSunriseAndSchedule: Sunrise time is null in API response")
                    println("$TAG ERROR: refreshSunriseAndSchedule: Sunrise time is null")
                    return@onSuccess
                }

                Log.d(TAG, "refreshSunriseAndSchedule: Sunrise time from API: $sunriseStr, Date: $dateStr")
                println("$TAG: refreshSunriseAndSchedule: Sunrise time from API: $sunriseStr, Date: $dateStr")

                _latestSunriseResults.value = response.results

                val currentSettings = alarmSettings.value ?: AlarmSettings()
                val triggerTimeEpoch = SunriseAlarmCalculator.calculateTriggerTime(
                    sunriseStr = sunriseStr,
                    dateStr = dateStr,
                    offsetMinutes = currentSettings.offsetMinutes
                )

                val updated = currentSettings.copy(
                    nextSunriseTime = sunriseStr,
                    nextAlarmTriggerTime = triggerTimeEpoch
                )
                alarmRepository.updateAlarmSettings(updated)
                alarmRepository.scheduleAlarm(updated.nextAlarmTriggerTime!!)

                Log.d(TAG, "refreshSunriseAndSchedule: Alarm scheduled for epoch $triggerTimeEpoch")
                println("$TAG: refreshSunriseAndSchedule: Alarm scheduled for epoch $triggerTimeEpoch")
                if (showToast) {
                    val dateInfo = if (!dateStr.isNullOrEmpty()) " on $dateStr" else ""
                    _uiEvent.emit(UiEvent.ShowToast("Alarm Set for $sunriseStr$dateInfo (minus ${currentSettings.offsetMinutes} mins)"))
                }
            }.onFailure {
                Log.e(TAG, "refreshSunriseAndSchedule: Failed to fetch sunrise time", it)
                println("$TAG ERROR: refreshSunriseAndSchedule: Failed to fetch sunrise time - ${it.message}")
            }
        }
    }
}
