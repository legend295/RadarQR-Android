package com.radarqr.dating.android.di

import org.koin.core.module.Module


val appComponent: List<Module> = listOf(
    dataPreferenceModule,
    activityModule,
    networkModule,
    repositoryModule,
    viewModel,
)
