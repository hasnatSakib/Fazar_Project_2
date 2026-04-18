package com.example.fazarproject2.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fazarproject2.data.local.daos.AudioDao
import com.example.fazarproject2.data.local.entities.AudioEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioDatabaseTest {

    private lateinit var db: AlarmDatabase
    private lateinit var dao: AudioDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AlarmDatabase::class.java).build()
        dao = db.audioDao
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun writeAndReadAudio() = runBlocking {
        val audio = AudioEntity("content://test", "Test Song", 30000L)
        dao.insertAudio(audio)
        val allAudios = dao.getAllSavedAudio().first()
        assertThat(allAudios).contains(audio)
    }
}
