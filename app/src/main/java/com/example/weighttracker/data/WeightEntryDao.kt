package com.example.weighttracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightEntryDao {
    @Query("SELECT * FROM weight_entries ORDER BY dateEpochDay DESC, id DESC")
    fun observeEntriesNewestFirst(): Flow<List<WeightEntryEntity>>

    @Query("SELECT * FROM weight_entries WHERE dateEpochDay = :dateEpochDay ORDER BY id DESC")
    suspend fun entriesForDate(dateEpochDay: Long): List<WeightEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WeightEntryEntity): Long

    @Update
    suspend fun update(entry: WeightEntryEntity)

    @Query("DELETE FROM weight_entries WHERE dateEpochDay = :dateEpochDay AND id != :keepId")
    suspend fun deleteOtherEntriesForDate(dateEpochDay: Long, keepId: Long)

    @Delete
    suspend fun delete(entry: WeightEntryEntity)
}
