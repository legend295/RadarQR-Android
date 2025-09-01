package com.radarqr.dating.android.di

import com.radarqr.dating.android.ui.bottomSheet.OptionsBottomSheetFragment
import com.radarqr.dating.android.ui.welcome.InitialActivity
import org.koin.dsl.module

val activityModule = module {
    single { InitialActivity() }
    single { OptionsBottomSheetFragment() }

}
