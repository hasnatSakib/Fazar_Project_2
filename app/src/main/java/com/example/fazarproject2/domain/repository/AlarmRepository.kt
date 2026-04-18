package com.example.fazarproject2.domain.repository

import com.example.fazarproject2.domain.model.AlarmSettings
import kotlinx.coroutines.flow.Flow

/**
 * Interface for managing alarm settings and scheduling.
 */
interface AlarmRepository {
    fun getAlarmSettings(): Flow<AlarmSettings?>
    suspend fun getAlarmSettingsSync(): AlarmSettings?
    suspend fun updateAlarmSettings(settings: AlarmSettings)
    suspend fun scheduleAlarm(triggerTimeMillis: Long)
    suspend fun cancelAlarm()
}
