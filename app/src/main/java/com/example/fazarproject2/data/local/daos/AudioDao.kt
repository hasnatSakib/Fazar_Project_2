package com.example.fazarproject2.data.local.daos

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fazarproject2.data.local.entities.AudioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AudioDao {
    @Query("SELECT * FROM saved_audio")
    fun getAllSavedAudio(): Flow<List<AudioEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudio(audio: AudioEntity)

    @Delete
    suspend fun deleteAudio(audio: AudioEntity)

    @Query("SELECT * FROM saved_audio WHERE uri = :uri")
    suspend fun getAudioByUri(uri: String): AudioEntity?
}