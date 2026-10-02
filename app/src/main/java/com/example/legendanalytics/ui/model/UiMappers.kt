package com.example.legendanalytics.ui.model

import com.example.legendanalytics.data.remote.DataDragonUrls
import com.example.legendanalytics.domain.model.ChampionMastery
import com.example.legendanalytics.domain.model.Match
import com.example.legendanalytics.domain.model.MatchOutcome
import com.example.legendanalytics.domain.model.Participant
import com.example.legendanalytics.domain.model.PlayerProfile
import com.example.legendanalytics.domain.model.RankedEntry
import com.example.legendanalytics.domain.model.RankedQueue
import com.example.legendanalytics.domain.model.Region
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.domain.model.StaticData
import com.example.legendanalytics.domain.util.PlayerStats
import com.example.legendanalytics.domain.util.Stats

/** Mappers domaine -> UI : calculs d'affichage et URLs Data Dragon. */
class UiMapper(private val staticData: StaticData?) {

    private val version get() = staticData?.version

    fun profileHeader(
        riotId: RiotId,
        region: Region,
        profile: PlayerProfile,
        mains: List<MainChampionUi>,
    ) = ProfileHeaderUi(
        riotId = riotId,
        regionLabel = region.label,
        level = profile.summonerLevel,
        profileIconUrl = version?.let { DataDragonUrls.profileIcon(it, profile.profileIconId) },
        bannerUrl = mains.firstOrNull()?.let { DataDragonUrls.championSplash(it.championName) },
        soloDuo = rank(RankedQueue.SOLO_DUO, profile.soloDuo),
        flex = rank(RankedQueue.FLEX, profile.flex),
    )

    fun overview(profile: PlayerProfile, matches: List<Match>, puuid: String): OverviewUi {
        val ranked = PlayerStats.rankedTotal(profile.soloDuo, profile.flex)
        val recent = PlayerStats.recentSummary(matches, puuid)
        return OverviewUi(
            rankedGames = ranked.games,
            rankedWins = ranked.wins,
            rankedLosses = ranked.losses,
            rankedWinrate = ranked.winrate,
            recentGames = recent.record.games,
            recentWins = recent.record.wins,
            recentLosses = recent.record.losses,
            recentWinrate = recent.record.winrate,
            recentKda = recent.kda,
            averageKills = recent.average(recent.kills),
            averageDeaths = recent.average(recent.deaths),
            averageAssists = recent.average(recent.assists),
            form = recent.form.take(FORM_SIZE),
        )
    }

    /**
     * Champions principaux : d'abord les meilleures maîtrises (vision long terme), complétées par
     * les champions les plus joués récemment si les maîtrises sont indisponibles.
     */
    fun mains(profile: PlayerProfile, matches: List<Match>, puuid: String): List<MainChampionUi> {
        val records = PlayerStats.championRecords(matches, puuid)
        fun recordOf(championName: String) = records.firstOrNull { it.championName.equals(championName, ignoreCase = true) }

        val fromMastery = profile.topMasteries.mapNotNull { mastery ->
            val info = staticData?.champions?.get(mastery.championId) ?: return@mapNotNull null
            val record = recordOf(info.id)
            MainChampionUi(
                championName = info.id,
                displayName = info.name,
                tileUrl = DataDragonUrls.championTile(info.id),
                masteryLevel = mastery.level,
                masteryPoints = mastery.points,
                recentGames = record?.record?.games ?: 0,
                recentWinrate = record?.record?.winrate ?: 0,
                recentKda = record?.kda,
            )
        }
        if (fromMastery.isNotEmpty()) return fromMastery.take(MAINS_SIZE)

        return records.take(MAINS_SIZE).map { record ->
            MainChampionUi(
                championName = record.championName,
                displayName = displayName(record.championName),
                tileUrl = DataDragonUrls.championTile(record.championName),
                masteryLevel = null,
                masteryPoints = null,
                recentGames = record.record.games,
                recentWinrate = record.record.winrate,
                recentKda = record.kda,
            )
        }
    }

    /** Une entrée par champion maîtrisé, de la plus haute à la plus basse maîtrise. */
    fun affinities(masteries: List<ChampionMastery>): List<ChampionAffinityUi> =
        masteries.sortedByDescending { it.points }.map { mastery ->
            val info = staticData?.champions?.get(mastery.championId)
            ChampionAffinityUi(
                championId = mastery.championId,
                displayName = info?.name ?: "#${mastery.championId}",
                iconUrl = info?.let { championUrl(it.id) },
                masteryLevel = mastery.level,
                masteryPoints = mastery.points,
                lastPlayMillis = mastery.lastPlayMillis,
                classes = info?.classes.orEmpty(),
            )
        }

    private fun displayName(championName: String): String =
        staticData?.champions?.values?.firstOrNull { it.id.equals(championName, ignoreCase = true) }?.name
            ?: championName

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
            championDisplayName = displayName(me.championName),
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
            placement = me.placement.takeIf { match.isArena },
        )
    }

    fun matchDetail(match: Match, focusPuuid: String): MatchDetailUi {
        val maxDamage = match.participants.maxOfOrNull { it.damageToChampions }?.coerceAtLeast(1) ?: 1
        if (match.isArena) return arenaDetail(match, focusPuuid, maxDamage)
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

    /** Arena : un bloc par duo, du 1er au 8e. */
    private fun arenaDetail(match: Match, focusPuuid: String, maxDamage: Int): MatchDetailUi {
        val duos = match.participants
            .groupBy { it.subteamId ?: it.teamId }
            .map { (subteamId, members) ->
                val placement = members.firstNotNullOfOrNull { it.placement }
                TeamUi(
                    teamId = subteamId,
                    placement = placement,
                    outcome = if (members.any { it.win }) MatchOutcome.VICTORY else MatchOutcome.DEFEAT,
                    totalKills = members.sumOf { it.kills },
                    totalGold = members.sumOf { it.goldEarned },
                    objectives = null,
                    participants = members.map { participant(it, focusPuuid, maxDamage) },
                )
            }
            .sortedBy { it.placement ?: Int.MAX_VALUE }
        return MatchDetailUi(
            matchId = match.matchId,
            queueId = match.queueId,
            gameMode = match.gameMode,
            gameEndMillis = match.gameEndMillis,
            durationSeconds = match.durationSeconds,
            teams = duos,
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

    private companion object {
        const val FORM_SIZE = 10
        const val MAINS_SIZE = 3
    }
}
