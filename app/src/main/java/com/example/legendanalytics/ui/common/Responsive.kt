package com.example.legendanalytics.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Classes de largeur Material : téléphone portrait, téléphone paysage / petite tablette, tablette. */
enum class WidthClass { COMPACT, MEDIUM, EXPANDED }

fun widthClassOf(width: Dp): WidthClass = when {
    width < 600.dp -> WidthClass.COMPACT
    width < 840.dp -> WidthClass.MEDIUM
    else -> WidthClass.EXPANDED
}

/** Largeur maximale d'une colonne de contenu : au-delà, les lignes deviennent difficiles à lire. */
val ContentMaxWidth = 640.dp

/** Occupe toute la largeur disponible jusqu'à [maxWidth], centré horizontalement au-delà. */
fun Modifier.centeredMaxWidth(maxWidth: Dp = ContentMaxWidth): Modifier = this
    .fillMaxWidth()
    .wrapContentWidth(Alignment.CenterHorizontally)
    .widthIn(max = maxWidth)
    .fillMaxWidth()
