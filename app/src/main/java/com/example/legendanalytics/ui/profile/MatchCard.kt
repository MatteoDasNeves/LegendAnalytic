package com.example.legendanalytics.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.legendanalytics.R
import com.example.legendanalytics.ui.common.GameImage
import com.example.legendanalytics.ui.common.color
import com.example.legendanalytics.ui.common.formatDecimal
import com.example.legendanalytics.ui.common.formatDuration
import com.example.legendanalytics.ui.common.kdaLabel
import com.example.legendanalytics.ui.common.labelRes
import com.example.legendanalytics.ui.common.placementLabel
import com.example.legendanalytics.ui.common.queueLabel
import com.example.legendanalytics.ui.model.MatchCardUi
import com.example.legendanalytics.ui.theme.LegendColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

/** Couleur du ratio KDA : or au-dessus de 4, bleu au-dessus de 3, neutre sinon. */
fun kdaColor(kda: Double): Color = when {
    kda >= 4.0 -> LegendColors.Gold
    kda >= 3.0 -> LegendColors.Victory
    else -> LegendColors.Muted
}

@Composable
fun MatchCard(match: MatchCardUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val accent = match.outcome.color()
    val portraitShape = RoundedCornerShape(16.dp)
    val endTime = Instant.ofEpochMilli(match.gameEndMillis).atZone(ZoneId.systemDefault()).format(timeFormatter)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(LegendColors.Surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Portrait encadré de la couleur du résultat.
            Box {
                GameImage(
                    url = match.championIconUrl,
                    contentDescription = match.championDisplayName,
                    size = 56.dp,
                    shape = portraitShape,
                    modifier = Modifier.border(BorderStroke(2.dp, accent), portraitShape),
                )
                Text(
                    match.championLevel.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(3.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.75f))
                        .padding(horizontal = 4.dp),
                )
            }
            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    buildAnnotatedString {
                        withStyle(SpanStyle(color = accent, fontWeight = FontWeight.Bold)) {
                            append(match.placement?.let { placementLabel(it) } ?: stringResource(match.outcome.labelRes()))
                        }
                        withStyle(SpanStyle(color = LegendColors.Muted)) {
                            append("  ·  ")
                            append(queueLabel(match.queueId, match.gameMode))
                        }
                    },
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    match.championDisplayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${formatDuration(match.durationSeconds)} · $endTime",
                    style = MaterialTheme.typography.bodySmall,
                    color = LegendColors.Muted,
                )
            }
            Spacer(Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    buildAnnotatedString {
                        append("${match.kills}/")
                        withStyle(SpanStyle(color = LegendColors.Defeat)) { append(match.deaths.toString()) }
                        append("/${match.assists}")
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                )
                Text(
                    kdaLabel(match.kda),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = kdaColor(match.kda),
                )
            }
        }

        // Ligne basse : sorts + objets, puis CS et participation.
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
                match.spellIconUrls.forEach { GameImage(it, contentDescription = null, size = 18.dp) }
                Spacer(Modifier.width(5.dp))
                match.itemIconUrls.forEach {
                    GameImage(it, contentDescription = null, size = 18.dp, shape = RoundedCornerShape(5.dp))
                }
            }
            Spacer(Modifier.width(8.dp))
            // Prend la place restante : sur un écran étroit, le texte se tronque au lieu de déborder.
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                // Pas de sbires en Arena : le CS n'y a pas de sens.
                if (match.placement == null) {
                    Text(
                        stringResource(R.string.cs_per_min, match.creepScore, formatDecimal(match.csPerMinute, 1)),
                        style = MaterialTheme.typography.bodySmall,
                        color = LegendColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    stringResource(R.string.kill_participation, match.killParticipation),
                    style = MaterialTheme.typography.bodySmall,
                    color = LegendColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
