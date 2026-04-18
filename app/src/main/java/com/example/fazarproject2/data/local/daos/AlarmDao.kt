package com.example.fazarproject2.data.local.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.fazarproject2.data.local.entities.AlarmEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for accessing alarm settings in Room.
 */
@Dao
interface AlarmDao {
    @Query("SELECT * FROM alarm_settings WHERE id = 1")
    fun getAlarmSettings(): Flow<AlarmEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateAlarmSettings(settings: AlarmEntity)

    @Query("SELECT * FROM alarm_settings WHERE id = 1")
    suspend fun getAlarmSettingsSync(): AlarmEntity?
}