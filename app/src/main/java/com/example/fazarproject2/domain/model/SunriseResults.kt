package com.example.fazarproject2.domain.model

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SunriseResults(
    @SerialName("date") @SerializedName("date") val date: String? = null,
    @SerialName("sunrise") @SerializedName("sunrise") val sunrise: String? = null,
    @SerialName("sunset") @SerializedName("sunset") val sunset: String? = null,
    @SerialName("first_light") @SerializedName("first_light") val firstLight: String? = null,
    @SerialName("last_light") @SerializedName("last_light") val lastLight: String? = null,
    @SerialName("dawn") @SerializedName("dawn") val dawn: String? = null,
    @SerialName("dusk") @SerializedName("dusk") val dusk: String? = null,
    @SerialName("solar_noon") @SerializedName("solar_noon") val solarNoon: String? = null,
    @SerialName("golden_hour") @SerializedName("golden_hour") val goldenHour: String? = null,
    @SerialName("day_length") @SerializedName("day_length") val dayLength: String? = null,
    @SerialName("timezone") @SerializedName("timezone") val timezone: String? = null,
    @SerialName("utc_offset") @SerializedName("utc_offset") val utcOffset: Int? = null
)
