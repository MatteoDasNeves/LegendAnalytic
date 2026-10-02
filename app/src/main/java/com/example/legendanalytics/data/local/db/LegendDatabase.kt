package com.example.legendanalytics.data.local.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteTable
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.AutoMigrationSpec

@Database(
    entities = [SearchHistoryEntity::class, MatchEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        // v2 : tables lane_games et lane_sync (classement par lane), retirées en v3.
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3, spec = LegendDatabase.DropLaneTables::class),
    ],
)
abstract class LegendDatabase : RoomDatabase() {

    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun matchDao(): MatchDao

    @DeleteTable(tableName = "lane_games")
    @DeleteTable(tableName = "lane_sync")
    class DropLaneTables : AutoMigrationSpec

    companion object {
        private const val NAME = "legend_analytics.db"

        fun create(context: Context): LegendDatabase =
            Room.databaseBuilder(context.applicationContext, LegendDatabase::class.java, NAME).build()
    }
}
