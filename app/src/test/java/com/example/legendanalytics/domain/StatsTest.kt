package com.example.legendanalytics.domain

import com.example.legendanalytics.domain.util.Stats
import com.example.legendanalytics.domain.util.TimeAgo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsTest {

    @Test
    fun `kda classique`() {
        assertEquals(3.0, Stats.kda(kills = 5, deaths = 3, assists = 4), 0.001)
        assertEquals(0.5, Stats.kda(kills = 0, deaths = 4, assists = 2), 0.001)
    }

    @Test
    fun `kda parfait sans mort`() {
        assertTrue(Stats.kda(kills = 7, deaths = 0, assists = 3).isInfinite())
    }

    @Test
    fun `kda nul sans aucune action`() {
        assertEquals(0.0, Stats.kda(0, 0, 0), 0.0)
    }

    @Test
    fun `winrate arrondi`() {
        assertEquals(50, Stats.winrate(10, 10))
        assertEquals(67, Stats.winrate(2, 1))
        assertEquals(100, Stats.winrate(5, 0))
        assertEquals(0, Stats.winrate(0, 0))
    }

    @Test
    fun `cs par minute`() {
        assertEquals(7.5, Stats.csPerMinute(creepScore = 225, durationSeconds = 1800), 0.001)
        assertEquals(0.0, Stats.csPerMinute(100, 0), 0.0)
    }

    @Test
    fun `participation aux kills`() {
        assertEquals(50, Stats.killParticipation(kills = 3, assists = 7, teamKills = 20))
        assertEquals(0, Stats.killParticipation(1, 1, 0))
    }

    @Test
    fun `anciennete des parties`() {
        val now = 1_000_000_000_000L
        val minute = 60_000L
        assertEquals(TimeAgo.JustNow, TimeAgo.between(now - 10_000, now))
        assertEquals(TimeAgo.Minutes(5), TimeAgo.between(now - 5 * minute, now))
        assertEquals(TimeAgo.Hours(3), TimeAgo.between(now - 3 * 60 * minute, now))
        assertEquals(TimeAgo.Days(2), TimeAgo.between(now - 2 * 24 * 60 * minute, now))
        assertEquals(TimeAgo.Months(2), TimeAgo.between(now - 65L * 24 * 60 * minute, now))
    }
}
