package com.example.fazarproject2.domain.repository

import com.example.fazarproject2.data.remote.SunriseResponse

/**
 * Interface for fetching sunrise data.
 */
interface SunriseRepository {
    suspend fun getSunrise(lat: Double, lng: Double): Result<SunriseResponse>
}
