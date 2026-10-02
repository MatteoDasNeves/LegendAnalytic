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

/** /cdn/{version}/data/fr_FR/champion.json */
@Serializable
data class ChampionListDto(
    val data: Map<String, ChampionSummaryDto> = emptyMap(),
)

@Serializable
data class ChampionSummaryDto(
    /** Identifiant Data Dragon, ex. "MonkeyKing". */
    val id: String,
    /** Identifiant numérique sous forme de chaîne, ex. "62". */
    val key: String,
    val name: String = "",
    /** Classes du champion, la principale en premier, ex. ["Fighter", "Tank"]. */
    val tags: List<String> = emptyList(),
)
