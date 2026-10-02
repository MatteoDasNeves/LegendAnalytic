package com.example.legendanalytics.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.legendanalytics.R
import com.example.legendanalytics.domain.model.MatchOutcome
import com.example.legendanalytics.ui.theme.LegendColors

fun MatchOutcome.color(): Color = when (this) {
    MatchOutcome.VICTORY -> LegendColors.Victory
    MatchOutcome.DEFEAT -> LegendColors.Defeat
    MatchOutcome.REMAKE -> LegendColors.Remake
}

/** Titre de section : petites capitales dorées suivies d'un filet. */
@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier, trailing: String? = null) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = LegendColors.Gold,
        )
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(LegendColors.Outline),
        )
        if (trailing != null) {
            Spacer(Modifier.width(10.dp))
            Text(trailing, style = MaterialTheme.typography.labelMedium, color = LegendColors.Muted)
        }
    }
}

/** Anneau de winrate : arc bleu (victoires) sur fond rouge (défaites). */
@Composable
fun WinrateRing(
    winrate: Int,
    hasGames: Boolean,
    caption: String,
    modifier: Modifier = Modifier,
    size: Dp = 104.dp,
    strokeWidth: Dp = 10.dp,
) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = strokeWidth.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            val topLeft = Offset(stroke / 2, stroke / 2)
            val track = if (hasGames) LegendColors.Defeat else LegendColors.EmptySlot
            drawArc(track, 0f, 360f, false, topLeft, arcSize, style = Stroke(stroke))
            if (hasGames && winrate > 0) {
                drawArc(
                    LegendColors.Victory, -90f, 360f * winrate / 100f, false, topLeft, arcSize,
                    style = Stroke(stroke, cap = StrokeCap.Round),
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (hasGames) "$winrate %" else "–",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
            Text(caption, style = MaterialTheme.typography.labelSmall, color = LegendColors.Muted)
        }
    }
}

/** Forme récente : une pastille lettrée par partie, de la plus récente à la plus ancienne. */
@Composable
fun FormStrip(form: List<MatchOutcome>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        form.forEach { outcome ->
            val letter = when (outcome) {
                MatchOutcome.VICTORY -> R.string.form_letter_victory
                MatchOutcome.DEFEAT -> R.string.form_letter_defeat
                MatchOutcome.REMAKE -> R.string.form_letter_remake
            }
            Box(
                Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(outcome.color().copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    stringResource(letter),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = outcome.color(),
                )
            }
        }
    }
}

/** Petite pastille colorée devant une légende. */
@Composable
fun LegendDot(color: Color, modifier: Modifier = Modifier) {
    Box(modifier.size(8.dp).clip(CircleShape).background(color))
}
