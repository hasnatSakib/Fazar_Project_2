package com.example.fazarproject2.domain.repository

import com.example.fazarproject2.domain.model.AudioFile
import kotlinx.coroutines.flow.Flow

interface MediaRepository {
    suspend fun fetchSystemAudio(): List<AudioFile>
    fun getSavedAudio(): Flow<List<AudioFile>>
    suspend fun saveAudio(audio: AudioFile)
    suspend fun deleteAudio(audio: AudioFile)
}
