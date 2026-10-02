package com.example.legendanalytics.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class AccountDto(
    val puuid: String,
    val gameName: String? = null,
    val tagLine: String? = null,
)

@Serializable
data class SummonerDto(
    val puuid: String,
    val profileIconId: Int = 0,
    val summonerLevel: Long = 0,
    val revisionDate: Long = 0,
)

@Serializable
data class LeagueEntryDto(
    val queueType: String,
    val tier: String = "",
    val rank: String = "",
    val leaguePoints: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
)

@Serializable
data class MatchDto(
    val metadata: MatchMetadataDto,
    val info: MatchInfoDto,
)

@Serializable
data class MatchMetadataDto(
    val matchId: String,
    val participants: List<String> = emptyList(),
)

@Serializable
data class MatchInfoDto(
    val gameCreation: Long = 0,
    val gameStartTimestamp: Long = 0,
    val gameEndTimestamp: Long? = null,
    /** En secondes si gameEndTimestamp est présent, en millisecondes sinon (anciennes parties). */
    val gameDuration: Long = 0,
    val gameMode: String = "",
    val queueId: Int = 0,
    val participants: List<ParticipantDto> = emptyList(),
    val teams: List<TeamDto> = emptyList(),
)

@Serializable
data class ParticipantDto(
    val puuid: String = "",
    val riotIdGameName: String? = null,
    val riotIdTagline: String? = null,
    val summonerName: String? = null,
    val teamId: Int = 0,
    val championName: String = "",
    val champLevel: Int = 0,
    val kills: Int = 0,
    val deaths: Int = 0,
    val assists: Int = 0,
    val totalMinionsKilled: Int = 0,
    val neutralMinionsKilled: Int = 0,
    val goldEarned: Int = 0,
    val totalDamageDealtToChampions: Int = 0,
    val visionScore: Int = 0,
    val item0: Int = 0,
    val item1: Int = 0,
    val item2: Int = 0,
    val item3: Int = 0,
    val item4: Int = 0,
    val item5: Int = 0,
    val item6: Int = 0,
    val summoner1Id: Int = 0,
    val summoner2Id: Int = 0,
    val win: Boolean = false,
    val gameEndedInEarlySurrender: Boolean = false,
    /** Arena : classement final (1 à 8) et duo du joueur ; 0 dans les autres modes. */
    val placement: Int = 0,
    val playerSubteamId: Int = 0,
)

@Serializable
data class TeamDto(
    val teamId: Int,
    val win: Boolean = false,
    val objectives: ObjectivesDto? = null,
)

@Serializable
data class ObjectivesDto(
    val baron: ObjectiveDto? = null,
    val champion: ObjectiveDto? = null,
    val dragon: ObjectiveDto? = null,
    val horde: ObjectiveDto? = null,
    val inhibitor: ObjectiveDto? = null,
    val riftHerald: ObjectiveDto? = null,
    val tower: ObjectiveDto? = null,
)

@Serializable
data class ObjectiveDto(
    val first: Boolean = false,
    val kills: Int = 0,
)

@Serializable
data class ChampionMasteryDto(
    val championId: Int,
    val championLevel: Int = 0,
    val championPoints: Int = 0,
    val lastPlayTime: Long = 0,
)
