package com.example.fazarproject2.util

import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

object SunriseAlarmCalculator {
    private const val TAG = "SunriseAlarmCalculator"

    fun calculateTriggerTime(
        sunriseStr: String,
        dateStr: String?,
        offsetMinutes: Int,
        now: ZonedDateTime = ZonedDateTime.now()
    ): Long {
        val timeFormatter = DateTimeFormatter.ofPattern("h:mm:ss a")
        val localTime = LocalTime.parse(sunriseStr, timeFormatter)

        val sunriseDateTime = if (!dateStr.isNullOrEmpty()) {
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val localDate = LocalDate.parse(dateStr, dateFormatter)
            ZonedDateTime.of(localDate, localTime, now.zone)
        } else {
            // Fallback to today if date is missing
            now.with(localTime)
        }

        var triggerDateTime = sunriseDateTime.minusMinutes(offsetMinutes.toLong())

        // If the calculated trigger time is in the past, and we didn't have a specific date from API,
        // or if the specific date's trigger is already past, we might need to handle it.
        // Usually, the API returns the sunrise for a specific date (today or tomorrow).
        if (triggerDateTime.isBefore(now) && dateStr.isNullOrEmpty()) {
            triggerDateTime = triggerDateTime.plusDays(1)
        }

        val triggerEpoch = triggerDateTime.toInstant().toEpochMilli()

        println("$TAG: calculateTriggerTime: Sunrise API=$sunriseStr, Date API=$dateStr, Offset=$offsetMinutes. " +
                "Final Trigger Time=$triggerDateTime ($triggerEpoch)")

        return triggerEpoch
    }
}
