package com.example.fazarproject2.domain.model

/**
 * Domain model representing an audio file (either from system or saved collection).
 */
data class AudioFile(
    val uri: String,
    val title: String,
    val durationMs: Long
)
