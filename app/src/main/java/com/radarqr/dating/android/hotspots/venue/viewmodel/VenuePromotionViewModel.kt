package com.radarqr.dating.android.hotspots.venue.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.data.model.SubscriptionStatus
import com.radarqr.dating.android.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class VenuePromotionViewModel(
    application: Application,
    dataRepository: DataRepository,
) : AndroidViewModel(application) {

    var dataRepository: WeakReference<DataRepository> = WeakReference(dataRepository)
    val subscriptionStatus: MutableLiveData<SubscriptionStatus?> = MutableLiveData()

    fun venueSubscriptionStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            dataRepository.get()?.venueSubscriptionStatus()?.collect {
                when (it) {
                    DataResult.Empty -> {}
                    is DataResult.Failure -> {
                        subscriptionStatus.postValue(null)
                    }
                    DataResult.Loading -> {}
                    is DataResult.Success -> {
                        it.data.data?.apply {
                            subscriptionStatus.postValue(this)
                        }
                    }
                }
            }
        }
    }
}