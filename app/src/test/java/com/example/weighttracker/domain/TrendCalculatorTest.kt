package com.example.weighttracker.domain

import com.example.weighttracker.data.WeightEntry
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TrendCalculatorTest {
    @Test
    fun emptyEntriesHaveNoTrend() {
        val trend = TrendCalculator.calculate(emptyList())

        assertNull(trend.latestWeightKg)
        assertEquals(0, trend.entryCount)
    }

    @Test
    fun oneEntryHasLatestButNoPreviousDelta() {
        val trend = TrendCalculator.calculate(
            listOf(entry(date = "2026-06-09", weightKg = 78.2, waistCm = null)),
        )

        assertEquals(78.2, trend.latestWeightKg!!, 0.001)
        assertNull(trend.previousDeltaKg)
        assertEquals(1, trend.entryCount)
    }

    @Test
    fun multipleEntriesCalculatePreviousAndTotalDelta() {
        val trend = TrendCalculator.calculate(
            listOf(
                entry(date = "2026-06-09", weightKg = 78.2, waistCm = 88.0),
                entry(date = "2026-06-01", weightKg = 79.0, waistCm = null),
                entry(date = "2026-05-22", weightKg = 80.0, waistCm = null),
            ),
        )

        assertEquals(-0.8, trend.previousDeltaKg!!, 0.001)
        assertEquals(-1.8, trend.totalDeltaKg!!, 0.001)
        assertEquals(88.0, trend.latestWaistCm!!, 0.001)
        assertEquals(3, trend.entryCount)
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
