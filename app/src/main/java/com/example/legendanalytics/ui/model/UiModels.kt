package com.example.legendanalytics.ui.model

import com.example.legendanalytics.domain.model.ChampionClass
import com.example.legendanalytics.domain.model.MatchOutcome
import com.example.legendanalytics.domain.model.RankedQueue
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.domain.model.TeamObjectives

data class RankUi(
    val queue: RankedQueue,
    /** null = non classé. */
    val tier: String?,
    val division: String,
    val leaguePoints: Int,
    val wins: Int,
    val losses: Int,
    val winrate: Int,
)

data class ProfileHeaderUi(
    val riotId: RiotId,
    val regionLabel: String,
    val level: Long,
    val profileIconUrl: String?,
    /** Illustration du champion principal, en fond de l'en-tête. */
    val bannerUrl: String?,
    val soloDuo: RankUi,
    val flex: RankUi,
)

/** Bilan chiffré du joueur : saison classée + parties récentes chargées. */
data class OverviewUi(
    val rankedGames: Int,
    val rankedWins: Int,
    val rankedLosses: Int,
    val rankedWinrate: Int,
    val recentGames: Int,
    val recentWins: Int,
    val recentLosses: Int,
    val recentWinrate: Int,
    val recentKda: Double,
    val averageKills: Double,
    val averageDeaths: Double,
    val averageAssists: Double,
    /** Issues des dernières parties, de la plus récente à la plus ancienne. */
    val form: List<MatchOutcome>,
)

/** Un champion principal : maîtrise (si disponible) + résultats sur les parties chargées. */
data class MainChampionUi(
    val championName: String,
    val displayName: String,
    val tileUrl: String,
    val masteryLevel: Int?,
    val masteryPoints: Int?,
    val recentGames: Int,
    val recentWinrate: Int,
    val recentKda: Double?,
)

data class MatchCardUi(
    val matchId: String,
    val outcome: MatchOutcome,
    val queueId: Int,
    val gameMode: String,
    val gameEndMillis: Long,
    val durationSeconds: Long,
    val championName: String,
    val championDisplayName: String,
    val championIconUrl: String?,
    val championLevel: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val kda: Double,
    val creepScore: Int,
    val csPerMinute: Double,
    val killParticipation: Int,
    /** 7 emplacements, null = vide ou image indisponible. */
    val itemIconUrls: List<String?>,
    val spellIconUrls: List<String?>,
    /** Arena : classement final (1 à 8), null dans les autres modes. */
    val placement: Int? = null,
)

data class ParticipantUi(
    val puuid: String,
    val riotId: RiotId?,
    val championName: String,
    val championIconUrl: String?,
    val championLevel: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val kda: Double,
    val creepScore: Int,
    val damageToChampions: Int,
    /** Part des dégâts par rapport au meilleur joueur de la partie (0..1), pour la jauge. */
    val damageRatio: Float,
    val goldEarned: Int,
    val visionScore: Int,
    val itemIconUrls: List<String?>,
    val spellIconUrls: List<String?>,
    val isFocused: Boolean,
)

data class TeamUi(
    val teamId: Int,
    /** Arena : classement du duo ; null pour une partie classique à deux équipes. */
    val placement: Int? = null,
    val outcome: MatchOutcome,
    val totalKills: Int,
    val totalGold: Int,
    val objectives: TeamObjectives?,
    val participants: List<ParticipantUi>,
) {
    val isBlueSide: Boolean get() = teamId == 100
}

data class MatchDetailUi(
    val matchId: String,
    val queueId: Int,
    val gameMode: String,
    val gameEndMillis: Long,
    val durationSeconds: Long,
    val teams: List<TeamUi>,
)

/** Affinité du joueur avec un champion : maîtrise et classes Data Dragon. */
data class ChampionAffinityUi(
    val championId: Int,
    val displayName: String,
    val iconUrl: String?,
    val masteryLevel: Int,
    val masteryPoints: Int,
    val lastPlayMillis: Long,
    /** Classes du champion, la principale en premier. */
    val classes: List<ChampionClass>,
)
