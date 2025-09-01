package com.radarqr.dating.android.di

import com.radarqr.dating.android.data.repository.DataRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.koin.dsl.module

val repositoryModule = module {
    single { DataRepository(get()) }
}