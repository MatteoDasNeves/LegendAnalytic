package com.example.legendanalytics.data.local

import com.example.legendanalytics.domain.model.Match
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Cache LRU en mémoire des parties déjà chargées. Une partie terminée ne change plus,
 * donc aucune expiration n'est nécessaire.
 */
class MatchMemoryCache(private val maxSize: Int = 300) {

    private val mutex = Mutex()
    private val entries = object : LinkedHashMap<String, Match>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Match>?): Boolean =
            size > maxSize
    }

    suspend fun get(matchId: String): Match? = mutex.withLock { entries[matchId] }

    suspend fun put(match: Match) = mutex.withLock { entries[match.matchId] = match }

    suspend fun size(): Int = mutex.withLock { entries.size }
}
