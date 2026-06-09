package com.example.weighttracker.data

import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WeightRepository(
    private val dao: WeightEntryDao,
) {
    val entries: Flow<List<WeightEntry>> =
        dao.observeEntriesNewestFirst().map { entities -> entities.map { it.toModel() } }

    suspend fun save(
        id: Long?,
        date: LocalDate,
        weightKg: Double,
        waistCm: Double?,
        existingCreatedAtMillis: Long?,
    ) {
        val now = System.currentTimeMillis()
        val entity = WeightEntryEntity(
            id = id ?: 0,
            dateEpochDay = date.toEpochDay(),
            weightKg = weightKg,
            waistCm = waistCm,
            createdAtMillis = existingCreatedAtMillis ?: now,
            updatedAtMillis = now,
        )
        if (id == null || id == 0L) {
            dao.insert(entity)
        } else {
            dao.update(entity)
        }
    }

    suspend fun delete(entry: WeightEntry) {
        dao.delete(entry.toEntity())
    }
}

data class WeightEntry(
    val id: Long,
    val date: LocalDate,
    val weightKg: Double,
    val waistCm: Double?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
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
