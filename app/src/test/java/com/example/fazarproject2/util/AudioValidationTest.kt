package com.example.fazarproject2.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AudioValidationTest {

    @Test
    fun `validate audio duration - duration 59s - returns true`() {
        // Given
        val durationMs = 59000L
        
        // When
        val isValid = isAudioDurationValid(durationMs)

        // Then
        assertThat(isValid).isTrue()
    }

    @Test
    fun `validate audio duration - duration 61s - returns false`() {
        // Given
        val durationMs = 61000L
        
        // When
        val isValid = isAudioDurationValid(durationMs)

        // Then
        assertThat(isValid).isFalse()
    }

    private fun isAudioDurationValid(durationMs: Long): Boolean {
        return durationMs in 1..59999
    }
}
