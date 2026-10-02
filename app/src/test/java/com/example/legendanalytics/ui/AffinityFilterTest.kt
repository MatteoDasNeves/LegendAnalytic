package com.example.legendanalytics.ui

import com.example.legendanalytics.domain.model.ChampionClass
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.ui.affinity.AffinityUiState
import com.example.legendanalytics.ui.affinity.filterByClass
import com.example.legendanalytics.ui.common.UiState
import com.example.legendanalytics.ui.model.ChampionAffinityUi
import org.junit.Assert.assertEquals
import org.junit.Test

class AffinityFilterTest {

    // Déjà triés par maîtrise décroissante.
    private val garen = champion(86, ChampionClass.FIGHTER, ChampionClass.TANK)
    private val ahri = champion(103, ChampionClass.MAGE, ChampionClass.ASSASSIN)
    private val unknown = champion(9999)
    private val malphite = champion(54, ChampionClass.TANK, ChampionClass.FIGHTER)
    private val all = listOf(garen, ahri, unknown, malphite)

    @Test
    fun `un filtre garde les champions ayant la classe, principale ou secondaire, dans l'ordre de maitrise`() {
        assertEquals(listOf(garen, malphite), filterByClass(all, ChampionClass.TANK))
        assertEquals(listOf(ahri), filterByClass(all, ChampionClass.ASSASSIN))
    }

    @Test
    fun `sans filtre, les champions de classe inconnue sont masques`() {
        assertEquals(listOf(garen, ahri, malphite), filterByClass(all, null))
    }

    @Test
    fun `seules les classes presentes sont proposees, avec leur nombre`() {
        val state = AffinityUiState(RiotId("A", "EUW"), content = UiState.Success(all))

        assertEquals(
            listOf(
                ChampionClass.ASSASSIN to 1,
                ChampionClass.FIGHTER to 2,
                ChampionClass.MAGE to 1,
                ChampionClass.TANK to 2,
            ),
            state.classCounts,
        )
    }

    private fun champion(id: Int, vararg classes: ChampionClass) = ChampionAffinityUi(
        championId = id,
        displayName = "#$id",
        iconUrl = null,
        masteryLevel = 5,
        masteryPoints = 10_000,
        lastPlayMillis = 0,
        classes = classes.toList(),
    )
}
