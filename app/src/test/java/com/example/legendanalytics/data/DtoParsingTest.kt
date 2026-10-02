package com.example.legendanalytics.data

import com.example.legendanalytics.data.mapper.toDomain
import com.example.legendanalytics.data.mapper.toChampionMap
import com.example.legendanalytics.data.mapper.toImageMap
import com.example.legendanalytics.data.mapper.toProfile
import com.example.legendanalytics.data.remote.DataDragonUrls
import com.example.legendanalytics.data.remote.HttpClientFactory
import com.example.legendanalytics.data.remote.dto.AccountDto
import com.example.legendanalytics.data.remote.dto.ChampionListDto
import com.example.legendanalytics.data.remote.dto.ChampionMasteryDto
import com.example.legendanalytics.data.remote.dto.LeagueEntryDto
import com.example.legendanalytics.data.remote.dto.MatchDto
import com.example.legendanalytics.data.remote.dto.SummonerDto
import com.example.legendanalytics.data.remote.dto.SummonerSpellsDto
import com.example.legendanalytics.domain.model.ChampionInfo
import com.example.legendanalytics.domain.model.MatchOutcome
import com.example.legendanalytics.domain.model.RiotId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DtoParsingTest {

    private val json = HttpClientFactory.json

    @Test
    fun `parse un compte et ignore les champs inconnus`() {
        val dto = json.decodeFromString<AccountDto>(
            """{"puuid":"abc","gameName":"Faker","tagLine":"KR1","nouveauChamp":42}""",
        )
        val account = dto.toDomain(fallback = RiotId("faker", "kr1"))
        assertEquals("abc", account.puuid)
        assertEquals(RiotId("Faker", "KR1"), account.riotId)
    }

    @Test
    fun `parse summoner et rangs, en ignorant les files non classees`() {
        val summoner = json.decodeFromString<SummonerDto>(
            """{"puuid":"abc","profileIconId":6,"revisionDate":1,"summonerLevel":512}""",
        )
        val leagues = json.decodeFromString<List<LeagueEntryDto>>(
            """
            [
              {"leagueId":"x","queueType":"RANKED_FLEX_SR","tier":"GOLD","rank":"II","leaguePoints":40,"wins":12,"losses":8},
              {"leagueId":"y","queueType":"RANKED_SOLO_5x5","tier":"DIAMOND","rank":"I","leaguePoints":75,"wins":100,"losses":90,"hotStreak":true},
              {"queueType":"CHERRY","wins":3,"losses":1}
            ]
            """.trimIndent(),
        )
        val profile = summoner.toProfile(leagues)
        assertEquals(512L, profile.summonerLevel)
        assertEquals(6, profile.profileIconId)
        assertEquals("DIAMOND", profile.soloDuo?.tier)
        assertEquals(75, profile.soloDuo?.leaguePoints)
        assertEquals("GOLD", profile.flex?.tier)
        assertEquals("II", profile.flex?.division)
    }

    @Test
    fun `parse une partie et calcule CS, objets et issue`() {
        val match = json.decodeFromString<MatchDto>(MATCH_JSON).toDomain()

        assertEquals("EUW1_123", match.matchId)
        assertEquals(1830L, match.durationSeconds)
        assertEquals(420, match.queueId)
        assertEquals(2, match.participants.size)

        val me = match.participant("me")!!
        assertEquals(RiotId("Moi", "EUW"), me.riotId)
        assertEquals(180 + 12, me.creepScore)
        assertEquals(listOf(3031, 0, 0, 0, 0, 0, 3340), me.items)
        assertEquals(listOf(4, 14), me.summonerSpells)
        assertEquals(MatchOutcome.VICTORY, match.outcomeFor(me))

        val other = match.participant("other")!!
        assertNull(other.riotId)
        assertEquals(MatchOutcome.DEFEAT, match.outcomeFor(other))

        val blue = match.teams.first { it.teamId == 100 }
        assertTrue(blue.win)
        assertEquals(9, blue.objectives?.towers)
        assertEquals(3, blue.objectives?.dragons)
        assertEquals(6, blue.objectives?.voidGrubs)
        assertEquals(0, blue.objectives?.riftHeralds)
    }

    @Test
    fun `un remake est detecte`() {
        val remakeJson = MATCH_JSON.replace("\"gameEndedInEarlySurrender\":false", "\"gameEndedInEarlySurrender\":true")
        val match = json.decodeFromString<MatchDto>(remakeJson).toDomain()
        assertEquals(MatchOutcome.REMAKE, match.outcomeFor(match.participant("me")!!))
    }

    @Test
    fun `anciennes parties avec gameDuration en millisecondes`() {
        val legacy = MATCH_JSON
            .replace("\"gameEndTimestamp\":1700001830000,", "")
            .replace("\"gameDuration\":1830", "\"gameDuration\":1830000")
        val match = json.decodeFromString<MatchDto>(legacy).toDomain()
        assertEquals(1830L, match.durationSeconds)
        assertFalse(match.participants.isEmpty())
    }

    @Test
    fun `table des sorts Data Dragon`() {
        val dto = json.decodeFromString<SummonerSpellsDto>(
            """
            {"type":"summoner","version":"15.1.1","data":{
              "SummonerFlash":{"id":"SummonerFlash","name":"Saut éclair","key":"4","image":{"full":"SummonerFlash.png","sprite":"spell0.png"}},
              "SummonerDot":{"id":"SummonerDot","name":"Embrasement","key":"14","image":{"full":"SummonerDot.png"}}
            }}
            """.trimIndent(),
        )
        assertEquals(mapOf(4 to "SummonerFlash.png", 14 to "SummonerDot.png"), dto.toImageMap())
    }

    @Test
    fun `maitrises triees par points et table des champions`() {
        val masteries = json.decodeFromString<List<ChampionMasteryDto>>(
            """
            [{"puuid":"p","championId":268,"championLevel":39,"championPoints":396500,"lastPlayTime":1,"milestoneGrades":[]},
             {"puuid":"p","championId":7,"championLevel":60,"championPoints":628773,"lastPlayTime":2,"nextSeasonMilestone":{"rewardMarks":2}}]
            """.trimIndent(),
        )
        val summoner = SummonerDto(puuid = "p", profileIconId = 1, summonerLevel = 30)
        val profile = summoner.toProfile(leagues = emptyList(), masteries = masteries)
        assertEquals(listOf(7, 268), profile.topMasteries.map { it.championId })
        assertEquals(60, profile.topMasteries.first().level)

        val champions = json.decodeFromString<ChampionListDto>(
            """
            {"type":"champion","data":{
              "MonkeyKing":{"version":"16.19.1","id":"MonkeyKing","key":"62","name":"Wukong","title":"le Roi des singes"},
              "Nunu":{"id":"Nunu","key":"20","name":"Nunu et Willump"}
            }}
            """.trimIndent(),
        ).toChampionMap()
        assertEquals(ChampionInfo("MonkeyKing", "Wukong"), champions[62])
        assertEquals("Nunu et Willump", champions[20]?.name)
    }

    @Test
    fun `urls Data Dragon et cas particulier Fiddlesticks`() {
        assertEquals(
            "https://ddragon.leagueoflegends.com/cdn/img/champion/tiles/Fiddlesticks_0.jpg",
            DataDragonUrls.championTile("FiddleSticks"),
        )
        assertEquals(
            "https://ddragon.leagueoflegends.com/cdn/15.1.1/img/champion/Fiddlesticks.png",
            DataDragonUrls.champion("15.1.1", "FiddleSticks"),
        )
        assertEquals(
            "https://ddragon.leagueoflegends.com/cdn/15.1.1/img/item/3031.png",
            DataDragonUrls.item("15.1.1", 3031),
        )
    }

    companion object {
        val MATCH_JSON = """
            {
              "metadata": {"dataVersion":"2","matchId":"EUW1_123","participants":["me","other"]},
              "info": {
                "gameCreation": 1700000000000,
                "gameStartTimestamp": 1700000000000,
                "gameEndTimestamp":1700001830000,
                "gameDuration":1830,
                "gameMode": "CLASSIC",
                "queueId": 420,
                "participants": [
                  {"puuid":"me","riotIdGameName":"Moi","riotIdTagline":"EUW","teamId":100,"championName":"Ahri",
                   "champLevel":16,"kills":8,"deaths":2,"assists":10,"totalMinionsKilled":180,"neutralMinionsKilled":12,
                   "goldEarned":13000,"totalDamageDealtToChampions":25000,"visionScore":30,
                   "item0":3031,"item1":0,"item2":0,"item3":0,"item4":0,"item5":0,"item6":3340,
                   "summoner1Id":4,"summoner2Id":14,"win":true,"gameEndedInEarlySurrender":false,
                   "challenges":{"kda":9.0}},
                  {"puuid":"other","teamId":200,"championName":"FiddleSticks","kills":1,"deaths":5,"assists":2,
                   "win":false,"gameEndedInEarlySurrender":false}
                ],
                "teams": [
                  {"teamId":100,"win":true,"bans":[],"objectives":{
                    "baron":{"first":true,"kills":1},"champion":{"first":true,"kills":30},
                    "dragon":{"first":false,"kills":3},"horde":{"first":true,"kills":6},
                    "inhibitor":{"first":true,"kills":2},"riftHerald":{"first":false,"kills":0},
                    "tower":{"first":true,"kills":9}}},
                  {"teamId":200,"win":false,"objectives":{"tower":{"first":false,"kills":2}}}
                ]
              }
            }
        """.trimIndent()
    }
}
