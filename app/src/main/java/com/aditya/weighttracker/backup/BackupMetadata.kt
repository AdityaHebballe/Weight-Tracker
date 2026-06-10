package com.aditya.weighttracker.backup

import com.aditya.weighttracker.data.UserProfile
import com.aditya.weighttracker.domain.UnitSystem
import org.json.JSONObject

object BackupMetadata {
    private const val SCHEMA_VERSION = 1

    fun build(profile: UserProfile, frequency: BackupFrequency): String =
        JSONObject()
            .put("schemaVersion", SCHEMA_VERSION)
            .put("createdAtMillis", System.currentTimeMillis())
            .put("preferredUnitSystem", profile.preferredUnitSystem.storageValue)
            .put("heightCm", profile.heightCm)
            .put("birthYear", profile.birthYear)
            .put("onboardingCompleted", profile.onboardingCompleted)
            .put("backupFrequency", frequency.storageValue)
            .toString(2)

    fun parse(json: String): RestoredProfile {
        val objectValue = JSONObject(json)
        return RestoredProfile(
            preferredUnitSystem = UnitSystem.fromStorage(objectValue.optString("preferredUnitSystem")),
            heightCm = objectValue.optionalDouble("heightCm"),
            birthYear = objectValue.optionalInt("birthYear"),
            onboardingCompleted = objectValue.optBoolean("onboardingCompleted", true),
            backupFrequency = BackupFrequency.fromStorage(objectValue.optString("backupFrequency")),
        )
    }

    private fun JSONObject.optionalDouble(name: String): Double? =
        if (isNull(name) || !has(name)) null else optDouble(name)

    private fun JSONObject.optionalInt(name: String): Int? =
        if (isNull(name) || !has(name)) null else optInt(name)
}

data class RestoredProfile(
    val preferredUnitSystem: UnitSystem,
    val heightCm: Double?,
    val birthYear: Int?,
    val onboardingCompleted: Boolean,
    val backupFrequency: BackupFrequency,
)
