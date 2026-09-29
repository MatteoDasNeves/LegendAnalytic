package com.example.legendanalytics.data.remote

import com.example.legendanalytics.data.remote.dto.SummonerSpellsDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess

class DataDragonApi(private val client: HttpClient) {

    suspend fun getVersions(): List<String> = get("${DataDragonUrls.BASE_URL}/api/versions.json").body()

    suspend fun getSummonerSpells(version: String): SummonerSpellsDto =
        get("${DataDragonUrls.BASE_URL}/cdn/$version/data/fr_FR/summoner.json").body()

    private suspend fun get(url: String): HttpResponse {
        val response = client.get(url)
        if (!response.status.isSuccess()) {
            throw java.io.IOException("Data Dragon a répondu ${response.status.value}")
        }
        return response
    }
}

/** Construction des URLs d'images Data Dragon. */
object DataDragonUrls {
    const val BASE_URL = "https://ddragon.leagueoflegends.com"

    /** Noms de champions renvoyés par match-v5 qui diffèrent du nom de fichier Data Dragon. */
    private val championFileOverrides = mapOf("FiddleSticks" to "Fiddlesticks")

    fun champion(version: String, championName: String): String {
        val file = championFileOverrides[championName] ?: championName
        return "$BASE_URL/cdn/$version/img/champion/$file.png"
    }

    fun item(version: String, itemId: Int): String = "$BASE_URL/cdn/$version/img/item/$itemId.png"

    fun profileIcon(version: String, iconId: Int): String =
        "$BASE_URL/cdn/$version/img/profileicon/$iconId.png"

    fun summonerSpell(version: String, imageFile: String): String =
        "$BASE_URL/cdn/$version/img/spell/$imageFile"
}
