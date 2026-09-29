package com.example.legendanalytics.domain.model

import kotlinx.serialization.Serializable

/** Cluster régional utilisé par account-v1 et match-v5. */
enum class RegionalCluster(val host: String) {
    AMERICAS("americas.api.riotgames.com"),
    EUROPE("europe.api.riotgames.com"),
    ASIA("asia.api.riotgames.com"),
    SEA("sea.api.riotgames.com"),
}

/**
 * Région de jeu choisie par l'utilisateur, avec son routage plateforme (summoner-v4, league-v4)
 * et son routage régional (match-v5).
 */
@Serializable
enum class Region(
    val label: String,
    val platformId: String,
    val cluster: RegionalCluster,
) {
    EUW("EUW", "euw1", RegionalCluster.EUROPE),
    EUNE("EUNE", "eun1", RegionalCluster.EUROPE),
    TR("TR", "tr1", RegionalCluster.EUROPE),
    RU("RU", "ru", RegionalCluster.EUROPE),
    ME("ME", "me1", RegionalCluster.EUROPE),
    NA("NA", "na1", RegionalCluster.AMERICAS),
    BR("BR", "br1", RegionalCluster.AMERICAS),
    LAN("LAN", "la1", RegionalCluster.AMERICAS),
    LAS("LAS", "la2", RegionalCluster.AMERICAS),
    KR("KR", "kr", RegionalCluster.ASIA),
    JP("JP", "jp1", RegionalCluster.ASIA),
    OCE("OCE", "oc1", RegionalCluster.SEA),
    SG("SG", "sg2", RegionalCluster.SEA),
    TW("TW", "tw2", RegionalCluster.SEA),
    VN("VN", "vn2", RegionalCluster.SEA);

    val platformHost: String get() = "$platformId.api.riotgames.com"

    /** match-v5 : le cluster de la région (y compris SEA). */
    val matchCluster: RegionalCluster get() = cluster

    /** account-v1 n'est pas servi par le cluster SEA : on passe alors par ASIA. */
    val accountCluster: RegionalCluster
        get() = if (cluster == RegionalCluster.SEA) RegionalCluster.ASIA else cluster

    companion object {
        val DEFAULT = EUW

        /** Retrouve la région à partir du préfixe d'un matchId (ex. "EUW1_7012345678"). */
        fun fromMatchId(matchId: String): Region? {
            val prefix = matchId.substringBefore('_', missingDelimiterValue = "")
            return entries.firstOrNull { it.platformId.equals(prefix, ignoreCase = true) }
        }
    }
}
