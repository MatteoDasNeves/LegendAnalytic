package com.example.legendanalytics.domain.repository

import com.example.legendanalytics.domain.model.Account
import com.example.legendanalytics.domain.model.AppResult
import com.example.legendanalytics.domain.model.Match
import com.example.legendanalytics.domain.model.MatchPage
import com.example.legendanalytics.domain.model.PlayerProfile
import com.example.legendanalytics.domain.model.Region
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.domain.model.SearchHistoryEntry
import com.example.legendanalytics.domain.model.StaticData
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    suspend fun findAccount(riotId: RiotId, region: Region): AppResult<Account>
    suspend fun getProfile(puuid: String, region: Region): AppResult<PlayerProfile>
}

interface MatchRepository {
    suspend fun getMatches(puuid: String, region: Region, start: Int, count: Int): AppResult<MatchPage>
    suspend fun getMatch(matchId: String, region: Region): AppResult<Match>
}

interface StaticDataRepository {
    /** Ne renvoie jamais d'erreur bloquante : null si Data Dragon est indisponible. */
    suspend fun getStaticData(): StaticData?
}

interface SearchHistoryRepository {
    val history: Flow<List<SearchHistoryEntry>>
    suspend fun add(riotId: RiotId, region: Region)
    suspend fun remove(entry: SearchHistoryEntry)
    suspend fun clear()
}
