package com.example.legendanalytics.data

import com.example.legendanalytics.data.local.MatchMemoryCache
import com.example.legendanalytics.data.remote.HttpClientFactory
import com.example.legendanalytics.data.remote.RiotApi
import com.example.legendanalytics.data.repository.MatchRepositoryImpl
import com.example.legendanalytics.data.repository.PlayerRepositoryImpl
import com.example.legendanalytics.domain.model.AppError
import com.example.legendanalytics.domain.model.AppResult
import com.example.legendanalytics.domain.model.Region
import com.example.legendanalytics.domain.model.RiotId
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RiotApiTest {

    private val jsonHeaders = headersOf(HttpHeaders.ContentType, "application/json")

    private fun clientOf(engine: MockEngine) = HttpClient(engine) {
        expectSuccess = false
        install(ContentNegotiation) { json(HttpClientFactory.json) }
    }

    @Test
    fun `account-v1 utilise le cluster regional et encode le Riot ID`() = runTest {
        var requestedUrl = ""
        val engine = MockEngine { request ->
            requestedUrl = request.url.toString()
            respond("""{"puuid":"p1","gameName":"Le Grand","tagLine":"EUW"}""", HttpStatusCode.OK, jsonHeaders)
        }
        val repository = PlayerRepositoryImpl(RiotApi(clientOf(engine), apiKey = "RGAPI-test"))

        val result = repository.findAccount(RiotId("Le Grand", "EUW"), Region.EUW)

        assertEquals(
            "https://europe.api.riotgames.com/riot/account/v1/accounts/by-riot-id/Le%20Grand/EUW",
            requestedUrl,
        )
        assertEquals("p1", (result as AppResult.Success).data.puuid)
    }

    @Test
    fun `les codes HTTP sont convertis en erreurs typees`() = runTest {
        val cases = mapOf(
            HttpStatusCode.NotFound to AppError.NotFound,
            HttpStatusCode.Forbidden to AppError.InvalidApiKey,
            HttpStatusCode.Unauthorized to AppError.InvalidApiKey,
            HttpStatusCode.ServiceUnavailable to AppError.Server(503),
        )
        cases.forEach { (status, expected) ->
            val engine = MockEngine { respond("{}", status, jsonHeaders) }
            val repository = PlayerRepositoryImpl(RiotApi(clientOf(engine), apiKey = "RGAPI-test"))
            val result = repository.findAccount(RiotId("A", "EUW"), Region.EUW)
            assertEquals(AppResult.Failure(expected), result)
        }
    }

    @Test
    fun `cle absente sans appel reseau`() = runTest {
        var calls = 0
        val engine = MockEngine { calls++; respond("{}", HttpStatusCode.OK, jsonHeaders) }
        val repository = PlayerRepositoryImpl(RiotApi(clientOf(engine), apiKey = ""))
        assertEquals(AppResult.Failure(AppError.MissingApiKey), repository.findAccount(RiotId("A", "EUW"), Region.EUW))
        assertEquals(0, calls)
    }

    @Test
    fun `un 429 est reessaye apres Retry-After`() = runTest {
        var calls = 0
        val engine = MockEngine {
            calls++
            if (calls == 1) {
                respond("", HttpStatusCode.TooManyRequests, headersOf(HttpHeaders.RetryAfter, "2"))
            } else {
                respond("""{"puuid":"p1"}""", HttpStatusCode.OK, jsonHeaders)
            }
        }
        val repository = PlayerRepositoryImpl(RiotApi(clientOf(engine), apiKey = "RGAPI-test"))

        val result = repository.findAccount(RiotId("A", "EUW"), Region.EUW)

        assertTrue(result is AppResult.Success)
        assertEquals(2, calls)
    }

    @Test
    fun `un Retry-After trop long remonte RateLimited`() = runTest {
        val engine = MockEngine {
            respond("", HttpStatusCode.TooManyRequests, headersOf(HttpHeaders.RetryAfter, "90"))
        }
        val repository = PlayerRepositoryImpl(RiotApi(clientOf(engine), apiKey = "RGAPI-test"))
        assertEquals(
            AppResult.Failure(AppError.RateLimited(90)),
            repository.findAccount(RiotId("A", "EUW"), Region.EUW),
        )
    }

    @Test
    fun `les parties deja chargees viennent du cache`() = runTest {
        val detailCalls = mutableMapOf<String, Int>()
        val engine = MockEngine { request ->
            val path = request.url.encodedPath
            if (path.endsWith("/ids")) {
                respond("""["EUW1_123"]""", HttpStatusCode.OK, jsonHeaders)
            } else {
                detailCalls.merge(path, 1, Int::plus)
                respond(DtoParsingTest.MATCH_JSON, HttpStatusCode.OK, jsonHeaders)
            }
        }
        val repository = MatchRepositoryImpl(RiotApi(clientOf(engine), apiKey = "RGAPI-test"), MatchMemoryCache())

        val first = repository.getMatches("me", Region.EUW, start = 0, count = 20)
        repository.getMatches("me", Region.EUW, start = 0, count = 20)
        repository.getMatch("EUW1_123", Region.EUW)

        val page = (first as AppResult.Success).data
        assertEquals(1, page.matches.size)
        assertEquals(false, page.hasMore)
        assertEquals(mapOf("/lol/match/v5/matches/EUW1_123" to 1), detailCalls)
    }
}
