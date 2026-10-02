package com.example.legendanalytics.ui.affinity

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legendanalytics.R
import com.example.legendanalytics.domain.model.ChampionClass
import com.example.legendanalytics.domain.util.TimeAgo
import com.example.legendanalytics.ui.common.ChampionIcon
import com.example.legendanalytics.ui.common.ErrorView
import com.example.legendanalytics.ui.common.LoadingView
import com.example.legendanalytics.ui.common.UiState
import com.example.legendanalytics.ui.common.formatCompact
import com.example.legendanalytics.ui.common.label
import com.example.legendanalytics.ui.model.ChampionAffinityUi
import com.example.legendanalytics.ui.theme.LegendColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffinityScreen(viewModel: AffinityViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.affinity_title))
                        Text(
                            state.riotId.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val content = state.content) {
                UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(content.error, onRetry = viewModel::retry)
                is UiState.Success -> AffinityContent(
                    state = state,
                    totalCount = filterByClass(content.data, null).size,
                    onClassFilterChange = viewModel::onClassFilterChange,
                )
            }
        }
    }
}

@Composable
private fun AffinityContent(
    state: AffinityUiState,
    totalCount: Int,
    onClassFilterChange: (ChampionClass?) -> Unit,
) {
    val classCounts = state.classCounts

    // Grille adaptative : 2 colonnes sur téléphone, davantage sur tablette ou en paysage.
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 168.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (classCounts.isEmpty()) {
            fullWidthItem("empty") {
                Text(
                    stringResource(R.string.affinity_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
        } else {
            fullWidthItem("filters") {
                ClassFilters(classCounts, totalCount, state.classFilter, onClassFilterChange)
            }
        }
        items(state.visibleChampions, key = { it.championId }) { champion -> AffinityCard(champion) }
        fullWidthItem("bottom_inset") { Spacer(Modifier.navigationBarsPadding()) }
    }
}

private fun LazyGridScope.fullWidthItem(key: String, content: @Composable () -> Unit) =
    item(key = key, span = { GridItemSpan(maxLineSpan) }) { content() }

@Composable
private fun ClassFilters(
    classCounts: List<Pair<ChampionClass, Int>>,
    totalCount: Int,
    selected: ChampionClass?,
    onSelected: (ChampionClass?) -> Unit,
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item(key = "all") {
            FilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text("${stringResource(R.string.affinity_filter_all)}  $totalCount") },
                leadingIcon = { ClassIcon(R.drawable.specialist_icon, contentDescription = null) },
            )
        }
        items(classCounts, key = { it.first.name }) { (championClass, count) ->
            FilterChip(
                selected = selected == championClass,
                onClick = { onSelected(championClass) },
                label = { Text("${stringResource(championClass.labelRes())}  $count") },
                leadingIcon = { ClassIcon(championClass.iconRes(), contentDescription = null) },
            )
        }
    }
}

@Composable
private fun AffinityCard(champion: ChampionAffinityUi, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(LegendColors.Surface)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ChampionIcon(champion.iconUrl, champion.displayName, size = 44.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    champion.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(R.string.affinity_mastery, champion.masteryLevel, formatCompact(champion.masteryPoints)),
                    style = MaterialTheme.typography.labelMedium,
                    color = LegendColors.Gold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            champion.classes.forEach { ClassIcon(it.iconRes(), stringResource(it.labelRes())) }
        }

        if (champion.lastPlayMillis > 0) {
            Text(
                stringResource(
                    R.string.affinity_last_played,
                    TimeAgo.between(champion.lastPlayMillis, System.currentTimeMillis()).label(),
                ),
                style = MaterialTheme.typography.labelMedium,
                color = LegendColors.Muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ClassIcon(@DrawableRes res: Int, contentDescription: String?) {
    Image(painterResource(res), contentDescription = contentDescription, modifier = Modifier.size(18.dp))
}

// Data Dragon ne fournit pas d'icône de classe : on utilise celles du dossier drawable.
// La classe Support correspond au rôle « Controller » de Riot.

@DrawableRes
private fun ChampionClass.iconRes(): Int = when (this) {
    ChampionClass.ASSASSIN -> R.drawable.slayer_icon
    ChampionClass.FIGHTER -> R.drawable.fighter_icon
    ChampionClass.MAGE -> R.drawable.mage_icon
    ChampionClass.MARKSMAN -> R.drawable.marksman_icon
    ChampionClass.SUPPORT -> R.drawable.controller_icon
    ChampionClass.TANK -> R.drawable.tank_icon
}

@StringRes
private fun ChampionClass.labelRes(): Int = when (this) {
    ChampionClass.ASSASSIN -> R.string.class_assassin
    ChampionClass.FIGHTER -> R.string.class_fighter
    ChampionClass.MAGE -> R.string.class_mage
    ChampionClass.MARKSMAN -> R.string.class_marksman
    ChampionClass.SUPPORT -> R.string.class_support
    ChampionClass.TANK -> R.string.class_tank
}
