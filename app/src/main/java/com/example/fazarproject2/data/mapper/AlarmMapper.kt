package com.example.fazarproject2.data.mapper

import com.example.fazarproject2.data.local.entities.AlarmEntity
import com.example.fazarproject2.domain.model.AlarmSettings

fun AlarmEntity.toDomain(): AlarmSettings {
    return AlarmSettings(
        id = id,
        isEnabled = isEnabled,
        offsetMinutes = offsetMinutes,
        customAudioUri = customAudioUri,
        customAudioFileName = customAudioFileName,
        nextSunriseTime = nextSunriseTime,
        nextAlarmTriggerTime = nextAlarmTriggerTime,
        snoozeCount = snoozeCount
    )
}

fun AlarmSettings.toEntity(): AlarmEntity {
    return AlarmEntity(
        id = id,
        isEnabled = isEnabled,
        offsetMinutes = offsetMinutes,
        customAudioUri = customAudioUri,
        customAudioFileName = customAudioFileName,
        nextSunriseTime = nextSunriseTime,
        nextAlarmTriggerTime = nextAlarmTriggerTime,
        snoozeCount = snoozeCount
    )
}
