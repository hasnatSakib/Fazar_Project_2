package com.example.fazarproject2.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.fazarproject2.data.local.daos.AlarmDao
import com.example.fazarproject2.domain.model.AlarmSettings
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AlarmDatabaseTest {

    private lateinit var database: AlarmDatabase
    private lateinit var dao: AlarmDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AlarmDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.alarmDao
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun updateAndGetAlarmSettings() = runBlocking {
        // Given
        val settings = AlarmSettings(
            id = 1,
            isEnabled = true,
            offsetMinutes = 45,
            customAudioUri = "content://media/external/audio/123"
        )

        // When
        dao.updateAlarmSettings(settings)
        val retrieved = dao.getAlarmSettings().first()

        // Then
        assertThat(retrieved).isEqualTo(settings)
        assertThat(retrieved?.isEnabled).isTrue()
        assertThat(retrieved?.offsetMinutes).isEqualTo(45)
    }

    @Test
    fun getAlarmSettingsSync() = runBlocking {
        // Given
        val settings = AlarmSettings(id = 1, isEnabled = false)
        dao.updateAlarmSettings(settings)

        // When
        val retrieved = dao.getAlarmSettingsSync()

        // Then
        assertThat(retrieved?.isEnabled).isFalse()
    }
}
