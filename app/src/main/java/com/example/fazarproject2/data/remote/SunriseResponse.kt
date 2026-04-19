package com.example.fazarproject2.data.remote

import com.example.fazarproject2.domain.model.SunriseResults
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

/**
 * DTO for the Sunrise API response.
 */
@Serializable
data class SunriseResponse(
    @SerializedName("results") val results: SunriseResults,
    @SerializedName("status") val status: String
)
