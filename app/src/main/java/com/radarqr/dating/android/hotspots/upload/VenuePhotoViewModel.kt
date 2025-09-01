package com.radarqr.dating.android.hotspots.upload

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.radarqr.dating.android.data.model.VenueImageAddRequest
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.ui.welcome.mobileLogin.DeleteImagesRequest

class VenuePhotoViewModel(private val dataRepository: DataRepository) : ViewModel() {

    fun uploadVenuePost(request: VenueImageAddRequest) = dataRepository.uploadVenueImages(request).asLiveData()
    fun deleteImage(deleteImagesRequest: DeleteImagesRequest) =
        dataRepository.deleteImage(deleteImagesRequest).asLiveData()
}