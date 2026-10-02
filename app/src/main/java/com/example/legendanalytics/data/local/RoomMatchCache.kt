package com.example.legendanalytics.data.local

import com.example.legendanalytics.data.local.db.MatchDao
import com.example.legendanalytics.data.local.db.MatchEntity
import com.example.legendanalytics.domain.model.Match
import kotlinx.serialization.json.Json

/**
 * Cache persistant (Room) : les parties restent disponibles d'un lancement à l'autre.
 * Un [MatchMemoryCache] placé devant évite de relire et décoder le JSON à chaque accès.
 */
class RoomMatchCache(
    private val dao: MatchDao,
    private val memory: MatchMemoryCache = MatchMemoryCache(),
    private val maxSize: Int = 500,
    private val clock: () -> Long = System::currentTimeMillis,
) : MatchCache {

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun get(matchId: String): Match? {
        memory.get(matchId)?.let { return it }
        val entity = dao.get(matchId) ?: return null
        // Un JSON illisible (modèle modifié depuis) est traité comme une absence de cache.
        return runCatching { json.decodeFromString(Match.serializer(), entity.payload) }
            .getOrNull()
            ?.also { memory.put(it) }
    }

    override suspend fun put(match: Match) {
        memory.put(match)
        val entity = MatchEntity(
            matchId = match.matchId,
            gameStartMillis = match.gameStartMillis,
            cachedAt = clock(),
            payload = json.encodeToString(Match.serializer(), match),
        )
        dao.insertAndTrim(entity, maxSize)
    }
}
