package com.example.legendanalytics.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color
import com.example.legendanalytics.R
import com.example.legendanalytics.domain.model.RankedQueue
import com.example.legendanalytics.ui.common.ErrorView
import com.example.legendanalytics.ui.common.GameImage
import com.example.legendanalytics.ui.common.LoadingView
import com.example.legendanalytics.ui.common.UiState
import com.example.legendanalytics.ui.common.message
import com.example.legendanalytics.ui.model.ProfileHeaderUi
import com.example.legendanalytics.ui.model.RankUi
import com.example.legendanalytics.ui.theme.LegendColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onMatchClick: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val messageText = state.message?.let { message ->
        when (message) {
            is ProfileMessage.Error -> message.error.message()
            is ProfileMessage.PartialMatches -> stringResource(R.string.error_partial_matches, message.failedCount)
        }
    }

    LaunchedEffect(state.message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            viewModel.messageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.riotId.toString(), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = state.content is UiState.Success) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.profile_refresh))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (val content = state.content) {
                UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(content.error, onRetry = viewModel::retry)
                is UiState.Success -> PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    ProfileContentList(
                        content = content.data,
                        isLoadingMore = state.isLoadingMore,
                        canLoadMore = state.canLoadMore,
                        onLoadMore = viewModel::loadMore,
                        onMatchClick = onMatchClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileContentList(
    content: ProfileContent,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    onMatchClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "header") { ProfileHeader(content.header) }
        item(key = "title") {
            Text(
                stringResource(R.string.profile_recent_matches),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (content.matches.isEmpty()) {
            item(key = "empty") {
                Text(
                    stringResource(R.string.profile_no_matches),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(content.matches, key = { it.matchId }) { match ->
            MatchCard(match = match, onClick = { onMatchClick(match.matchId) })
        }
        if (canLoadMore || isLoadingMore) {
            item(key = "load_more") {
                Box(Modifier.fillMaxWidth().padding(8.dp), contentAlignment = Alignment.Center) {
                    if (isLoadingMore) {
                        CircularProgressIndicator(Modifier.size(28.dp))
                    } else {
                        OutlinedButton(onClick = onLoadMore) { Text(stringResource(R.string.profile_load_more)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(header: ProfileHeaderUi) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GameImage(
                    url = header.profileIconUrl,
                    contentDescription = null,
                    size = 72.dp,
                    shape = RoundedCornerShape(16.dp),
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        header.riotId.gameName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        "#${header.riotId.tagLine}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        stringResource(R.string.profile_level, header.level.toInt()),
                        style = MaterialTheme.typography.labelLarge,
                        color = LegendColors.Highlight,
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RankBlock(header.soloDuo, Modifier.weight(1f))
                RankBlock(header.flex, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun RankBlock(rank: RankUi, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            stringResource(if (rank.queue == RankedQueue.SOLO_DUO) R.string.rank_solo_duo else R.string.rank_flex),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (rank.tier == null) {
            Text(stringResource(R.string.rank_unranked), style = MaterialTheme.typography.titleSmall)
            return@Column
        }
        Text(
            tierLabel(rank),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = tierColor(rank.tier),
        )
        Text(stringResource(R.string.rank_lp, rank.leaguePoints), style = MaterialTheme.typography.bodySmall)
        Text(
            stringResource(R.string.rank_record, rank.wins, rank.losses) + " · " +
                stringResource(R.string.rank_winrate, rank.winrate),
            style = MaterialTheme.typography.bodySmall,
            color = if (rank.winrate >= 50) LegendColors.Victory else LegendColors.Defeat,
        )
    }
}

/** Les tiers Master+ n'ont pas de division affichée. */
private fun tierLabel(rank: RankUi): String {
    val tier = rank.tier.orEmpty().lowercase().replaceFirstChar { it.uppercase() }
    return if (rank.tier in setOf("MASTER", "GRANDMASTER", "CHALLENGER")) tier else "$tier ${rank.division}"
}

private fun tierColor(tier: String) = when (tier) {
    "IRON" -> Color(0xFF8C8181)
    "BRONZE" -> Color(0xFFB0795B)
    "SILVER" -> Color(0xFFA7B4BB)
    "GOLD" -> Color(0xFFE0B35A)
    "PLATINUM" -> Color(0xFF4FB5A5)
    "EMERALD" -> Color(0xFF2DBE78)
    "DIAMOND" -> Color(0xFF6E8CF0)
    "MASTER" -> Color(0xFFB06BD6)
    "GRANDMASTER" -> Color(0xFFE0565B)
    "CHALLENGER" -> Color(0xFFF4C874)
    else -> Color.White
}
