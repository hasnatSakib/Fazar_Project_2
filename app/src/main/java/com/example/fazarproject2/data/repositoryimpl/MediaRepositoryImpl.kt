package com.example.fazarproject2.data.repositoryimpl

import android.util.Log
import com.example.fazarproject2.data.local.daos.AudioDao
import com.example.fazarproject2.data.mapper.toDomain
import com.example.fazarproject2.data.mapper.toEntity
import com.example.fazarproject2.domain.model.AudioFile
import com.example.fazarproject2.domain.repository.MediaRepository
import com.example.fazarproject2.util.MediaScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(
    private val mediaScanner: MediaScanner,
    private val audioDao: AudioDao
) : MediaRepository {

    override suspend fun fetchSystemAudio(): List<AudioFile> {
        log("Scanning system sounds")
        return mediaScanner.scanAudioFiles()
    }

    override fun getSavedAudio(): Flow<List<AudioFile>> {
        log("Fetching saved audio collection from DB")
        return audioDao.getAllSavedAudio().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun saveAudio(audio: AudioFile) {
        log("Saving to collection: ${audio.title}")
        audioDao.insertAudio(audio.toEntity())
    }

    override suspend fun deleteAudio(audio: AudioFile) {
        log("Deleting from collection: ${audio.title}")
        audioDao.deleteAudio(audio.toEntity())
    }

    private fun log(message: String) {
        Log.d("MediaRepo", message)
        println("MediaRepo: $message")
    }
}
