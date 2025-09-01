package com.radarqr.dating.android.hotspots.venue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.radarqr.dating.android.data.model.NearByVenueRequest
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.hotspots.model.MyVenuesData

class MyVenueViewModel(private val dataRepository: DataRepository) : ViewModel() {

    var pageNo: Int = 1
    val limit: Int = 10
    var isLastPage = false
    var isLoading = false
    var position = -1
    var myVenueData: ArrayList<MyVenuesData?> = ArrayList()

    fun getMyVenueList() = dataRepository.getMyVenuesList(pageNo, limit).asLiveData()
    fun getNearByVenues(request: NearByVenueRequest) =
        dataRepository.getNearByVenues(request).asLiveData()

    fun getVenueById(venueId: String, info: Int = 0, status: Int = 0) =
        dataRepository.getVenueById(venueId, info, status).asLiveData()

    fun getSinglesForVenue(venueId: String, pageNo: Int, limit: Int) =
        dataRepository.getSinglesForVenue(venueId, pageNo, limit).asLiveData()

    fun clear() {
        pageNo = 1
        position = -1
        isLastPage = false
        isLoading = false
        myVenueData = ArrayList()
    }

}