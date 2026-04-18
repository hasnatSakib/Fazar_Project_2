package com.example.fazarproject2.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class SunriseLogicTest {

    @Test
    fun `calculate trigger time - given sunrise and offset - returns correct time`() {
        // Given
        val sunriseStr = "6:00:00 AM"
        val offsetMinutes = 30
        val formatter = DateTimeFormatter.ofPattern("h:mm:ss a")
        val localTime = LocalTime.parse(sunriseStr, formatter)
        val sunriseDateTime = ZonedDateTime.now().with(localTime)

        // When
        val triggerTime = sunriseDateTime.minusMinutes(offsetMinutes.toLong())

        // Then
        val expectedTime = localTime.minusMinutes(offsetMinutes.toLong())
        assertThat(triggerTime.toLocalTime()).isEqualTo(expectedTime)
    }
}
