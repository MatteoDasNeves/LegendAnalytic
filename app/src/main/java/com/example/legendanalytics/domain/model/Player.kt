package com.example.legendanalytics.domain.model

data class Account(
    val puuid: String,
    val riotId: RiotId,
)

enum class RankedQueue { SOLO_DUO, FLEX }

data class RankedEntry(
    val queue: RankedQueue,
    val tier: String,
    val division: String,
    val leaguePoints: Int,
    val wins: Int,
    val losses: Int,
)

data class PlayerProfile(
    val puuid: String,
    val summonerLevel: Long,
    val profileIconId: Int,
    val soloDuo: RankedEntry?,
    val flex: RankedEntry?,
)

data class SearchHistoryEntry(
    val riotId: RiotId,
    val region: Region,
    val timestamp: Long,
)
