package com.example.weighttracker.domain

import com.example.weighttracker.data.WeightEntry
import java.util.Locale

object CsvExporter {
    fun build(entriesNewestFirst: List<WeightEntry>): String {
        val rows = entriesNewestFirst
            .sortedBy { it.date }
            .joinToString(separator = "\n") { entry ->
                listOf(
                    entry.date.toString(),
                    formatNumber(entry.weightKg),
                    entry.waistCm?.let(::formatNumber).orEmpty(),
                ).joinToString(",")
            }
        return if (rows.isEmpty()) {
            "date,weight_kg,waist_cm\n"
        } else {
            "date,weight_kg,waist_cm\n$rows\n"
        }
    }

    private fun formatNumber(value: Double): String =
        String.format(Locale.US, "%.1f", value)
}
