package com.radarqr.dating.android.hotspots.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.icu.util.MeasureUnit.DOT
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PatternItem
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions
import com.google.android.gms.maps.model.VisibleRegion
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.NearByVenueRequest
import com.radarqr.dating.android.data.model.RoamingTimerStatusResponse
import com.radarqr.dating.android.data.model.Venue
import com.radarqr.dating.android.databinding.FragmentHotspotsMapBinding
import com.radarqr.dating.android.databinding.LayoutHotspotsClusterMarkerBinding
import com.radarqr.dating.android.databinding.LayoutHotspotsMarkerIconBinding
import com.radarqr.dating.android.databinding.MarkerInfoWindowBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.createvenue.VenueUpdateViewModel
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerFragment
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerNotificationManager
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerViewModel
import com.radarqr.dating.android.hotspots.venue.MyVenueViewModel
import com.radarqr.dating.android.hotspots.venuedetail.VenueDetailsFragment
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.utility.CustomClusterRenderer
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.Utility
import com.radarqr.dating.android.utility.Utility.getDistanceBetweenTwoLatLngInMiles
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.toPx
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.handler.LocationPermissionHandler
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.*


class HotspotsMapFragment : VenueBaseFragment<FragmentHotspotsMapBinding>(), ViewClickHandler,
    GoogleMap.OnPolygonClickListener {

    private var centerLatLng: LatLng? = null
    private var centerLatLngForApi: LatLng? = null

    private var area: Int = 0

    private val hotspotViewModel: HotspotViewModel by viewModel()
    private val venueUpdateViewModel: VenueUpdateViewModel by viewModel()
    private val myVenueViewModel: MyVenueViewModel by viewModel()
    private val preferencesHelper: PreferencesHelper by inject()
    private val roamingTimerViewModel: RoamingTimerViewModel by viewModel()
    private val roamingTimerNotificationManager: RoamingTimerNotificationManager by inject()
    val getProfileViewModel: GetProfileViewModel by viewModel()
    private var map: GoogleMap? = null

    private var venueId = ""
    private var animateCamera = true

    val list = HashMap<String, Venue>()

    private var page = 1
    private var previousCenterLatLng: LatLng? = null
    private var currentCenterLatLng: LatLng? = null
    private var distance: Int = 5
    private val zoomLevel = 13.5f
    private var isIdleCameraListenerApiEnabled = false

    // Declare a variable for the cluster manager.
    private var clusterManager: ClusterManager<Venue>? = null
    private var customClusterRenderer: CustomClusterRenderer? = null

    //Cluster renderer
    private var clusterIconGenerator: IconGenerator? = null
    private var markerIconGenerator: IconGenerator? = null
    private var clusterView: LayoutHotspotsClusterMarkerBinding? = null
    private var markerView: LayoutHotspotsMarkerIconBinding? = null
    private var icon: Bitmap? = null
    private var markerIcon: Bitmap? = null
    private var size: Int = 0
    private var bottomNavType: HomeActivity.BottomNavType = HomeActivity.BottomNavType.HOTSPOT
    private var isFirstTime = true

    override fun getLayoutRes(): Int = R.layout.fragment_hotspots_map

    fun getClusterIconGenerator(): IconGenerator {
        if (clusterIconGenerator == null)
            clusterIconGenerator = IconGenerator(requireContext())
        return clusterIconGenerator!!
    }

    fun getMarkerIconGenerator(): IconGenerator {
        if (markerIconGenerator == null)
            markerIconGenerator = IconGenerator(requireContext())
        return markerIconGenerator!!
    }

    override fun init(view: View, savedInstanceState: Bundle?) {

        binding.viewHandler = this
        isIdleCameraListenerApiEnabled = false
        bottomNavType = HomeActivity.BottomNavType.HOTSPOT
        getLocation {}
        getRoamingStatus()
        roamingTimerViewModel.roamingTimerResponse?.handleRoamingTimerResponse()

        initMap()

        setLikeCount(HomeActivity.likeCount)
        setMessageCount(HomeActivity.chatCount)

        getProfileViewModel.profileData.observe(viewLifecycleOwner) {
            it?.let {
                if (!it.images.isNullOrEmpty())
                    binding.ivUser.loadImage(it.images[0], isThumb = true)
            }
        }
    }

    private fun initMap() {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            animateCamera = map == null
//            if (map == null)
            map = it
            map?.uiSettings?.isMyLocationButtonEnabled = false
            map?.uiSettings?.isCompassEnabled = false
            setRequestPermissionListener()

            // Initialize the manager with the context and the map.
            // (Activity extends context, so we can pass 'this' in the constructor.)
            if (clusterManager == null) {
                clusterManager = ClusterManager(context, map)

            }
            callApi {
                clusterManager?.renderer = CustomClusterRenderer(
                    requireContext(),
                    map,
                    clusterManager,
                    HomeActivity.location
                ) { _, _ ->
                }

                isIdleCameraListenerApiEnabled = true
                clusterManager?.setClusterToMap()
                map?.setMapListeners()

            }
        }
    }


    override fun onStop() {
        super.onStop()
        page = 1
    }

    private fun setRequestPermissionListener() {
        isLocationPermissionGranted(object : LocationPermissionHandler {
            @SuppressLint("MissingPermission")
            override fun onPermissionGranted(isPermissionGranted: Boolean) {
                if (isPermissionGranted) map?.isMyLocationEnabled = true
            }
        })
    }

    /*private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { requestPermissions ->
            val granted = requestPermissions.entries.all {
                it.value == true
            }
            requestPermissionListener.isGranted(granted)
        }*/

    private fun ClusterManager<Venue>.setClusterToMap() {
        // set marker click listener and show marker info window
        var currentMarker: Marker? = null
        markerCollection.setOnMarkerClickListener {
            currentMarker = it
            it.showInfoWindow()
            true
        }

        // Info window click listener
        // open venue details Screen
        setOnClusterItemInfoWindowClickListener {
            venueUpdateViewModel.updatingVenueData.value = null
            val bundle = Bundle().apply {
                putString(Constants.VENUE_ID, it._id)
                putInt(Constants.FROM, VenueDetailsFragment.FROM_HOTSPOT)
            }
            this@HotspotsMapFragment.view?.findNavController()
                ?.navigate(R.id.venueDetailsFragment, bundle)
        }
        val layout = MarkerInfoWindowBinding.inflate(
            LayoutInflater.from(context),
            null,
            false
        )
        // set custom info window UI to the adapter
        markerCollection.setInfoWindowAdapter(object :
            GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(p0: Marker): View {
                val title: String = p0.title as String
                layout.title.text = title
                return layout.root
            }

            override fun getInfoContents(p0: Marker): View? {
                /* if (p0.isInfoWindowShown) {
                     p0.hideInfoWindow()
                     p0.showInfoWindow()
                 }*/
                return null
            }
        })

        renderer.setAnimation(true)

        setOnClusterClickListener {
            map?.cameraPosition?.zoom?.let { it1 ->
                CameraUpdateFactory.newLatLngZoom(
                    it.position, it1 + 1
                )
            }?.let { it2 ->
                map?.animateCamera(
                    it2
                )
            }
            true
        }

        setOnClusterItemClickListener { item ->
//                venueUpdateViewModel.updatingVenueData.value = null
//                val bundle = Bundle().apply {
//                    putString(Constants.VENUE_ID, item._id)
//                }
//                this@HotspotsMapFragment.view?.findNavController()
//                    ?.navigate(R.id.venueDetailsFragment, bundle)
            true
        }
    }

    private fun GoogleMap.setMapListeners() {
        setOnCameraIdleListener {
            clusterManager?.onCameraIdle()
            Log.d("camera", "Camera Idle listener")
            projection.visibleRegion.getVisibleCenter()
            page = 1
            if (isIdleCameraListenerApiEnabled)
                Handler(Looper.getMainLooper()).postDelayed({
                    callApi {}
                }, 800)
        }

        setOnCameraMoveStartedListener { reason: Int ->
            Log.d("camera", "reason = $reason")
            when (reason) {
                GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE -> {
                    Log.d("camera", "The user gestured on the map.")
                }

                GoogleMap.OnCameraMoveStartedListener
                    .REASON_API_ANIMATION -> {
                    Log.d("camera", "The user tapped something on the map.")
                }

                GoogleMap.OnCameraMoveStartedListener
                    .REASON_DEVELOPER_ANIMATION -> {
//                        map?.projection?.visibleRegion?.getVisibleCenter()
                    Log.d("camera", "The app moved the camera.")
                }
            }
        }

        setOnMarkerClickListener(clusterManager)
        if (animateCamera)
            animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        HomeActivity.location?.latitude ?: 30.7333,
                        HomeActivity.location?.longitude ?: 76.7794
                    ), zoomLevel
                )
            )


//        onReadyMap(this)
    }

    private fun onReadyMap(googleMap: GoogleMap) {
        val currentLocation = LatLng(19.427834, -99.091773)
        // Add polygons to indicate areas on the map.
        val polygon1 = map?.addPolygon(
            PolygonOptions()
                .clickable(true)
                .add(
                    LatLng(19.427834, -99.091773),
                    LatLng(19.417432, -99.075516),
                    LatLng(19.427418, -99.074057),
                    LatLng(19.430458, -99.068288),
                    LatLng(19.428698, -99.064012),
                    LatLng(19.435771, -99.060109),
                    LatLng(19.443068, -99.047857),
                    LatLng(19.452092, -99.050946),
                    LatLng(19.445116, -99.06357),
                    LatLng(19.450076, -99.079317),
                    LatLng(19.44614, -99.084781),
                    LatLng(19.442812, -99.076263),
                    LatLng(19.438427, -99.078265),
                )
        )
        // Store a data object with the polygon, used here to indicate an arbitrary type.
        polygon1?.tag = "alpha"
        // Style the polygon.
        polygon1?.let { stylePolygon(it) }
        /*val sector17Coordinates = listOf(
            LatLng(30.741790, 76.772780),
            LatLng(30.740374, 76.774104),
            LatLng(30.740321, 76.774997),
            LatLng(30.741459, 76.776845),
            LatLng(30.743193, 76.775059)
        )
        val polygon2 = map?.addPolygon(
            PolygonOptions()
                .clickable(true)
                .addAll(sector17Coordinates)
        )
        polygon2?.tag = "beta"
        polygon2?.let { stylePolygon(it) }*/
//        showText(latlngList,googleMap,"Noor trade center")
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel)
            )


        } else {
            googleMap.isMyLocationEnabled = true
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel)
            )
        }

        // Set listeners for click events.
        googleMap.setOnPolygonClickListener(this)
    }

    // recurring function
    private fun callApi(callback: (Boolean) -> Unit) {
        centerLatLngForApi = centerLatLng ?: HomeActivity.location?.latitude?.let { it1 ->
            HomeActivity.location?.longitude?.let { it2 ->
                LatLng(
                    it1,
                    it2
                )
            }
        }
        centerLatLngForApi?.let { centerLatLngForApi ->
            getNearByVenue(centerLatLngForApi, callback)
        } ?: kotlin.run {
            Handler(Looper.getMainLooper()).postDelayed({
                callApi(callback)
            }, 1000)
        }
    }

    private fun getNearByVenue(latLng: LatLng, callback: (Boolean) -> Unit) {
        if (this.view != null && isAdded) {
            binding.tvUpdating.visible(isVisible = list.isEmpty())
//            binding.progressBar.visible(isVisible = true)
            lifecycleScope.launch {
                myVenueViewModel.getNearByVenues(
                    NearByVenueRequest(
                        latLng.latitude.toString(),
                        latLng.longitude.toString(),
                        page = page,
                        distance = distance
                    )
                ).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "venue/nearyou-hotspots",
                                requireActivity().componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("venue/nearyou-hotspots Api Error"))
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            it.data.data?.venues?.forEach { venue ->
                                Log.d(HotspotsMapFragment::class.simpleName, "looping")
                                val `object` = Venue(
                                    venue._id,
                                    venue.contactInfo,
                                    venue.dist,
                                    venue.name,
                                    venue.status,
                                    venue.name,
                                    venue.roamingTimerActiveUsersCount
                                )

                                if (!list.containsKey(`object`._id)) {
                                    list[`object`._id] = (`object`)
                                    clusterManager?.addItem(list[`object`._id])
                                } else {
                                    val data = list[`object`._id]
                                    data?.let {
                                        if (data.roamingTimerActiveUsersCount != `object`.roamingTimerActiveUsersCount) {
                                            list[`object`._id] = (`object`)
                                            clusterManager?.updateItem(list[`object`._id])
                                        }
                                    }
                                }
                            }


                            Log.d(HotspotsMapFragment::class.simpleName, "cluster called")
                            clusterManager?.cluster()
                            callback(true)
                            if (list.size < (it.data.data?.total_count
                                    ?: 0) && !it.data.data?.venues.isNullOrEmpty()
                            ) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    ++page
                                    callApi(callback)
                                }, 500)

                            }
                        }
                    }
                    binding.tvUpdating.visible(isVisible = false)
//                    binding.progressBar.visible(isVisible = false)
                }
            }
        }
    }

    private fun calculatePulseRadius(zoomLevel: Float): Float {
        return (2.0.pow((16 - zoomLevel).toDouble()) * 160).toFloat()
    }

    private fun VisibleRegion.getVisibleCenter() {
        if (centerLatLng == null || centerLatLng != latLngBounds.center) {
            Log.d("DISTANCE: ", "Center ${latLngBounds.center}")
            centerLatLng = latLngBounds.center
        }

        currentCenterLatLng = centerLatLng
        if (previousCenterLatLng == null)
            previousCenterLatLng = centerLatLng
        val distanceBetweenPreviousAndCurrentLatLng =
            getDistanceBetweenTwoLatLngInMiles(currentCenterLatLng, previousCenterLatLng)
        previousCenterLatLng = centerLatLng

        Log.d("DISTANCE: ", "DISTANCE between = $distanceBetweenPreviousAndCurrentLatLng")
        Log.d(
            "DISTANCE: ",
            "DISTANCE between = ${map?.cameraPosition?.zoom?.let { calculatePulseRadius(it) }}"
        )
        val m =
            156543.03392 * cos(
                (centerLatLng?.latitude ?: 0.0) * Math.PI / 180
            ) / 2.0.pow(((map?.cameraPosition?.zoom ?: 1).toDouble()))
        Log.d("DISTANCE: ", "DISTANCE between m = $m")
        centerLatLngForApi = centerLatLng
//        val nearRight: LatLng = nearRight
//        val farLeft: LatLng = farLeft
//        val farRight: LatLng = farRight
        val distanceBetweenNearLeftAndCenterLatLng =
            getDistanceBetweenTwoLatLngInMiles(centerLatLng, nearLeft)
        Log.d("DISTANCE: ", "Center to near left: $distanceBetweenNearLeftAndCenterLatLng")
        distance = if (distanceBetweenNearLeftAndCenterLatLng.toInt() <= 5) {
            5
        } else if (distanceBetweenNearLeftAndCenterLatLng.toInt() <= 20) {
//            20
            distanceBetweenNearLeftAndCenterLatLng.toInt()
        } else if (distanceBetweenNearLeftAndCenterLatLng >= 300) {
            300
        } else {
            distanceBetweenNearLeftAndCenterLatLng.toInt()
        }
        /*val distWidth: Double =
            (SphericalUtil.computeDistanceBetween(nearLeft, nearRight) * 0.62137) / 1000
        val distHeight: Double =
            (SphericalUtil.computeDistanceBetween(farLeft, farRight) * 0.62137) / 1000
        area = (distWidth * distHeight).toInt()*/
        /*
                val halfHeight = distHeight / 2
                val halfWidth = distWidth / 2
                val distance = (distWidth * distHeight)*/

//        Log.d("DISTANCE: ", "DISTANCE WIDTH: $distWidth DISTANCE HEIGHT: $distHeight")
//        Log.d("DISTANCE: ", "Area = $distance")
//        Log.d("DISTANCE: ", "Hypotenuse = ${(distWidth + distHeight).toInt()}")
//        Log.d("DISTANCE: ", "Zoom level = ${map?.cameraPosition?.zoom}")
//        mapRipple?.withDistance(20.0)
//        mapRipple?.withLatLng(centerLatLng)
    }

    fun distanceFrom(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        // Return distance between 2 points, stored as 2 pair location;
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2) * sin(dLat / 2) + (cos(Math.toRadians(lat1))
                * cos(Math.toRadians(lat2)) * sin(dLng / 2) * sin(dLng / 2))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val dist: Double = 3958.75 * c
        return dist * 1609.toFloat()
    }

    private fun getRoamingStatus() {
        if (view != null && isAdded) {
            roamingTimerViewModel.getTimerStatus(lifecycleScope) { response, _ ->
                response.handleRoamingTimerResponse()
            }
        }
    }

    private fun RoamingTimerStatusResponse?.handleRoamingTimerResponse() {
        if (this == null) {
            binding.ivRoamingTimer.visible(isVisible = false)
            binding.ivUser.visible(isVisible = true)
            roamingTimerViewModel.roamingTimerResponse = null
            roamingTimerNotificationManager.cancelRoamingTimerNotification()
            return
        }
        val diff = roamingTimerViewModel.getTimerDifference()
        if (diff <= 0) {
            binding.ivRoamingTimer.visible(isVisible = false)
            binding.ivUser.visible(isVisible = true)
            roamingTimerNotificationManager.cancelRoamingTimerNotification()
            roamingTimerViewModel.deleteRoamingTimer(lifecycleScope) { _, _ -> }
            roamingTimerViewModel.roamingTimerResponse = null
        } else {
            binding.ivRoamingTimer.visible(isVisible = true)
            binding.ivUser.visible(isVisible = false)
        }
        this.let {
            venueId
        }
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivRoamingTimer -> {
                if (roamingTimerViewModel.roamingTimerResponse?.venue_detail != null) {
                    val bundle = Bundle().apply {
                        putString(
                            Constants.VENUE_ID,
                            roamingTimerViewModel.roamingTimerResponse?.venue_id
                        )
                        putInt(Constants.FROM, RoamingTimerFragment.FROM_MAP)
                        if (!roamingTimerViewModel.roamingTimerResponse?.venue_detail?.images.isNullOrEmpty())
                            putString(
                                Constants.EXTRA,
                                roamingTimerViewModel.roamingTimerResponse?.venue_detail?.images?.get(
                                    0
                                )?.image
                            )
                    }
                    this.view?.findNavController()?.navigate(R.id.fragmentRoamingTimer, bundle)
                } else {
                    requireContext().showToast("Venue has been deleted by the owner.")
                    roamingTimerViewModel.deleteRoamingTimer(lifecycleScope) { isSuccess, _ ->
                        roamingTimerViewModel.roamingTimerResponse = null
                        if (isSuccess == true) binding.ivRoamingTimer.visible(
                            isVisible = false
                        )
                    }
                }
            }

            R.id.iv_user -> {
                this.view?.findNavController()?.navigate(R.id.editProfileFragment)
            }

            R.id.ivCheckIn -> {
                val bundle = Bundle().apply {
                    putString(Constants.VENUE_ID, venueId)
                }
                this.view?.findNavController()?.navigate(R.id.imageTagFragment, bundle)
            }

            R.id.ivBack -> {
//                this.view?.findNavController()?.popBackStack()
            }

            /* R.id.tvRefresh->{
                 callApi()
             }*/

            R.id.ivGps -> {
                map?.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            HomeActivity.location?.latitude ?: 30.7333,
                            HomeActivity.location?.longitude ?: 76.7794
                        ), map?.cameraPosition?.zoom ?: zoomLevel
                    )
                )
            }

            R.id.ivSettings, R.id.viewSettings -> {
                if (bottomNavType == HomeActivity.BottomNavType.SETTING) return
                Utility.preventMultipleClicks {
                    bottomNavType = HomeActivity.BottomNavType.SETTING
                    this.view?.findNavController()?.navigate(
                        R.id.setting_fragment,
                        null,
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                }

            }

            R.id.ivLike, R.id.viewLike -> {
                if (bottomNavType == HomeActivity.BottomNavType.LIKE) return
                Utility.preventMultipleClicks {
                    bottomNavType = HomeActivity.BottomNavType.LIKE

                    this.view?.findNavController()?.navigate(
                        R.id.likes_main_fragment,
                        null,
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                }
            }

            R.id.ivRecommendation, R.id.viewRecommendation -> {
                if (bottomNavType == HomeActivity.BottomNavType.RECOMMENDATION) return
                Utility.preventMultipleClicks {
                    bottomNavType = HomeActivity.BottomNavType.RECOMMENDATION
                    this.view?.findNavController()?.navigate(
                        R.id.home_fragment,
                        null,
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                }
            }

            R.id.ivChat, R.id.viewChat -> {
                if (bottomNavType == HomeActivity.BottomNavType.CHAT) return
                Utility.preventMultipleClicks {
                    bottomNavType = HomeActivity.BottomNavType.CHAT
                    this.view?.findNavController()?.navigate(
                        R.id.dialogs_fragment,
                        null,
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                }
            }
        }
    }

    fun setLikeCount(count: Int) {
        if (count != 0) {
            binding.bottomNav.tvLikeBadge.visible(isVisible = true)
            binding.bottomNav.tvLikeBadge.text = "$count"
//            val badge = bottomNavigationView?.getOrCreateBadge(R.id.likes_main_fragment)
//            badge?.number = count
//            badge?.verticalOffset = 4.toPx.toInt()
//            badge?.horizontalOffset = 4.toPx.toInt()
        } else binding.bottomNav.tvLikeBadge.visible(isVisible = false)
    }

    private fun setMessageCount(count: Int) {
        runBlocking {
            preferencesHelper.saveYourMoveCount(count)
        }
        if (count != 0) {
            binding.bottomNav.tvChatBadge.visible(isVisible = true)
//            val badge = bottomNavigationView?.getOrCreateBadge(R.id.dialogs_fragment)
//                badge?.number = count //
//            badge?.verticalOffset = (3).toPx.toInt()
//            badge?.horizontalOffset = 3.toPx.toInt()
        } else binding.bottomNav.tvChatBadge.visible(isVisible = false)
    }

    override fun onPolygonClick(p0: Polygon) {
        /* var color = polygon.strokeColor xor 0x00ffffff
         polygon.strokeColor = color
         color = polygon.fillColor xor 0x00ffffff
         polygon.fillColor = color
         Toast.makeText(this, "Area type ${polygon.tag?.toString()}", Toast.LENGTH_SHORT).show()*/
    }

    private val COLOR_WHITE_ARGB = -0x1
    private val COLOR_DARK_GREEN_ARGB = -0xc771c4
    private val COLOR_LIGHT_GREEN_ARGB = -0x7e387c
    private val COLOR_DARK_ORANGE_ARGB = -0xa80e9
    private val COLOR_LIGHT_ORANGE_ARGB = -0x657db
    private val POLYGON_STROKE_WIDTH_PX = 4
    private val PATTERN_DASH_LENGTH_PX = 20

    private val DASH: PatternItem = Dash(PATTERN_DASH_LENGTH_PX.toFloat())
    private val GAP: PatternItem = Gap(PATTERN_DASH_LENGTH_PX.toFloat())

    // Create a stroke pattern of a gap followed by a dash.
    private val PATTERN_POLYGON_ALPHA = listOf(GAP, DASH)

    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
//    private val PATTERN_POLYGON_BETA = listOf(DOT, GAP, DASH, GAP)

    /**
     * Styles the polygon, based on type.
     * @param polygon The polygon object that needs styling.
     */
    private fun stylePolygon(polygon: Polygon) {
        // Get the data object stored with the polygon.
        val type = polygon.tag?.toString() ?: ""
        var pattern: List<PatternItem>? = null
        var strokeColor = COLOR_DARK_ORANGE_ARGB
        var fillColor = COLOR_WHITE_ARGB
        when (type) {
            "alpha" -> {
                // Apply a stroke pattern to render a dashed line, and define colors.
                pattern = PATTERN_POLYGON_ALPHA
                strokeColor = COLOR_DARK_GREEN_ARGB
                fillColor = ContextCompat.getColor(requireContext(), R.color.greenAlpha)
            }

            "beta" -> {
                // Apply a stroke pattern to render a line of dots and dashes, and define colors.
                pattern = PATTERN_POLYGON_ALPHA
                strokeColor = COLOR_DARK_ORANGE_ARGB
                fillColor = ContextCompat.getColor(requireContext(), R.color.orangeAlpha)
            }
        }
        polygon.strokePattern = pattern
        polygon.strokeWidth = POLYGON_STROKE_WIDTH_PX.toFloat()
        polygon.strokeColor = strokeColor
        polygon.fillColor = fillColor
    }
}