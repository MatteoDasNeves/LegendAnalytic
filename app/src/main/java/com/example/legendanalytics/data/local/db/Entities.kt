package com.example.legendanalytics.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.legendanalytics.domain.model.Region

/**
 * Une recherche passée. La clé est normalisée en minuscules pour qu'une même personne
 * recherchée avec une casse différente ne crée pas de doublon.
 */
@Entity(
    tableName = "search_history",
    primaryKeys = ["game_name_key", "tag_line_key", "region"],
    indices = [Index("timestamp")],
)
data class SearchHistoryEntity(
    @ColumnInfo(name = "game_name_key") val gameNameKey: String,
    @ColumnInfo(name = "tag_line_key") val tagLineKey: String,
    val region: Region,
    @ColumnInfo(name = "game_name") val gameName: String,
    @ColumnInfo(name = "tag_line") val tagLine: String,
    val timestamp: Long,
)

/** Une partie terminée, stockée en JSON : elle ne change plus une fois jouée. */
@Entity(tableName = "matches", indices = [Index("cached_at")])
data class MatchEntity(
    @PrimaryKey @ColumnInfo(name = "match_id") val matchId: String,
    @ColumnInfo(name = "game_start_millis") val gameStartMillis: Long,
    @ColumnInfo(name = "cached_at") val cachedAt: Long,
    val payload: String,
)
