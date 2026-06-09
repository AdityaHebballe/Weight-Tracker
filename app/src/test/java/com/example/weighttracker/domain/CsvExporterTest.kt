package com.example.weighttracker.domain

import com.example.weighttracker.data.WeightEntry
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class CsvExporterTest {
    @Test
    fun emptyExportContainsOnlyHeader() {
        assertEquals(
            "date,weight_kg,waist_cm\n",
            CsvExporter.build(emptyList()),
        )
    }

    @Test
    fun exportSortsOldestFirstAndKeepsBlankWaist() {
        val entries = listOf(
            entry(date = "2026-06-09", weightKg = 78.2, waistCm = 88.0),
            entry(date = "2026-06-01", weightKg = 79.0, waistCm = null),
        )

        assertEquals(
            "date,weight_kg,waist_cm\n" +
                "2026-06-01,79.0,\n" +
                "2026-06-09,78.2,88.0\n",
            CsvExporter.build(entries),
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
