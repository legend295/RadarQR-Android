package com.radarqr.dating.android.hotspots.map

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.ClusterManager
import com.radarqr.dating.android.data.model.Venue

class HotspotViewModel : ViewModel() {
    var map: GoogleMap? = null
    var clusterManager: ClusterManager<Venue>? = null
    var cameraUpdate: CameraUpdate? = null
}