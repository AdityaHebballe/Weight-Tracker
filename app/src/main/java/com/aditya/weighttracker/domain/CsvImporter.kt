package com.aditya.weighttracker.domain

import java.time.LocalDate

object CsvImporter {
    fun parse(csv: String): CsvImportResult {
        val lines = csv.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()
        if (lines.isEmpty()) {
            return CsvImportResult(emptyList(), listOf("CSV is empty."))
        }

        val headers = lines.first().split(",").map { it.trim().lowercase() }
        val dateIndex = headers.indexOf("date")
        val weightKgIndex = headers.indexOf("weight_kg")
        val weightLbIndex = headers.indexOf("weight_lb")
        val waistCmIndex = headers.indexOf("waist_cm")
        val waistInIndex = headers.indexOf("waist_in")
        val heightCmIndex = headers.indexOf("height_cm")
        val heightInIndex = headers.indexOf("height_in")
        val unitSystem = when {
            weightLbIndex >= 0 || waistInIndex >= 0 || heightInIndex >= 0 -> UnitSystem.Imperial
            else -> UnitSystem.Metric
        }
        val weightIndex = if (unitSystem == UnitSystem.Imperial) weightLbIndex else weightKgIndex
        val waistIndex = if (unitSystem == UnitSystem.Imperial) waistInIndex else waistCmIndex
        val heightIndex = if (unitSystem == UnitSystem.Imperial) heightInIndex else heightCmIndex
        if (dateIndex < 0 || weightIndex < 0) {
            return CsvImportResult(emptyList(), listOf("CSV must include date and weight columns."))
        }

        val errors = mutableListOf<String>()
        val rows = lines.drop(1).mapIndexedNotNull { rowOffset, line ->
            val rowNumber = rowOffset + 2
            val cells = line.split(",").map { it.trim() }
            val date = cells.getOrNull(dateIndex)?.let {
                runCatching { LocalDate.parse(it) }.getOrNull()
            }
            val weight = cells.getOrNull(weightIndex)?.toDoubleOrNull()
            if (date == null || weight == null || weight <= 0.0) {
                errors += "Row $rowNumber has an invalid date or weight."
                return@mapIndexedNotNull null
            }

            val waist = cells.getOptionalPositiveDouble(waistIndex, rowNumber, "waist", errors)
            val height = cells.getOptionalPositiveDouble(heightIndex, rowNumber, "height", errors)
            ImportedWeightEntry(
                date = date,
                weightKg = UnitConverter.weightToKg(weight, unitSystem),
                waistCm = waist?.let { UnitConverter.lengthToCm(it, unitSystem) },
                heightCm = height?.let { UnitConverter.lengthToCm(it, unitSystem) },
            )
        }
        return CsvImportResult(rows, errors)
    }

    private fun List<String>.getOptionalPositiveDouble(
        index: Int,
        rowNumber: Int,
        name: String,
        errors: MutableList<String>,
    ): Double? {
        if (index < 0) return null
        val raw = getOrNull(index).orEmpty()
        if (raw.isBlank()) return null
        val value = raw.toDoubleOrNull()
        if (value == null || value <= 0.0) {
            errors += "Row $rowNumber has an invalid $name."
            return null
        }
        return value
    }
}

data class CsvImportResult(
    val entries: List<ImportedWeightEntry>,
    val errors: List<String>,
)

data class ImportedWeightEntry(
    val date: LocalDate,
    val weightKg: Double,
    val waistCm: Double?,
    val heightCm: Double?,
)

enum class DuplicateImportPolicy {
    Replace,
    Skip,
    Cancel,
}
