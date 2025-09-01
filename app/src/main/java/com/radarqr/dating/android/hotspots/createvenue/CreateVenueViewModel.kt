package com.radarqr.dating.android.hotspots.createvenue

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.data.model.BaseApiResponse
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.hotspots.model.CreateVenue
import com.radarqr.dating.android.hotspots.model.CreateVenueResponse
import com.radarqr.dating.android.hotspots.model.MyVenuesData
import com.radarqr.dating.android.hotspots.model.UpdateVenueRequest
import com.radarqr.dating.android.utility.exception.RequiredFieldException

class CreateVenueViewModel(private val dataRepository: DataRepository) : ViewModel() {

    val venueName: ObservableField<String> = ObservableField("")
    val uniqueName: ObservableField<String> = ObservableField("")
    val venueId: ObservableField<String> = ObservableField("")
    var venueData: MyVenuesData? = null

    fun createVenue(): LiveData<DataResult<BaseApiResponse<MyVenuesData>>> {
        val createVenue = CreateVenue(venueName.get() ?: "", uniqueName.get() ?: "")
        return dataRepository.createVenue(createVenue).asLiveData()
    }

    fun updateVenue(): LiveData<DataResult<BaseApiResponse<MyVenuesData>>> {
        val updateVenueRequest =
            UpdateVenueRequest(
                name = venueName.get(),
                uniquename = if (venueData?.uniquename == uniqueName.get()) null else uniqueName.get(),
                venue_id = venueId.get()
            )
        return dataRepository.updateVenue(updateVenueRequest).asLiveData()
    }

    fun isValid() {
        when{
            venueName.get().isNullOrEmpty()-> throw RequiredFieldException("")
            uniqueName.get().isNullOrEmpty()-> throw RequiredFieldException("")
            (uniqueName.get()?.length ?: 0) < 5-> throw RequiredFieldException("Venue handle should be greater than 4 digits.")
        }
      /*  return !((venueName.get().isNullOrEmpty() || uniqueName.get()
            .isNullOrEmpty() || (uniqueName.get()?.length ?: 0) < 5))*/
    }
}