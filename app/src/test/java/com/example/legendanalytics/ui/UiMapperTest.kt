package com.example.legendanalytics.ui

import com.example.legendanalytics.domain.PlayerStatsTest.Companion.ME
import com.example.legendanalytics.domain.PlayerStatsTest.Companion.match
import com.example.legendanalytics.domain.model.ChampionInfo
import com.example.legendanalytics.domain.model.ChampionMastery
import com.example.legendanalytics.domain.model.PlayerProfile
import com.example.legendanalytics.domain.model.Region
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.domain.model.StaticData
import com.example.legendanalytics.ui.model.UiMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UiMapperTest {

    private val staticData = StaticData(
        version = "16.19.1",
        summonerSpellImages = emptyMap(),
        champions = mapOf(
            7 to ChampionInfo("Leblanc", "LeBlanc"),
            62 to ChampionInfo("MonkeyKing", "Wukong"),
            103 to ChampionInfo("Ahri", "Ahri"),
        ),
    )

    private fun profile(masteries: List<ChampionMastery>) = PlayerProfile(
        puuid = ME, summonerLevel = 100, profileIconId = 1, soloDuo = null, flex = null, topMasteries = masteries,
    )

    @Test
    fun `les mains viennent des maitrises, completes par les stats recentes`() {
        val masteries = listOf(
            ChampionMastery(championId = 7, level = 60, points = 628_773, lastPlayMillis = 0),
            ChampionMastery(championId = 103, level = 12, points = 120_000, lastPlayMillis = 0),
        )
        val matches = listOf(
            match(1, "Ahri", win = true),
            match(2, "Ahri", win = false),
            match(3, "Lux", win = true),
        )

        val mains = UiMapper(staticData).mains(profile(masteries), matches, ME)

        assertEquals(listOf("LeBlanc", "Ahri"), mains.map { it.displayName })
        assertEquals(60, mains[0].masteryLevel)
        assertEquals(0, mains[0].recentGames)
        assertNull(mains[0].recentKda)
        assertEquals(2, mains[1].recentGames)
        assertEquals(50, mains[1].recentWinrate)
        assertEquals(
            "https://ddragon.leagueoflegends.com/cdn/img/champion/tiles/Leblanc_0.jpg",
            mains[0].tileUrl,
        )
    }

    @Test
    fun `sans maitrise, les mains sont les champions les plus joues recemment`() {
        val matches = listOf(
            match(1, "MonkeyKing", win = true),
            match(2, "MonkeyKing", win = true),
            match(3, "Ahri", win = false),
        )

        val mains = UiMapper(staticData).mains(profile(emptyList()), matches, ME)

        assertEquals(listOf("Wukong", "Ahri"), mains.map { it.displayName })
        assertNull(mains[0].masteryLevel)
        assertEquals(100, mains[0].recentWinrate)
    }

    @Test
    fun `en Arena le detail est groupe par duo et trie par place`() {
        fun player(puuid: String, subteam: Int, placement: Int, win: Boolean) =
            match(1, "Ahri", win).participants.single().copy(
                puuid = puuid, teamId = 100, subteamId = subteam, placement = placement,
            )
        val arena = match(1, "Ahri", win = true).copy(
            queueId = 1750,
            gameMode = "CHERRY",
            participants = listOf(
                player("a", subteam = 3, placement = 4, win = false),
                player(ME, subteam = 3, placement = 4, win = false),
                player("c", subteam = 1, placement = 1, win = true),
                player("d", subteam = 1, placement = 1, win = true),
            ),
        )

        val detail = UiMapper(staticData).matchDetail(arena, ME)
        assertEquals(listOf(1, 4), detail.teams.map { it.placement })
        assertEquals(listOf("c", "d"), detail.teams.first().participants.map { it.puuid })
        assertEquals(4, UiMapper(staticData).matchCard(arena, ME)?.placement)
    }

    @Test
    fun `l'en-tete utilise l'illustration du premier main`() {
        val mains = UiMapper(staticData).mains(
            profile(listOf(ChampionMastery(62, 10, 50_000, 0))), emptyList(), ME,
        )
        val header = UiMapper(staticData).profileHeader(RiotId("A", "EUW"), Region.EUW, profile(emptyList()), mains)
        assertEquals(
            "https://ddragon.leagueoflegends.com/cdn/img/champion/splash/MonkeyKing_0.jpg",
            header.bannerUrl,
        )
        assertEquals("EUW", header.regionLabel)
    }
}
