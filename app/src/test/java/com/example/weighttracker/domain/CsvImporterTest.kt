package com.example.weighttracker.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvImporterTest {
    @Test
    fun importsMetricCsv() {
        val result = CsvImporter.parse(
            "date,weight_kg,waist_cm,height_cm,bmi\n" +
                "2026-06-09,78.2,88.0,178.0,24.7\n",
        )

        assertTrue(result.errors.isEmpty())
        assertEquals(1, result.entries.size)
        assertEquals(78.2, result.entries.first().weightKg, 0.001)
        assertEquals(88.0, result.entries.first().waistCm!!, 0.001)
        assertEquals(178.0, result.entries.first().heightCm!!, 0.001)
    }

    @Test
    fun importsImperialCsvAsMetricStorageValues() {
        val result = CsvImporter.parse(
            "date,weight_lb,waist_in,height_in,bmi\n" +
                "2026-06-09,172.4,34.6,70.1,24.7\n",
        )

        assertTrue(result.errors.isEmpty())
        assertEquals(78.2, result.entries.first().weightKg, 0.1)
        assertEquals(87.9, result.entries.first().waistCm!!, 0.1)
        assertEquals(178.1, result.entries.first().heightCm!!, 0.1)
    }
}
