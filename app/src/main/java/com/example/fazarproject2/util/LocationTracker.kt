package com.example.fazarproject2.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationTracker @Inject constructor(
    private val locationClient: FusedLocationProviderClient
) {
    private val TAG = "LocationTracker"

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        Log.d(TAG, "getCurrentLocation: Requesting current location")
        println("$TAG: getCurrentLocation: Requesting current location")
        return try {
            val location = locationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).await()
            Log.d(TAG, "getCurrentLocation: Result: $location")
            println("$TAG: getCurrentLocation: Result: $location")
            location
        } catch (e: Exception) {
            Log.e(TAG, "getCurrentLocation: Error", e)
            println("$TAG ERROR: getCurrentLocation: Error - ${e.message}")
            null
        }
    }
}
