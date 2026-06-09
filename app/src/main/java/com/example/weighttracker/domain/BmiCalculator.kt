package com.example.weighttracker.domain

import kotlin.math.pow

object BmiCalculator {
    fun calculate(weightKg: Double?, heightCm: Double?): BmiSummary? {
        if (weightKg == null || heightCm == null || weightKg <= 0.0 || heightCm <= 0.0) {
            return null
        }
        val heightM = heightCm / 100.0
        val bmi = weightKg / heightM.pow(2)
        return BmiSummary(value = bmi, category = BmiCategory.from(bmi))
    }
}

data class BmiSummary(
    val value: Double,
    val category: BmiCategory,
)

enum class BmiCategory(
    val label: String,
    val detail: String,
    val min: Double,
    val max: Double,
) {
    Light("Light", "A bit under the usual range.", 0.0, 18.5),
    InRange("In range", "Right in the common BMI range.", 18.5, 25.0),
    GettingHeavy("Getting heavy", "A little above the usual range.", 25.0, 30.0),
    High("High", "Worth keeping an eye on.", 30.0, 35.0),
    VeryHigh("Very high", "A stronger signal to pay attention to.", 35.0, 80.0);

    companion object {
        fun from(value: Double): BmiCategory =
            entries.firstOrNull { value >= it.min && value < it.max } ?: VeryHigh
    }
}
