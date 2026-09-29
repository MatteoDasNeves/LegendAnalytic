package com.example.legendanalytics.data.mapper

import com.example.legendanalytics.data.remote.dto.AccountDto
import com.example.legendanalytics.data.remote.dto.LeagueEntryDto
import com.example.legendanalytics.data.remote.dto.MatchDto
import com.example.legendanalytics.data.remote.dto.ObjectivesDto
import com.example.legendanalytics.data.remote.dto.ParticipantDto
import com.example.legendanalytics.data.remote.dto.SummonerDto
import com.example.legendanalytics.data.remote.dto.SummonerSpellsDto
import com.example.legendanalytics.data.remote.dto.TeamDto
import com.example.legendanalytics.domain.model.Account
import com.example.legendanalytics.domain.model.Match
import com.example.legendanalytics.domain.model.Participant
import com.example.legendanalytics.domain.model.PlayerProfile
import com.example.legendanalytics.domain.model.RankedEntry
import com.example.legendanalytics.domain.model.RankedQueue
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.domain.model.Team
import com.example.legendanalytics.domain.model.TeamObjectives

fun AccountDto.toDomain(fallback: RiotId): Account = Account(
    puuid = puuid,
    riotId = if (!gameName.isNullOrBlank() && !tagLine.isNullOrBlank()) RiotId(gameName, tagLine) else fallback,
)

fun LeagueEntryDto.toDomain(): RankedEntry? {
    val queue = when (queueType) {
        "RANKED_SOLO_5x5" -> RankedQueue.SOLO_DUO
        "RANKED_FLEX_SR" -> RankedQueue.FLEX
        else -> return null
    }
    return RankedEntry(
        queue = queue,
        tier = tier,
        division = rank,
        leaguePoints = leaguePoints,
        wins = wins,
        losses = losses,
    )
}

fun SummonerDto.toProfile(leagues: List<LeagueEntryDto>): PlayerProfile {
    val entries = leagues.mapNotNull { it.toDomain() }
    return PlayerProfile(
        puuid = puuid,
        summonerLevel = summonerLevel,
        profileIconId = profileIconId,
        soloDuo = entries.firstOrNull { it.queue == RankedQueue.SOLO_DUO },
        flex = entries.firstOrNull { it.queue == RankedQueue.FLEX },
    )
}

fun MatchDto.toDomain(): Match {
    // Avant fin 2021, gameDuration était en millisecondes et gameEndTimestamp absent.
    val durationSeconds = if (info.gameEndTimestamp != null) info.gameDuration else info.gameDuration / 1000
    val start = info.gameStartTimestamp.takeIf { it > 0 } ?: info.gameCreation
    return Match(
        matchId = metadata.matchId,
        queueId = info.queueId,
        gameMode = info.gameMode,
        gameStartMillis = start,
        gameEndMillis = info.gameEndTimestamp ?: (start + durationSeconds * 1000),
        durationSeconds = durationSeconds,
        participants = info.participants.map { it.toDomain() },
        teams = info.teams.map { it.toDomain() },
    )
}

fun ParticipantDto.toDomain(): Participant = Participant(
    puuid = puuid,
    riotId = if (!riotIdGameName.isNullOrBlank() && !riotIdTagline.isNullOrBlank()) {
        RiotId(riotIdGameName, riotIdTagline)
    } else {
        null
    },
    teamId = teamId,
    championName = championName,
    championLevel = champLevel,
    kills = kills,
    deaths = deaths,
    assists = assists,
    creepScore = totalMinionsKilled + neutralMinionsKilled,
    goldEarned = goldEarned,
    damageToChampions = totalDamageDealtToChampions,
    visionScore = visionScore,
    items = listOf(item0, item1, item2, item3, item4, item5, item6),
    summonerSpells = listOf(summoner1Id, summoner2Id),
    win = win,
    earlySurrender = gameEndedInEarlySurrender,
)

fun TeamDto.toDomain(): Team = Team(
    teamId = teamId,
    win = win,
    objectives = objectives?.toDomain(),
)

fun ObjectivesDto.toDomain(): TeamObjectives = TeamObjectives(
    towers = tower?.kills ?: 0,
    inhibitors = inhibitor?.kills ?: 0,
    dragons = dragon?.kills ?: 0,
    barons = baron?.kills ?: 0,
    riftHeralds = riftHerald?.kills ?: 0,
    voidGrubs = horde?.kills ?: 0,
    champions = champion?.kills ?: 0,
)

/** id numérique -> fichier image, ex. 4 -> "SummonerFlash.png". */
fun SummonerSpellsDto.toImageMap(): Map<Int, String> =
    data.values.mapNotNull { spell -> spell.key.toIntOrNull()?.let { it to spell.image.full } }.toMap()
