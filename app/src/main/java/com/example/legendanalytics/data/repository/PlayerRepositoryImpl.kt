package com.example.legendanalytics.data.repository

import com.example.legendanalytics.data.mapper.toDomain
import com.example.legendanalytics.data.mapper.toProfile
import com.example.legendanalytics.data.remote.RiotApi
import com.example.legendanalytics.data.remote.safeCall
import com.example.legendanalytics.domain.model.Account
import com.example.legendanalytics.domain.model.AppResult
import com.example.legendanalytics.domain.model.PlayerProfile
import com.example.legendanalytics.domain.model.Region
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.domain.repository.PlayerRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class PlayerRepositoryImpl(private val api: RiotApi) : PlayerRepository {

    override suspend fun findAccount(riotId: RiotId, region: Region): AppResult<Account> = safeCall {
        api.getAccountByRiotId(riotId.gameName, riotId.tagLine, region).toDomain(fallback = riotId)
    }

    override suspend fun getProfile(puuid: String, region: Region): AppResult<PlayerProfile> = safeCall {
        coroutineScope {
            val summoner = async { api.getSummonerByPuuid(puuid, region) }
            val leagues = async { api.getLeagueEntries(puuid, region) }
            summoner.await().toProfile(leagues.await())
        }
    }
}
