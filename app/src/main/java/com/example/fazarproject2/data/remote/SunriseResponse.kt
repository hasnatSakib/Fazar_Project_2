package com.example.fazarproject2.data.remote

import com.google.gson.annotations.SerializedName

/**
 * DTO for the Sunrise API response.
 */
data class SunriseResponse(
    @SerializedName("results") val results: SunriseResults,
    @SerializedName("status") val status: String
)

data class SunriseResults(
    @SerializedName("date") val date: String,
    @SerializedName("sunrise") val sunrise: String,
    @SerializedName("sunset") val sunset: String,
    @SerializedName("first_light") val firstLight: String,
    @SerializedName("last_light") val lastLight: String,
    @SerializedName("dawn") val dawn: String,
    @SerializedName("dusk") val dusk: String,
    @SerializedName("solar_noon") val solarNoon: String,
    @SerializedName("golden_hour") val goldenHour: String,
    @SerializedName("day_length") val dayLength: String,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("utc_offset") val utcOffset: Int
)
