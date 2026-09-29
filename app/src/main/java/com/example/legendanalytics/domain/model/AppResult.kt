package com.example.legendanalytics.domain.model

/** Erreurs métier typées, traduites en texte par la couche UI. */
sealed interface AppError {
    data object MissingApiKey : AppError
    data object InvalidApiKey : AppError
    data object NotFound : AppError
    data object Network : AppError
    data class RateLimited(val retryAfterSeconds: Long?) : AppError
    data class Server(val code: Int) : AppError
    data class Unknown(val message: String?) : AppError
}

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Failure -> this
}
