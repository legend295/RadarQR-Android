package com.radarqr.dating.android.ui.home.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.*
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentLocationBinding
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.LocationEdit
import com.radarqr.dating.android.utility.BaseUtils.getLatLngFromProfile
import com.radarqr.dating.android.utility.BaseUtils.isInternetAvailable
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.OnMapAndViewReadyListener
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.util.*


class LocationFragment : BaseFragment<FragmentLocationBinding>(),
    OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener,
    OnCameraMoveListener,
    OnCameraIdleListener {
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private lateinit var mLocationRequest: LocationRequest
    var city = ""
    var state = ""
    var country = ""
    var lat = 36.1249185
    var longt = -115.3150852
    val PERMISSION_ID = 42
    var locality = ""
    var lat_bundle = 36.1249185
    var isConnected = false
    var long_bundle = -115.3150852
    private lateinit var locationCallback: LocationCallback
    lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val mixPanelWrapper: MixPanelWrapper by inject()

    private var latlng: LatLng? = null

    companion object {

        fun newInstance(): LocationFragment {
            return LocationFragment()
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_location

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.locationFragment = this
//        showToolbarLayout(false)
//        showNavigation(false)
        val mapFragment = childFragmentManager.findFragmentById(R.id.gmap) as SupportMapFragment
        OnMapAndViewReadyListener(mapFragment, this)

        try {
            val data: Bundle? = arguments
            lat_bundle = data?.getDouble("lati") ?: 36.1249185
            long_bundle = data?.getDouble("longt") ?: -115.3150852

        } catch (e: Exception) {

        }
        if (isInternetAvailable()) {
            intializeLocation()
        }
        binding.tvSave.setOnClickListener {
            save()
        }

        binding.ivBack.setOnClickListener {
            this.view?.findNavController()?.popBackStack()
//            save()
        }

    }

    fun save() {
        if (requireContext().isInternetAvailable()) {
            binding.tvSave.isEnabled = false
            binding.progressBar.visible(isVisible = true)
            editProfile(
                EditProfileApiRequest(
                    StepProgress = 5,
                    location = LocationEdit(
                        city = city,
                        state = getShortState(),
                        country = country,
                        locality = locality,
                        lat = lat,
                        lng = longt
                    )
                )
            )
        }
    }

    private fun getShortState(): String {
        return if (getStateShortName().containsKey(state))
            getStateShortName()[state] ?: state
        else state
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetworkInfo.also { return it != null && it.isConnected }
    }

    private fun intializeLocation() {
        mLocationRequest = LocationRequest.create()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult) {
                if (p0.locations.size == 0) {
                    currentLocation(lat_bundle, long_bundle)
                }

                for (location in p0.locations) {
                    if (location != null) {
                        currentLocation(location.latitude, location.longitude)
                        if (lat_bundle == 0.0 || long_bundle == 0.0) {
                            setMapLocation(LatLng(location.latitude, location.longitude))
                        } else {
                            setMapLocation(LatLng(lat_bundle, long_bundle))
                        }

                    } else {
                        currentLocation(lat_bundle, long_bundle)
                    }
                    break
                }

                fusedLocationClient.removeLocationUpdates(locationCallback)
            }
        }

    }


    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap ?: return
        if (isInternetAvailable()) {
            enableMyLocationIfPermitted()
        }
        with(mMap) {
            // Hide the zoom controls as the button panel will cover it.
            uiSettings.isZoomControlsEnabled = false
            uiSettings.isMyLocationButtonEnabled = false

            // Setting an info window adapter allows us to change the both the contents and
            // look of the info window.


            mapType = MAP_TYPE_NORMAL
            setPadding(0, 10, 0, 0)
            uiSettings.isMapToolbarEnabled = true

//            mMap.setOnCameraChangeListener { cameraPosition ->
//
//                latlng = cameraPosition.target
//                mMap.clear()
//                try {
//                    latlng?.let { setMapLocation(it) }
//                } catch (e: Exception) {
//                    e.printStackTrace()
//                }
//            }
            setOnCameraIdleListener(this@LocationFragment)
            setOnCameraMoveListener(this@LocationFragment)
            if (latlng != null) {
                setMapLocation(latlng!!)
            } else {
                setMapLocation(LatLng(lat_bundle, long_bundle))
            }

            //moveCamera(com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(builder.build(), 50))


        }
    }


    private fun setMapLocation(latLng: LatLng) {
        if (view != null && isAdded) {
            if (!::mMap.isInitialized) return
            mMap.clear()

            with(mMap) {
                moveCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        latLng,
                        15f
                    )
                )
                mapType = MAP_TYPE_NORMAL
                try {
                    val gcd = Geocoder(requireContext(), Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= 33) {
                        gcd.getFromLocation(latLng.latitude, latLng.longitude, 1) { address ->
                            address.setAddress(latLng)
                        }
                    } else {
                        gcd.getFromLocation(latLng.latitude, latLng.longitude, 1)
                            ?.setAddress(latLng)
                    }
                } catch (e: Exception) {
                }
            }
        }
    }

    private fun List<Address>.setAddress(latLng: LatLng) {
        if (isNotEmpty()) {
            if (this[0].subLocality == null) {
                if (this[0].locality == null) {
                    if (this[0].subAdminArea == null) {
                        if (this[0].adminArea == null) {

                        } else {
                            city = this[0].adminArea
                            locality = this[0].adminArea
                        }
                        state = ""
                    } else {
                        city = this[0].subAdminArea
                        state = this[0].adminArea
                        locality = this[0].subAdminArea
                    }

                } else {
                    city = this[0].locality
                    state = this[0].adminArea
                    locality = this[0].locality
                }
            } else {
                city = this[0].subLocality
                state = this[0].adminArea
                locality = this[0].subLocality
            }
            country = this[0].countryName
            lat = latLng.latitude
            longt = latLng.longitude
            lifecycleScope.launch(Dispatchers.Main) {
                binding.clMarker.visibility = View.VISIBLE
            }

        } else {
            // do your stuff
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1002 && resultCode == Activity.RESULT_OK) {
            getLastLocation()
        }
    }


    override fun onCameraMove() {

//        iv_loc_marker.visibility = View.GONE
//        shimmer_view_container.visibility = View.VISIBLE
//        loc_container.visibility = View.INVISIBLE
    }

    override fun onCameraIdle() {
        try {
            if (isInternetAvailable()) {
                val center: LatLng = mMap.cameraPosition.target
                center.getAddressOnCameraIdle { addresses ->
                    if ((addresses?.size ?: 0) > 0) {
                        addresses?.let { showAddress(it, center) }
                    } else {
                        val latLng = LatLng(lat_bundle, long_bundle)
                        latLng.getAddressOnCameraIdle { address ->
                            if ((address?.size ?: 0) > 0)
                                address?.let { showAddress(it, latLng) }
                        }

                    }
                }

            } else {
                CommonCode.setToast(requireActivity(), Constants.CONNECTION_ERROR)
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

    }

    private fun LatLng.getAddressOnCameraIdle(callback: (MutableList<Address>?) -> Unit) {
        var addressList: MutableList<Address>? = null
        val gcd = Geocoder(requireContext(), Locale("en"))
        if (Build.VERSION.SDK_INT >= 33) {
            gcd.getFromLocation(latitude, longitude, 1) { address ->
                addressList = address
                callback(addressList)
            }
        } else {
            addressList = gcd.getFromLocation(latitude, longitude, 1)
            callback(addressList)
        }
    }

    private fun showAddress(addresses: MutableList<Address>, center: LatLng) {
        if (addresses[0].subLocality == null) {
            if (addresses[0].locality == null) {
                if (addresses[0].subAdminArea == null) {// set state to empty because city will be admin area
                    if (addresses[0].adminArea == null) {
                        city = addresses[0].featureName
                        locality = ""
                    } else {
                        city = addresses[0].adminArea
                        locality = addresses[0].adminArea
                    }
                    state = ""
                } else { // set state to admin area
                    city = addresses[0].subAdminArea
                    state = addresses[0].adminArea
                    locality = addresses[0].subAdminArea
                }

            } else {// set state to admin area
                city = addresses[0].locality
                state = addresses[0].adminArea
                locality = addresses[0].locality
            }
        } else { // set state to admin area
            city = addresses[0].subLocality
            state = addresses[0].adminArea
            locality = addresses[0].subLocality
        }
        country = if (addresses[0].countryName == null) {
            addresses[0].featureName

        } else {
            addresses[0].countryName
        }

        lat = center.latitude
        longt = center.longitude
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            binding.clMarker.visibility = View.VISIBLE
            when {
                city == "" -> {
                    binding.tvLocMarker.text = getShortState()
                }

                getShortState() == "" -> {
                    binding.tvLocMarker.text = city
                }

                country == "" -> {
                    binding.tvLocMarker.text = city
                }

                else -> {
                    binding.tvLocMarker.text = "$city, ${getShortState()}"
                }
            }
        }
    }

    private fun createLocationRequest() {

        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest)

        val client = LocationServices.getSettingsClient(requireActivity())
        val task = client.checkLocationSettings(builder.build())


        task.addOnSuccessListener { locationSettingsResponse ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            getLastLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().

                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }

    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        fusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

        /*fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                }
*/

    }


    private fun currentLocation(lat: Double, lng: Double) {
        try {
            latlng = LatLng(lat, lng)

            try {
                LatLng(lat, lng).getAddressOnCameraIdle() { addressList ->
                    if (!addressList.isNullOrEmpty()) {
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                            if (lat == 0.0 && lng == 0.0) {
                                binding.tvLocMarker.text = addressList[0].featureName
                            } else
                                binding.tvLocMarker.text = addressList[0].getAddressLine(0)
                        }
                    }
                }


            } catch (e: IOException) {

            }
        } catch (e: Exception) {

        }
    }


    @SuppressLint("MissingPermission")
    private fun enableMyLocationIfPermitted() {
        if (checkPermissions()) {
            binding.clMap.visibility = View.VISIBLE
            mMap!!.setMyLocationEnabled(true)
            createLocationRequest()

        } else {
            binding.clMap.visibility = View.VISIBLE
            requestPermissions()
        }


    }


    private fun editProfile(editprofileApiRequest: EditProfileApiRequest) {
        lifecycleScope.launch {
            getProfileViewModel.editProfile(editprofileApiRequest)
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {

                        }

                        is DataResult.Success -> {
                            /*it.data.data.replaceProfileImagesWithUrl(requireContext()) { data ->
                                getProfileViewModel.profileData.value = data
                            }*/
                            mixPanelWrapper.updateUserPropertyOverMixpanel(JSONObject().apply {
                                put(
                                    MixPanelWrapper.PropertiesKey.LOCATION_CITY,
                                    editprofileApiRequest.location?.city
                                )
                                put(
                                    MixPanelWrapper.PropertiesKey.LOCATION_STATE,
                                    editprofileApiRequest.location?.state
                                )
                            })
                            mixPanelWrapper.setSuperProperties(data = it.data.data)
                            getProfileViewModel.profileData.value = it.data.data
                            getProfileViewModel.isLocationUpdated = true
                            HomeActivity.userProfileLocation = it.data.data.getLatLngFromProfile()
                            findNavController()
                                .popBackStack()
                        }

                        is DataResult.Failure -> {
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/edit-profile",
                                requireActivity().componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/edit-profile Api Error"))
                        }

                        else -> {}
                    }
                    binding.tvSave.isEnabled = true
                    binding.progressBar.visible(isVisible = false)
                }

        }
    }

    private fun checkPermissions(): Boolean {
        if (activity is HomeActivity) {
            if (ActivityCompat.checkSelfPermission(
                    activity as HomeActivity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            )
                return true
        }

        return false
    }

    private fun requestPermissions() {
        showProgressBar(false)
        if (activity is HomeActivity) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_ID
            )
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                PERMISSION_ID
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {

            PERMISSION_ID -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    enableMyLocationIfPermitted()
                } else {

                }
            }
        }
    }

    private fun getStateShortName(): HashMap<String, String> {
        val states: HashMap<String, String> = HashMap()
        states["Alabama"] = "AL"
        states["Alaska"] = "AK"
        states["Alberta"] = "AB"
        states["American Samoa"] = "AS"
        states["Arizona"] = "AZ"
        states["Arkansas"] = "AR"
        states["Armed Forces (AE)"] = "AE"
        states["Armed Forces Americas"] = "AA"
        states["Armed Forces Pacific"] = "AP"
        states["British Columbia"] = "BC"
        states["California"] = "CA"
        states["Colorado"] = "CO"
        states["Connecticut"] = "CT"
        states["Delaware"] = "DE"
        states["District Of Columbia"] = "DC"
        states["Florida"] = "FL"
        states["Georgia"] = "GA"
        states["Guam"] = "GU"
        states["Hawaii"] = "HI"
        states["Idaho"] = "ID"
        states["Illinois"] = "IL"
        states["Indiana"] = "IN"
        states["Iowa"] = "IA"
        states["Kansas"] = "KS"
        states["Kentucky"] = "KY"
        states["Louisiana"] = "LA"
        states["Maine"] = "ME"
        states["Manitoba"] = "MB"
        states["Maryland"] = "MD"
        states["Massachusetts"] = "MA"
        states["Michigan"] = "MI"
        states["Minnesota"] = "MN"
        states["Mississippi"] = "MS"
        states["Missouri"] = "MO"
        states["Montana"] = "MT"
        states["Nebraska"] = "NE"
        states["Nevada"] = "NV"
        states["New Brunswick"] = "NB"
        states["New Hampshire"] = "NH"
        states["New Jersey"] = "NJ"
        states["New Mexico"] = "NM"
        states["New York"] = "NY"
        states["Newfoundland"] = "NF"
        states["North Carolina"] = "NC"
        states["North Dakota"] = "ND"
        states["Northwest Territories"] = "NT"
        states["Nova Scotia"] = "NS"
        states["Nunavut"] = "NU"
        states["Ohio"] = "OH"
        states["Oklahoma"] = "OK"
        states["Ontario"] = "ON"
        states["Oregon"] = "OR"
        states["Pennsylvania"] = "PA"
        states["Prince Edward Island"] = "PE"
        states["Puerto Rico"] = "PR"
        states["Quebec"] = "PQ"
        states["Rhode Island"] = "RI"
        states["Saskatchewan"] = "SK"
        states["South Carolina"] = "SC"
        states["South Dakota"] = "SD"
        states["Tennessee"] = "TN"
        states["Texas"] = "TX"
        states["Utah"] = "UT"
        states["Vermont"] = "VT"
        states["Virgin Islands"] = "VI"
        states["Virginia"] = "VA"
        states["Washington"] = "WA"
        states["West Virginia"] = "WV"
        states["Wisconsin"] = "WI"
        states["Wyoming"] = "WY"
        states["Yukon Territory"] = "YT"

        return states
    }

}




