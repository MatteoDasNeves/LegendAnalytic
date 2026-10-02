package com.example.legendanalytics.data.local

import com.example.legendanalytics.domain.model.Match

/** Cache des parties déjà chargées. Une partie terminée ne change plus : aucune expiration. */
interface MatchCache {
    suspend fun get(matchId: String): Match?
    suspend fun put(match: Match)
}
