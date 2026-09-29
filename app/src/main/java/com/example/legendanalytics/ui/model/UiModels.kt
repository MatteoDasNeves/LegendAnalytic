package com.example.legendanalytics.ui.model

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
    val level: Long,
    val profileIconUrl: String?,
    val soloDuo: RankUi,
    val flex: RankUi,
)

data class MatchCardUi(
    val matchId: String,
    val outcome: MatchOutcome,
    val queueId: Int,
    val gameMode: String,
    val gameEndMillis: Long,
    val durationSeconds: Long,
    val championName: String,
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
