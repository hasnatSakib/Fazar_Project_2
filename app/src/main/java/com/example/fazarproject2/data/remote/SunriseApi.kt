package com.example.fazarproject2.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Sunrise API.
 */
interface SunriseApi {
    @GET("json")
    suspend fun getSunriseSunset(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("date") date: String = "tomorrow"
    ): SunriseResponse

    companion object {
        const val BASE_URL = "https://api.sunrisesunset.io/"
    }
}
