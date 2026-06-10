package com.aditya.weighttracker.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.aditya.weighttracker.data.WeightDatabase
import com.aditya.weighttracker.data.WeightRepository
import com.aditya.weighttracker.domain.CsvExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriveBackupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result =
        withContext(Dispatchers.IO) {
            val settingsRepository = BackupSettingsRepository(applicationContext)
            val settings = settingsRepository.get()
            if (!settings.connected) {
                return@withContext Result.success()
            }

            try {
                val authorization = DriveAuthorizationRepository(applicationContext).authorize()
                val accessToken = when (authorization) {
                    is DriveAuthorizationState.Authorized -> authorization.accessToken
                    is DriveAuthorizationState.NeedsResolution -> {
                        settingsRepository.setBackupResult(
                            BackupStatus.Failed,
                            "Reconnect Google Drive to continue backups.",
                        )
                        return@withContext Result.failure()
                    }
                    is DriveAuthorizationState.Failed -> {
                        settingsRepository.setBackupResult(BackupStatus.Failed, authorization.message)
                        return@withContext Result.failure()
                    }
                }

                val database = WeightDatabase.get(applicationContext)
                val repository = WeightRepository(database.weightEntryDao(), database.userProfileDao())
                val snapshot = repository.snapshot()
                val csv = CsvExporter.build(
                    entriesNewestFirst = snapshot.entries,
                    heightCm = snapshot.profile.heightCm,
                    unitSystem = snapshot.profile.preferredUnitSystem,
                )
                val metadata = BackupMetadata.build(snapshot.profile, settings.frequency)
                val client = DriveBackupClient(accessToken)
                client.uploadTextFile(
                    name = DriveBackupClient.BACKUP_CSV_NAME,
                    mimeType = "text/csv",
                    content = csv,
                )
                client.uploadTextFile(
                    name = DriveBackupClient.BACKUP_METADATA_NAME,
                    mimeType = "application/json",
                    content = metadata,
                )
                settingsRepository.setBackupResult(BackupStatus.Success)
                Result.success()
            } catch (error: Exception) {
                settingsRepository.setBackupResult(BackupStatus.Failed, error.message ?: "Backup failed.")
                Result.retry()
            }
        }
}
