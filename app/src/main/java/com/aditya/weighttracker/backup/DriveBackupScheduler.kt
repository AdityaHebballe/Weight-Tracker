package com.aditya.weighttracker.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class DriveBackupScheduler(context: Context) {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    fun schedule(frequency: BackupFrequency) {
        val repeatDays = when (frequency) {
            BackupFrequency.Daily -> 1L
            BackupFrequency.Weekly -> 7L
        }
        val request = PeriodicWorkRequestBuilder<DriveBackupWorker>(
            repeatDays,
            TimeUnit.DAYS,
            6,
            TimeUnit.HOURS,
        )
            .setConstraints(networkConstraints())
            .build()
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_PERIODIC_BACKUP,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork(UNIQUE_PERIODIC_BACKUP)
    }

    private fun networkConstraints(): Constraints =
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    companion object {
        private const val UNIQUE_PERIODIC_BACKUP = "drive-backup-periodic"
    }
}
