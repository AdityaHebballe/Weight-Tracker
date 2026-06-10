package com.aditya.weighttracker.data

import com.aditya.weighttracker.domain.UnitSystem
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeightRepository(
    private val entryDao: WeightEntryDao,
    private val profileDao: UserProfileDao,
) {
    val entries: Flow<List<WeightEntry>> =
        entryDao.observeEntriesNewestFirst().map { entities -> entities.map { it.toModel() } }

    val profile: Flow<UserProfile> =
        profileDao.observeProfile().map { entity -> entity?.toModel() ?: UserProfile() }

    suspend fun save(
        id: Long?,
        date: LocalDate,
        weightKg: Double,
        waistCm: Double?,
        heightCm: Double?,
        existingCreatedAtMillis: Long?,
    ) {
        val now = System.currentTimeMillis()
        val dateEpochDay = date.toEpochDay()
        val sameDateEntries = if (id == null || id == 0L) {
            entryDao.entriesForDate(dateEpochDay)
        } else {
            emptyList()
        }
        val sameDateEntry = sameDateEntries.firstOrNull()
        val resolvedId = id?.takeIf { it != 0L } ?: sameDateEntry?.id ?: 0L
        val entity = WeightEntryEntity(
            id = resolvedId,
            dateEpochDay = dateEpochDay,
            weightKg = weightKg,
            waistCm = waistCm,
            createdAtMillis = existingCreatedAtMillis ?: sameDateEntry?.createdAtMillis ?: now,
            updatedAtMillis = now,
        )
        val keptId = if (resolvedId == 0L) {
            entryDao.insert(entity)
        } else {
            entryDao.update(entity)
            resolvedId
        }
        if (sameDateEntries.size > 1) {
            entryDao.deleteOtherEntriesForDate(dateEpochDay, keptId)
        }
        if (heightCm != null) {
            val currentProfile = profileDao.getProfile()
            profileDao.upsert(
                UserProfileEntity(
                    heightCm = heightCm,
                    preferredUnitSystem = currentProfile?.preferredUnitSystem ?: UnitSystem.Metric.storageValue,
                    birthYear = currentProfile?.birthYear,
                    onboardingCompleted = currentProfile?.onboardingCompleted ?: false,
                    createdAtMillis = currentProfile?.createdAtMillis ?: now,
                    updatedAtMillis = now,
                ),
            )
        }
    }

    suspend fun setUnitSystem(unitSystem: UnitSystem, currentProfile: UserProfile) {
        val now = System.currentTimeMillis()
        profileDao.upsert(
            UserProfileEntity(
                heightCm = currentProfile.heightCm,
                preferredUnitSystem = unitSystem.storageValue,
                birthYear = currentProfile.birthYear,
                onboardingCompleted = currentProfile.onboardingCompleted,
                createdAtMillis = currentProfile.createdAtMillis.takeIf { it > 0 } ?: now,
                updatedAtMillis = now,
            ),
        )
    }

    suspend fun updateProfile(
        heightCm: Double?,
        preferredUnitSystem: UnitSystem,
        birthYear: Int?,
        onboardingCompleted: Boolean,
        currentProfile: UserProfile,
    ) {
        val now = System.currentTimeMillis()
        profileDao.upsert(
            UserProfileEntity(
                heightCm = heightCm,
                preferredUnitSystem = preferredUnitSystem.storageValue,
                birthYear = birthYear,
                onboardingCompleted = onboardingCompleted,
                createdAtMillis = currentProfile.createdAtMillis.takeIf { it > 0 } ?: now,
                updatedAtMillis = now,
            ),
        )
    }

    suspend fun delete(entry: WeightEntry) {
        entryDao.delete(entry.toEntity())
    }

    suspend fun snapshot(): WeightBackupSnapshot =
        WeightBackupSnapshot(
            entries = entryDao.entriesNewestFirst().map { it.toModel() },
            profile = profileDao.getProfile()?.toModel() ?: UserProfile(),
        )
}

data class WeightEntry(
    val id: Long,
    val date: LocalDate,
    val weightKg: Double,
    val waistCm: Double?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
)

data class UserProfile(
    val heightCm: Double? = null,
    val preferredUnitSystem: UnitSystem = UnitSystem.Metric,
    val birthYear: Int? = null,
    val onboardingCompleted: Boolean = false,
    val createdAtMillis: Long = 0,
    val updatedAtMillis: Long = 0,
)

data class WeightBackupSnapshot(
    val entries: List<WeightEntry>,
    val profile: UserProfile,
)

private fun WeightEntryEntity.toModel(): WeightEntry =
    WeightEntry(
        id = id,
        date = LocalDate.ofEpochDay(dateEpochDay),
        weightKg = weightKg,
        waistCm = waistCm,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )

private fun WeightEntry.toEntity(): WeightEntryEntity =
    WeightEntryEntity(
        id = id,
        dateEpochDay = date.toEpochDay(),
        weightKg = weightKg,
        waistCm = waistCm,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )

private fun UserProfileEntity.toModel(): UserProfile =
    UserProfile(
        heightCm = heightCm,
        preferredUnitSystem = UnitSystem.fromStorage(preferredUnitSystem),
        birthYear = birthYear,
        onboardingCompleted = onboardingCompleted,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
    )
