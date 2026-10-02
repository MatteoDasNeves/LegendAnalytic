package com.example.legendanalytics.data.repository

import com.example.legendanalytics.data.mapper.toChampionMap
import com.example.legendanalytics.data.mapper.toImageMap
import com.example.legendanalytics.data.remote.DataDragonApi
import com.example.legendanalytics.domain.model.StaticData
import com.example.legendanalytics.domain.repository.StaticDataRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Charge une seule fois la version Data Dragon, les sorts d'invocateur et la liste des champions. */
class StaticDataRepositoryImpl(private val api: DataDragonApi) : StaticDataRepository {

    private val mutex = Mutex()
    private var cached: StaticData? = null

    override suspend fun getStaticData(): StaticData? = mutex.withLock {
        cached ?: load()?.also { cached = it }
    }

    private suspend fun load(): StaticData? = try {
        val version = api.getVersions().first()
        coroutineScope {
            val spells = async { orEmpty { api.getSummonerSpells(version).toImageMap() } }
            val champions = async { orEmpty { api.getChampions(version).toChampionMap() } }
            StaticData(version, spells.await(), champions.await())
        }
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        // Pas bloquant : l'UI affichera des emplacements vides à la place des images.
        null
    }

    private inline fun <K, V> orEmpty(block: () -> Map<K, V>): Map<K, V> = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        emptyMap()
    }
}
