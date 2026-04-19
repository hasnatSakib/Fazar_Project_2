package com.example.fazarproject2.ui.navigation

import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavType
import com.example.fazarproject2.domain.model.SunriseResults
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
object Dashboard

@Serializable
object SoundSelector

@Serializable
data class SunriseDetails(val results: SunriseResults)

val SunriseResultsType = object : NavType<SunriseResults>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): SunriseResults? {
        return bundle.getString(key)?.let { Json.decodeFromString(it) }
    }

    override fun parseValue(value: String): SunriseResults {
        return Json.decodeFromString(Uri.decode(value))
    }

    override fun put(bundle: Bundle, key: String, value: SunriseResults) {
        bundle.putString(key, Json.encodeToString(value))
    }

    override fun serializeAsValue(value: SunriseResults): String {
        return Uri.encode(Json.encodeToString(value))
    }
}
