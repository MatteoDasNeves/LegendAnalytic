package com.example.legendanalytics.domain

import com.example.legendanalytics.domain.model.Match
import com.example.legendanalytics.domain.model.MatchOutcome
import com.example.legendanalytics.domain.model.Participant
import com.example.legendanalytics.domain.model.RankedEntry
import com.example.legendanalytics.domain.model.RankedQueue
import com.example.legendanalytics.domain.util.PlayerStats
import org.junit.Assert.assertEquals
import org.junit.Test

class PlayerStatsTest {

    @Test
    fun `total classe additionne Solo-Duo et Flex`() {
        val solo = RankedEntry(RankedQueue.SOLO_DUO, "GOLD", "II", 40, wins = 60, losses = 40)
        val flex = RankedEntry(RankedQueue.FLEX, "SILVER", "I", 10, wins = 5, losses = 15)
        val total = PlayerStats.rankedTotal(solo, flex)
        assertEquals(120, total.games)
        assertEquals(65, total.wins)
        assertEquals(55, total.losses)
        assertEquals(54, total.winrate)
    }

    @Test
    fun `total classe sans rang`() {
        val total = PlayerStats.rankedTotal(null, null)
        assertEquals(0, total.games)
        assertEquals(0, total.winrate)
    }

    @Test
    fun `resume recent ignore les remakes dans le bilan mais les garde dans la forme`() {
        val matches = listOf(
            match(1, "Ahri", win = true, k = 10, d = 2, a = 5),
            match(2, "Ahri", win = false, k = 2, d = 6, a = 3),
            match(3, "Lux", win = false, k = 0, d = 0, a = 0, remake = true),
            match(4, "Lux", win = true, k = 4, d = 2, a = 12),
        )
        val summary = PlayerStats.recentSummary(matches, ME)

        assertEquals(3, summary.record.games)
        assertEquals(2, summary.record.wins)
        assertEquals(1, summary.record.losses)
        assertEquals(16, summary.kills)
        assertEquals(10, summary.deaths)
        assertEquals(20, summary.assists)
        // Du plus récent (start = 4) au plus ancien.
        assertEquals(
            listOf(MatchOutcome.VICTORY, MatchOutcome.REMAKE, MatchOutcome.DEFEAT, MatchOutcome.VICTORY),
            summary.form,
        )
        assertEquals(16.0 / 3, summary.average(summary.kills), 0.001)
    }

    @Test
    fun `champions tries par nombre de parties puis victoires`() {
        val matches = listOf(
            match(1, "Lux", win = false),
            match(2, "Ahri", win = true, k = 5, d = 1, a = 5),
            match(3, "Ahri", win = true, k = 3, d = 3, a = 3),
            match(4, "Ahri", win = false, k = 1, d = 4, a = 2),
            match(5, "Jinx", win = true),
        )
        val records = PlayerStats.championRecords(matches, ME)

        assertEquals(listOf("Ahri", "Jinx", "Lux"), records.map { it.championName })
        val ahri = records.first()
        assertEquals(3, ahri.record.games)
        assertEquals(67, ahri.record.winrate)
        assertEquals((9 + 10) / 8.0, ahri.kda, 0.001)
    }

    companion object {
        const val ME = "me"

        fun match(
            start: Long,
            champion: String,
            win: Boolean,
            k: Int = 1,
            d: Int = 1,
            a: Int = 1,
            remake: Boolean = false,
        ) = Match(
            matchId = "EUW1_$start",
            queueId = 420,
            gameMode = "CLASSIC",
            gameStartMillis = start,
            gameEndMillis = start + 1,
            durationSeconds = 1800,
            participants = listOf(
                Participant(
                    puuid = ME, riotId = null, teamId = 100, championName = champion, championLevel = 18,
                    kills = k, deaths = d, assists = a, creepScore = 200, goldEarned = 12000,
                    damageToChampions = 20000, visionScore = 20, items = List(7) { 0 },
                    summonerSpells = listOf(4, 14), win = win, earlySurrender = remake,
                ),
            ),
            teams = emptyList(),
        )
    }
}
