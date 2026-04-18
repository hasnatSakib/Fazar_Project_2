package com.example.fazarproject2.data.repositoryimpl

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.fazarproject2.data.local.daos.AlarmDao
import com.example.fazarproject2.data.mapper.toDomain
import com.example.fazarproject2.data.mapper.toEntity
import com.example.fazarproject2.domain.model.AlarmSettings
import com.example.fazarproject2.domain.repository.AlarmRepository
import com.example.fazarproject2.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepositoryImpl @Inject constructor(
    private val dao: AlarmDao,
    @ApplicationContext private val context: Context
) : AlarmRepository {

    override fun getAlarmSettings(): Flow<AlarmSettings?> = 
        dao.getAlarmSettings().map { it?.toDomain() }

    override suspend fun getAlarmSettingsSync(): AlarmSettings? = 
        dao.getAlarmSettingsSync()?.toDomain()

    override suspend fun updateAlarmSettings(settings: AlarmSettings) {
        log("Updating alarm settings: $settings")
        dao.updateAlarmSettings(settings.toEntity())
    }

    override suspend fun scheduleAlarm(triggerTimeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        log("Scheduling exact alarm for: $triggerTimeMillis")
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMillis,
            pendingIntent
        )
    }

    override suspend fun cancelAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        log("Cancelling scheduled alarm")
        alarmManager.cancel(pendingIntent)
    }

    private fun log(message: String) {
        Log.d("AlarmRepo", message)
        println("AlarmRepo: $message")
    }

    companion object {
        private const val ALARM_REQUEST_CODE = 1001
    }
}
