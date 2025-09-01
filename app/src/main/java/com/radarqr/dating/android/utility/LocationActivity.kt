package com.radarqr.dating.android.utility

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.radarqr.dating.android.R
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.utility.BaseUtils.isGpsEnabled
import com.radarqr.dating.android.utility.Utility.showToast

class LocationActivity(var activity: HomeActivity, val callback: (Location?) -> Unit) {
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    fun getLocation() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        getLastLocation()
        val task = mFusedLocationClient.lastLocation
        task.addOnSuccessListener { location ->
            if (activity.isGpsEnabled()) {
                callback(location)
                val latLng = LatLng(location.latitude, location.longitude)
                HomeActivity.userLocation = latLng
                SharedPrefsHelper.saveLastLocation(latLng)
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(activity)
                mFusedLocationClient.lastLocation.addOnCompleteListener(activity) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        callback(location)
                        val latLng = LatLng(location.latitude, location.longitude)
                        HomeActivity.userLocation = latLng
                        SharedPrefsHelper.saveLastLocation(latLng)
                    }
                }
            } else {
                activity.showToast(activity.getString(R.string.enable_gps_msg))
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                activity as Context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                activity as Context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private val requestPermissionLauncher =
        activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val list = ArrayList<Boolean>()
            permissions.forEach { actionMap ->
                if (actionMap.value) list.add(actionMap.value)
            }
            if (list.size == 2) {
                getLocation()
            }
        }


    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }
        /* val mLocationRequest = LocationRequest()
         mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
         mLocationRequest.interval = 0
         mLocationRequest.fastestInterval = 0
         mLocationRequest.numUpdates = 1*/

        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(activity)
        Looper.myLooper()?.let {
            mFusedLocationClient.requestLocationUpdates(
                locationRequest, mLocationCallback,
                it
            )
        }
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location? = locationResult.lastLocation
            callback(mLastLocation)
            mLastLocation?.let {

                val latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
                HomeActivity.userLocation = latLng
                SharedPrefsHelper.saveLastLocation(latLng)
            }
        }
    }
}