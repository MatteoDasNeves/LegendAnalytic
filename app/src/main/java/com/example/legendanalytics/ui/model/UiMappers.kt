package com.example.legendanalytics.ui.model

import com.example.legendanalytics.data.remote.DataDragonUrls
import com.example.legendanalytics.domain.model.Match
import com.example.legendanalytics.domain.model.MatchOutcome
import com.example.legendanalytics.domain.model.Participant
import com.example.legendanalytics.domain.model.PlayerProfile
import com.example.legendanalytics.domain.model.RankedEntry
import com.example.legendanalytics.domain.model.RankedQueue
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.domain.model.StaticData
import com.example.legendanalytics.domain.util.Stats

/** Mappers domaine -> UI : calculs d'affichage et URLs Data Dragon. */
class UiMapper(private val staticData: StaticData?) {

    private val version get() = staticData?.version

    fun profileHeader(riotId: RiotId, profile: PlayerProfile) = ProfileHeaderUi(
        riotId = riotId,
        level = profile.summonerLevel,
        profileIconUrl = version?.let { DataDragonUrls.profileIcon(it, profile.profileIconId) },
        soloDuo = rank(RankedQueue.SOLO_DUO, profile.soloDuo),
        flex = rank(RankedQueue.FLEX, profile.flex),
    )

    fun rank(queue: RankedQueue, entry: RankedEntry?) = RankUi(
        queue = queue,
        tier = entry?.tier?.takeIf { it.isNotBlank() },
        division = entry?.division.orEmpty(),
        leaguePoints = entry?.leaguePoints ?: 0,
        wins = entry?.wins ?: 0,
        losses = entry?.losses ?: 0,
        winrate = entry?.let { Stats.winrate(it.wins, it.losses) } ?: 0,
    )

    fun matchCard(match: Match, puuid: String): MatchCardUi? {
        val me = match.participant(puuid) ?: return null
        val teamKills = match.participants.filter { it.teamId == me.teamId }.sumOf { it.kills }
        return MatchCardUi(
            matchId = match.matchId,
            outcome = match.outcomeFor(me),
            queueId = match.queueId,
            gameMode = match.gameMode,
            gameEndMillis = match.gameEndMillis,
            durationSeconds = match.durationSeconds,
            championName = me.championName,
            championIconUrl = championUrl(me.championName),
            championLevel = me.championLevel,
            kills = me.kills,
            deaths = me.deaths,
            assists = me.assists,
            kda = Stats.kda(me.kills, me.deaths, me.assists),
            creepScore = me.creepScore,
            csPerMinute = Stats.csPerMinute(me.creepScore, match.durationSeconds),
            killParticipation = Stats.killParticipation(me.kills, me.assists, teamKills),
            itemIconUrls = me.items.map(::itemUrl),
            spellIconUrls = me.summonerSpells.map(::spellUrl),
        )
    }

    fun matchDetail(match: Match, focusPuuid: String): MatchDetailUi {
        val maxDamage = match.participants.maxOfOrNull { it.damageToChampions }?.coerceAtLeast(1) ?: 1
        val teamIds = (match.teams.map { it.teamId } + match.participants.map { it.teamId }).distinct().sorted()
        val teams = teamIds.map { teamId ->
            val members = match.participants.filter { it.teamId == teamId }
            val team = match.teams.firstOrNull { it.teamId == teamId }
            val outcome = when {
                members.any { it.earlySurrender } -> MatchOutcome.REMAKE
                team?.win ?: members.firstOrNull()?.win ?: false -> MatchOutcome.VICTORY
                else -> MatchOutcome.DEFEAT
            }
            TeamUi(
                teamId = teamId,
                outcome = outcome,
                totalKills = members.sumOf { it.kills },
                totalGold = members.sumOf { it.goldEarned },
                objectives = team?.objectives,
                participants = members.map { participant(it, focusPuuid, maxDamage) },
            )
        }
        return MatchDetailUi(
            matchId = match.matchId,
            queueId = match.queueId,
            gameMode = match.gameMode,
            gameEndMillis = match.gameEndMillis,
            durationSeconds = match.durationSeconds,
            teams = teams,
        )
    }

    private fun participant(p: Participant, focusPuuid: String, maxDamage: Int) = ParticipantUi(
        puuid = p.puuid,
        riotId = p.riotId,
        championName = p.championName,
        championIconUrl = championUrl(p.championName),
        championLevel = p.championLevel,
        kills = p.kills,
        deaths = p.deaths,
        assists = p.assists,
        kda = Stats.kda(p.kills, p.deaths, p.assists),
        creepScore = p.creepScore,
        damageToChampions = p.damageToChampions,
        damageRatio = p.damageToChampions.toFloat() / maxDamage,
        goldEarned = p.goldEarned,
        visionScore = p.visionScore,
        itemIconUrls = p.items.map(::itemUrl),
        spellIconUrls = p.summonerSpells.map(::spellUrl),
        isFocused = p.puuid == focusPuuid,
    )

    private fun championUrl(name: String): String? =
        version?.takeIf { name.isNotBlank() }?.let { DataDragonUrls.champion(it, name) }

    private fun itemUrl(itemId: Int): String? =
        version?.takeIf { itemId > 0 }?.let { DataDragonUrls.item(it, itemId) }

    private fun spellUrl(spellId: Int): String? {
        val v = version ?: return null
        val file = staticData?.summonerSpellImages?.get(spellId) ?: return null
        return DataDragonUrls.summonerSpell(v, file)
    }
}
