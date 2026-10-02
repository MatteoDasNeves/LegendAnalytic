package com.example.legendanalytics.data.repository

import com.example.legendanalytics.data.mapper.toDomain
import com.example.legendanalytics.data.mapper.toProfile
import com.example.legendanalytics.data.remote.RiotApi
import com.example.legendanalytics.data.remote.dto.ChampionMasteryDto
import com.example.legendanalytics.data.remote.safeCall
import com.example.legendanalytics.domain.model.Account
import com.example.legendanalytics.domain.model.AppResult
import com.example.legendanalytics.domain.model.ChampionMastery
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
            // Les maîtrises sont un bonus : leur échec ne doit pas empêcher d'afficher le profil.
            val masteries = async {
                when (val result = safeCall { api.getTopMasteries(puuid, region, TOP_MASTERIES) }) {
                    is AppResult.Success -> result.data
                    is AppResult.Failure -> emptyList<ChampionMasteryDto>()
                }
            }
            summoner.await().toProfile(leagues.await(), masteries.await())
        }
    }

    override suspend fun getMasteries(puuid: String, region: Region): AppResult<List<ChampionMastery>> = safeCall {
        api.getAllMasteries(puuid, region).map { it.toDomain() }.sortedByDescending { it.points }
    }

    private companion object {
        const val TOP_MASTERIES = 3
    }
}
