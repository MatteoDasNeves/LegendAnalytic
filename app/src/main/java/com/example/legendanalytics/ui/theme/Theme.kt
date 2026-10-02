package com.example.legendanalytics.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object LegendColors {
    // Issues de partie : bleu / rouge, utilisés en accents et non en fonds de cartes.
    val Victory = Color(0xFF4DA3FF)
    val Defeat = Color(0xFFFF5A67)
    val Remake = Color(0xFF8A919E)

    /** Or « Hextech » : couleur de marque de l'application. */
    val Gold = Color(0xFFC8AA6E)
    val GoldDim = Color(0xFF7A6840)

    val Background = Color(0xFF0B0D12)
    val Surface = Color(0xFF151922)
    val SurfaceHigh = Color(0xFF1D222D)
    val Outline = Color(0xFF2A303C)
    val Muted = Color(0xFF8C95A3)
    val EmptySlot = Color(0xFF232833)
}

private val DarkScheme = darkColorScheme(
    primary = LegendColors.Gold,
    onPrimary = Color(0xFF1A1407),
    primaryContainer = LegendColors.GoldDim,
    onPrimaryContainer = Color.White,
    secondary = LegendColors.Victory,
    onSecondary = Color.White,
    error = LegendColors.Defeat,
    background = LegendColors.Background,
    onBackground = Color(0xFFE9EBEF),
    surface = LegendColors.Surface,
    onSurface = Color(0xFFE9EBEF),
    surfaceVariant = LegendColors.SurfaceHigh,
    onSurfaceVariant = LegendColors.Muted,
    surfaceContainerLowest = LegendColors.Background,
    surfaceContainerLow = LegendColors.Surface,
    surfaceContainer = LegendColors.Surface,
    surfaceContainerHigh = LegendColors.SurfaceHigh,
    surfaceContainerHighest = LegendColors.SurfaceHigh,
    outline = LegendColors.Outline,
    outlineVariant = LegendColors.Outline,
)

private val LegendTypography = Typography().run {
    copy(
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Black, letterSpacing = (-0.5).sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.Bold),
        // Petites capitales espacées pour les titres de section.
        labelSmall = labelSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = 1.2.sp),
    )
}

private val LegendShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
)

/** Thème sombre uniquement. */
@Composable
fun LegendTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = LegendTypography,
        shapes = LegendShapes,
        content = content,
    )
}
