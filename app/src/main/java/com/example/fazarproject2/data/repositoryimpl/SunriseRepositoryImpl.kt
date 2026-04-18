package com.example.fazarproject2.data.repositoryimpl

import android.util.Log
import com.example.fazarproject2.data.remote.SunriseApi
import com.example.fazarproject2.data.remote.SunriseResponse
import com.example.fazarproject2.domain.repository.SunriseRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SunriseRepositoryImpl @Inject constructor(
    private val api: SunriseApi
) : SunriseRepository {

    override suspend fun getSunrise(lat: Double, lng: Double): Result<SunriseResponse> {
        return try {
            log("Fetching sunrise for lat=$lat, lng=$lng")
            val response = api.getSunriseSunset(lat, lng)
            if (response.status == "OK") {
                log("Successfully fetched sunrise data")
                Result.success(response)
            } else {
                val errorMsg = "API Error: ${response.status}"
                log(errorMsg)
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            log("Network Error: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    private fun log(message: String) {
        Log.d("SunriseRepo", message)
        println("SunriseRepo: $message")
    }
}
