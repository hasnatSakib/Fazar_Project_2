package com.example.fazarproject2.ui.ringing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fazarproject2.R
import com.example.fazarproject2.data.worker.ScheduleNextAlarmWorker
import com.example.fazarproject2.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RingingViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var exoPlayer: ExoPlayer? = null

    private val _snoozeCount = MutableStateFlow(0)
    val snoozeCount: StateFlow<Int> = _snoozeCount.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
    }

    init {
        viewModelScope.launch {
            val settings = alarmRepository.getAlarmSettingsSync()
            _snoozeCount.value = settings?.snoozeCount ?: 0
        }
        startAlarm()
    }

    private fun startAlarm() {
        viewModelScope.launch {
            val settings = alarmRepository.getAlarmSettingsSync()
            val customUri = settings?.customAudioUri

            val finalUri = if (!customUri.isNullOrEmpty() && isUriAccessible(customUri)) {
                customUri
            } else {
                "android.resource://${context.packageName}/${R.raw.fazar_alarm_sound}"
            }

            exoPlayer = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(finalUri))
                repeatMode = ExoPlayer.REPEAT_MODE_ONE
                prepare()
                play()
            }
        }
    }

    private fun isUriAccessible(uriString: String): Boolean {
        return try {
            val uri = Uri.parse(uriString)
            context.contentResolver.openInputStream(uri)?.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun dismissAlarm() {
        stopPlayer()
        viewModelScope.launch {
            val settings = alarmRepository.getAlarmSettingsSync()
            if (settings != null) {
                alarmRepository.updateAlarmSettings(settings.copy(snoozeCount = 0))
            }
            _uiEvent.emit(UiEvent.ShowToast("Alarm Dismissed"))
            // Trigger WorkManager to schedule next alarm
            val workRequest = OneTimeWorkRequestBuilder<ScheduleNextAlarmWorker>().build()
            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    fun snoozeAlarm() {
        stopPlayer()
        viewModelScope.launch {
            val settings = alarmRepository.getAlarmSettingsSync() ?: return@launch
            val nextSnoozeCount = settings.snoozeCount + 1
            if (nextSnoozeCount > 3) return@launch

            val snoozeMinutes = when (nextSnoozeCount) {
                1 -> 5
                2 -> 3
                3 -> 2
                else -> 0
            }

            val triggerTime = System.currentTimeMillis() + (snoozeMinutes * 60 * 1000)
            
            alarmRepository.updateAlarmSettings(settings.copy(snoozeCount = nextSnoozeCount))
            alarmRepository.scheduleAlarm(triggerTime)
            
            _uiEvent.emit(UiEvent.ShowToast("Snoozed for $snoozeMinutes minutes"))
        }
    }

    private fun stopPlayer() {
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer?.release()
    }
}
