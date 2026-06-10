package com.aditya.weighttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Long = 1,
    val heightCm: Double?,
    val preferredUnitSystem: String,
    val birthYear: Int?,
    val onboardingCompleted: Boolean,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
