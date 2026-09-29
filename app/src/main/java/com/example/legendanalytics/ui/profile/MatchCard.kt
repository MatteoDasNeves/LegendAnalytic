package com.example.legendanalytics.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.legendanalytics.R
import com.example.legendanalytics.domain.model.MatchOutcome
import com.example.legendanalytics.domain.util.TimeAgo
import com.example.legendanalytics.ui.common.ChampionIcon
import com.example.legendanalytics.ui.common.ItemsRow
import com.example.legendanalytics.ui.common.SpellsColumn
import com.example.legendanalytics.ui.common.formatDecimal
import com.example.legendanalytics.ui.common.formatDuration
import com.example.legendanalytics.ui.common.kdaLabel
import com.example.legendanalytics.ui.common.label
import com.example.legendanalytics.ui.common.labelRes
import com.example.legendanalytics.ui.common.queueLabel
import com.example.legendanalytics.ui.model.MatchCardUi
import com.example.legendanalytics.ui.theme.LegendColors

fun MatchOutcome.accentColor(): Color = when (this) {
    MatchOutcome.VICTORY -> LegendColors.Victory
    MatchOutcome.DEFEAT -> LegendColors.Defeat
    MatchOutcome.REMAKE -> LegendColors.Remake
}

fun MatchOutcome.backgroundColor(): Color = when (this) {
    MatchOutcome.VICTORY -> LegendColors.VictoryBackground
    MatchOutcome.DEFEAT -> LegendColors.DefeatBackground
    MatchOutcome.REMAKE -> LegendColors.RemakeBackground
}

@Composable
fun MatchCard(match: MatchCardUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = match.outcome.accentColor()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(RoundedCornerShape(8.dp))
            .background(match.outcome.backgroundColor())
            .clickable(onClick = onClick),
    ) {
        // Liseré coloré victoire / défaite.
        Box(Modifier.width(6.dp).fillMaxHeight().background(accent))
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    queueLabel(match.queueId, match.gameMode),
                    style = MaterialTheme.typography.labelLarge,
                    color = accent,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    TimeAgo.between(match.gameEndMillis, System.currentTimeMillis()).label(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${stringResource(match.outcome.labelRes())} · ${formatDuration(match.durationSeconds)}",
                    style = MaterialTheme.typography.labelMedium,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box {
                    ChampionIcon(match.championIconUrl, match.championName, size = 48.dp)
                    Text(
                        match.championLevel.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Black.copy(alpha = 0.7f))
                            .padding(horizontal = 3.dp),
                    )
                }
                Spacer(Modifier.width(4.dp))
                SpellsColumn(match.spellIconUrls, spellSize = 22.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        buildAnnotatedString {
                            append("${match.kills} / ")
                            withStyle(SpanStyle(color = LegendColors.Defeat)) { append(match.deaths.toString()) }
                            append(" / ${match.assists}")
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        kdaLabel(match.kda),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        stringResource(R.string.cs_value, match.creepScore, formatDecimal(match.csPerMinute, 1)),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text(
                        stringResource(R.string.kill_participation, match.killParticipation),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            ItemsRow(match.itemIconUrls, itemSize = 24.dp)
        }
    }
}
