package com.example.fazarproject2.data.worker

import android.content.Context
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fazarproject2.domain.repository.AlarmRepository
import com.example.fazarproject2.domain.usecase.GetSunriseTimeUseCase
import com.example.fazarproject2.util.LocationTracker
import com.example.fazarproject2.util.SunriseAlarmCalculator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ScheduleNextAlarmWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val alarmRepository: AlarmRepository,
    private val getSunriseTimeUseCase: GetSunriseTimeUseCase,
    private val locationTracker: LocationTracker
) : CoroutineWorker(context, workerParams) {

    private val TAG = "ScheduleNextAlarmWorker"

    init {
        println("$TAG: Worker initialized via Hilt")
    }

    override suspend fun doWork(): Result {
        println("$TAG: doWork() started")
        Log.d(TAG, "doWork: Starting next day alarm rescheduling (MOCK MODE: +2 MINS)")
        
        val currentSettings = alarmRepository.getAlarmSettingsSync() ?: return Result.failure()
        
        // MOCK LOGIC: Reschedule for 2 minutes from now
        val mockTriggerEpoch = System.currentTimeMillis() + (2 * 60 * 1000)
        
        val updated = currentSettings.copy(
            nextAlarmTriggerTime = mockTriggerEpoch,
            snoozeCount = 0 // Reset snooze on dismiss/reschedule
        )
        
        alarmRepository.updateAlarmSettings(updated)
        alarmRepository.scheduleAlarm(mockTriggerEpoch)

        val readableTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(mockTriggerEpoch))
        Log.d(TAG, "doWork: Successfully scheduled MOCK alarm for: $readableTime")
        println("$TAG: ALARM TRULY SET FOR: $readableTime")
        
        return Result.success()
    }
}
