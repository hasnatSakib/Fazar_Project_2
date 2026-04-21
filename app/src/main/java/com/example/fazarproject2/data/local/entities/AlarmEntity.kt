package com.example.fazarproject2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarm_settings")
data class AlarmEntity(
    @PrimaryKey val id: Int = 1,
    val isEnabled: Boolean,
    val offsetMinutes: Int,
    val customAudioUri: String?,
    val customAudioFileName: String?,
    val nextSunriseTime: String?,
    val nextAlarmTriggerTime: Long?,
    val snoozeCount: Int = 0
)
