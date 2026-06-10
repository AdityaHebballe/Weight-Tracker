package com.aditya.weighttracker.domain

import com.aditya.weighttracker.data.WeightEntry
import java.util.Locale

object CsvExporter {
    fun build(entriesNewestFirst: List<WeightEntry>, heightCm: Double?, unitSystem: UnitSystem): String {
        val weightHeader = if (unitSystem == UnitSystem.Imperial) "weight_lb" else "weight_kg"
        val waistHeader = if (unitSystem == UnitSystem.Imperial) "waist_in" else "waist_cm"
        val heightHeader = if (unitSystem == UnitSystem.Imperial) "height_in" else "height_cm"
        val rows = entriesNewestFirst
            .sortedBy { it.date }
            .joinToString(separator = "\n") { entry ->
                val bmi = BmiCalculator.calculate(entry.weightKg, heightCm)
                val waistToHeight = WaistToHeightCalculator.calculate(entry.waistCm, heightCm)
                listOf(
                    entry.date.toString(),
                    formatNumber(UnitConverter.weightFromKg(entry.weightKg, unitSystem)),
                    entry.waistCm?.let { UnitConverter.lengthFromCm(it, unitSystem) }?.let(::formatNumber).orEmpty(),
                    heightCm?.let { UnitConverter.lengthFromCm(it, unitSystem) }?.let(::formatNumber).orEmpty(),
                    bmi?.value?.let(::formatNumber).orEmpty(),
                    waistToHeight?.value?.let(::formatRatio).orEmpty(),
                ).joinToString(",")
            }
        val header = "date,$weightHeader,$waistHeader,$heightHeader,bmi,waist_to_height\n"
        return if (rows.isEmpty()) {
            header
        } else {
            "$header$rows\n"
        }
    }

    private fun formatNumber(value: Double): String =
        String.format(Locale.US, "%.1f", value)

    private fun formatRatio(value: Double): String =
        String.format(Locale.US, "%.2f", value)
}
