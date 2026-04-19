package com.example.fazarproject2.domain.model

/**
 * Domain model representing the alarm settings.
 */
data class AlarmSettings(
    val id: Int = 1, // Singleton settings
    val isEnabled: Boolean = false,
    val offsetMinutes: Int = 30, // Minutes before sunrise
    val customAudioUri: String? = null,
    val customAudioFileName: String? = null,
    val nextSunriseTime: String? = null, // Formatted string or timestamp
    val nextAlarmTriggerTime: Long? = null, // Epoch milliseconds
    val snoozeCount: Int = 0
)
