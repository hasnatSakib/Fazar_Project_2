package com.example.fazarproject2.data.remote

import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import org.junit.Test

class SunriseApiMappingTest {

    @Test
    fun `map json to SunriseResponse - valid json - returns correct DTO`() {
        // Given
        val json = """
            {
                "results": {
                    "sunrise": "6:00:00 AM",
                    "sunset": "6:00:00 PM",
                    "first_light": "5:30:00 AM",
                    "last_light": "6:30:00 PM",
                    "dawn": "5:45:00 AM",
                    "dusk": "6:15:00 PM",
                    "solar_noon": "12:00:00 PM",
                    "golden_hour": "5:00:00 PM",
                    "day_length": "12:00:00",
                    "timezone": "UTC",
                    "utc_offset": 0
                },
                "status": "OK"
            }
        """.trimIndent()

        // When
        val response = Gson().fromJson(json, SunriseResponse::class.java)

        // Then
        assertThat(response.status).isEqualTo("OK")
        assertThat(response.results.sunrise).isEqualTo("6:00:00 AM")
        assertThat(response.results.utcOffset).isEqualTo(0)
    }
}
