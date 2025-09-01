package com.radarqr.dating.android.ui.home.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.ui.welcome.mobileLogin.CurrentLocationUpdateRequest

class HomeViewModel(val dataRepository: DataRepository) : ViewModel() {

    fun updateCurrentLocation(
        request: CurrentLocationUpdateRequest
    ) = dataRepository.updateCurrentLocation(request).asLiveData()
}