package com.radarqr.dating.android.hotspots.roamingtimer

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.AddTimerRequest
import com.radarqr.dating.android.databinding.FragmentRoamingTimerBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.createvenue.VenueUpdateViewModel
import com.radarqr.dating.android.hotspots.helpers.roamingTimerEnableMessage
import com.radarqr.dating.android.hotspots.helpers.unableToEnableRoamingTimerDialog
import com.radarqr.dating.android.hotspots.helpers.venuePausedDialog
import com.radarqr.dating.android.hotspots.model.MyVenuesData
import com.radarqr.dating.android.hotspots.venue.MyVenueViewModel
import com.radarqr.dating.android.hotspots.venuedetail.VenueDetailsFragment
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.main.HomeActivity.Companion.userCurrentLocation
import com.radarqr.dating.android.utility.BaseUtils.isGpsEnabled
import com.radarqr.dating.android.utility.BaseUtils.isInternetAvailable
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.Utility.getCircularDrawable
import com.radarqr.dating.android.utility.Utility.getVenueUrl
import com.radarqr.dating.android.utility.Utility.listener
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.handler.LocationPermissionHandler
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.serializable
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil


class RoamingTimerFragment : VenueBaseFragment<FragmentRoamingTimerBinding>(), ViewClickHandler {

    private val roamingViewModel: RoamingTimerViewModel by viewModel()
    private val venueUpdateViewModel: VenueUpdateViewModel by viewModel()
    private val roamingTimerNotificationManager: RoamingTimerNotificationManager by inject()
    private val preferencesHelper: PreferencesHelper by inject()
    private val myVenueViewModel: MyVenueViewModel by viewModel()
    private var selectedHour = 1
    private var countDownTimer: CountDownTimer? = null
    private var isUserAbleToTurnOnRoamingTimer = false
    private val hoursList =
        listOf(
            "1 hour",
            "2 hour",
            "3 hour",
            "4 hour"
        )

    private var imageUrl = ""
    private var venueId = ""
    private var isRoamingTimingEnabled = false
    private var openedFrom = FROM_VENUE_DETAILS
    private var currentSeconds = 0L
    private var venueData: MyVenuesData? = null
    private var notificationData: JSONObject = JSONObject()
    private var venueLatLng: LatLng? = null
    private val mixPanelWrapper: MixPanelWrapper by inject()


    companion object {
        const val FROM_MAP = 1
        const val FROM_VENUE_DETAILS = 2
        const val FROM_HOME = 3
        const val FIFTEEN_MINUTE = 900
        var VENUE_CHECK_IN_DISTANCE_FEET = 5280
        const val ONE_HOUR = 60
    }

    override fun getLayoutRes(): Int = R.layout.fragment_roaming_timer

    override fun init(view: View, savedInstanceState: Bundle?) {
        HomeActivity.activeFragment.value = this
        binding.clickHandler = this
        handleUiWithOrWithoutData(isVisible = true)
        // get venue data when screen is opened from Venue details else get from the stored roaming timer response
        getDataFromPreviousUI()

        getLocation()
        loadBlurImage()
        initializeNumberPicker()
    }

    override fun onResume() {
        super.onResume()
        roamingViewModel.roamingTimerResponse?.let {
            handleResponse()
        } ?: getTimerStatus()
//        venueLatLng?.let { checkIsUserNearToTheVenue(it) }
        getUserCurrentLocation { _, _ -> }
    }

    private fun getDataFromPreviousUI() {
        imageUrl = arguments?.getString(Constants.EXTRA, "") ?: ""
        venueId = arguments?.getString(Constants.VENUE_ID, "") ?: ""
        openedFrom = arguments?.getInt(Constants.FROM, FROM_VENUE_DETAILS) ?: FROM_VENUE_DETAILS
        VENUE_CHECK_IN_DISTANCE_FEET =
            if (venueId == "6615530efb7aad5ee8e6b43d" || venueId == "6616315afb7aad5ee8e6b5e5" || venueId == "66146b334f5fc86635f35e23")
                5280
            else 5280
        when (openedFrom) {
            FROM_VENUE_DETAILS -> {
                venueData = arguments?.serializable(Constants.EXTRA_DATA) as MyVenuesData?
                binding.tvVenueName.text = venueData?.name
                binding.tvVenueAddress.text = venueData?.contactinfo?.address
                setNotificationData(venueData?.description ?: "")
                /* checkIsUserNearToTheVenue(
                     LatLng(
                         venueData?.contactinfo?.latlon?.coordinates?.get(1)!!,
                         venueData?.contactinfo?.latlon?.coordinates?.get(0)!!
                     )
                 )*/
            }

            FROM_HOME -> {
//                getVenueById(venueId)
            }

            else -> {
                binding.tvVenueName.text = roamingViewModel.roamingTimerResponse?.venue_detail?.name
                binding.tvVenueAddress.text =
                    roamingViewModel.roamingTimerResponse?.venue_detail?.contactinfo?.address
                setNotificationData(venueData?.description ?: "")
            }
        }

        if (venueId.isNotEmpty()) {
            getVenueById(venueId)
        }
    }

    // hide show empty view with white background
    // used to maintain UI until api response come
    private fun handleUiWithOrWithoutData(isVisible: Boolean) {
        binding.emptyView.visible(isVisible)
        binding.progressBar.visible(isVisible)
    }

    private fun setNotificationData(description: String) {
        notificationData.put(RoamingTimerNotificationManager.VENUE_NAME, binding.tvVenueName.text)
        notificationData.put(RoamingTimerNotificationManager.VENUE_DESCRIPTION, description)
        notificationData.put(RoamingTimerNotificationManager.VENUE_ID, venueId)
    }

    private fun initializeNumberPicker() {
        binding.numberPicker.apply {
            minValue = 0
            maxValue = hoursList.size - 1
            displayedValues = hoursList.toTypedArray()

            setOnValueChangedListener { picker, _, _ ->
                selectedHour = picker.value + 1
                picker.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
            }
        }
    }

    private fun loadBlurImage() {
        Glide.with(this).load(requireContext().getVenueUrl(imageUrl))
            .apply(RequestOptions.bitmapTransform(BlurTransformation(14, 6)))
            .placeholder(
                binding.ivVenue.getCircularDrawable(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.teal_200
                    )
                )
            ).error({
                ContextCompat.getDrawable(requireContext(), R.drawable.placeholder)
            }).addListener(binding.ivVenue.listener(url = imageUrl))
            .into(binding.ivVenue)
    }

    private fun getStartTime(): String {
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("UTC")
        return (cal.time.time / 1000).toString()
    }

    private fun Int.getEndTime(): String {
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("UTC")
        cal.add(Calendar.HOUR, this)
//        cal.add(Calendar.MINUTE, 16)
        return (cal.time.time / 1000).toString()
    }

    private fun Boolean.foundRoamingTimer() {
        if (view != null && isAdded) {
            handleUiWithOrWithoutData(isVisible = false)
            binding.btnSetTimer.text =
                requireContext().getString(if (this) R.string.extend_timer else R.string.set_timer_amp_show_me_singles)
            binding.tvNoTimer.text =
                requireContext().getString(if (this) R.string.stop_timer_amp_don_t_show_singles else R.string.no_timer_amp_don_t_show_singles)
            binding.numberPicker.visible(!this)
            binding.tvTimerBelowGivenMin.visible(isVisible = this)
            binding.btnStopTimer.visible(this)
            binding.tvExploreTheVenue.visible(this)
            binding.btnSetTimer.visible(!this)
            binding.tvNoTimer.visible(!this)
            binding.tvTimer.visible(this)
            binding.tvMessage.text =
                if (this) "Your timer has been activated, Go and explore the singles around the venue." else "Your safety is our goal! Please select a timeframe to be on the ''Room Radar'' for the location you have selected."
        }
    }

    private fun Long.startRoamingTimer() {
        countDownTimer = object : CountDownTimer(this, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                currentSeconds = seconds
                val duration = getDurationString(seconds.toInt())
                binding.tvTimer.text = duration
                binding.tvTimerBelowGivenMin.text = duration
                setExtendTimerUI(seconds <= FIFTEEN_MINUTE)
            }

            override fun onFinish() {
                currentSeconds = 0
                roamingViewModel.roamingTimerResponse = null
                (false).foundRoamingTimer()
                deleteTimer()

            }
        }.start()
    }

    private fun setExtendTimerUI(isVisible: Boolean) {
        binding.tvTimerBelowGivenMin.visible(isVisible)
        binding.tvTimer.visible(!isVisible)
        binding.numberPicker.visible(isVisible)
        binding.btnStopTimer.visible(!isVisible)
        binding.tvExploreTheVenue.visible(!isVisible)
        binding.btnSetTimer.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
        binding.tvNoTimer.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
    }

    override fun onStop() {
        super.onStop()
        val locationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        locationClient.removeLocationUpdates(locationCallback)
        stopTimer()
    }

    private fun stopTimer() {
        currentSeconds = 0
        countDownTimer?.cancel()
    }

    private fun getDurationString(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = seconds % 3600 / 60
        val second = seconds % 60
        return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " + twoDigitString(
            second
        )
    }

    private fun twoDigitString(number: Int): String {
        if (number == 0) {
            return "00"
        }
        return if (number / 10 == 0) {
            "0$number"
        } else number.toString()
    }


    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnSetTimer -> {
                if (requireContext().isGpsEnabled()) {
                    if (isUserAbleToTurnOnRoamingTimer) {
                        if (currentSeconds <= 0)
                            enableTimer()
                        else {
                            updateTimer()
                        }
                    } else
                        venueLatLng?.let {
                            enableDisableSetTimerButton(isEnabled = false)
                            checkIsUserNearToTheVenue(it) { isSuccess ->
                                enableDisableSetTimerButton(isEnabled = true)
                                if (isSuccess)
                                    if (currentSeconds <= 0)
                                        enableTimer()
                                    else {
                                        updateTimer()
                                    }
                                else {
                                    requireContext().unableToEnableRoamingTimerDialog()
                                }
                            }
                        }
                } else findNavController().navigate(R.id.enableLocationFragment)
            }

            R.id.ivBack -> {
                this.view?.findNavController()?.popBackStack()
            }

            R.id.tvNoTimer -> {
                if (isRoamingTimingEnabled) {
                    deleteTimerConfirmation()
                } else {
                    this.view?.findNavController()?.popBackStack()
                }
            }

            R.id.btnStopTimer -> {
                deleteTimerConfirmation()
            }

            R.id.ivCheckIn -> {
                val bundle = Bundle().apply {
                    putString(Constants.VENUE_ID, venueId)
                }
                this.view?.findNavController()?.navigate(R.id.imageTagFragment, bundle)
            }

            R.id.tvExploreTheVenue, R.id.llVenueDetails -> {
                if (openedFrom == FROM_VENUE_DETAILS) {
                    this.view?.findNavController()?.popBackStack()
                } else {
                    venueUpdateViewModel.updatingVenueData.value = null
                    val bundle = Bundle().apply {
                        putString(Constants.VENUE_ID, venueId)
                        putInt(Constants.FROM, VenueDetailsFragment.FROM_ROAMING_TIMER)
                    }
                    this.view?.findNavController()
                        ?.navigate(R.id.venueDetailsFragment, bundle)
                }
            }

            R.id.ivInfo -> {
                requireContext().roamingTimerEnableMessage { _, _, _ -> }
            }
        }
    }

    private fun enableDisableSetTimerButton(isEnabled: Boolean) {
        if (view != null && isAdded && isVisible) {
            binding.btnSetTimer.isEnabled = isEnabled
            binding.btnSetTimer.text =
                resources.getText(if (isEnabled) R.string.set_timer_amp_show_me_singles else R.string.please_wait)
        }
    }

    private fun deleteTimerConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Are you sure you want to remove the Timer?")
            .setPositiveButton("Yes") { _: DialogInterface, _: Int ->
                deleteTimer()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    //#region API
    private fun deleteTimer() {
        if (view != null && isAdded && requireContext().isInternetAvailable()) {
            binding.progressBarResponse.visible(isVisible = true)
            roamingViewModel.deleteRoamingTimer(lifecycleScope) { _, pair ->
                logVenueCheckOutEvent()
                removeRoamingTimer()
                binding.progressBarResponse.visible(isVisible = false)
                pair?.second?.let { requireContext().showToast(it) }
                this.findNavController().popBackStack()
            }
        }
    }

    private fun removeRoamingTimer() {
        isRoamingTimingEnabled = false
        stopTimer()
        setExtendTimerUI(isVisible = true)
        (false).foundRoamingTimer()
        roamingViewModel.roamingTimerResponse = null
        roamingTimerNotificationManager.cancelRoamingTimerNotification()
    }

    private fun enableTimer() {
        if (venueId.isEmpty()) {
            Log.e("ERROR", "empty venue id")
            return
        }
        if (view != null && isVisible && isAdded) {
            lifecycleScope.launch {
                roamingViewModel.addTimer(
                    AddTimerRequest(
                        selectedHour,
                        selectedHour.getEndTime(),
                        getStartTime(),
                        venueId
                    )
                )
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                if (it.statusCode == 422) {
                                    it.message?.let { it1 -> requireContext().showToast(it1) }
                                } else if (it.statusCode == 423) {
                                    requireContext().venuePausedDialog {
                                        this@RoamingTimerFragment.view?.findNavController()
                                            ?.popBackStack(R.id.hotspotsFragment, true)
                                        this@RoamingTimerFragment.view?.findNavController()
                                            ?.navigate(R.id.hotspotsFragment)
                                    }
                                }
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
//                                this@RoamingTimerFragment.view?.findNavController()?.popBackStack()
                                getTimerStatus()
                                logVenueCheckInEvent()
                            }
                        }
                    }
            }
        }
    }

    private fun updateTimer() {
        if (venueId.isEmpty()) {
            Log.e("ERROR", "empty venue id")
            return
        }
        if (view != null && isVisible && isAdded) {
            lifecycleScope.launch {
                roamingViewModel.updateRoamingTimer(
                    AddTimerRequest(
                        selectedHour,
                        selectedHour.getEndTime(),
                        getStartTime(),
                        venueId
                    )
                )
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                if (it.statusCode == 422) {
                                    it.message?.let { it1 -> requireContext().showToast(it1) }
                                } else if (it.statusCode == 423) {
                                    requireContext().venuePausedDialog {
                                        this@RoamingTimerFragment.view?.findNavController()
                                            ?.popBackStack(R.id.hotspotsFragment, true)
                                        this@RoamingTimerFragment.view?.findNavController()
                                            ?.navigate(R.id.hotspotsFragment)
                                    }
                                }
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                stopTimer()
                                getTimerStatus()
                                logVenueCheckInExtendEvent()
                            }
                        }
                    }
            }
        }
    }

    private fun getTimerStatus() {
        if (view != null && isAdded && requireActivity().isInternetAvailable()) {
            lifecycleScope.launch {
                roamingViewModel.getTimerStatus().observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            roamingViewModel.roamingTimerResponse = null
                            isRoamingTimingEnabled = false
                            (false).foundRoamingTimer()
                            stopTimer()
                            if (it.statusCode == 404) {
                                if (openedFrom == FROM_HOME) {
                                    showExpireTimerDialog()
                                }
                            }
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            roamingViewModel.roamingTimerResponse = it.data.data
                            handleResponse()
                        }
                    }
                }
            }
        } else (false).foundRoamingTimer()
    }

    private fun getVenueById(id: String) {
        if (view != null && isAdded)
            lifecycleScope.launch {
                myVenueViewModel.getVenueById(id).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            if (it.statusCode == 404 && it.message == "Venue not found") {
                                removeRoamingTimer()
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Venue has been deleted")
                                    .setPositiveButton("Ok") { _, _ ->
                                        this@RoamingTimerFragment.view?.findNavController()
                                            ?.popBackStack(R.id.hotspotsFragment, false)
                                    }.show()
                            }
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            venueData = it.data.data
                            if (venueData?.images?.isNotEmpty() == true)
                                imageUrl = venueData?.images?.get(0)?.image ?: ""
                            loadBlurImage()
                            binding.tvVenueName.text = venueData?.name
                            binding.tvVenueAddress.text = venueData?.contactinfo?.address
                            setNotificationData(venueData?.description ?: "")
                            checkIsUserNearToTheVenue(
                                LatLng(
                                    venueData?.contactinfo?.latlon?.coordinates?.get(1)!!,
                                    venueData?.contactinfo?.latlon?.coordinates?.get(0)!!
                                )
                            ) {}
                        }
                    }
                }
            }
    }
    //#endregion

    private fun handleResponse() {
        if (view != null && isAdded) {
            isRoamingTimingEnabled = true
            val diff = roamingViewModel.getTimerDifference()
            Log.d(Log.DEBUG.toString(), "$diff")
            if (diff <= 0) {
                deleteTimer()
                if (openedFrom == FROM_HOME) {
                    showExpireTimerDialog()
                }
            } else {
                val sec = diff / 1000
                val notificationTime = sec - FIFTEEN_MINUTE
                if (notificationTime > 0) {
                    roamingTimerNotificationManager.scheduleNotification(
                        delay = notificationTime * 1000,
                        notificationData
                    )
                }
                diff.startRoamingTimer()

                //todo check if venue id is equal to the venueId (for which roaming timer is enabled) then show that foundRoamingTimer
                // else show toast on enable roaming timer that roaming timer is already enabled
                // this need to be discussed by harjot sir that how we will manage
                (true).foundRoamingTimer()
            }
        }
    }

    private fun showExpireTimerDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Roaming Timer")
            .setMessage("Your Roaming Timer has been stopped. Do Enable the timer for the venue.")
            .setPositiveButton("Ok") { _, _ ->
                this@RoamingTimerFragment.view?.findNavController()
                    ?.popBackStack()
            }.show()
    }

    private fun checkIsUserNearToTheVenue(latLng: LatLng, isSuccess: (Boolean) -> Unit) {
        // check for location permission granted or not
        isLocationPermissionGranted(object : LocationPermissionHandler {
            override fun onPermissionGranted(isPermissionGranted: Boolean) {
                if (isPermissionGranted) latLng.checkUserNearIfPermissionGranted(isSuccess)
                else {
                    enableDisableSetTimerButton(isEnabled = true)
                    this@RoamingTimerFragment.view?.findNavController()
                        ?.navigate(R.id.enableLocationPermissionFragment)
                }
            }

        })
    }

    private fun LatLng.checkUserNearIfPermissionGranted(isSuccess: (Boolean) -> Unit) {
        venueLatLng = this
        getUserCurrentLocation { success, currentLatLng ->
            if (success) {
                if (currentLatLng != null) {
                    val distanceInMeter = SphericalUtil.computeDistanceBetween(this, currentLatLng)
                    val distanceInFoot = ceil(distanceInMeter * 3.281)
                    isUserAbleToTurnOnRoamingTimer = distanceInFoot <= VENUE_CHECK_IN_DISTANCE_FEET
                    isSuccess(isUserAbleToTurnOnRoamingTimer)
                } else {
                    requireContext().showToast("Unable to fetch current location. Please try again.")
                }
            } else {
                requireContext().showToast("Unable to fetch current location. Please try again.")
            }
        }


    }

    @SuppressLint("MissingPermission")
    private fun getLocation() {
        isLocationPermissionGranted(object : LocationPermissionHandler {
            override fun onPermissionGranted(isPermissionGranted: Boolean) {
                if (isPermissionGranted) {
                    val locationClient =
                        LocationServices.getFusedLocationProviderClient(requireActivity())
                    val locationRequest =
                        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 0L)
                            .setMinUpdateDistanceMeters(2f)
                    locationClient.requestLocationUpdates(
                        locationRequest.build(),
                        locationCallback,
                        Looper.getMainLooper()
                    )
                } else {
                    enableDisableSetTimerButton(isEnabled = true)
                    this@RoamingTimerFragment.view?.findNavController()
                        ?.navigate(R.id.enableLocationPermissionFragment)
                }
            }

        })

    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            if (p0.locations.isNotEmpty()) {
                val latLng =
                    LatLng(p0.locations[0].latitude, p0.locations[0].longitude)
                userCurrentLocation = latLng
                mixPanelWrapper.updateUserPropertyOverMixpanel(JSONObject().apply {
                    put(MixPanelWrapper.PropertiesKey.USER_LOCATION, userCurrentLocation)
                })
                venueLatLng?.let {
                    val distanceInMeter =
                        SphericalUtil.computeDistanceBetween(it, latLng)
                    val distanceInFoot = ceil(distanceInMeter * 3.281)
                    isUserAbleToTurnOnRoamingTimer = distanceInFoot <= VENUE_CHECK_IN_DISTANCE_FEET
                }
            }
        }
    }

    private fun logVenueCheckInEvent() {
        mixPanelWrapper.logEvent(MixPanelWrapper.VENUE_CHECK_IN, JSONObject().apply {
            venueData?.let {
                put(MixPanelWrapper.PropertiesKey.VENUE_NAME, it.name)
                put(MixPanelWrapper.PropertiesKey.VENUE_ID, it._id)
                if (!it.contactinfo.latlon?.coordinates.isNullOrEmpty() &&
                    (it.contactinfo.latlon?.coordinates?.size ?: 0) >= 2
                )
                    put(
                        MixPanelWrapper.PropertiesKey.VENUE_LOCATION,
                        LatLng(
                            it.contactinfo.latlon?.coordinates?.get(1) ?: 0.0,
                            it.contactinfo.latlon?.coordinates?.get(0) ?: 0.0
                        )
                    )
                else put(
                    MixPanelWrapper.PropertiesKey.VENUE_LOCATION,
                    Constants.MixPanelFrom.NA
                )
                put(
                    MixPanelWrapper.PropertiesKey.CHECKED_IN_USER_COUNT,
                    it.roamingtimeractiveuserscount
                )
                put(MixPanelWrapper.PropertiesKey.CHECK_IN_DURATION_HRS, selectedHour * ONE_HOUR)
                put(
                    MixPanelWrapper.PropertiesKey.VENUE_LOCATION_CITY,
                    it.contactinfo.city ?: Constants.MixPanelFrom.NA
                )
                put(
                    MixPanelWrapper.PropertiesKey.VENUE_LOCATION_STATE,
                    it.contactinfo.state ?: Constants.MixPanelFrom.NA
                )
            }
        })
    }

    private fun logVenueCheckOutEvent() {
        mixPanelWrapper.logEvent(MixPanelWrapper.VENUE_CHECKOUT, JSONObject().apply {

            venueData?.let {
                put(MixPanelWrapper.PropertiesKey.VENUE_NAME, it.name)
                put(MixPanelWrapper.PropertiesKey.VENUE_ID, it._id)
                if (!it.contactinfo.latlon?.coordinates.isNullOrEmpty() &&
                    (it.contactinfo.latlon?.coordinates?.size ?: 0) >= 2
                )
                    put(
                        MixPanelWrapper.PropertiesKey.VENUE_LOCATION,
                        LatLng(
                            it.contactinfo.latlon?.coordinates?.get(1) ?: 0.0,
                            it.contactinfo.latlon?.coordinates?.get(0) ?: 0.0
                        )
                    )
                else put(MixPanelWrapper.PropertiesKey.VENUE_LOCATION, Constants.MixPanelFrom.NA)
                put(
                    MixPanelWrapper.PropertiesKey.CHECKED_IN_USER_COUNT,
                    it.roamingtimeractiveuserscount
                )
                put(MixPanelWrapper.PropertiesKey.CHECK_OUT_DURATION, findCheckOutDuration())
                put(
                    MixPanelWrapper.PropertiesKey.VENUE_LOCATION_CITY,
                    it.contactinfo.city ?: Constants.MixPanelFrom.NA
                )
                put(
                    MixPanelWrapper.PropertiesKey.VENUE_LOCATION_STATE,
                    it.contactinfo.state ?: Constants.MixPanelFrom.NA
                )
                put(
                    MixPanelWrapper.PropertiesKey.STOP,
                    Constants.MixPanelFrom.MANUAL
                )
            }
        })
    }

    private fun findCheckOutDuration(): Long {
        roamingViewModel.roamingTimerResponse?.let {
            // first convert total duration into min
            val totalDuration = it.duration * ONE_HOUR
            // find pending time in milliseconds
            val diff = roamingViewModel.getTimerDifference()
            // convert into sec
            val sec = diff / 1000
            // convert into min
            val min = sec / 60
            // return total min - pending pin
            // this will get time duration in min for how much time roaming timer was enabled
            return totalDuration - min
        }
        return 0
    }

    private fun logVenueCheckInExtendEvent() {
        mixPanelWrapper.logEvent(MixPanelWrapper.VENUE_EXTEND, JSONObject().apply {
            venueData?.let {
                put(MixPanelWrapper.PropertiesKey.VENUE_NAME, it.name)
                put(MixPanelWrapper.PropertiesKey.VENUE_ID, it._id)
                if (!it.contactinfo.latlon?.coordinates.isNullOrEmpty() &&
                    (it.contactinfo.latlon?.coordinates?.size ?: 0) >= 2
                )
                    put(
                        MixPanelWrapper.PropertiesKey.VENUE_LOCATION,
                        LatLng(
                            it.contactinfo.latlon?.coordinates?.get(1) ?: 0.0,
                            it.contactinfo.latlon?.coordinates?.get(0) ?: 0.0
                        )
                    )
                else put(MixPanelWrapper.PropertiesKey.VENUE_LOCATION, Constants.MixPanelFrom.NA)
                put(
                    MixPanelWrapper.PropertiesKey.CHECKED_IN_USER_COUNT,
                    it.roamingtimeractiveuserscount
                )
                put(MixPanelWrapper.PropertiesKey.EXTEND_TIME, selectedHour * ONE_HOUR)
                put(
                    MixPanelWrapper.PropertiesKey.VENUE_LOCATION_CITY,
                    it.contactinfo.city ?: Constants.MixPanelFrom.NA
                )
                put(
                    MixPanelWrapper.PropertiesKey.VENUE_LOCATION_STATE,
                    it.contactinfo.state ?: Constants.MixPanelFrom.NA
                )
            }
        })
    }
}