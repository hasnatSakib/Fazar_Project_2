package com.example.fazarproject2.ui.ringing

import android.app.NotificationManager
import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fazarproject2.R
import com.example.fazarproject2.data.worker.ScheduleNextAlarmWorker
import com.example.fazarproject2.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.media.AudioAttributes as AndroidAudioAttributes

@HiltViewModel
class RingingViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

    private val _snoozeCount = MutableStateFlow(0)
    val snoozeCount: StateFlow<Int> = _snoozeCount.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
    }

    init {
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        vibrator = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        viewModelScope.launch {
            val settings = alarmRepository.getAlarmSettingsSync()
            _snoozeCount.value = settings?.snoozeCount ?: 0
        }
        startAlarm()
    }

    private fun startAlarm() {
        requestAudioFocus()
        startVibration()

        viewModelScope.launch {
            val settings = alarmRepository.getAlarmSettingsSync()
            val customUri = settings?.customAudioUri

            val finalUri = if (!customUri.isNullOrEmpty() && isUriAccessible(customUri)) {
                Uri.parse(customUri)
            } else {
                Uri.parse("android.resource://${context.packageName}/${R.raw.fazar_alarm_sound}")
            }

            try {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, finalUri)

                    val audioAttributes = AndroidAudioAttributes.Builder()
                        .setUsage(AndroidAudioAttributes.USAGE_ALARM)
                        .setContentType(AndroidAudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                    setAudioAttributes(audioAttributes)

                    isLooping = true
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to system default alarm if custom fails
                playFallbackAlarm()
            }
        }
    }

    private fun playFallbackAlarm() {
        try {
            val alert =
                android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, alert)
                val audioAttributes = AndroidAudioAttributes.Builder()
                    .setUsage(AndroidAudioAttributes.USAGE_ALARM)
                    .setContentType(AndroidAudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                setAudioAttributes(audioAttributes)
                isLooping = true
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestAudioFocus() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val playbackAttributes = AndroidAudioAttributes.Builder()
                .setUsage(AndroidAudioAttributes.USAGE_ALARM)
                .setContentType(AndroidAudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            focusRequest =
                AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(playbackAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener { }
                    .build()

            audioManager?.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.requestAudioFocus(
                { },
                AudioManager.STREAM_ALARM,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            )
        }
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 500, 500) // Start immediately, vibrate 500ms, pause 500ms
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopVibration() {
        vibrator?.cancel()
    }

    private fun abandonAudioFocus() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && focusRequest != null) {
            audioManager?.abandonAudioFocusRequest(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager?.abandonAudioFocus { }
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
        cancelNotification()
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
        cancelNotification()
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
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        stopVibration()
        abandonAudioFocus()
    }

    private fun cancelNotification() {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(100) // Matches the ID used in AlarmReceiver
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
