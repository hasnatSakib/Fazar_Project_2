package com.example.fazarproject2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.fazarproject2.data.local.daos.AlarmDao
import com.example.fazarproject2.data.local.daos.AudioDao
import com.example.fazarproject2.data.local.entities.AlarmEntity
import com.example.fazarproject2.data.local.entities.AudioEntity

/**
 * The Room database for the application.
 */
@Database(entities = [AlarmEntity::class, AudioEntity::class], version = 2, exportSchema = false)
abstract class AlarmDatabase : RoomDatabase() {
    abstract val alarmDao: AlarmDao
    abstract val audioDao: AudioDao

    companion object {
        const val DATABASE_NAME = "sunrise_alarm_db"
    }
}
