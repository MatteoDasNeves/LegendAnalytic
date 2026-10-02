package com.example.legendanalytics.ui.match

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.legendanalytics.R
import com.example.legendanalytics.domain.model.MatchOutcome
import com.example.legendanalytics.domain.util.TimeAgo
import com.example.legendanalytics.ui.common.ChampionIcon
import com.example.legendanalytics.ui.common.ErrorView
import com.example.legendanalytics.ui.common.ItemsRow
import com.example.legendanalytics.ui.common.LoadingView
import com.example.legendanalytics.ui.common.SpellsColumn
import com.example.legendanalytics.ui.common.LegendDot
import com.example.legendanalytics.ui.common.SectionTitle
import com.example.legendanalytics.ui.common.UiState
import com.example.legendanalytics.ui.common.WidthClass
import com.example.legendanalytics.ui.common.centeredMaxWidth
import com.example.legendanalytics.ui.common.widthClassOf
import com.example.legendanalytics.ui.common.color
import com.example.legendanalytics.ui.common.dataOrNull
import com.example.legendanalytics.ui.common.formatCompact
import com.example.legendanalytics.ui.common.formatDuration
import com.example.legendanalytics.ui.common.kdaLabel
import com.example.legendanalytics.ui.common.label
import com.example.legendanalytics.ui.common.labelRes
import com.example.legendanalytics.ui.common.placementLabel
import com.example.legendanalytics.ui.common.queueLabel
import com.example.legendanalytics.ui.model.MatchDetailUi
import com.example.legendanalytics.ui.model.ParticipantUi
import com.example.legendanalytics.ui.model.TeamUi
import com.example.legendanalytics.ui.navigation.ProfileRoute
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
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Sur grand écran, les équipes s'affichent côte à côte (deux par deux en Arena).
        val sideBySide = widthClassOf(maxWidth) == WidthClass.EXPANDED
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Tableau des scores face à face, seulement pour les modes à deux équipes (pas l'Arena).
            if (detail.teams.size == 2 && detail.teams.none { it.placement != null }) {
                item(key = "scoreboard") {
                    Scoreboard(detail.teams[0], detail.teams[1], Modifier.centeredMaxWidth())
                }
            }
            if (sideBySide) {
                detail.teams.chunked(2).forEach { pair ->
                    item(key = "teams_${pair.first().teamId}") {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            pair.forEach { team ->
                                TeamColumn(team, onParticipantClick, Modifier.weight(1f))
                            }
                            // Garde la même largeur de colonne pour une équipe seule en fin de liste.
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            } else {
                detail.teams.forEach { team ->
                    item(key = "team_${team.teamId}") { TeamTitle(team, Modifier.centeredMaxWidth()) }
                    itemsIndexed(team.participants, key = { index, _ -> "p_${team.teamId}_$index" }) { _, participant ->
                        ParticipantRow(
                            participant = participant,
                            onClick = { onParticipantClick(participant) },
                            modifier = Modifier.centeredMaxWidth(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeamColumn(team: TeamUi, onParticipantClick: (ParticipantUi) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TeamTitle(team)
        team.participants.forEach { participant ->
            ParticipantRow(participant = participant, onClick = { onParticipantClick(participant) })
        }
    }
}

@Composable
private fun teamName(team: TeamUi): String =
    stringResource(if (team.isBlueSide) R.string.team_blue else R.string.team_red)

@Composable
private fun Scoreboard(left: TeamUi, right: TeamUi, modifier: Modifier = Modifier) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(LegendColors.Surface)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScoreSide(left, Alignment.Start, Modifier.weight(1f))
            Text(
                buildAnnotatedString {
                    withStyle(SpanStyle(color = left.outcome.color())) { append(left.totalKills.toString()) }
                    withStyle(SpanStyle(color = LegendColors.Muted)) { append("  :  ") }
                    withStyle(SpanStyle(color = right.outcome.color())) { append(right.totalKills.toString()) }
                },
                style = MaterialTheme.typography.headlineMedium,
            )
            ScoreSide(right, Alignment.End, Modifier.weight(1f))
        }
        // Les barres suivent le code couleur du résultat (bleu = vainqueur), pas le côté de la carte.
        val colors = left.outcome.color() to right.outcome.color()
        ComparisonRow(stringResource(R.string.scoreboard_gold), left.totalGold, right.totalGold, colors, ::formatCompact)
        val l = left.objectives
        val r = right.objectives
        if (l != null && r != null) {
            ComparisonRow(stringResource(R.string.objective_towers), l.towers, r.towers, colors)
            ComparisonRow(stringResource(R.string.objective_dragons), l.dragons, r.dragons, colors)
            ComparisonRow(stringResource(R.string.objective_barons), l.barons, r.barons, colors)
            ComparisonRow(stringResource(R.string.objective_grubs), l.voidGrubs, r.voidGrubs, colors)
            ComparisonRow(stringResource(R.string.objective_heralds), l.riftHeralds, r.riftHeralds, colors)
            ComparisonRow(stringResource(R.string.objective_inhibitors), l.inhibitors, r.inhibitors, colors)
        }
    }
}

@Composable
private fun ScoreSide(team: TeamUi, alignment: Alignment.Horizontal, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = alignment, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(teamName(team), style = MaterialTheme.typography.labelMedium, color = LegendColors.Muted)
        Text(
            stringResource(team.outcome.labelRes()),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = team.outcome.color(),
        )
        if (team.outcome == MatchOutcome.VICTORY) {
            Text(
                stringResource(R.string.scoreboard_winner).uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(LegendColors.Gold)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

/** Valeur gauche | libellé | valeur droite, avec deux barres qui partent du centre. */
@Composable
private fun ComparisonRow(
    label: String,
    left: Int,
    right: Int,
    colors: Pair<Color, Color>,
    format: (Int) -> String = { it.toString() },
) {
    val total = (left + right).coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                format(left),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (left > right) FontWeight.Black else FontWeight.Normal,
                modifier = Modifier.weight(1f),
            )
            Text(label, style = MaterialTheme.typography.labelMedium, color = LegendColors.Muted)
            Text(
                format(right),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (right > left) FontWeight.Black else FontWeight.Normal,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
        }
        Row(Modifier.fillMaxWidth().height(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(LegendColors.EmptySlot)) {
                if (left > 0) {
                    Box(
                        Modifier
                            .align(Alignment.CenterEnd)
                            .fillMaxWidth(left.toFloat() / total)
                            .fillMaxHeight()
                            .background(colors.first),
                    )
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(2.dp)).background(LegendColors.EmptySlot)) {
                if (right > 0) {
                    Box(
                        Modifier
                            .fillMaxWidth(right.toFloat() / total)
                            .fillMaxHeight()
                            .background(colors.second),
                    )
                }
            }
        }
    }
}

@Composable
private fun TeamTitle(team: TeamUi, modifier: Modifier = Modifier) {
    Row(
        modifier.padding(top = 16.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LegendDot(team.outcome.color())
        Spacer(Modifier.width(8.dp))
        SectionTitle(
            team.placement?.let { placementLabel(it) }
                ?: "${teamName(team)} · ${stringResource(team.outcome.labelRes())}",
            trailing = stringResource(R.string.team_kills_gold, team.totalKills, formatCompact(team.totalGold)),
        )
    }
}

@Composable
private fun ParticipantRow(participant: ParticipantUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(14.dp)
    val clickable = participant.riotId != null
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (participant.isFocused) LegendColors.SurfaceHigh else LegendColors.Surface)
            .then(
                if (participant.isFocused) Modifier.border(BorderStroke(1.5.dp, LegendColors.Gold), shape)
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
                    color = if (participant.isFocused) LegendColors.Gold else Color.Unspecified,
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
