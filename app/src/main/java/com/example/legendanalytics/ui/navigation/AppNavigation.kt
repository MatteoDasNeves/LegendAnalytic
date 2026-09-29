package com.example.legendanalytics.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.example.legendanalytics.ui.match.MatchDetailScreen
import com.example.legendanalytics.ui.match.MatchDetailViewModel
import com.example.legendanalytics.ui.profile.ProfileScreen
import com.example.legendanalytics.ui.profile.ProfileViewModel
import com.example.legendanalytics.ui.search.SearchScreen
import com.example.legendanalytics.ui.search.SearchViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavigation() {
    // Pile de navigation sauvegardée (changement de configuration et mort du process).
    val backStack = rememberNavBackStack(SearchRoute)

    fun goBack() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    NavDisplay(
        backStack = backStack,
        onBack = ::goBack,
        // Un ViewModelStore par entrée : chaque profil ouvert a son propre ViewModel.
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<SearchRoute> {
                val viewModel = koinViewModel<SearchViewModel>()
                SearchScreen(
                    viewModel = viewModel,
                    onPlayerFound = { route -> backStack.add(route) },
                )
            }
            entry<ProfileRoute> { route ->
                val viewModel = koinViewModel<ProfileViewModel> { parametersOf(route) }
                ProfileScreen(
                    viewModel = viewModel,
                    onBack = ::goBack,
                    onMatchClick = { matchId ->
                        backStack.add(MatchDetailRoute(matchId, route.puuid, route.region))
                    },
                )
            }
            entry<MatchDetailRoute> { route ->
                val viewModel = koinViewModel<MatchDetailViewModel> { parametersOf(route) }
                MatchDetailScreen(
                    viewModel = viewModel,
                    onBack = ::goBack,
                    onPlayerClick = { profile ->
                        val previous = backStack.getOrNull(backStack.lastIndex - 1)
                        // Retour direct si l'on clique sur le joueur dont on vient.
                        if (previous is ProfileRoute && previous.puuid == profile.puuid) goBack()
                        else backStack.add(profile)
                    },
                )
            }
        },
    )
}
