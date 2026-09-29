package com.example.legendanalytics.ui.match

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legendanalytics.R
import com.example.legendanalytics.domain.model.MatchOutcome
import com.example.legendanalytics.domain.model.TeamObjectives
import com.example.legendanalytics.domain.util.TimeAgo
import com.example.legendanalytics.ui.common.ChampionIcon
import com.example.legendanalytics.ui.common.ErrorView
import com.example.legendanalytics.ui.common.ItemsRow
import com.example.legendanalytics.ui.common.LoadingView
import com.example.legendanalytics.ui.common.SpellsColumn
import com.example.legendanalytics.ui.common.UiState
import com.example.legendanalytics.ui.common.dataOrNull
import com.example.legendanalytics.ui.common.formatCompact
import com.example.legendanalytics.ui.common.formatDuration
import com.example.legendanalytics.ui.common.kdaLabel
import com.example.legendanalytics.ui.common.label
import com.example.legendanalytics.ui.common.labelRes
import com.example.legendanalytics.ui.common.queueLabel
import com.example.legendanalytics.ui.model.MatchDetailUi
import com.example.legendanalytics.ui.model.ParticipantUi
import com.example.legendanalytics.ui.model.TeamUi
import com.example.legendanalytics.ui.navigation.ProfileRoute
import com.example.legendanalytics.ui.profile.accentColor
import com.example.legendanalytics.ui.profile.backgroundColor
import com.example.legendanalytics.ui.theme.LegendColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    viewModel: MatchDetailViewModel,
    onBack: () -> Unit,
    onPlayerClick: (ProfileRoute) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val detail = state.dataOrNull
                    Column {
                        Text(
                            detail?.let { queueLabel(it.queueId, it.gameMode) }
                                ?: stringResource(R.string.match_detail_title),
                        )
                        if (detail != null) {
                            Text(
                                "${formatDuration(detail.durationSeconds)} · " +
                                    TimeAgo.between(detail.gameEndMillis, System.currentTimeMillis()).label(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
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
            when (val s = state) {
                UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(s.error, onRetry = viewModel::retry)
                is UiState.Success -> MatchDetailContent(
                    detail = s.data,
                    onParticipantClick = { participant ->
                        viewModel.profileRouteFor(participant)?.let(onPlayerClick)
                    },
                )
            }
        }
    }
}

@Composable
private fun MatchDetailContent(detail: MatchDetailUi, onParticipantClick: (ParticipantUi) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        detail.teams.forEach { team ->
            item(key = "team_${team.teamId}") {
                Spacer(Modifier.height(6.dp))
                TeamHeader(team)
            }
            itemsIndexed(team.participants, key = { index, _ -> "p_${team.teamId}_$index" }) { _, participant ->
                ParticipantRow(
                    participant = participant,
                    outcome = team.outcome,
                    onClick = { onParticipantClick(participant) },
                )
            }
        }
    }
}

@Composable
private fun TeamHeader(team: TeamUi) {
    val accent = team.outcome.accentColor()
    val isWinner = team.outcome == MatchOutcome.VICTORY
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface)
            .then(if (isWinner) Modifier.border(BorderStroke(2.dp, accent), RoundedCornerShape(8.dp)) else Modifier)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(team.outcome.labelRes()),
                color = accent,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(if (team.isBlueSide) R.string.team_blue else R.string.team_red),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            Text(
                stringResource(R.string.team_kills_gold, team.totalKills, formatCompact(team.totalGold)),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        team.objectives?.let { ObjectivesRow(it) }
    }
}

@Composable
private fun ObjectivesRow(objectives: TeamObjectives) {
    val values = listOf(
        R.string.objective_towers to objectives.towers,
        R.string.objective_inhibitors to objectives.inhibitors,
        R.string.objective_dragons to objectives.dragons,
        R.string.objective_barons to objectives.barons,
        R.string.objective_heralds to objectives.riftHeralds,
        R.string.objective_grubs to objectives.voidGrubs,
    )
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        values.forEach { (label, value) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value.toString(), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(
                    stringResource(label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun ParticipantRow(participant: ParticipantUi, outcome: MatchOutcome, onClick: () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    val clickable = participant.riotId != null
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(outcome.backgroundColor())
            .then(
                if (participant.isFocused) Modifier.border(BorderStroke(2.dp, LegendColors.Highlight), shape)
                else Modifier,
            )
            .clickable(enabled = clickable, onClick = onClick)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box {
                ChampionIcon(participant.championIconUrl, participant.championName, size = 40.dp)
                Text(
                    participant.championLevel.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 3.dp),
                )
            }
            Spacer(Modifier.width(4.dp))
            SpellsColumn(participant.spellIconUrls, spellSize = 18.dp)
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    participant.riotId?.gameName ?: stringResource(R.string.unknown_player),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (participant.isFocused) FontWeight.Bold else FontWeight.Normal,
                    color = if (participant.isFocused) LegendColors.Highlight else Color.Unspecified,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    participant.championName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${participant.kills} / ${participant.deaths} / ${participant.assists}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    kdaLabel(participant.kda),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        ItemsRow(participant.itemIconUrls, itemSize = 22.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                StatLabel(R.string.header_damage, formatCompact(participant.damageToChampions))
                LinearProgressIndicator(
                    progress = { participant.damageRatio },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = LegendColors.Defeat,
                    trackColor = LegendColors.EmptySlot,
                    strokeCap = StrokeCap.Round,
                    drawStopIndicator = {},
                )
            }
            StatLabel(R.string.header_gold, formatCompact(participant.goldEarned))
            StatLabel(R.string.header_cs, participant.creepScore.toString())
            StatLabel(R.string.header_vision, participant.visionScore.toString())
        }
    }
}

@Composable
private fun StatLabel(labelRes: Int, value: String) {
    Row(verticalAlignment = Alignment.Bottom) {
        Text(
            stringResource(labelRes) + " ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
}
