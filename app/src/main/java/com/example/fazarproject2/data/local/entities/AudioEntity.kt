package com.example.fazarproject2.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_audio")
data class AudioEntity(
    @PrimaryKey val uri: String,
    val title: String,
    val durationMs: Long
)
