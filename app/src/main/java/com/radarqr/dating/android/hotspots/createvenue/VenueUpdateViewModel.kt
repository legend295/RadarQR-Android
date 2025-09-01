package com.radarqr.dating.android.hotspots.createvenue

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.model.Place
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.data.model.DeleteVenueImageRequest
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.hotspots.model.*
import com.radarqr.dating.android.ui.welcome.mobileLogin.DeleteImagesRequest
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class VenueUpdateViewModel(private val dataRepository: DataRepository) : ViewModel() {

    val place: MutableLiveData<Place> = MutableLiveData()
    val venueTypeList: ArrayList<VenueTypeAndAmbianceResponse> = ArrayList()
    val venueAmbianceList: ArrayList<VenueTypeAndAmbianceResponse> = ArrayList()

    init {
        getVenueAmbiance {
            venueAmbianceList.clear()
            venueAmbianceList.addAll(it)
        }
        getVenueTypes {
            venueTypeList.clear()
            venueTypeList.addAll(it)
        }
    }

    /** update value whenever we update any venue and we can listen any update using this */
    val updatingVenueData: MutableLiveData<MyVenuesData> = MutableLiveData()

    val address: ObservableField<String> = ObservableField()
    val phoneNumber: ObservableField<String> = ObservableField()
    val website: ObservableField<String> = ObservableField()
    val description: ObservableField<String> = ObservableField()
    var updateListener: UpdateListener? = null

    val updateVenueRequest: MutableLiveData<UpdateVenueRequest> = MutableLiveData(
        UpdateVenueRequest()
    )

    fun setContactInfo(data: MyVenuesData) {
        address.set(data.contactinfo.address)
        phoneNumber.set(data.contactinfo.phonenumber)
        website.set(data.contactinfo.website)
    }

    fun updateVenue(request: UpdateVenueRequest) = dataRepository.updateVenue(request).asLiveData()

    fun deleteImage(deleteImagesRequest: DeleteImagesRequest) =
        dataRepository.deleteImage(deleteImagesRequest).asLiveData()

    fun deleteVenue(deleteVenue: DeleteVenue) = dataRepository.deleteVenue(deleteVenue).asLiveData()

    fun submitVenue(request: SubmitVenue) = dataRepository.submitVenue(request).asLiveData()

    fun deleteVenueImages(request: DeleteVenueImageRequest) = dataRepository.deleteVenueImages(request).asLiveData()

    fun getVenueTypes(callback: (ArrayList<VenueTypeAndAmbianceResponse>) -> Unit) {
        viewModelScope.launch {
            dataRepository.getVenueTypes().collect {
                when (it) {
                    DataResult.Empty -> {}
                    is DataResult.Failure -> {}
                    DataResult.Loading -> {}
                    is DataResult.Success -> {
                        callback(it.data.data ?: ArrayList())
                    }
                }
            }
        }
    }

    fun getVenueAmbiance(callback: (ArrayList<VenueTypeAndAmbianceResponse>) -> Unit) {
        viewModelScope.launch {
            dataRepository.getVenueAmbiance().collect {
                when (it) {
                    DataResult.Empty -> {}
                    is DataResult.Failure -> {}
                    DataResult.Loading -> {}
                    is DataResult.Success -> {
                        callback(it.data.data ?: ArrayList())

                    }
                }
            }
        }
    }

    fun getVenueImages(venueId: String, pageNo: Int, limit: Int) =
        dataRepository.getVenueImages(venueId, pageNo, limit).asLiveData()

    interface UpdateListener {
        fun onUpdate(venueData: MyVenuesData?)
    }
}