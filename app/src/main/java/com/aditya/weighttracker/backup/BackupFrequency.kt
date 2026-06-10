package com.aditya.weighttracker.backup

enum class BackupFrequency(
    val storageValue: String,
    val label: String,
) {
    Daily("DAILY", "Daily"),
    Weekly("WEEKLY", "Weekly");

    companion object {
        fun fromStorage(value: String?): BackupFrequency =
            entries.firstOrNull { it.storageValue == value } ?: Daily
    }
}
