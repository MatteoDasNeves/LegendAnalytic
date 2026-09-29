package com.example.legendanalytics.data.remote

import com.example.legendanalytics.domain.model.AppError
import com.example.legendanalytics.domain.model.AppResult
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException

/** Exception interne portant une [AppError] ; ne sort jamais de la couche data. */
class RiotApiException(val error: AppError) : Exception(error.toString())

fun Throwable.toAppError(): AppError = when (this) {
    is RiotApiException -> error
    // Couvre aussi les timeouts Ktor (HttpRequestTimeoutException, ConnectTimeoutException...).
    is IOException -> AppError.Network
    is SerializationException -> AppError.Unknown("Réponse inattendue : $message")
    else -> AppError.Unknown(message)
}

/** Exécute [block] et convertit toute exception (hors annulation) en [AppResult.Failure]. */
inline fun <T> safeCall(block: () -> T): AppResult<T> =
    try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        AppResult.Failure(e.toAppError())
    }
