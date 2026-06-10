package com.aditya.weighttracker.domain

object WaistToHeightCalculator {
    fun calculate(waistCm: Double?, heightCm: Double?): WaistToHeightSummary? {
        if (waistCm == null || heightCm == null || waistCm <= 0.0 || heightCm <= 0.0) {
            return null
        }
        val ratio = waistCm / heightCm
        return WaistToHeightSummary(value = ratio, category = WaistToHeightCategory.from(ratio))
    }
}

data class WaistToHeightSummary(
    val value: Double,
    val category: WaistToHeightCategory,
)

enum class WaistToHeightCategory(
    val label: String,
    val detail: String,
    val min: Double,
    val max: Double,
) {
    Low("Low", "Below the common tracking range.", 0.0, 0.4),
    InRange("In range", "Waist is under half your height.", 0.4, 0.5),
    Increased("Increased", "A signal to watch over time.", 0.5, 0.6),
    High("High", "A stronger signal to pay attention to.", 0.6, 2.0);

    companion object {
        fun from(value: Double): WaistToHeightCategory =
            entries.firstOrNull { value >= it.min && value < it.max } ?: High
    }
}
