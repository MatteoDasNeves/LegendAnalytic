package com.example.legendanalytics.data.remote

import android.util.Log
import com.example.legendanalytics.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    const val RIOT_TOKEN_HEADER = "X-Riot-Token"

    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    /** Client de l'API Riot : ajoute la clé sur chaque requête. */
    fun createRiotClient(apiKey: String): HttpClient = createClient(tag = "RiotApi") {
        defaultRequest { headers.append(RIOT_TOKEN_HEADER, apiKey) }
    }

    /** Client Data Dragon (JSON + images Coil) : surtout pas de clé Riot ici. */
    fun createDataDragonClient(): HttpClient = createClient(tag = "DataDragon")

    private fun createClient(
        tag: String,
        extra: HttpClientConfig<*>.() -> Unit = {},
    ): HttpClient = HttpClient(OkHttp) {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            connectTimeoutMillis = 10_000
            requestTimeoutMillis = 20_000
            socketTimeoutMillis = 20_000
        }
        if (BuildConfig.DEBUG) {
            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d(tag, message)
                    }
                }
                sanitizeHeader { it.equals(RIOT_TOKEN_HEADER, ignoreCase = true) }
            }
        }
        extra()
    }
}
