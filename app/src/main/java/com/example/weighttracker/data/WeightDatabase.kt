package com.example.weighttracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [WeightEntryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class WeightDatabase : RoomDatabase() {
    abstract fun weightEntryDao(): WeightEntryDao

    companion object {
        @Volatile
        private var instance: WeightDatabase? = null

        fun get(context: Context): WeightDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeightDatabase::class.java,
                    "weight-tracker.db",
                ).build().also { instance = it }
            }
    }
}
