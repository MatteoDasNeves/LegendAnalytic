package com.example.legendanalytics.data.repository

import com.example.legendanalytics.data.mapper.toImageMap
import com.example.legendanalytics.data.remote.DataDragonApi
import com.example.legendanalytics.domain.model.StaticData
import com.example.legendanalytics.domain.repository.StaticDataRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Charge une seule fois la version Data Dragon et la table des sorts d'invocateur. */
class StaticDataRepositoryImpl(private val api: DataDragonApi) : StaticDataRepository {

    private val mutex = Mutex()
    private var cached: StaticData? = null

    override suspend fun getStaticData(): StaticData? = mutex.withLock {
        cached ?: load()?.also { cached = it }
    }

    private suspend fun load(): StaticData? = try {
        val version = api.getVersions().first()
        val spells = try {
            api.getSummonerSpells(version).toImageMap()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            emptyMap()
        }
        StaticData(version, spells)
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        // Pas bloquant : l'UI affichera des emplacements vides à la place des images.
        null
    }
}
