package com.aditya.weighttracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [WeightEntryEntity::class, UserProfileEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class WeightDatabase : RoomDatabase() {
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var instance: WeightDatabase? = null

        private val migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `user_profile` (
                        `id` INTEGER NOT NULL,
                        `heightCm` REAL,
                        `createdAtMillis` INTEGER NOT NULL,
                        `updatedAtMillis` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
            }
        }

        private val migration2To3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE `user_profile` ADD COLUMN `preferredUnitSystem` TEXT NOT NULL DEFAULT 'METRIC'",
                )
            }
        }

        private val migration3To4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `user_profile` ADD COLUMN `birthYear` INTEGER")
                db.execSQL(
                    "ALTER TABLE `user_profile` ADD COLUMN `onboardingCompleted` INTEGER NOT NULL DEFAULT 0",
                )
                db.execSQL(
                    """
                    UPDATE `user_profile`
                    SET `onboardingCompleted` = 1
                    WHERE EXISTS (SELECT 1 FROM `weight_entries` LIMIT 1)
                    """.trimIndent(),
                )
            }
        }

        fun get(context: Context): WeightDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WeightDatabase::class.java,
                    "weight-tracker.db",
                ).addMigrations(migration1To2, migration2To3, migration3To4)
                    .build()
                    .also { instance = it }
            }
    }
}
