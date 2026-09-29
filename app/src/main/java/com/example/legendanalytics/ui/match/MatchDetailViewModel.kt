package com.example.legendanalytics.ui.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legendanalytics.domain.model.AppResult
import com.example.legendanalytics.domain.model.Region
import com.example.legendanalytics.domain.repository.MatchRepository
import com.example.legendanalytics.domain.repository.StaticDataRepository
import com.example.legendanalytics.ui.common.UiState
import com.example.legendanalytics.ui.model.MatchDetailUi
import com.example.legendanalytics.ui.model.ParticipantUi
import com.example.legendanalytics.ui.model.UiMapper
import com.example.legendanalytics.ui.navigation.MatchDetailRoute
import com.example.legendanalytics.ui.navigation.ProfileRoute
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MatchDetailViewModel(
    private val route: MatchDetailRoute,
    private val matchRepository: MatchRepository,
    private val staticDataRepository: StaticDataRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<MatchDetailUi>>(UiState.Loading)
    val state: StateFlow<UiState<MatchDetailUi>> = _state.asStateFlow()

    /** Le préfixe du matchId donne la plateforme ; sinon on garde la région du profil. */
    private val region: Region = Region.fromMatchId(route.matchId) ?: route.region

    init {
        load()
    }

    fun retry() = load()

    /** Route vers le profil d'un participant, ou null si son Riot ID est inconnu. */
    fun profileRouteFor(participant: ParticipantUi): ProfileRoute? {
        val riotId = participant.riotId ?: return null
        if (participant.puuid.isBlank()) return null
        return ProfileRoute(participant.puuid, riotId.gameName, riotId.tagLine, region)
    }

    private fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            // La partie est souvent déjà en cache : seul Data Dragon peut nécessiter un appel.
            val staticData = async { staticDataRepository.getStaticData() }
            _state.value = when (val result = matchRepository.getMatch(route.matchId, region)) {
                is AppResult.Success -> UiState.Success(
                    UiMapper(staticData.await()).matchDetail(result.data, route.focusPuuid),
                )
                is AppResult.Failure -> UiState.Error(result.error)
            }
        }
    }
}
