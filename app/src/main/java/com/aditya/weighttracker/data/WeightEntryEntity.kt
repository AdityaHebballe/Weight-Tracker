package com.aditya.weighttracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weight_entries")
data class WeightEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateEpochDay: Long,
    val weightKg: Double,
    val waistCm: Double?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)
