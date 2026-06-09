package com.example.weighttracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.weighttracker.data.WeightDatabase
import com.example.weighttracker.data.WeightEntry
import com.example.weighttracker.data.WeightRepository
import com.example.weighttracker.domain.BmiCalculator
import com.example.weighttracker.domain.BmiSummary
import com.example.weighttracker.domain.CsvExporter
import com.example.weighttracker.domain.CsvImporter
import com.example.weighttracker.domain.DuplicateImportPolicy
import com.example.weighttracker.domain.ImportedWeightEntry
import com.example.weighttracker.domain.TrendCalculator
import com.example.weighttracker.domain.TrendSummary
import com.example.weighttracker.domain.UnitSystem
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WeightTrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val database = WeightDatabase.get(application)
    private val repository = WeightRepository(
        database.weightEntryDao(),
        database.userProfileDao(),
    )
    private val message = MutableStateFlow<String?>(null)
    private val pendingImport = MutableStateFlow<PendingCsvImport?>(null)

    val uiState: StateFlow<WeightTrackerUiState> =
        combine(repository.entries, repository.profile, message, pendingImport) { entries, profile, currentMessage, import ->
            WeightTrackerUiState(
                entries = entries,
                trend = TrendCalculator.calculate(entries),
                todayEntry = entries.firstOrNull { it.date == LocalDate.now() },
                heightCm = profile.heightCm,
                unitSystem = profile.preferredUnitSystem,
                bmi = BmiCalculator.calculate(entries.firstOrNull()?.weightKg, profile.heightCm),
                pendingImport = import,
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
        heightCm: Double?,
    ) {
        viewModelScope.launch {
            val willUpdateExistingDate = editingEntry != null || uiState.value.entries.any { it.date == date }
            repository.save(
                id = editingEntry?.id,
                date = date,
                weightKg = weightKg,
                waistCm = waistCm,
                heightCm = heightCm,
                existingCreatedAtMillis = editingEntry?.createdAtMillis,
            )
            message.value = if (willUpdateExistingDate) "Entry updated" else "Entry added"
        }
    }

    fun deleteEntry(entry: WeightEntry) {
        viewModelScope.launch {
            repository.delete(entry)
            message.value = "Entry deleted"
        }
    }

    fun csv(): String = CsvExporter.build(
        entriesNewestFirst = uiState.value.entries,
        heightCm = uiState.value.heightCm,
        unitSystem = uiState.value.unitSystem,
    )

    fun setUnitSystem(unitSystem: UnitSystem) {
        viewModelScope.launch {
            repository.setUnitSystem(
                unitSystem = unitSystem,
                currentProfile = com.example.weighttracker.data.UserProfile(
                    heightCm = uiState.value.heightCm,
                    preferredUnitSystem = uiState.value.unitSystem,
                ),
            )
        }
    }

    fun previewCsvImport(csv: String) {
        val result = CsvImporter.parse(csv)
        if (result.errors.isNotEmpty()) {
            message.value = result.errors.first()
        }
        if (result.entries.isEmpty()) {
            if (result.errors.isEmpty()) message.value = "No rows to import"
            return
        }
        val duplicateDates = result.entries
            .map { it.date }
            .intersect(uiState.value.entries.map { it.date }.toSet())
        if (duplicateDates.isNotEmpty()) {
            pendingImport.value = PendingCsvImport(
                entries = result.entries,
                duplicateCount = duplicateDates.size,
            )
        } else {
            applyImportedEntries(result.entries, DuplicateImportPolicy.Replace)
        }
    }

    fun resolveCsvImport(policy: DuplicateImportPolicy) {
        val import = pendingImport.value ?: return
        pendingImport.value = null
        if (policy == DuplicateImportPolicy.Cancel) {
            message.value = "Import canceled"
            return
        }
        applyImportedEntries(import.entries, policy)
    }

    fun clearMessage() {
        message.value = null
    }

    private fun applyImportedEntries(importedEntries: List<ImportedWeightEntry>, policy: DuplicateImportPolicy) {
        viewModelScope.launch {
            val existingByDate = uiState.value.entries.associateBy { it.date }
            var importedCount = 0
            importedEntries.forEach { imported ->
                val existing = existingByDate[imported.date]
                if (existing != null && policy == DuplicateImportPolicy.Skip) {
                    return@forEach
                }
                repository.save(
                    id = existing?.id,
                    date = imported.date,
                    weightKg = imported.weightKg,
                    waistCm = imported.waistCm,
                    heightCm = imported.heightCm,
                    existingCreatedAtMillis = existing?.createdAtMillis,
                )
                importedCount += 1
            }
            message.value = "Imported $importedCount rows"
        }
    }
}

data class WeightTrackerUiState(
    val entries: List<WeightEntry> = emptyList(),
    val trend: TrendSummary = TrendSummary(),
    val todayEntry: WeightEntry? = null,
    val heightCm: Double? = null,
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val bmi: BmiSummary? = null,
    val pendingImport: PendingCsvImport? = null,
    val message: String? = null,
)

data class PendingCsvImport(
    val entries: List<ImportedWeightEntry>,
    val duplicateCount: Int,
)
