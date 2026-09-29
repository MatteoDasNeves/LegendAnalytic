package com.example.legendanalytics.domain.util

import kotlin.math.roundToInt

object Stats {

    /** (K + A) / D ; renvoie [Double.POSITIVE_INFINITY] pour une partie sans mort ("Parfait"). */
    fun kda(kills: Int, deaths: Int, assists: Int): Double =
        if (deaths == 0) {
            if (kills + assists == 0) 0.0 else Double.POSITIVE_INFINITY
        } else {
            (kills + assists).toDouble() / deaths
        }

    /** Winrate en pourcentage entier, 0 s'il n'y a aucune partie. */
    fun winrate(wins: Int, losses: Int): Int {
        val total = wins + losses
        return if (total <= 0) 0 else (wins * 100.0 / total).roundToInt()
    }

    fun csPerMinute(creepScore: Int, durationSeconds: Long): Double =
        if (durationSeconds <= 0) 0.0 else creepScore * 60.0 / durationSeconds

    /** Participation aux kills de l'équipe, en pourcentage entier. */
    fun killParticipation(kills: Int, assists: Int, teamKills: Int): Int =
        if (teamKills <= 0) 0 else ((kills + assists) * 100.0 / teamKills).roundToInt()
}

/** Ancienneté d'une partie, découpée en unité lisible. */
sealed interface TimeAgo {
    data object JustNow : TimeAgo
    data class Minutes(val value: Long) : TimeAgo
    data class Hours(val value: Long) : TimeAgo
    data class Days(val value: Long) : TimeAgo
    data class Months(val value: Long) : TimeAgo
    data class Years(val value: Long) : TimeAgo

    companion object {
        fun between(pastMillis: Long, nowMillis: Long): TimeAgo {
            val minutes = (nowMillis - pastMillis).coerceAtLeast(0) / 60_000
            val hours = minutes / 60
            val days = hours / 24
            return when {
                minutes < 1 -> JustNow
                hours < 1 -> Minutes(minutes)
                days < 1 -> Hours(hours)
                days < 30 -> Days(days)
                days < 365 -> Months(days / 30)
                else -> Years(days / 365)
            }
        }
    }
}
