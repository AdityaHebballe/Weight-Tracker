package com.example.weighttracker.domain

import com.example.weighttracker.data.WeightEntry

object TrendCalculator {
    fun calculate(entriesNewestFirst: List<WeightEntry>): TrendSummary {
        if (entriesNewestFirst.isEmpty()) {
            return TrendSummary()
        }

        val latest = entriesNewestFirst.first()
        val previous = entriesNewestFirst.drop(1).firstOrNull()
        val oldest = entriesNewestFirst.last()

        return TrendSummary(
            latestWeightKg = latest.weightKg,
            latestWaistCm = latest.waistCm,
            previousDeltaKg = previous?.let { latest.weightKg - it.weightKg },
            totalDeltaKg = latest.weightKg - oldest.weightKg,
            entryCount = entriesNewestFirst.size,
        )
    }
}

data class TrendSummary(
    val latestWeightKg: Double? = null,
    val latestWaistCm: Double? = null,
    val previousDeltaKg: Double? = null,
    val totalDeltaKg: Double? = null,
    val entryCount: Int = 0,
)
