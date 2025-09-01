package com.radarqr.dating.android.hotspots.roamingtimer

import android.location.Location
import android.location.LocationListener
import com.radarqr.dating.android.ui.home.main.HomeActivity


class MyLocationListener : LocationListener {
    override fun onLocationChanged(p0: Location) {
        HomeActivity.userCurrentLocation
    }

    override fun onLocationChanged(locations: MutableList<Location>) {
        super.onLocationChanged(locations)
    }

    override fun onProviderEnabled(provider: String) {
        super.onProviderEnabled(provider)
    }
}