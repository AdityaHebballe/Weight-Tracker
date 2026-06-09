package com.example.weighttracker.domain

import com.example.weighttracker.data.WeightEntry
import java.time.YearMonth

object LogGrouping {
    fun groupByMonth(entriesNewestFirst: List<WeightEntry>): List<MonthlyLogGroup> =
        entriesNewestFirst
            .groupBy { YearMonth.from(it.date) }
            .toSortedMap(compareByDescending { it })
            .map { (month, entries) ->
                MonthlyLogGroup(
                    month = month,
                    entries = entries.sortedWith(compareByDescending<WeightEntry> { it.date }.thenByDescending { it.id }),
                )
            }
}

data class MonthlyLogGroup(
    val month: YearMonth,
    val entries: List<WeightEntry>,
)
