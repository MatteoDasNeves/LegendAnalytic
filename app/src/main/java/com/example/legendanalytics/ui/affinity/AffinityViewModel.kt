package com.example.legendanalytics.ui.affinity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.legendanalytics.domain.model.AppResult
import com.example.legendanalytics.domain.model.ChampionClass
import com.example.legendanalytics.domain.model.RiotId
import com.example.legendanalytics.domain.repository.PlayerRepository
import com.example.legendanalytics.domain.repository.StaticDataRepository
import com.example.legendanalytics.ui.common.UiState
import com.example.legendanalytics.ui.model.ChampionAffinityUi
import com.example.legendanalytics.ui.model.UiMapper
import com.example.legendanalytics.ui.navigation.AffinityRoute
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AffinityUiState(
    val riotId: RiotId,
    /** Champions triés par maîtrise décroissante. */
    val content: UiState<List<ChampionAffinityUi>> = UiState.Loading,
    /** null = toutes les classes. */
    val classFilter: ChampionClass? = null,
) {
    private val champions: List<ChampionAffinityUi>
        get() = (content as? UiState.Success)?.data.orEmpty()

    /** Classes présentes parmi les champions du joueur, avec leur nombre, dans l'ordre de l'enum. */
    val classCounts: List<Pair<ChampionClass, Int>>
        get() = ChampionClass.entries.mapNotNull { championClass ->
            champions.count { championClass in it.classes }.takeIf { it > 0 }?.let { championClass to it }
        }

    val visibleChampions: List<ChampionAffinityUi>
        get() = filterByClass(champions, classFilter)
}

/**
 * Un champion correspond à une classe dès qu'elle figure parmi ses classes Data Dragon
 * (Garen, « Combattant / Tank », apparaît sous les deux). L'ordre par maîtrise est conservé.
 * Sans filtre, les champions dont la classe est inconnue sont masqués.
 */
fun filterByClass(champions: List<ChampionAffinityUi>, championClass: ChampionClass?): List<ChampionAffinityUi> =
    if (championClass == null) champions.filter { it.classes.isNotEmpty() }
    else champions.filter { championClass in it.classes }

/** Maîtrises : champion-mastery-v4. Classes : Data Dragon (`tags` de champion.json). */
class AffinityViewModel(
    private val route: AffinityRoute,
    private val playerRepository: PlayerRepository,
    private val staticDataRepository: StaticDataRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AffinityUiState(RiotId(route.gameName, route.tagLine)))
    val state: StateFlow<AffinityUiState> = _state.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun retry() = load()

    /** Un second appui sur la classe active revient à « Tous ». */
    fun onClassFilterChange(championClass: ChampionClass?) =
        _state.update { it.copy(classFilter = if (it.classFilter == championClass) null else championClass) }

    private fun load() {
        loadJob?.cancel()
        _state.update { it.copy(content = UiState.Loading) }
        loadJob = viewModelScope.launch {
            val (staticData, masteries) = coroutineScope {
                val static = async { staticDataRepository.getStaticData() }
                val masteryResult = async { playerRepository.getMasteries(route.puuid, route.region) }
                static.await() to masteryResult.await()
            }
            val content = when (masteries) {
                is AppResult.Failure -> UiState.Error(masteries.error)
                is AppResult.Success -> UiState.Success(UiMapper(staticData).affinities(masteries.data))
            }
            _state.update { it.copy(content = content) }
        }
    }
}
