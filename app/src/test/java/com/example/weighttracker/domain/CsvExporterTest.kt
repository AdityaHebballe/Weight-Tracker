package com.example.weighttracker.domain

import com.example.weighttracker.data.WeightEntry
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvExporterTest {
    @Test
    fun emptyExportContainsOnlyHeader() {
        assertEquals(
            "date,weight_kg,waist_cm,height_cm,bmi\n",
            CsvExporter.build(emptyList(), heightCm = null, unitSystem = UnitSystem.Metric),
        )
    }

    @Test
    fun exportSortsOldestFirstAndKeepsBlankWaist() {
        val entries = listOf(
            entry(date = "2026-06-09", weightKg = 78.2, waistCm = 88.0),
            entry(date = "2026-06-01", weightKg = 79.0, waistCm = null),
        )

        assertEquals(
            "date,weight_kg,waist_cm,height_cm,bmi\n" +
                "2026-06-01,79.0,,,\n" +
                "2026-06-09,78.2,88.0,,\n",
            CsvExporter.build(entries, heightCm = null, unitSystem = UnitSystem.Metric),
        )
    }

    @Test
    fun exportIncludesHeightAndBmiWhenHeightExists() {
        val entries = listOf(
            entry(date = "2026-06-09", weightKg = 78.2, waistCm = 88.0),
        )

        assertEquals(
            "date,weight_kg,waist_cm,height_cm,bmi\n" +
                "2026-06-09,78.2,88.0,178.0,24.7\n",
            CsvExporter.build(entries, heightCm = 178.0, unitSystem = UnitSystem.Metric),
        )
    }

    @Test
    fun exportUsesImperialHeadersAndValuesWhenSelected() {
        val entries = listOf(
            entry(date = "2026-06-09", weightKg = 78.2, waistCm = 88.0),
        )

        assertEquals(
            "date,weight_lb,waist_in,height_in,bmi\n" +
                "2026-06-09,172.4,34.6,70.1,24.7\n",
            CsvExporter.build(entries, heightCm = 178.0, unitSystem = UnitSystem.Imperial),
        )
    }

    private fun entry(date: String, weightKg: Double, waistCm: Double?): WeightEntry =
        WeightEntry(
            id = date.hashCode().toLong(),
            date = LocalDate.parse(date),
            weightKg = weightKg,
            waistCm = waistCm,
            createdAtMillis = 0L,
            updatedAtMillis = 0L,
        )
}
