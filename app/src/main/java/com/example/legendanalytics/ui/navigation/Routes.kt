package com.example.legendanalytics.ui.navigation

import androidx.navigation3.runtime.NavKey
import com.example.legendanalytics.domain.model.Region
import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute : NavKey

@Serializable
data class ProfileRoute(
    val puuid: String,
    val gameName: String,
    val tagLine: String,
    val region: Region,
) : NavKey

/** Affinités du joueur avec ses champions. */
@Serializable
data class AffinityRoute(
    val puuid: String,
    val gameName: String,
    val tagLine: String,
    val region: Region,
) : NavKey

@Serializable
data class MatchDetailRoute(
    val matchId: String,
    /** Joueur à mettre en évidence (celui dont on consultait le profil). */
    val focusPuuid: String,
    val region: Region,
) : NavKey
