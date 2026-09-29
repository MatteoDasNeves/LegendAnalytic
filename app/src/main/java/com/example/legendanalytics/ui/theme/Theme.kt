package com.example.legendanalytics.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object LegendColors {
    val Victory = Color(0xFF5383E8)
    val VictoryBackground = Color(0xFF28344E)
    val Defeat = Color(0xFFE84057)
    val DefeatBackground = Color(0xFF59343B)
    val Remake = Color(0xFF9E9EB1)
    val RemakeBackground = Color(0xFF2E3038)
    val Highlight = Color(0xFFF4C874)
    val Background = Color(0xFF12151C)
    val Surface = Color(0xFF1C2029)
    val SurfaceVariant = Color(0xFF262B36)
    val Muted = Color(0xFF9AA4AF)
    val EmptySlot = Color(0xFF2F3440)
}

private val DarkScheme = darkColorScheme(
    primary = LegendColors.Victory,
    onPrimary = Color.White,
    secondary = LegendColors.Highlight,
    onSecondary = Color.Black,
    error = LegendColors.Defeat,
    background = LegendColors.Background,
    onBackground = Color(0xFFE6E8EC),
    surface = LegendColors.Surface,
    onSurface = Color(0xFFE6E8EC),
    surfaceVariant = LegendColors.SurfaceVariant,
    onSurfaceVariant = LegendColors.Muted,
    surfaceContainer = LegendColors.Surface,
    surfaceContainerHigh = LegendColors.SurfaceVariant,
    outline = Color(0xFF3A4150),
)

/** Thème sombre uniquement : c'est le choix par défaut de ce type d'outil. */
@Composable
fun LegendTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = Typography(),
        content = content,
    )
}
