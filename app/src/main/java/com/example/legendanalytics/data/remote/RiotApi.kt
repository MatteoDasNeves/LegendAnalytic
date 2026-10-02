package com.example.legendanalytics.data.remote

import com.example.legendanalytics.data.remote.dto.AccountDto
import com.example.legendanalytics.data.remote.dto.ChampionMasteryDto
import com.example.legendanalytics.data.remote.dto.LeagueEntryDto
import com.example.legendanalytics.data.remote.dto.MatchDto
import com.example.legendanalytics.data.remote.dto.SummonerDto
import com.example.legendanalytics.domain.model.AppError
import com.example.legendanalytics.domain.model.Region
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay

/**
 * Accès bas niveau à l'API Riot. Chaque appel renvoie un DTO ou lève une [RiotApiException].
 * Les 429 sont réessayés automatiquement si le `Retry-After` reste raisonnable.
 */
class RiotApi(
    private val client: HttpClient,
    private val apiKey: String,
    private val maxRetries: Int = 2,
    private val maxRetryWaitSeconds: Long = 10,
) {

    suspend fun getAccountByRiotId(gameName: String, tagLine: String, region: Region): AccountDto =
        execute(region.accountCluster.host, "riot", "account", "v1", "accounts", "by-riot-id", gameName, tagLine).body()

    suspend fun getSummonerByPuuid(puuid: String, region: Region): SummonerDto =
        execute(region.platformHost, "lol", "summoner", "v4", "summoners", "by-puuid", puuid).body()

    suspend fun getLeagueEntries(puuid: String, region: Region): List<LeagueEntryDto> =
        execute(region.platformHost, "lol", "league", "v4", "entries", "by-puuid", puuid).body()

    suspend fun getTopMasteries(puuid: String, region: Region, count: Int): List<ChampionMasteryDto> =
        execute(
            region.platformHost, "lol", "champion-mastery", "v4", "champion-masteries", "by-puuid", puuid, "top",
            query = mapOf("count" to count.toString()),
        ).body()

    /** Toutes les maîtrises du joueur, de la plus haute à la plus basse. */
    suspend fun getAllMasteries(puuid: String, region: Region): List<ChampionMasteryDto> =
        execute(region.platformHost, "lol", "champion-mastery", "v4", "champion-masteries", "by-puuid", puuid).body()

    suspend fun getMatchIds(puuid: String, region: Region, start: Int, count: Int): List<String> =
        execute(
            region.matchCluster.host, "lol", "match", "v5", "matches", "by-puuid", puuid, "ids",
            query = mapOf("start" to start.toString(), "count" to count.toString()),
        ).body()

    suspend fun getMatch(matchId: String, region: Region): MatchDto =
        execute(region.matchCluster.host, "lol", "match", "v5", "matches", matchId).body()

    private suspend fun execute(
        host: String,
        vararg segments: String,
        query: Map<String, String> = emptyMap(),
    ): HttpResponse {
        if (apiKey.isBlank()) throw RiotApiException(AppError.MissingApiKey)

        // Les segments sont encodés un par un (espaces, accents dans les Riot ID).
        val url = URLBuilder(protocol = URLProtocol.HTTPS, host = host).apply {
            appendPathSegments(*segments)
            query.forEach { (key, value) -> parameters.append(key, value) }
        }.build()

        var attempt = 0
        while (true) {
            val response = client.get(url)
            if (response.status.isSuccess()) return response

            val error = response.toAppError()
            if (error is AppError.RateLimited && attempt < maxRetries) {
                val wait = error.retryAfterSeconds ?: 1
                if (wait <= maxRetryWaitSeconds) {
                    attempt++
                    delay(wait * 1_000)
                    continue
                }
            }
            throw RiotApiException(error)
        }
    }

    private fun HttpResponse.toAppError(): AppError = when (status) {
        HttpStatusCode.Unauthorized, HttpStatusCode.Forbidden -> AppError.InvalidApiKey
        HttpStatusCode.NotFound -> AppError.NotFound
        HttpStatusCode.TooManyRequests ->
            AppError.RateLimited(headers[HttpHeaders.RetryAfter]?.trim()?.toLongOrNull())
        else -> AppError.Server(status.value)
    }
}
