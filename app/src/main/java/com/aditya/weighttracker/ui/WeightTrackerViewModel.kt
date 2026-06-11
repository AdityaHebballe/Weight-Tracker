package com.aditya.weighttracker.ui

import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aditya.weighttracker.backup.BackupFrequency
import com.aditya.weighttracker.backup.BackupMetadata
import com.aditya.weighttracker.backup.BackupSettings
import com.aditya.weighttracker.backup.BackupSettingsRepository
import com.aditya.weighttracker.backup.BackupStatus
import com.aditya.weighttracker.backup.DriveAuthorizationRepository
import com.aditya.weighttracker.backup.DriveAuthorizationState
import com.aditya.weighttracker.backup.DriveBackupClient
import com.aditya.weighttracker.backup.DriveBackupScheduler
import com.aditya.weighttracker.data.WeightDatabase
import com.aditya.weighttracker.data.WeightEntry
import com.aditya.weighttracker.data.WeightRepository
import com.aditya.weighttracker.domain.BmiCalculator
import com.aditya.weighttracker.domain.BmiSummary
import com.aditya.weighttracker.domain.CsvExporter
import com.aditya.weighttracker.domain.CsvImporter
import com.aditya.weighttracker.domain.DuplicateImportPolicy
import com.aditya.weighttracker.domain.ImportedWeightEntry
import com.aditya.weighttracker.domain.TrendCalculator
import com.aditya.weighttracker.domain.TrendSummary
import com.aditya.weighttracker.domain.UnitSystem
import com.aditya.weighttracker.domain.WaistToHeightCalculator
import com.aditya.weighttracker.domain.WaistToHeightSummary
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
    private val backupSettingsRepository = BackupSettingsRepository(application)
    private val driveAuthorizationRepository = DriveAuthorizationRepository(application)
    private val driveBackupScheduler = DriveBackupScheduler(application)
    private val backupSettings = MutableStateFlow(backupSettingsRepository.get())
    private val backupInProgress = MutableStateFlow(false)
    private val backupUiState = combine(backupSettings, backupInProgress) { settings, inProgress ->
        BackupUiState(settings, inProgress)
    }
    private var pendingDriveAction: PendingDriveAction = PendingDriveAction.Connect

    val uiState: StateFlow<WeightTrackerUiState> =
        combine(repository.entries, repository.profile, message, pendingImport, backupUiState) { entries, profile, currentMessage, import, backupUi ->
            WeightTrackerUiState(
                entries = entries,
                trend = TrendCalculator.calculate(entries),
                todayEntry = entries.firstOrNull { it.date == LocalDate.now() },
                heightCm = profile.heightCm,
                birthYear = profile.birthYear,
                onboardingCompleted = profile.onboardingCompleted,
                unitSystem = profile.preferredUnitSystem,
                bmi = BmiCalculator.calculate(entries.firstOrNull()?.weightKg, profile.heightCm),
                waistToHeight = WaistToHeightCalculator.calculate(entries.firstOrNull()?.waistCm, profile.heightCm),
                pendingImport = import,
                backupSettings = backupUi.settings,
                isBackupInProgress = backupUi.inProgress,
                message = currentMessage,
                isLoading = false,
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
                currentProfile = com.aditya.weighttracker.data.UserProfile(
                    heightCm = uiState.value.heightCm,
                    birthYear = uiState.value.birthYear,
                    onboardingCompleted = uiState.value.onboardingCompleted,
                    preferredUnitSystem = uiState.value.unitSystem,
                ),
            )
        }
    }

    fun setBirthYear(birthYear: Int?) {
        viewModelScope.launch {
            val current = uiState.value
            repository.updateProfile(
                heightCm = current.heightCm,
                preferredUnitSystem = current.unitSystem,
                birthYear = birthYear,
                onboardingCompleted = current.onboardingCompleted,
                currentProfile = current.toUserProfile(),
            )
        }
    }

    fun completeOnboarding(
        unitSystem: UnitSystem,
        birthYear: Int?,
        heightCm: Double?,
        date: LocalDate?,
        weightKg: Double?,
        waistCm: Double?,
    ) {
        viewModelScope.launch {
            val current = uiState.value
            repository.updateProfile(
                heightCm = heightCm,
                preferredUnitSystem = unitSystem,
                birthYear = birthYear,
                onboardingCompleted = true,
                currentProfile = current.toUserProfile(),
            )
            if (date != null && weightKg != null) {
                repository.save(
                    id = null,
                    date = date,
                    weightKg = weightKg,
                    waistCm = waistCm,
                    heightCm = heightCm,
                    existingCreatedAtMillis = null,
                )
                message.value = "First log added"
            } else {
                message.value = "Setup saved"
            }
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
            viewModelScope.launch {
                val importedCount = applyImportedEntries(result.entries, DuplicateImportPolicy.Replace)
                message.value = "Imported $importedCount rows"
            }
        }
    }

    fun resolveCsvImport(policy: DuplicateImportPolicy) {
        val import = pendingImport.value ?: return
        pendingImport.value = null
        if (policy == DuplicateImportPolicy.Cancel) {
            message.value = "Import canceled"
            return
        }
        viewModelScope.launch {
            val importedCount = applyImportedEntries(import.entries, policy)
            message.value = "Imported $importedCount rows"
        }
    }

    fun connectGoogleDrive(onNeedsResolution: (PendingIntent) -> Unit) {
        viewModelScope.launch {
            pendingDriveAction = PendingDriveAction.Connect
            handleDriveAuthorizationState(driveAuthorizationRepository.authorize(), onNeedsResolution)
        }
    }

    fun finishGoogleDriveAuthorization(data: Intent?) {
        viewModelScope.launch {
            when (val state = driveAuthorizationRepository.authorizationResultFromIntent(data)) {
                is DriveAuthorizationState.Authorized -> {
                    markDriveConnected()
                    when (pendingDriveAction) {
                        PendingDriveAction.Connect -> message.value = "Google Drive backup connected"
                        PendingDriveAction.Backup -> backupToDrive(state.accessToken)
                        PendingDriveAction.Restore -> restoreFromDrive(state.accessToken)
                    }
                    pendingDriveAction = PendingDriveAction.Connect
                }

                is DriveAuthorizationState.Failed -> message.value = state.message
                is DriveAuthorizationState.NeedsResolution -> message.value = "Google Drive approval was not completed"
            }
        }
    }

    fun setBackupFrequency(frequency: BackupFrequency) {
        backupSettingsRepository.setFrequency(frequency)
        refreshBackupSettings()
        if (backupSettings.value.connected) {
            driveBackupScheduler.schedule(frequency)
        }
    }

    fun backupNow(onNeedsResolution: (PendingIntent) -> Unit) {
        if (backupInProgress.value) {
            return
        }
        if (!backupSettings.value.connected) {
            message.value = "Connect Google Drive first"
            return
        }
        viewModelScope.launch {
            pendingDriveAction = PendingDriveAction.Backup
            when (val state = driveAuthorizationRepository.authorize()) {
                is DriveAuthorizationState.Authorized -> {
                    pendingDriveAction = PendingDriveAction.Connect
                    backupToDrive(state.accessToken)
                }

                is DriveAuthorizationState.NeedsResolution -> {
                    onNeedsResolution(state.pendingIntent)
                    message.value = "Approve Google Drive to back up"
                }

                is DriveAuthorizationState.Failed -> {
                    pendingDriveAction = PendingDriveAction.Connect
                    message.value = state.message
                }
            }
        }
    }

    fun restoreDriveBackup(onNeedsResolution: (PendingIntent) -> Unit) {
        viewModelScope.launch {
            pendingDriveAction = PendingDriveAction.Restore
            val state = driveAuthorizationRepository.authorize()
            when (state) {
                is DriveAuthorizationState.NeedsResolution -> {
                    onNeedsResolution(state.pendingIntent)
                    message.value = "Approve Google Drive to restore"
                }

                is DriveAuthorizationState.Failed -> message.value = state.message
                is DriveAuthorizationState.Authorized -> restoreFromDrive(state.accessToken)
            }
        }
    }

    fun disconnectGoogleDrive() {
        backupSettingsRepository.clear()
        driveBackupScheduler.cancel()
        refreshBackupSettings()
        message.value = "Google Drive backup disconnected"
    }

    fun clearMessage() {
        message.value = null
    }

    fun showMessage(value: String) {
        message.value = value
    }

    private suspend fun handleDriveAuthorizationState(
        state: DriveAuthorizationState,
        onNeedsResolution: (PendingIntent) -> Unit,
    ) {
        when (state) {
            is DriveAuthorizationState.Authorized -> {
                markDriveConnected()
                message.value = "Google Drive backup connected"
            }

            is DriveAuthorizationState.NeedsResolution -> onNeedsResolution(state.pendingIntent)
            is DriveAuthorizationState.Failed -> message.value = state.message
        }
    }

    private fun markDriveConnected() {
        backupSettingsRepository.setConnected(true)
        refreshBackupSettings()
        driveBackupScheduler.schedule(backupSettings.value.frequency)
    }

    private suspend fun backupToDrive(accessToken: String) {
        if (backupInProgress.value) {
            return
        }
        backupInProgress.value = true
        message.value = "Backing up to Google Drive..."
        try {
            val snapshot = repository.snapshot()
            val csv = CsvExporter.build(
                entriesNewestFirst = snapshot.entries,
                heightCm = snapshot.profile.heightCm,
                unitSystem = snapshot.profile.preferredUnitSystem,
            )
            val metadata = BackupMetadata.build(snapshot.profile, backupSettings.value.frequency)
            val driveClient = DriveBackupClient(accessToken)
            driveClient.uploadTextFile(
                name = DriveBackupClient.BACKUP_CSV_NAME,
                mimeType = "text/csv",
                content = csv,
            )
            driveClient.uploadTextFile(
                name = DriveBackupClient.BACKUP_METADATA_NAME,
                mimeType = "application/json",
                content = metadata,
            )
            backupSettingsRepository.setConnected(true)
            backupSettingsRepository.setBackupResult(BackupStatus.Success)
            refreshBackupSettings()
            message.value = "Backup complete"
        } catch (error: Exception) {
            val errorMessage = error.message ?: "Backup failed."
            backupSettingsRepository.setBackupResult(BackupStatus.Failed, errorMessage)
            refreshBackupSettings()
            message.value = "Backup failed: $errorMessage"
        } finally {
            backupInProgress.value = false
        }
    }

    private suspend fun restoreFromDrive(accessToken: String) {
        val driveClient = DriveBackupClient(accessToken)
        val csv = runCatching {
            driveClient.downloadTextFile(DriveBackupClient.BACKUP_CSV_NAME)
        }.getOrElse {
            message.value = "Restore failed: ${it.message ?: "Drive error"}"
            return
        }
        if (csv.isNullOrBlank()) {
            message.value = "No Drive backup found"
            return
        }

        val result = CsvImporter.parse(csv)
        if (result.entries.isEmpty()) {
            message.value = result.errors.firstOrNull() ?: "Backup has no rows"
            return
        }

        val importedCount = applyImportedEntries(result.entries, DuplicateImportPolicy.Replace)
        val metadata = runCatching {
            driveClient.downloadTextFile(DriveBackupClient.BACKUP_METADATA_NAME)?.let(BackupMetadata::parse)
        }.getOrNull()
        if (metadata != null) {
            repository.updateProfile(
                heightCm = metadata.heightCm,
                preferredUnitSystem = metadata.preferredUnitSystem,
                birthYear = metadata.birthYear,
                onboardingCompleted = metadata.onboardingCompleted,
                currentProfile = uiState.value.toUserProfile(),
            )
            backupSettingsRepository.setFrequency(metadata.backupFrequency)
            driveBackupScheduler.schedule(metadata.backupFrequency)
        }
        backupSettingsRepository.setConnected(true)
        refreshBackupSettings()
        message.value = "Restored $importedCount rows"
    }

    private suspend fun applyImportedEntries(
        importedEntries: List<ImportedWeightEntry>,
        policy: DuplicateImportPolicy,
    ): Int {
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
        return importedCount
    }

    private fun refreshBackupSettings() {
        backupSettings.value = backupSettingsRepository.get()
    }
}

data class WeightTrackerUiState(
    val entries: List<WeightEntry> = emptyList(),
    val trend: TrendSummary = TrendSummary(),
    val todayEntry: WeightEntry? = null,
    val heightCm: Double? = null,
    val birthYear: Int? = null,
    val onboardingCompleted: Boolean = false,
    val unitSystem: UnitSystem = UnitSystem.Metric,
    val bmi: BmiSummary? = null,
    val waistToHeight: WaistToHeightSummary? = null,
    val pendingImport: PendingCsvImport? = null,
    val backupSettings: BackupSettings = BackupSettings(),
    val isBackupInProgress: Boolean = false,
    val message: String? = null,
    val isLoading: Boolean = true,
)

data class PendingCsvImport(
    val entries: List<ImportedWeightEntry>,
    val duplicateCount: Int,
)

private data class BackupUiState(
    val settings: BackupSettings,
    val inProgress: Boolean,
)

private enum class PendingDriveAction {
    Connect,
    Backup,
    Restore,
}

private fun WeightTrackerUiState.toUserProfile(): com.aditya.weighttracker.data.UserProfile =
    com.aditya.weighttracker.data.UserProfile(
        heightCm = heightCm,
        preferredUnitSystem = unitSystem,
        birthYear = birthYear,
        onboardingCompleted = onboardingCompleted,
    )
