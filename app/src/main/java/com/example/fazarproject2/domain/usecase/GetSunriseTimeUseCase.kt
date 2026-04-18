package com.example.fazarproject2.domain.usecase

import com.example.fazarproject2.data.remote.SunriseResponse
import com.example.fazarproject2.domain.repository.SunriseRepository
import javax.inject.Inject

/**
 * Use case to fetch sunrise time for given coordinates.
 */
class GetSunriseTimeUseCase @Inject constructor(
    private val repository: SunriseRepository
) {
    suspend operator fun invoke(lat: Double, lng: Double): Result<SunriseResponse> {
        return repository.getSunrise(lat, lng)
    }
}
