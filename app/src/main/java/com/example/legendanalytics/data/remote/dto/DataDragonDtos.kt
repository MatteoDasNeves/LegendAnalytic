package com.example.legendanalytics.data.remote.dto

import kotlinx.serialization.Serializable

/** /cdn/{version}/data/fr_FR/summoner.json */
@Serializable
data class SummonerSpellsDto(
    val data: Map<String, SummonerSpellDto> = emptyMap(),
)

@Serializable
data class SummonerSpellDto(
    val id: String,
    /** Identifiant numérique sous forme de chaîne, ex. "4" pour Flash. */
    val key: String,
    val name: String = "",
    val image: DataDragonImageDto,
)

@Serializable
data class DataDragonImageDto(
    val full: String,
)
