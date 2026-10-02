package com.example.legendanalytics.data.local

import com.example.legendanalytics.data.local.db.SearchHistoryDao
import com.example.legendanalytics.data.local.db.SearchHistoryEntity
import com.example.legendanalytics.domain.model.Region
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.domain.model.SearchHistoryEntry
import com.example.legendanalytics.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Historique des recherches, stocké dans la table Room `search_history`. */
class SearchHistoryLocalDataSource(
    private val dao: SearchHistoryDao,
    private val maxEntries: Int = 10,
    private val clock: () -> Long = System::currentTimeMillis,
) : SearchHistoryRepository {

    override val history: Flow<List<SearchHistoryEntry>> = dao.observeRecent(maxEntries).map { rows ->
        rows.map { SearchHistoryEntry(RiotId(it.gameName, it.tagLine), it.region, it.timestamp) }
    }

    override suspend fun add(riotId: RiotId, region: Region) {
        val entry = SearchHistoryEntity(
            gameNameKey = riotId.gameName.lowercase(),
            tagLineKey = riotId.tagLine.lowercase(),
            region = region,
            gameName = riotId.gameName,
            tagLine = riotId.tagLine,
            timestamp = clock(),
        )
        dao.insertAndTrim(entry, maxEntries)
    }

    override suspend fun remove(entry: SearchHistoryEntry) {
        dao.delete(entry.riotId.gameName.lowercase(), entry.riotId.tagLine.lowercase(), entry.region)
    }

    override suspend fun clear() = dao.clear()
}
