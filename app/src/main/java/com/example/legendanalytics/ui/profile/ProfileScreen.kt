package com.example.legendanalytics.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.legendanalytics.R
import com.example.legendanalytics.domain.model.RankedQueue
import com.example.legendanalytics.ui.common.ErrorView
import com.example.legendanalytics.ui.common.FormStrip
import com.example.legendanalytics.ui.common.GameImage
import com.example.legendanalytics.ui.common.LegendDot
import com.example.legendanalytics.ui.common.LoadingView
import com.example.legendanalytics.ui.common.SectionTitle
import com.example.legendanalytics.ui.common.UiState
import com.example.legendanalytics.ui.common.WidthClass
import com.example.legendanalytics.ui.common.WinrateRing
import com.example.legendanalytics.ui.common.centeredMaxWidth
import com.example.legendanalytics.ui.common.formatCompact
import com.example.legendanalytics.ui.common.formatDecimal
import com.example.legendanalytics.ui.common.kdaLabel
import com.example.legendanalytics.ui.common.message
import com.example.legendanalytics.ui.common.widthClassOf
import com.example.legendanalytics.ui.model.MainChampionUi
import com.example.legendanalytics.ui.model.MatchCardUi
import com.example.legendanalytics.ui.model.OverviewUi
import com.example.legendanalytics.ui.model.ProfileHeaderUi
import com.example.legendanalytics.ui.model.RankUi
import com.example.legendanalytics.ui.theme.LegendColors
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
    onMatchClick: (String) -> Unit,
    onAffinitiesClick: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val summaryListState = rememberLazyListState()
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

    // Pas de barre d'application : l'en-tête illustré occupe le haut de l'écran, sous la barre d'état.
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState, Modifier.navigationBarsPadding()) },
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
                    ProfileContentLayout(
                        content = content.data,
                        listState = listState,
                        summaryListState = summaryListState,
                        isLoadingMore = state.isLoadingMore,
                        canLoadMore = state.canLoadMore,
                        onLoadMore = viewModel::loadMore,
                        onMatchClick = onMatchClick,
                        onAffinitiesClick = onAffinitiesClick,
                        heroActions = {
                            HeroActions(
                                onBack = onBack,
                                onRefresh = viewModel::refresh,
                                refreshEnabled = !state.isRefreshing,
                            )
                        },
                    )
                }
            }
            if (state.content !is UiState.Success) {
                HeroActions(onBack = onBack, onRefresh = viewModel::refresh, refreshEnabled = false)
            }
        }
    }
}

@Composable
private fun HeroActions(onBack: () -> Unit, onRefresh: () -> Unit, refreshEnabled: Boolean) {
    val colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.45f))
    Row(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        IconButton(onClick = onBack, colors = colors) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onRefresh, enabled = refreshEnabled, colors = colors) {
            Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.profile_refresh))
        }
    }
}

/**
 * Une colonne unique sur téléphone ; sur grand écran (tablette, paysage), le résumé du joueur
 * reste à gauche pendant que l'historique défile à droite.
 */
@Composable
private fun ProfileContentLayout(
    content: ProfileContent,
    listState: LazyListState,
    summaryListState: LazyListState,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    onMatchClick: (String) -> Unit,
    onAffinitiesClick: () -> Unit,
    heroActions: @Composable () -> Unit,
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // En paysage, un en-tête de 300 dp masquerait tout l'écran.
        val heroHeight = (maxHeight * 0.55f).coerceIn(240.dp, 300.dp)

        if (widthClassOf(maxWidth) == WidthClass.EXPANDED) {
            val summaryWidth = (maxWidth * 0.4f).coerceIn(340.dp, 440.dp)
            Row(Modifier.fillMaxSize()) {
                Box(Modifier.width(summaryWidth).fillMaxHeight()) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = summaryListState,
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        profileSummary(content, heroHeight, heroActions, onAffinitiesClick)
                        item(key = "bottom_inset") { Spacer(Modifier.navigationBarsPadding()) }
                    }
                    StatusBarScrim(summaryListState)
                }
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxHeight().statusBarsPadding(),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    matchHistory(content, isLoadingMore, canLoadMore, onLoadMore, onMatchClick)
                    item(key = "bottom_inset") { Spacer(Modifier.navigationBarsPadding()) }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                profileSummary(content, heroHeight, heroActions, onAffinitiesClick)
                matchHistory(content, isLoadingMore, canLoadMore, onLoadMore, onMatchClick)
                item(key = "bottom_inset") { Spacer(Modifier.navigationBarsPadding()) }
            }
            StatusBarScrim(listState)
        }
    }
}

/** Fond opaque sous la barre d'état une fois l'en-tête illustré sorti de l'écran. */
@Composable
private fun StatusBarScrim(listState: LazyListState) {
    val headerScrolledAway by remember(listState) { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    if (headerScrolledAway) {
        Box(
            Modifier
                .fillMaxWidth()
                .windowInsetsTopHeight(WindowInsets.statusBars)
                .background(LegendColors.Background),
        )
    }
}

private fun LazyListScope.profileSummary(
    content: ProfileContent,
    heroHeight: Dp,
    heroActions: @Composable () -> Unit,
    onAffinitiesClick: () -> Unit,
) {
    item(key = "hero") {
        Box {
            ProfileHero(content.header, heroHeight)
            heroActions()
        }
    }
    item(key = "overview") {
        Section(stringResource(R.string.section_overview)) { OverviewCard(content.overview) }
    }
    item(key = "ranks") {
        Section(stringResource(R.string.section_ranks)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RankCard(content.header.soloDuo, Modifier.weight(1f))
                RankCard(content.header.flex, Modifier.weight(1f))
            }
        }
    }
    item(key = "mains") {
        Section(stringResource(R.string.section_mains)) {
            MainsRow(content.mains)
            OutlinedButton(onClick = onAffinitiesClick, modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                Text(stringResource(R.string.profile_all_affinities))
            }
        }
    }
}

private fun LazyListScope.matchHistory(
    content: ProfileContent,
    isLoadingMore: Boolean,
    canLoadMore: Boolean,
    onLoadMore: () -> Unit,
    onMatchClick: (String) -> Unit,
) {
    item(key = "history_title") {
        SectionTitle(
            stringResource(R.string.section_history),
            modifier = Modifier.centeredMaxWidth().padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 4.dp),
            trailing = content.matches.size.toString(),
        )
    }
    if (content.matches.isEmpty()) {
        item(key = "empty") {
            Text(
                stringResource(R.string.profile_no_matches),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.centeredMaxWidth().padding(16.dp),
            )
        }
    }
    matchesByDay(content.matches, onMatchClick)

    if (canLoadMore || isLoadingMore) {
        item(key = "load_more") {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                if (isLoadingMore) {
                    CircularProgressIndicator(Modifier.size(28.dp))
                } else {
                    OutlinedButton(onClick = onLoadMore) { Text(stringResource(R.string.profile_load_more)) }
                }
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(Modifier.centeredMaxWidth().padding(start = 16.dp, end = 16.dp, top = 24.dp)) {
        SectionTitle(title)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

// --- En-tête illustré ------------------------------------------------------------------------

@Composable
private fun ProfileHero(header: ProfileHeaderUi, height: Dp) {
    Box(Modifier.fillMaxWidth().height(height)) {
        if (header.bannerUrl != null) {
            AsyncImage(
                model = header.bannerUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // Fondu vers le fond de l'écran pour que le texte reste lisible.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0f to LegendColors.Background.copy(alpha = 0.35f),
                        0.55f to LegendColors.Background.copy(alpha = 0.7f),
                        1f to LegendColors.Background,
                    ),
                ),
        )
        Column(
            Modifier
                .align(Alignment.BottomCenter)
                .centeredMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Box {
                GameImage(
                    url = header.profileIconUrl,
                    contentDescription = null,
                    size = 88.dp,
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier.border(BorderStroke(2.dp, LegendColors.Gold), RoundedCornerShape(26.dp)),
                )
                Text(
                    header.level.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 10.dp)
                        .clip(CircleShape)
                        .background(LegendColors.Gold)
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                header.riotId.gameName,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                stringResource(R.string.profile_region_tag, header.riotId.tagLine, header.regionLabel),
                style = MaterialTheme.typography.bodyMedium,
                color = LegendColors.Muted,
            )
        }
    }
}

// --- Bilan -----------------------------------------------------------------------------------

@Composable
private fun OverviewCard(overview: OverviewUi) {
    // Sans partie classée, l'anneau bascule sur les parties récentes.
    val useRanked = overview.rankedGames > 0
    val games = if (useRanked) overview.rankedGames else overview.recentGames
    val wins = if (useRanked) overview.rankedWins else overview.recentWins
    val losses = if (useRanked) overview.rankedLosses else overview.recentLosses
    val winrate = if (useRanked) overview.rankedWinrate else overview.recentWinrate

    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(LegendColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WinrateRing(
                winrate = winrate,
                hasGames = games > 0,
                caption = stringResource(if (useRanked) R.string.overview_ring_season else R.string.overview_ring_recent),
            )
            Spacer(Modifier.width(20.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(games.toString(), style = MaterialTheme.typography.headlineMedium)
                Text(
                    stringResource(if (useRanked) R.string.overview_ranked_games else R.string.overview_recent_games),
                    style = MaterialTheme.typography.bodySmall,
                    color = LegendColors.Muted,
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendDot(LegendColors.Victory)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.overview_wins, wins), style = MaterialTheme.typography.bodyMedium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendDot(LegendColors.Defeat)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.overview_losses, losses), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (overview.recentGames == 0) {
            Text(stringResource(R.string.overview_no_recent), color = LegendColors.Muted)
            return@Column
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.overview_form),
                style = MaterialTheme.typography.labelMedium,
                color = LegendColors.Muted,
            )
            FormStrip(overview.form)
            Text(
                stringResource(
                    R.string.overview_recent_line,
                    overview.recentGames,
                    overview.recentWins,
                    overview.recentLosses,
                    overview.recentWinrate,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                stringResource(
                    R.string.overview_average_kda,
                    formatDecimal(overview.averageKills, 1),
                    formatDecimal(overview.averageDeaths, 1),
                    formatDecimal(overview.averageAssists, 1),
                    kdaLabel(overview.recentKda),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = LegendColors.Muted,
            )
        }
    }
}

// --- Classement ------------------------------------------------------------------------------

@Composable
private fun RankCard(rank: RankUi, modifier: Modifier = Modifier) {
    val accent = rank.tier?.let(::tierColor) ?: LegendColors.Muted
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(LegendColors.Surface)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(
            stringResource(if (rank.queue == RankedQueue.SOLO_DUO) R.string.rank_solo_duo else R.string.rank_flex),
            style = MaterialTheme.typography.labelMedium,
            color = LegendColors.Muted,
        )
        if (rank.tier == null) {
            Text(stringResource(R.string.rank_unranked), style = MaterialTheme.typography.titleMedium)
            return@Column
        }
        Text(tierLabel(rank), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = accent)
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Au-delà de Diamant, les LP ne sont pas bornés à 100 : jauge pleine.
            LinearProgressIndicator(
                progress = { if (rank.tier in APEX_TIERS) 1f else (rank.leaguePoints / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.weight(1f).height(5.dp),
                color = accent,
                trackColor = LegendColors.EmptySlot,
                strokeCap = StrokeCap.Round,
                drawStopIndicator = {},
            )
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.rank_lp, rank.leaguePoints), style = MaterialTheme.typography.labelMedium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.rank_record, rank.wins, rank.losses), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.rank_winrate_short, rank.winrate),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (rank.winrate >= 50) LegendColors.Victory else LegendColors.Defeat,
            )
        }
    }
}

private val APEX_TIERS = setOf("MASTER", "GRANDMASTER", "CHALLENGER")

private fun tierLabel(rank: RankUi): String {
    val tier = rank.tier.orEmpty().lowercase().replaceFirstChar { it.uppercase() }
    return if (rank.tier in APEX_TIERS) tier else "$tier ${rank.division}"
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

// --- Champions principaux --------------------------------------------------------------------

@Composable
private fun MainsRow(mains: List<MainChampionUi>) {
    if (mains.isEmpty()) {
        Text(stringResource(R.string.mains_empty), color = LegendColors.Muted, style = MaterialTheme.typography.bodyMedium)
        return
    }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        mains.forEachIndexed { index, main ->
            MainChampionCard(main, rank = index + 1, modifier = Modifier.weight(1f))
        }
        // Garde des cartes de même largeur même avec moins de 3 champions.
        repeat(3 - mains.size) { Spacer(Modifier.weight(1f)) }
    }
}

@Composable
private fun MainChampionCard(main: MainChampionUi, rank: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(200.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(LegendColors.EmptySlot),
    ) {
        AsyncImage(
            model = main.tileUrl,
            contentDescription = main.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0.35f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.92f))),
        )
        Text(
            stringResource(R.string.main_rank, rank),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = LegendColors.Gold,
            modifier = Modifier
                .padding(8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.6f))
                .padding(horizontal = 7.dp, vertical = 2.dp),
        )
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                main.displayName,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (main.masteryLevel != null && main.masteryPoints != null) {
                Text(
                    stringResource(R.string.main_mastery_level, main.masteryLevel),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = LegendColors.Gold,
                    maxLines = 1,
                )
                Text(
                    stringResource(R.string.main_mastery_points, formatCompact(main.masteryPoints)),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = MaterialTheme.typography.labelMedium.letterSpacing),
                    color = LegendColors.Muted,
                    maxLines = 1,
                )
            }
            if (main.recentGames > 0) {
                Text(
                    pluralStringResource(R.plurals.main_recent, main.recentGames, main.recentGames, main.recentWinrate),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (main.recentWinrate >= 50) LegendColors.Victory else LegendColors.Defeat,
                )
                main.recentKda?.let {
                    Text(kdaLabel(it), style = MaterialTheme.typography.labelSmall, color = LegendColors.Muted)
                }
            } else {
                Text(
                    stringResource(R.string.main_not_recent),
                    style = MaterialTheme.typography.labelMedium,
                    color = LegendColors.Muted,
                )
            }
        }
    }
}

// --- Historique groupé par jour --------------------------------------------------------------

private fun LazyListScope.matchesByDay(matches: List<MatchCardUi>, onMatchClick: (String) -> Unit) {
    val zone = ZoneId.systemDefault()
    matches
        .groupBy { Instant.ofEpochMilli(it.gameEndMillis).atZone(zone).toLocalDate() }
        .forEach { (day, dayMatches) ->
            item(key = "day_$day") { DayHeader(day) }
            items(dayMatches, key = { it.matchId }) { match ->
                MatchCard(
                    match = match,
                    onClick = { onMatchClick(match.matchId) },
                    modifier = Modifier.centeredMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp),
                )
            }
        }
}

private val dayFormatter = DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)

@Composable
private fun DayHeader(day: LocalDate) {
    val today = LocalDate.now()
    val label = when (day) {
        today -> stringResource(R.string.day_today)
        today.minusDays(1) -> stringResource(R.string.day_yesterday)
        else -> day.format(dayFormatter).replaceFirstChar { it.uppercase() }
    }
    Text(
        label,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.centeredMaxWidth().padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp),
    )
}
