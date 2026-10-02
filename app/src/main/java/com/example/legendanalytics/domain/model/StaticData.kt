package com.example.legendanalytics.domain.model

/** Données Data Dragon nécessaires à l'affichage. */
data class StaticData(
    val version: String,
    /** id numérique du sort d'invocateur -> nom du fichier image (ex. 4 -> "SummonerFlash.png"). */
    val summonerSpellImages: Map<Int, String>,
    /** id numérique du champion -> identifiants Data Dragon (ex. 20 -> Nunu / "Nunu et Willump"). */
    val champions: Map<Int, ChampionInfo> = emptyMap(),
)

data class ChampionInfo(
    /** Identifiant Data Dragon, utilisé dans les URLs d'images (ex. "MonkeyKing"). */
    val id: String,
    /** Nom affiché en français (ex. "Wukong"). */
    val name: String,
    /** Classes Data Dragon, la principale en premier. */
    val classes: List<ChampionClass> = emptyList(),
)

/** Classes de champion telles que Data Dragon les expose (champ `tags`). */
enum class ChampionClass(val dataDragonTag: String) {
    ASSASSIN("Assassin"),
    FIGHTER("Fighter"),
    MAGE("Mage"),
    MARKSMAN("Marksman"),
    SUPPORT("Support"),
    TANK("Tank");

    companion object {
        fun fromTag(tag: String): ChampionClass? = entries.firstOrNull { it.dataDragonTag.equals(tag, ignoreCase = true) }
    }
}
