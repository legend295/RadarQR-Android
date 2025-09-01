package com.radarqr.dating.android.di

import com.radarqr.dating.android.base.BaseViewModel
import com.radarqr.dating.android.hotspots.closefriend.CloseFriendAndRequestViewModel
import com.radarqr.dating.android.hotspots.createvenue.CreateVenueViewModel
import com.radarqr.dating.android.hotspots.createvenue.VenueUpdateViewModel
import com.radarqr.dating.android.hotspots.map.HotspotViewModel
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerViewModel
import com.radarqr.dating.android.hotspots.tag.TagViewModel
import com.radarqr.dating.android.hotspots.upload.VenuePhotoViewModel
import com.radarqr.dating.android.hotspots.venue.MyVenueViewModel
import com.radarqr.dating.android.hotspots.venue.viewmodel.VenuePromotionViewModel
import com.radarqr.dating.android.ui.home.likes.model.LikesViewModel
import com.radarqr.dating.android.ui.home.main.HomeViewModel
import com.radarqr.dating.android.ui.home.main.model.GetRecommendationViewModel
import com.radarqr.dating.android.ui.home.quickBlox.ChatViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.subscription.SubscriptionViewModel
import com.radarqr.dating.android.ui.welcome.mobileLogin.SendOtpViewModel
import com.radarqr.dating.android.ui.welcome.otpVerify.VerifyOtpViewModel
import com.radarqr.dating.android.ui.welcome.registerScreens.ImageUploadViewModel
import org.koin.dsl.module


val viewModel = module {
    single { SendOtpViewModel(get()) }
    single { VerifyOtpViewModel(get()) }
    single { GetRecommendationViewModel(get()) }
    single { LikesViewModel(get(), get()) }
    single { ChatViewModel(get(), get()) }
    single { ImageUploadViewModel() }
    single { GetProfileViewModel(get(), get()) }
    single { BaseViewModel(get(), get()) }
    single { HomeViewModel(get()) }
    single { VenueUpdateViewModel(get()) }
    factory { CreateVenueViewModel(get()) }
    single { MyVenueViewModel(get()) }
    single { CloseFriendAndRequestViewModel(get()) }
    single { RoamingTimerViewModel(get()) }
    factory { VenuePhotoViewModel(get()) }
    single { TagViewModel(get()) }
    factory { VenuePromotionViewModel(get(), get()) }
    single { SubscriptionViewModel(get()) }
    single { HotspotViewModel() }
}
