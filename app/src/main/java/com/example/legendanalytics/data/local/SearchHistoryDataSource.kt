package com.example.legendanalytics.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.legendanalytics.domain.model.Region
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.domain.model.SearchHistoryEntry
import com.example.legendanalytics.domain.repository.SearchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.searchHistoryStore: DataStore<Preferences> by preferencesDataStore(name = "search_history")

@Serializable
private data class StoredSearch(
    val gameName: String,
    val tagLine: String,
    val region: Region,
    val timestamp: Long,
)

/** Historique des recherches, stocké en JSON dans DataStore Preferences. */
class SearchHistoryDataSource(
    context: Context,
    private val maxEntries: Int = 10,
    private val clock: () -> Long = System::currentTimeMillis,
) : SearchHistoryRepository {

    private val store = context.applicationContext.searchHistoryStore
    private val key = stringPreferencesKey("entries")
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(StoredSearch.serializer())

    override val history: Flow<List<SearchHistoryEntry>> = store.data.map { prefs ->
        decode(prefs[key]).map { SearchHistoryEntry(RiotId(it.gameName, it.tagLine), it.region, it.timestamp) }
    }

    override suspend fun add(riotId: RiotId, region: Region) {
        store.edit { prefs ->
            val current = decode(prefs[key])
            val updated = buildList {
                add(StoredSearch(riotId.gameName, riotId.tagLine, region, clock()))
                addAll(current.filterNot { it.matches(riotId, region) })
            }.take(maxEntries)
            prefs[key] = json.encodeToString(serializer, updated)
        }
    }

    override suspend fun remove(entry: SearchHistoryEntry) {
        store.edit { prefs ->
            val updated = decode(prefs[key]).filterNot { it.matches(entry.riotId, entry.region) }
            prefs[key] = json.encodeToString(serializer, updated)
        }
    }

    override suspend fun clear() {
        store.edit { it.remove(key) }
    }

    private fun decode(raw: String?): List<StoredSearch> =
        raw?.let { runCatching { json.decodeFromString(serializer, it) }.getOrNull() }.orEmpty()

    private fun StoredSearch.matches(riotId: RiotId, region: Region) =
        this.region == region &&
            gameName.equals(riotId.gameName, ignoreCase = true) &&
            tagLine.equals(riotId.tagLine, ignoreCase = true)
}
