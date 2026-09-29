package com.example.legendanalytics.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legendanalytics.domain.model.AppError
import com.example.legendanalytics.domain.model.AppResult
import com.example.legendanalytics.domain.model.Region
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.domain.model.SearchHistoryEntry
import com.example.legendanalytics.domain.repository.PlayerRepository
import com.example.legendanalytics.domain.repository.SearchHistoryRepository
import com.example.legendanalytics.ui.navigation.ProfileRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SearchError {
    data object InvalidFormat : SearchError
    data class Api(val error: AppError) : SearchError
}

data class SearchUiState(
    val query: String = "",
    val region: Region = Region.DEFAULT,
    val isLoading: Boolean = false,
    val error: SearchError? = null,
)

class SearchViewModel(
    private val playerRepository: PlayerRepository,
    private val historyRepository: SearchHistoryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SearchUiState())
    val state: StateFlow<SearchUiState> = _state.asStateFlow()

    val history: StateFlow<List<SearchHistoryEntry>> = historyRepository.history
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Événement de navigation consommé une seule fois par l'écran.
    private val _navigation = Channel<ProfileRoute>(Channel.BUFFERED)
    val navigation: Flow<ProfileRoute> = _navigation.receiveAsFlow()

    fun onQueryChange(value: String) = _state.update { it.copy(query = value, error = null) }

    fun onRegionChange(region: Region) = _state.update { it.copy(region = region, error = null) }

    fun search() {
        val current = _state.value
        if (current.isLoading) return
        val riotId = RiotId.parse(current.query)
        if (riotId == null) {
            _state.update { it.copy(error = SearchError.InvalidFormat) }
            return
        }
        search(riotId, current.region)
    }

    fun searchFromHistory(entry: SearchHistoryEntry) {
        _state.update { it.copy(query = entry.riotId.toString(), region = entry.region, error = null) }
        search(entry.riotId, entry.region)
    }

    fun removeFromHistory(entry: SearchHistoryEntry) {
        viewModelScope.launch { historyRepository.remove(entry) }
    }

    fun clearHistory() {
        viewModelScope.launch { historyRepository.clear() }
    }

    private fun search(riotId: RiotId, region: Region) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = playerRepository.findAccount(riotId, region)) {
                is AppResult.Success -> {
                    val account = result.data
                    // On enregistre l'orthographe officielle renvoyée par Riot.
                    historyRepository.add(account.riotId, region)
                    _state.update { it.copy(isLoading = false) }
                    _navigation.send(
                        ProfileRoute(account.puuid, account.riotId.gameName, account.riotId.tagLine, region),
                    )
                }
                is AppResult.Failure -> _state.update {
                    it.copy(isLoading = false, error = SearchError.Api(result.error))
                }
            }
        }
    }
}
