package com.example.legendanalytics.domain

import com.example.legendanalytics.domain.model.RiotId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RiotIdTest {

    @Test
    fun `parse un Riot ID simple`() {
        assertEquals(RiotId("Faker", "KR1"), RiotId.parse("Faker#KR1"))
    }

    @Test
    fun `parse un pseudo avec espaces et accents`() {
        assertEquals(RiotId("Le Grand Éric", "EUW"), RiotId.parse("  Le Grand Éric # EUW "))
    }

    @Test
    fun `refuse les formats invalides`() {
        assertNull(RiotId.parse("Faker"))
        assertNull(RiotId.parse("#KR1"))
        assertNull(RiotId.parse("Faker#"))
        assertNull(RiotId.parse("Faker#K"))
        assertNull(RiotId.parse("Faker#TOOLONG"))
        assertNull(RiotId.parse("UnPseudoBeaucoupTropLong#EUW"))
    }

    @Test
    fun `toString reconstruit le format GameName#TAG`() {
        assertEquals("Faker#KR1", RiotId("Faker", "KR1").toString())
    }
}
