package com.example.legendanalytics.domain.util

import com.example.legendanalytics.domain.model.Match
import com.example.legendanalytics.domain.model.MatchOutcome
import com.example.legendanalytics.domain.model.RankedEntry

/** Bilan victoires / défaites (les remakes ne comptent pas). */
data class WinLossRecord(val wins: Int, val losses: Int) {
    val games: Int get() = wins + losses
    val winrate: Int get() = Stats.winrate(wins, losses)
}

/** Statistiques cumulées d'un joueur sur un champion, calculées à partir des parties chargées. */
data class ChampionRecord(
    val championName: String,
    val record: WinLossRecord,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
) {
    val kda: Double get() = Stats.kda(kills, deaths, assists)
}

/** Résumé des parties récentes chargées. */
data class RecentSummary(
    val record: WinLossRecord,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    /** Issues des parties, de la plus récente à la plus ancienne (remakes inclus). */
    val form: List<MatchOutcome>,
) {
    val kda: Double get() = Stats.kda(kills, deaths, assists)
    fun average(total: Int): Double = if (record.games == 0) 0.0 else total.toDouble() / record.games
}

object PlayerStats {

    /** Total des parties classées de la saison (Solo/Duo + Flex). */
    fun rankedTotal(vararg entries: RankedEntry?): WinLossRecord =
        WinLossRecord(
            wins = entries.sumOf { it?.wins ?: 0 },
            losses = entries.sumOf { it?.losses ?: 0 },
        )

    fun recentSummary(matches: List<Match>, puuid: String): RecentSummary {
        val played = matches
            .sortedByDescending { it.gameStartMillis }
            .mapNotNull { match -> match.participant(puuid)?.let { match to it } }
        val counted = played.filter { (match, me) -> match.outcomeFor(me) != MatchOutcome.REMAKE }
        return RecentSummary(
            record = WinLossRecord(
                wins = counted.count { (_, me) -> me.win },
                losses = counted.count { (_, me) -> !me.win },
            ),
            kills = counted.sumOf { (_, me) -> me.kills },
            deaths = counted.sumOf { (_, me) -> me.deaths },
            assists = counted.sumOf { (_, me) -> me.assists },
            form = played.map { (match, me) -> match.outcomeFor(me) },
        )
    }

    /** Champions joués dans les parties chargées, du plus joué au moins joué. */
    fun championRecords(matches: List<Match>, puuid: String): List<ChampionRecord> =
        matches
            .mapNotNull { match -> match.participant(puuid)?.takeIf { match.outcomeFor(it) != MatchOutcome.REMAKE } }
            .groupBy { it.championName }
            .map { (champion, games) ->
                ChampionRecord(
                    championName = champion,
                    record = WinLossRecord(wins = games.count { it.win }, losses = games.count { !it.win }),
                    kills = games.sumOf { it.kills },
                    deaths = games.sumOf { it.deaths },
                    assists = games.sumOf { it.assists },
                )
            }
            .sortedWith(compareByDescending<ChampionRecord> { it.record.games }.thenByDescending { it.record.wins })
}
