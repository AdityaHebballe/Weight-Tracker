package com.example.weighttracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weighttracker.data.WeightDatabase
import com.example.weighttracker.data.WeightEntry
import com.example.weighttracker.data.WeightRepository
import com.example.weighttracker.domain.CsvExporter
import com.example.weighttracker.domain.TrendCalculator
import com.example.weighttracker.domain.TrendSummary
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeightTrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeightRepository(
        WeightDatabase.get(application).weightEntryDao(),
    )
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<WeightTrackerUiState> =
        combine(repository.entries, message) { entries, currentMessage ->
            WeightTrackerUiState(
                entries = entries,
                trend = TrendCalculator.calculate(entries),
                message = currentMessage,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeightTrackerUiState(),
        )

    fun saveEntry(
        editingEntry: WeightEntry?,
        date: LocalDate,
        weightKg: Double,
        waistCm: Double?,
    ) {
        viewModelScope.launch {
            repository.save(
                id = editingEntry?.id,
                date = date,
                weightKg = weightKg,
                waistCm = waistCm,
                existingCreatedAtMillis = editingEntry?.createdAtMillis,
            )
            message.value = if (editingEntry == null) "Entry added" else "Entry updated"
        }
    }

    fun deleteEntry(entry: WeightEntry) {
        viewModelScope.launch {
            repository.delete(entry)
            message.value = "Entry deleted"
        }
    }

    fun csv(): String = CsvExporter.build(uiState.value.entries)

    fun clearMessage() {
        message.value = null
    }
}

data class WeightTrackerUiState(
    val entries: List<WeightEntry> = emptyList(),
    val trend: TrendSummary = TrendSummary(),
    val message: String? = null,
)
