package com.example.legendanalytics.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legendanalytics.domain.model.AppError
import com.example.legendanalytics.domain.model.AppResult
import com.example.legendanalytics.domain.model.Match
import com.example.legendanalytics.domain.model.PlayerProfile
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.domain.model.StaticData
import com.example.legendanalytics.domain.repository.MatchRepository
import com.example.legendanalytics.domain.repository.PlayerRepository
import com.example.legendanalytics.domain.repository.StaticDataRepository
import com.example.legendanalytics.ui.common.UiState
import com.example.legendanalytics.ui.model.MatchCardUi
import com.example.legendanalytics.ui.model.ProfileHeaderUi
import com.example.legendanalytics.ui.model.UiMapper
import com.example.legendanalytics.ui.navigation.ProfileRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Message ponctuel affiché en snackbar (erreur de rafraîchissement, parties manquantes...). */
sealed interface ProfileMessage {
    data class Error(val error: AppError) : ProfileMessage
    data class PartialMatches(val failedCount: Int) : ProfileMessage
}

data class ProfileUiState(
    val riotId: RiotId,
    val content: UiState<ProfileContent> = UiState.Loading,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    val message: ProfileMessage? = null,
)

data class ProfileContent(
    val header: ProfileHeaderUi,
    val matches: List<MatchCardUi>,
)

class ProfileViewModel(
    private val route: ProfileRoute,
    private val playerRepository: PlayerRepository,
    private val matchRepository: MatchRepository,
    private val staticDataRepository: StaticDataRepository,
) : ViewModel() {

    private val riotId = RiotId(route.gameName, route.tagLine)
    private val _state = MutableStateFlow(ProfileUiState(riotId))
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    private var profile: PlayerProfile? = null
    private var matches: List<Match> = emptyList()
    private var staticData: StaticData? = null
    /** Nombre d'IDs déjà demandés (indépendant des parties en échec) : sert d'offset. */
    private var nextStart = 0
    private var loadJob: Job? = null

    init {
        load(isRefresh = false)
    }

    fun retry() = load(isRefresh = false)

    fun refresh() = load(isRefresh = true)

    fun messageShown() = _state.update { it.copy(message = null) }

    fun loadMore() {
        val current = _state.value
        if (current.isLoadingMore || current.isRefreshing || !current.canLoadMore) return
        if (loadJob?.isActive == true) return
        _state.update { it.copy(isLoadingMore = true) }
        loadJob = viewModelScope.launch {
            when (val result = matchRepository.getMatches(route.puuid, route.region, nextStart, PAGE_SIZE)) {
                is AppResult.Success -> {
                    val page = result.data
                    nextStart += PAGE_SIZE
                    val known = matches.mapTo(HashSet()) { it.matchId }
                    matches = matches + page.matches.filterNot { it.matchId in known }
                    publish(canLoadMore = page.hasMore, message = partialMessage(page.failedCount))
                }
                is AppResult.Failure -> _state.update {
                    it.copy(isLoadingMore = false, message = ProfileMessage.Error(result.error))
                }
            }
        }
    }

    private fun load(isRefresh: Boolean) {
        loadJob?.cancel()
        _state.update {
            if (isRefresh && it.content is UiState.Success) it.copy(isRefreshing = true, isLoadingMore = false)
            else it.copy(content = UiState.Loading, isLoadingMore = false)
        }
        loadJob = viewModelScope.launch {
            coroutineScope {
                val static = async { staticDataRepository.getStaticData() }
                val profileResult = async { playerRepository.getProfile(route.puuid, route.region) }
                val pageResult = async { matchRepository.getMatches(route.puuid, route.region, 0, PAGE_SIZE) }

                staticData = static.await() ?: staticData
                val p = profileResult.await()
                val page = pageResult.await()

                val error = (p as? AppResult.Failure)?.error ?: (page as? AppResult.Failure)?.error
                if (error != null) {
                    // En rafraîchissement, on garde les données affichées et on signale l'erreur.
                    _state.update {
                        if (isRefresh && it.content is UiState.Success) {
                            it.copy(isRefreshing = false, message = ProfileMessage.Error(error))
                        } else {
                            it.copy(content = UiState.Error(error), isRefreshing = false)
                        }
                    }
                    return@coroutineScope
                }

                profile = (p as AppResult.Success).data
                val matchPage = (page as AppResult.Success).data
                matches = matchPage.matches
                nextStart = PAGE_SIZE
                publish(canLoadMore = matchPage.hasMore, message = partialMessage(matchPage.failedCount))
            }
        }
    }

    private fun publish(canLoadMore: Boolean, message: ProfileMessage?) {
        val currentProfile = profile ?: return
        val mapper = UiMapper(staticData)
        val content = ProfileContent(
            header = mapper.profileHeader(riotId, currentProfile),
            matches = matches.mapNotNull { mapper.matchCard(it, route.puuid) },
        )
        _state.update {
            it.copy(
                content = UiState.Success(content),
                isRefreshing = false,
                isLoadingMore = false,
                canLoadMore = canLoadMore,
                message = message ?: it.message,
            )
        }
    }

    private fun partialMessage(failedCount: Int): ProfileMessage? =
        if (failedCount > 0) ProfileMessage.PartialMatches(failedCount) else null

    companion object {
        const val PAGE_SIZE = 20
    }
}
