package com.radarqr.dating.android.di

import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerNotificationManager
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import org.koin.dsl.module

val dataPreferenceModule = module {
    single {
        PreferencesHelper(get())
    }
    single { RoamingTimerNotificationManager(get()) }
    single { MixPanelWrapper(get()) }
//    single { QuickBloxManager(get(), get()) }
}
