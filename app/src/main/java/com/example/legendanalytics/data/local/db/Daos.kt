package com.example.legendanalytics.data.local.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.legendanalytics.domain.model.Region
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<SearchHistoryEntity>>

    @Upsert
    suspend fun upsert(entry: SearchHistoryEntity)

    @Query(
        """DELETE FROM search_history WHERE rowid NOT IN
           (SELECT rowid FROM search_history ORDER BY timestamp DESC LIMIT :keep)""",
    )
    suspend fun trimTo(keep: Int)

    /** Ajoute (ou remonte en tête) une recherche puis ne garde que les [keep] plus récentes. */
    @Transaction
    suspend fun insertAndTrim(entry: SearchHistoryEntity, keep: Int) {
        upsert(entry)
        trimTo(keep)
    }

    @Query(
        """DELETE FROM search_history
           WHERE game_name_key = :gameNameKey AND tag_line_key = :tagLineKey AND region = :region""",
    )
    suspend fun delete(gameNameKey: String, tagLineKey: String, region: Region)

    @Query("DELETE FROM search_history")
    suspend fun clear()
}

@Dao
interface MatchDao {

    @Query("SELECT * FROM matches WHERE match_id = :matchId")
    suspend fun get(matchId: String): MatchEntity?

    @Upsert
    suspend fun upsert(match: MatchEntity)

    @Query(
        """DELETE FROM matches WHERE match_id NOT IN
           (SELECT match_id FROM matches ORDER BY cached_at DESC LIMIT :keep)""",
    )
    suspend fun trimTo(keep: Int)

    /** Enregistre une partie puis ne garde que les [keep] plus récemment mises en cache. */
    @Transaction
    suspend fun insertAndTrim(match: MatchEntity, keep: Int) {
        upsert(match)
        trimTo(keep)
    }
}
