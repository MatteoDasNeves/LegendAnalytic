package com.example.legendanalytics.ui.common

import com.example.legendanalytics.domain.model.AppError

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val error: AppError) : UiState<Nothing>
}

val <T> UiState<T>.dataOrNull: T? get() = (this as? UiState.Success)?.data
