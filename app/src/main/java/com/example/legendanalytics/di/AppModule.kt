package com.example.legendanalytics.di

import com.example.legendanalytics.BuildConfig
import com.example.legendanalytics.data.local.MatchMemoryCache
import com.example.legendanalytics.data.local.SearchHistoryDataSource
import com.example.legendanalytics.data.remote.DataDragonApi
import com.example.legendanalytics.data.remote.HttpClientFactory
import com.example.legendanalytics.data.remote.RiotApi
import com.example.legendanalytics.data.repository.MatchRepositoryImpl
import com.example.legendanalytics.data.repository.PlayerRepositoryImpl
import com.example.legendanalytics.data.repository.StaticDataRepositoryImpl
import com.example.legendanalytics.domain.repository.MatchRepository
import com.example.legendanalytics.domain.repository.PlayerRepository
import com.example.legendanalytics.domain.repository.SearchHistoryRepository
import com.example.legendanalytics.domain.repository.StaticDataRepository
import com.example.legendanalytics.ui.match.MatchDetailViewModel
import com.example.legendanalytics.ui.navigation.MatchDetailRoute
import com.example.legendanalytics.ui.navigation.ProfileRoute
import com.example.legendanalytics.ui.profile.ProfileViewModel
import com.example.legendanalytics.ui.search.SearchViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val RIOT_CLIENT = named("riot")
val DDRAGON_CLIENT = named("ddragon")

val networkModule = module {
    single(RIOT_CLIENT) { HttpClientFactory.createRiotClient(BuildConfig.RIOT_API_KEY) }
    single(DDRAGON_CLIENT) { HttpClientFactory.createDataDragonClient() }
    single { RiotApi(get(RIOT_CLIENT), BuildConfig.RIOT_API_KEY) }
    single { DataDragonApi(get(DDRAGON_CLIENT)) }
}

val dataModule = module {
    single { MatchMemoryCache() }
    single<PlayerRepository> { PlayerRepositoryImpl(get()) }
    single<MatchRepository> { MatchRepositoryImpl(get(), get()) }
    single<StaticDataRepository> { StaticDataRepositoryImpl(get()) }
    single<SearchHistoryRepository> { SearchHistoryDataSource(androidContext()) }
}

val viewModelModule = module {
    viewModel { SearchViewModel(get(), get()) }
    viewModel { (route: ProfileRoute) -> ProfileViewModel(route, get(), get(), get()) }
    viewModel { (route: MatchDetailRoute) -> MatchDetailViewModel(route, get(), get()) }
}

val appModules = listOf(networkModule, dataModule, viewModelModule)
