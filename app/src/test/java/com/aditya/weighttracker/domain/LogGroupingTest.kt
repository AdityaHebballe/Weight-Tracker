package com.aditya.weighttracker.domain

import com.aditya.weighttracker.data.WeightEntry
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class LogGroupingTest {
    @Test
    fun groupsNewestMonthFirstAndEntriesNewestFirst() {
        val groups = LogGrouping.groupByMonth(
            listOf(
                entry(id = 1, date = "2026-05-20"),
                entry(id = 2, date = "2026-06-01"),
                entry(id = 3, date = "2026-06-09"),
            ),
        )

        assertEquals(YearMonth.of(2026, 6), groups[0].month)
        assertEquals(listOf(3L, 2L), groups[0].entries.map { it.id })
        assertEquals(YearMonth.of(2026, 5), groups[1].month)
    }

    private fun entry(id: Long, date: String): WeightEntry =
        WeightEntry(
            id = id,
            date = LocalDate.parse(date),
            weightKg = 78.0,
            waistCm = null,
            createdAtMillis = 0,
            updatedAtMillis = 0,
        )
}
