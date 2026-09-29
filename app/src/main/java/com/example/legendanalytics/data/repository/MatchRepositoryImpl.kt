package com.example.legendanalytics.data.repository

import com.example.legendanalytics.data.local.MatchMemoryCache
import com.example.legendanalytics.data.mapper.toDomain
import com.example.legendanalytics.data.remote.RiotApi
import com.example.legendanalytics.data.remote.safeCall
import com.example.legendanalytics.domain.model.AppError
import com.example.legendanalytics.domain.model.AppResult
import com.example.legendanalytics.domain.model.Match
import com.example.legendanalytics.domain.model.MatchPage
import com.example.legendanalytics.domain.model.Region
import com.example.legendanalytics.domain.repository.MatchRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class MatchRepositoryImpl(
    private val api: RiotApi,
    private val cache: MatchMemoryCache,
    /** Nombre maximal de détails de parties chargés simultanément (clé de dev : 20 req/s). */
    maxConcurrentRequests: Int = 4,
) : MatchRepository {

    // Partagé entre tous les appels pour que deux écrans ne doublent pas la charge.
    private val semaphore = Semaphore(maxConcurrentRequests)

    override suspend fun getMatches(
        puuid: String,
        region: Region,
        start: Int,
        count: Int,
    ): AppResult<MatchPage> {
        val ids = when (val result = safeCall { api.getMatchIds(puuid, region, start, count) }) {
            is AppResult.Success -> result.data
            is AppResult.Failure -> return result
        }

        val results = coroutineScope {
            ids.map { id -> async { getMatch(id, region) } }.awaitAll()
        }
        val matches = results.filterIsInstance<AppResult.Success<Match>>().map { it.data }
        val failures = results.filterIsInstance<AppResult.Failure>()

        // Si tout a échoué, on remonte la première erreur plutôt qu'une liste vide trompeuse.
        if (ids.isNotEmpty() && matches.isEmpty()) {
            return AppResult.Failure(failures.firstOrNull()?.error ?: AppError.Unknown(null))
        }
        return AppResult.Success(
            MatchPage(
                matches = matches.sortedByDescending { it.gameStartMillis },
                hasMore = ids.size >= count,
                failedCount = failures.size,
            ),
        )
    }

    override suspend fun getMatch(matchId: String, region: Region): AppResult<Match> {
        cache.get(matchId)?.let { return AppResult.Success(it) }
        return safeCall {
            semaphore.withPermit { api.getMatch(matchId, region) }.toDomain().also { cache.put(it) }
        }
    }
}
