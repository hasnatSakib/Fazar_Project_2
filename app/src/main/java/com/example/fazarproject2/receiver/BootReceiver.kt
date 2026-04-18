package com.example.fazarproject2.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.fazarproject2.domain.repository.AlarmRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmRepository: AlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED
        ) {

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settings = alarmRepository.getAlarmSettingsSync()
                    if (settings?.isEnabled == true && settings.nextAlarmTriggerTime != null) {
                        // Check if the time has already passed
                        if (settings.nextAlarmTriggerTime > System.currentTimeMillis()) {
                            alarmRepository.scheduleAlarm(settings.nextAlarmTriggerTime)
                        } else {
                            // Alarm passed while device was off, maybe reschedule for tomorrow?
                            // For now, just rescheduling if it's in the future.
                        }
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
