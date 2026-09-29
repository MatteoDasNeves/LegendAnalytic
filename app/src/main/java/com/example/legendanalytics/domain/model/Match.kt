package com.example.legendanalytics.domain.model

enum class MatchOutcome { VICTORY, DEFEAT, REMAKE }

data class Participant(
    val puuid: String,
    val riotId: RiotId?,
    val teamId: Int,
    val championName: String,
    val championLevel: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val creepScore: Int,
    val goldEarned: Int,
    val damageToChampions: Int,
    val visionScore: Int,
    /** 7 emplacements (item0..item6), 0 = vide. */
    val items: List<Int>,
    val summonerSpells: List<Int>,
    val win: Boolean,
    val earlySurrender: Boolean,
)

data class TeamObjectives(
    val towers: Int,
    val inhibitors: Int,
    val dragons: Int,
    val barons: Int,
    val riftHeralds: Int,
    val voidGrubs: Int,
    val champions: Int,
)

data class Team(
    val teamId: Int,
    val win: Boolean,
    val objectives: TeamObjectives?,
)

data class Match(
    val matchId: String,
    val queueId: Int,
    val gameMode: String,
    /** Début de partie (epoch ms). */
    val gameStartMillis: Long,
    /** Fin de partie (epoch ms). */
    val gameEndMillis: Long,
    val durationSeconds: Long,
    val participants: List<Participant>,
    val teams: List<Team>,
) {
    fun participant(puuid: String): Participant? = participants.firstOrNull { it.puuid == puuid }

    fun outcomeFor(participant: Participant): MatchOutcome = when {
        participant.earlySurrender -> MatchOutcome.REMAKE
        participant.win -> MatchOutcome.VICTORY
        else -> MatchOutcome.DEFEAT
    }
}

/** Une page de parties : [hasMore] est faux quand l'API a renvoyé moins d'IDs que demandé. */
data class MatchPage(
    val matches: List<Match>,
    val hasMore: Boolean,
    /** Nombre de parties dont le détail n'a pas pu être chargé. */
    val failedCount: Int,
)
