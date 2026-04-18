package com.example.fazarproject2.util

import android.util.Log
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object SunriseAlarmCalculator {
    private const val TAG = "SunriseAlarmCalculator"

    fun calculateTriggerTime(
        sunriseStr: String,
        offsetMinutes: Int,
        now: ZonedDateTime = ZonedDateTime.now()
    ): Long {
        val formatter = DateTimeFormatter.ofPattern("h:mm:ss a")
        val localTime = LocalTime.parse(sunriseStr, formatter)

        // Start with today at sunrise time
        var sunriseDateTime = now.with(localTime)
        var triggerDateTime = sunriseDateTime.minusMinutes(offsetMinutes.toLong())

        // If the calculated trigger time is in the past, move to the next day's sunrise
        if (triggerDateTime.isBefore(now)) {
            sunriseDateTime = sunriseDateTime.plusDays(1)
            triggerDateTime = sunriseDateTime.minusMinutes(offsetMinutes.toLong())
        }

        val triggerEpoch = triggerDateTime.toInstant().toEpochMilli()

        val logMsg = "calculateTriggerTime: Sunrise API=$sunriseStr, Offset=$offsetMinutes. " +
                "Actual Sunrise Target=$sunriseDateTime, Final Trigger Time=$triggerDateTime ($triggerEpoch)"

        Log.d(TAG, logMsg)
        println("$TAG: $logMsg")

        return triggerEpoch
    }
}
