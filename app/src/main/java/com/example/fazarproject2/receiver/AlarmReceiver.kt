package com.example.fazarproject2.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.fazarproject2.ui.ringing.RingingActivity

/**
 * BroadcastReceiver that handles the alarm trigger.
 */
class AlarmReceiver : BroadcastReceiver() {
    private val TAG = "AlarmReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: Alarm triggered!")
        println("$TAG: onReceive: Alarm triggered!")
        // Start the full-screen RingingActivity
        val ringingIntent = Intent(context, RingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(ringingIntent)
        Log.d(TAG, "onReceive: RingingActivity started")
        println("$TAG: onReceive: RingingActivity started")
    }
}
