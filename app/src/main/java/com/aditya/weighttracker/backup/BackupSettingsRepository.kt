package com.aditya.weighttracker.backup

import android.content.Context

class BackupSettingsRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("drive_backup", Context.MODE_PRIVATE)

    fun get(): BackupSettings =
        BackupSettings(
            connected = preferences.getBoolean(KEY_CONNECTED, false),
            accountHint = preferences.getString(KEY_ACCOUNT_HINT, null),
            frequency = BackupFrequency.fromStorage(preferences.getString(KEY_FREQUENCY, null)),
            lastBackupAtMillis = preferences.getLong(KEY_LAST_BACKUP_AT, 0L).takeIf { it > 0L },
            lastStatus = BackupStatus.fromStorage(preferences.getString(KEY_LAST_STATUS, null)),
            lastError = preferences.getString(KEY_LAST_ERROR, null),
        )

    fun setConnected(connected: Boolean, accountHint: String? = null) {
        preferences.edit()
            .putBoolean(KEY_CONNECTED, connected)
            .putString(KEY_ACCOUNT_HINT, accountHint)
            .apply()
    }

    fun setFrequency(frequency: BackupFrequency) {
        preferences.edit()
            .putString(KEY_FREQUENCY, frequency.storageValue)
            .apply()
    }

    fun setBackupResult(status: BackupStatus, error: String? = null) {
        preferences.edit()
            .putString(KEY_LAST_STATUS, status.storageValue)
            .putString(KEY_LAST_ERROR, error)
            .apply {
                if (status == BackupStatus.Success) {
                    putLong(KEY_LAST_BACKUP_AT, System.currentTimeMillis())
                }
            }
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val KEY_CONNECTED = "connected"
        const val KEY_ACCOUNT_HINT = "account_hint"
        const val KEY_FREQUENCY = "frequency"
        const val KEY_LAST_BACKUP_AT = "last_backup_at"
        const val KEY_LAST_STATUS = "last_status"
        const val KEY_LAST_ERROR = "last_error"
    }
}

data class BackupSettings(
    val connected: Boolean = false,
    val accountHint: String? = null,
    val frequency: BackupFrequency = BackupFrequency.Daily,
    val lastBackupAtMillis: Long? = null,
    val lastStatus: BackupStatus = BackupStatus.NotRun,
    val lastError: String? = null,
)

enum class BackupStatus(val storageValue: String) {
    NotRun("NOT_RUN"),
    Success("SUCCESS"),
    Failed("FAILED");

    companion object {
        fun fromStorage(value: String?): BackupStatus =
            entries.firstOrNull { it.storageValue == value } ?: NotRun
    }
}
