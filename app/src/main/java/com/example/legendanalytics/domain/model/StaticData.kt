package com.example.legendanalytics.domain.model

/** Données Data Dragon nécessaires à l'affichage. */
data class StaticData(
    val version: String,
    /** id numérique du sort d'invocateur -> nom du fichier image (ex. 4 -> "SummonerFlash.png"). */
    val summonerSpellImages: Map<Int, String>,
)
