package com.example.fazarproject2.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fazarproject2.FazarApplication
import com.example.fazarproject2.R
import com.example.fazarproject2.ui.ringing.RingingActivity

/**
 * BroadcastReceiver that handles the alarm trigger.
 */
class AlarmReceiver : BroadcastReceiver() {
    private val TAG = "AlarmReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "onReceive: Alarm triggered with action: $action")

        if (action != "com.example.fazarproject2.ALARM_TRIGGER") {
            Log.d(TAG, "onReceive: Ignoring unknown action")
            return
        }

        // Acquire a brief wakelock
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "FazarProject2:AlarmWakeLock"
        )
        wakeLock.acquire(10 * 1000L)

        val fullScreenIntent = Intent(context, RingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_TRIGGERED", true)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            0,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder =
            NotificationCompat.Builder(context, FazarApplication.ALARM_CHANNEL_ID)
                .setSmallIcon(R.drawable.ruku)
                .setContentTitle("Sunrise Alarm")
                .setContentText("Wake up! It's Fazar time.")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setAutoCancel(true)
                .setOngoing(true)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Use a consistent ID for the alarm notification
        val notificationId = 100
        notificationManager.notify(notificationId, notificationBuilder.build())

        Log.d(TAG, "onReceive: Full screen notification posted with ID: $notificationId")
    }
}
