package com.example.fazarproject2.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.ZonedDateTime

class SunriseAlarmCalculatorTest {

    @Test
    fun `calculateTriggerTime - 30 min offset - returns correct epoch`() {
        // Given
        val sunriseStr = "6:00:00 AM"
        val dateStr = "2023-10-10"
        val offsetMinutes = 30
        // Fix "now" to 5:00 AM on a specific day
        val now = ZonedDateTime.parse("2023-10-10T05:00:00Z[UTC]")
        
        // When
        val triggerTime = SunriseAlarmCalculator.calculateTriggerTime(sunriseStr, dateStr, offsetMinutes, now)
        
        // Then
        // Expected sunrise: 06:00:00. Trigger: 05:30:00
        val expectedTrigger = now.withHour(5).withMinute(30).withSecond(0).toInstant().toEpochMilli()
        assertThat(triggerTime).isEqualTo(expectedTrigger)
    }

    @Test
    fun `calculateTriggerTime - sunrise passed today - returns tomorrow's trigger`() {
        // Given
        val sunriseStr = "6:00:00 AM"
        val dateStr = null // Testing fallback behavior
        val offsetMinutes = 0
        // "Now" is 7:00 AM, sunrise (6 AM) already passed
        val now = ZonedDateTime.parse("2023-10-10T07:00:00Z[UTC]")
        
        // When
        val triggerTime = SunriseAlarmCalculator.calculateTriggerTime(sunriseStr, dateStr, offsetMinutes, now)
        
        // Then
        // Expected: Oct 11th, 06:00:00
        val expectedTrigger = now.plusDays(1).withHour(6).withMinute(0).withSecond(0).toInstant().toEpochMilli()
        assertThat(triggerTime).isEqualTo(expectedTrigger)
    }
}
