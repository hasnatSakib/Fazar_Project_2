package com.example.fazarproject2.data.worker

import android.content.Context
import android.util.Log
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

    override suspend fun doWork(): Result {
        Log.d(TAG, "doWork: Starting next day alarm rescheduling")
        println("$TAG: doWork: Starting next day alarm rescheduling")
        val location = locationTracker.getCurrentLocation()
        if (location == null) {
            Log.e(TAG, "doWork: Failed to get location")
            println("$TAG ERROR: doWork: Failed to get location")
            return Result.failure()
        }
        val result = getSunriseTimeUseCase(location.latitude, location.longitude)

        return result.fold(
            onSuccess = { response ->
                val sunriseStr = response.results.sunrise
                val currentSettings =
                    alarmRepository.getAlarmSettingsSync() ?: return@fold Result.failure()

                val triggerEpoch = SunriseAlarmCalculator.calculateTriggerTime(
                    sunriseStr = sunriseStr,
                    offsetMinutes = currentSettings.offsetMinutes
                )

                val updated = currentSettings.copy(
                    nextSunriseTime = sunriseStr,
                    nextAlarmTriggerTime = triggerEpoch
                )
                alarmRepository.updateAlarmSettings(updated)
                alarmRepository.scheduleAlarm(updated.nextAlarmTriggerTime!!)

                Log.d(TAG, "doWork: Successfully rescheduled alarm for epoch: $triggerEpoch")
                println("$TAG: doWork: Successfully rescheduled alarm for epoch: $triggerEpoch")
                Result.success()
            },
            onFailure = {
                Log.e(TAG, "doWork: API call failed, retrying...")
                println("$TAG ERROR: doWork: API call failed, retrying...")
                Result.retry()
            }
        )
    }
}
