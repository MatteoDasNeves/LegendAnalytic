package com.example.legendanalytics.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.legendanalytics.R
import com.example.legendanalytics.domain.model.AppError
import com.example.legendanalytics.domain.model.MatchOutcome
import com.example.legendanalytics.domain.util.TimeAgo
import java.util.Locale

@Composable
fun AppError.message(): String = when (this) {
    AppError.MissingApiKey -> stringResource(R.string.error_missing_key)
    AppError.InvalidApiKey -> stringResource(R.string.error_invalid_key)
    AppError.NotFound -> stringResource(R.string.error_not_found)
    AppError.Network -> stringResource(R.string.error_network)
    is AppError.RateLimited -> retryAfterSeconds
        ?.let { stringResource(R.string.error_rate_limited, it.toInt()) }
        ?: stringResource(R.string.error_rate_limited_generic)
    is AppError.Server -> stringResource(R.string.error_server, code)
    is AppError.Unknown -> stringResource(R.string.error_unknown)
}

@Composable
fun TimeAgo.label(): String = when (this) {
    TimeAgo.JustNow -> stringResource(R.string.time_just_now)
    is TimeAgo.Minutes -> stringResource(R.string.time_minutes_ago, value.toInt())
    is TimeAgo.Hours -> stringResource(R.string.time_hours_ago, value.toInt())
    is TimeAgo.Days -> stringResource(R.string.time_days_ago, value.toInt())
    is TimeAgo.Months -> stringResource(R.string.time_months_ago, value.toInt())
    is TimeAgo.Years -> stringResource(R.string.time_years_ago, value.toInt())
}

@StringRes
fun MatchOutcome.labelRes(): Int = when (this) {
    MatchOutcome.VICTORY -> R.string.outcome_victory
    MatchOutcome.DEFEAT -> R.string.outcome_defeat
    MatchOutcome.REMAKE -> R.string.outcome_remake
}

/** Libellé français d'une file (queueId de match-v5). */
@StringRes
fun queueLabelRes(queueId: Int): Int? = when (queueId) {
    420 -> R.string.queue_ranked_solo
    440 -> R.string.queue_ranked_flex
    400 -> R.string.queue_normal_draft
    430 -> R.string.queue_normal_blind
    490 -> R.string.queue_quickplay
    480 -> R.string.queue_swiftplay
    450 -> R.string.queue_aram
    2400 -> R.string.queue_aram_mayhem
    1700, 1710 -> R.string.queue_arena
    900, 1010, 1900 -> R.string.queue_urf
    1020 -> R.string.queue_one_for_all
    1300 -> R.string.queue_nexus_blitz
    700, 720 -> R.string.queue_clash
    830, 840, 850, 870, 880, 890 -> R.string.queue_coop_ai
    0 -> R.string.queue_custom
    else -> null
}

@Composable
fun queueLabel(queueId: Int, gameMode: String): String =
    queueLabelRes(queueId)?.let { stringResource(it) } ?: gameMode

@Composable
fun kdaLabel(kda: Double): String =
    if (kda.isInfinite()) stringResource(R.string.kda_perfect)
    else stringResource(R.string.kda_ratio, formatDecimal(kda))

fun formatDecimal(value: Double, digits: Int = 2): String =
    String.format(Locale.FRANCE, "%.${digits}f", value)

/** 1834 s -> "30:34" ; 3723 s -> "1:02:03". */
fun formatDuration(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) String.format(Locale.FRANCE, "%d:%02d:%02d", h, m, s)
    else String.format(Locale.FRANCE, "%d:%02d", m, s)
}

/** 12345 -> "12,3k". */
fun formatCompact(value: Int): String =
    if (value >= 1000) formatDecimal(value / 1000.0, 1) + "k" else value.toString()
