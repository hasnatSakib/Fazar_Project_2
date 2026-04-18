package com.example.fazarproject2.data.mapper

import com.example.fazarproject2.data.local.entities.AudioEntity
import com.example.fazarproject2.domain.model.AudioFile

fun AudioEntity.toDomain(): AudioFile {
    return AudioFile(
        uri = uri,
        title = title,
        durationMs = durationMs
    )
}

fun AudioFile.toEntity(): AudioEntity {
    return AudioEntity(
        uri = uri,
        title = title,
        durationMs = durationMs
    )
}
