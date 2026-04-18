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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RingingViewModel @Inject constructor(
    private val alarmRepository: AlarmRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var exoPlayer: ExoPlayer? = null

    init {
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
        exoPlayer?.stop()
        exoPlayer?.release()
        exoPlayer = null

        // Trigger WorkManager to schedule next alarm
        val workRequest = OneTimeWorkRequestBuilder<ScheduleNextAlarmWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    override fun onCleared() {
        super.onCleared()
        exoPlayer?.release()
    }
}
