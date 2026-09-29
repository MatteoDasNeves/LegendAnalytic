package com.example.legendanalytics.domain

import com.example.legendanalytics.domain.model.Region
import com.example.legendanalytics.domain.model.RegionalCluster
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegionTest {

    @Test
    fun `EUW route vers euw1 et europe`() {
        assertEquals("euw1.api.riotgames.com", Region.EUW.platformHost)
        assertEquals(RegionalCluster.EUROPE, Region.EUW.matchCluster)
        assertEquals("europe.api.riotgames.com", Region.EUW.accountCluster.host)
    }

    @Test
    fun `NA et KR utilisent le bon cluster`() {
        assertEquals("na1.api.riotgames.com", Region.NA.platformHost)
        assertEquals(RegionalCluster.AMERICAS, Region.NA.matchCluster)
        assertEquals("kr.api.riotgames.com", Region.KR.platformHost)
        assertEquals(RegionalCluster.ASIA, Region.KR.matchCluster)
    }

    @Test
    fun `les regions SEA passent par asia pour account-v1 mais sea pour match-v5`() {
        listOf(Region.OCE, Region.SG, Region.TW, Region.VN).forEach { region ->
            assertEquals(RegionalCluster.SEA, region.matchCluster)
            assertEquals(RegionalCluster.ASIA, region.accountCluster)
        }
    }

    @Test
    fun `chaque plateforme est unique`() {
        val platforms = Region.entries.map { it.platformId }
        assertEquals(platforms.size, platforms.toSet().size)
    }

    @Test
    fun `la region est deduite du prefixe du matchId`() {
        assertEquals(Region.EUW, Region.fromMatchId("EUW1_7012345678"))
        assertEquals(Region.EUNE, Region.fromMatchId("EUN1_123"))
        assertEquals(Region.KR, Region.fromMatchId("KR_123"))
        assertNull(Region.fromMatchId("XX9_123"))
        assertNull(Region.fromMatchId("sans-prefixe"))
    }
}
