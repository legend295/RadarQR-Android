package com.radarqr.dating.android.hotspots.venuedetail

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneNumberUtils
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.VenueImage
import com.radarqr.dating.android.databinding.FragmentVenueDetailsBinding
import com.radarqr.dating.android.databinding.LayoutImageViewingWithTagDialogBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.closefriend.adapter.CloseFriendAdapter
import com.radarqr.dating.android.hotspots.createvenue.VenueUpdateViewModel
import com.radarqr.dating.android.hotspots.helpers.deleteVenueDialog
import com.radarqr.dating.android.hotspots.helpers.enableRoamingTimerDialog
import com.radarqr.dating.android.hotspots.helpers.unableToEnableRoamingTimerDialog
import com.radarqr.dating.android.hotspots.model.DeleteVenue
import com.radarqr.dating.android.hotspots.model.MyVenuesData
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerFragment
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerNotificationManager
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerViewModel
import com.radarqr.dating.android.hotspots.venue.MyVenueViewModel
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.BaseUtils.isGpsEnabled
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.SharedPrefsHelper
import com.radarqr.dating.android.utility.Utility
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.loadVenueImage
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.toPx
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.instatag.InstaTag
import com.radarqr.dating.android.utility.introduction.IntroductionScreenType
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class VenueDetailsFragment : VenueBaseFragment<FragmentVenueDetailsBinding>(), ViewClickHandler {

    private val venuesImagesList = ArrayList<VenueImage>()
    private var isMyVenue: Boolean = false
    private var isVenueCheckIn: Boolean = false
    private val venueUpdateViewModel: VenueUpdateViewModel by viewModel()
    private val myVenueViewModel: MyVenueViewModel by viewModel()
    private val roamingViewModel: RoamingTimerViewModel by viewModel()
    private val preferencesHelper: PreferencesHelper by inject()
    private var userId = ""
    private var venueId = ""
    private var isUserAbleToTurnOnRoamingTimer = false
    private var openFrom = 0
    private var isVenueCheckedIn = false
    private val roamingTimerNotificationManager: RoamingTimerNotificationManager by inject()
    private val mixPanelWrapper: MixPanelWrapper by inject()
    private var isLogRegistered = false

    companion object {
        const val FROM_HOTSPOT = 1
        const val FROM_ROAMING_TIMER = 2
        const val FROM_MY_VENUE = 3
    }

    private val adapter by lazy { VenueImagesAdapter(venuesImagesList, adapterClickHandler) }

    override fun getLayoutRes(): Int = R.layout.fragment_venue_details

    override fun init(view: View, savedInstanceState: Bundle?) {
        isMyVenue = arguments?.getBoolean(Constants.IS_MY_VENUE, false) ?: false
        venueId = arguments?.getString(Constants.VENUE_ID, "") ?: ""
        openFrom = arguments?.getInt(Constants.FROM, 0) ?: 0

        hideShowSingleUI(value = false)

        // Roaming timer check
        roamingViewModel.roamingTimerResponse?.let {
            binding.isVenueCheckedIn = true
            isVenueCheckedIn = true
        } ?: getRoamingTimerStatus()

        // get data from preferences
        runBlocking {
            userId =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_USERID)
                    .first()
                    ?: "0"
        }

        venueUpdateViewModel.updatingVenueData.observe(viewLifecycleOwner) {
            it?.setData()
        }


        HomeActivity.activeFragment.value = this
        binding.viewHandler = this
        binding.rvTopImages.adapter = adapter

        binding.swipeRefreshLayout.setOnRefreshListener {
            getVenueById(venueId)
        }

    }

    override fun onResume() {
        super.onResume()
        getVenueById(venueId)
    }

    private fun hideShowSingleUI(value: Boolean) {
        binding.viewBackground.visible(isVisible = value)
        binding.ivCircleRound.visible(isVisible = value)
        binding.tvSinglesCount.visible(isVisible = value)
        binding.tvSinglesAreReady.visible(isVisible = value)
        binding.ivInfo.visible(isVisible = value)
    }

    private fun handleAddPhotoIntroductoryUI() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (binding.btnAddPhotos.isVisible && view != null && isVisible) {
                val isShown = SharedPrefsHelper.get(
                    Constants.IntroductionConstants.VENUE_DETAIL_ADD_PHOTO,
                    defValue = false
                )

                (activity as HomeActivity?)?.introductionHandler?.showIntroductoryUI(
                    IntroductionScreenType.VENUE_DETAIL_ADD_PHOTO,
                    hasShown = isShown,
                    Pair(
                        binding.btnAddPhotos.x + binding.btnAddPhotos.width / 2,
                        binding.btnAddPhotos.y
                    )
                ) {
                    SharedPrefsHelper.save(
                        Constants.IntroductionConstants.VENUE_DETAIL_ADD_PHOTO,
                        true
                    )
                    if (it) {
                        openAddPhotoUI()
                    }
                }
            }
        }, 500)
    }

    private fun handleRoamingTimerIntroductoryUI() {
        if (isVenueCheckedIn) {
            handleSinglesIntroductoryUI()
        } else {
            val isShown = SharedPrefsHelper.get(
                Constants.IntroductionConstants.ROAMING_TIMER,
                defValue = false
            )
            if (!isShown) {
                (activity as HomeActivity?)?.introductionHandler?.showIntroductoryUI(
                    IntroductionScreenType.ROAMING_TIMER,
                    hasShown = false,
                    Pair(
                        binding.ivRoamingTimer.x - binding.ivRoamingTimer.width - 10.toPx,
                        binding.ivRoamingTimer.y
                    )
                ) {
                    SharedPrefsHelper.save(Constants.IntroductionConstants.ROAMING_TIMER, true)
                    if (it) {
                        redirectToRoamingTimer()
                    } else {
                        handleSinglesIntroductoryUI()
                    }
                }
            } else {
                handleSinglesIntroductoryUI()
            }
        }
    }

    private fun handleSinglesIntroductoryUI() {
        val isShown = SharedPrefsHelper.get(
            Constants.IntroductionConstants.VENUE_DETAIL_SINGLES,
            defValue = false
        )

        if (!isShown) {
            (activity as HomeActivity?)?.introductionHandler?.showIntroductoryUI(
                IntroductionScreenType.VENUE_DETAIL_SINGLES,
                hasShown = false,
                Pair(
                    binding.ivCircleRound.x + binding.ivCircleRound.width / 2,
                    binding.ivCircleRound.y - binding.ivCircleRound.height
                )
            ) {
                SharedPrefsHelper.save(Constants.IntroductionConstants.VENUE_DETAIL_SINGLES, true)
                if (it) {
                    openSinglesUI()
                    return@showIntroductoryUI
                }
                if (isVenueCheckedIn) {
                    handleAddPhotoIntroductoryUI()
                }
            }
        } else {
            if (isVenueCheckedIn) {
                handleAddPhotoIntroductoryUI()
            }
        }

    }

    private fun getVenueById(id: String) {
        binding.progressBar.visible(isVisible = venueUpdateViewModel.updatingVenueData.value == null)
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                myVenueViewModel.getVenueById(id).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            if (it.statusCode == 404 && it.message == "Venue not found") {
                                roamingViewModel.roamingTimerResponse = null
                                AlertDialog.Builder(requireContext())
                                    .setTitle("Venue has been deleted")
                                    .setPositiveButton("Ok") { _, _ ->
                                        this@VenueDetailsFragment.view?.findNavController()
                                            ?.popBackStack(R.id.hotspotsFragment, false)
                                    }.show()
                            }
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            venueUpdateViewModel.updatingVenueData.value = it.data.data
                            if (openFrom != FROM_MY_VENUE)
                                Handler(Looper.getMainLooper()).postDelayed(
                                    { handleRoamingTimerIntroductoryUI() },
                                    50
                                )
                            logEvent()
                        }
                    }
                    binding.swipeRefreshLayout.isRefreshing = false
                    binding.progressBar.visible(isVisible = false)
                }
            }
        } else {
            binding.progressBar.visible(isVisible = false)
        }
    }

    private fun MyVenuesData.setData() {
        this.let {
            isMyVenue = userId == it.user_id
            binding.isMyVenue = isMyVenue

            binding.venueData = it
            binding.rvTopImages.visible(isVisible = true)
            hideShowSingleUI(value = true)
            it.checkIsUserNearToTheVenue()


            // ROAMING TIMER ICON VISIBILITY HANDLING
//            if (isMyVenue) {
//                binding.ivRoamingTimer.visible(isVisible = false)
//            } else {
//                if (roamingViewModel.roamingTimerResponse == null) {
//                    binding.ivRoamingTimer.visible(isVisible = true)
//                } else {
//                    if (roamingViewModel.roamingTimerResponse?.venue_detail?._id == it._id) {
//                        binding.ivRoamingTimer.visible(isVisible = true)
//                    } else {
//                        binding.ivRoamingTimer.visible(isVisible = false)
//                    }
//                }
//            }


            if (roamingViewModel.roamingTimerResponse == null) {
                binding.ivRoamingTimer.visible(isVisible = true)
                binding.btnAddPhotos.visible(isVisible = false)
            } else {
                if (roamingViewModel.roamingTimerResponse?.venue_detail?._id == it._id && openFrom != FROM_MY_VENUE) {
                    binding.ivRoamingTimer.visible(isVisible = true)
                    binding.btnAddPhotos.visible(isVisible = true)
                } else {
                    binding.btnAddPhotos.visible(isVisible = false)
                    binding.ivRoamingTimer.visible(isVisible = false)
                }
            }
            binding.btnViewAlbumCheckIn.visible(isVisible = true)
            if (openFrom == FROM_MY_VENUE) {
                binding.ivEditVenue.visible(isVisible = it.status != Constants.VenueStatus.IN_PROGRESS)
                binding.ivDelete.visible(isVisible = true)
                binding.ivRoamingTimer.visible(isVisible = false)
//                binding.btnViewAlbumCheckIn.visible(isVisible = false)
                hideShowSingleUI(value = false)
            } /*else {
//                binding.btnViewAlbumCheckIn.visible(isVisible = true)
            }*/

            // VENUE IMAGES
            venuesImagesList.clear()
            venuesImagesList.addAll(it.images ?: ArrayList())

            // VENUE TYPE AND AMBIANCE
            binding.tvVenueType.text =
                if ((it.type == null || it.type.value.isNullOrEmpty()) && (it.ambiance == null || it.ambiance.value.isNullOrEmpty())) ""
                else if (it.type?.name.isNullOrEmpty()) it.ambiance?.name
                else if (it.ambiance?.name.isNullOrEmpty()) it.type?.name
                else StringBuilder().append(it.type?.name).append("/").append(it.ambiance?.name)

            // PHONE NUMBER
            if (!it.contactinfo.phonenumber.isNullOrEmpty()) {
                binding.ivPhone.visible(isVisible = true)
                binding.tvPhoneNumber.visible(isVisible = true)
                binding.tvPhoneNumber.text =
                    PhoneNumberUtils.formatNumber(
                        it.contactinfo.phonenumber!!,
                        Locale.getDefault().isO3Country
                    )
                        ?: Utility.phoneFormat(it.contactinfo.phonenumber!!)
            }
            // WEBSITE
            if (!it.contactinfo.website.isNullOrEmpty()) {
                binding.ivGlobe.visible(isVisible = true)
                binding.tvWebsiteLink.visible(isVisible = true)
                binding.tvWebsiteLink.text = it.contactinfo.website
            }
            //DESCRIPTION
            if (!it.description.isNullOrEmpty()) {
                binding.viewBackgroundSecond.visible(isVisible = true)
                binding.tvDescriptionAbout.visible(isVisible = true)
                binding.tvDescription.visible(isVisible = true)
                binding.tvDescription.text = it.description
            }
            //SPECIAL OFFER
            if (!it.specialoffer.isNullOrEmpty()) {
                binding.viewBackgroundThird.visible(isVisible = true)
                binding.tvSpecialOfferTitle.visible(isVisible = true)
                binding.tvSpecialOffer.visible(isVisible = true)
                binding.tvSpecialOffer.text = it.specialoffer
            }
        }
    }

    private fun MyVenuesData.checkIsUserNearToTheVenue() {
        // check for 1
        val userLatLng = HomeActivity.userLocation
        if (userLatLng != null) {
            val venueLatLng = LatLng(
                this.contactinfo.latlon?.coordinates?.get(1) ?: 0.0,
                this.contactinfo.latlon?.coordinates?.get(0) ?: 0.0
            )
            val distanceBetweenVenueAndUserInMeter =
                ((SphericalUtil.computeDistanceBetween(
                    venueLatLng,
                    userLatLng
                ) * 0.62137) / 1000).toInt()
            isUserAbleToTurnOnRoamingTimer = distanceBetweenVenueAndUserInMeter <= 10
        }
    }

    private val adapterClickHandler = { data: VenueImage?, _: Boolean, _: Int ->
        handleClick(data)
    }

    private fun handleClick(data: VenueImage?) {
        if (venueUpdateViewModel.updatingVenueData.value?.status != 2 && venueUpdateViewModel.updatingVenueData.value?.status != 1) {
            this.view?.findNavController()?.navigate(R.id.createVenuePhotoFragment)
        } else {
            //TODO need to add user info in response from backend
            if (data?.userInfo != null)
                openImageViewingDialog(data)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivBack -> {
                this.view?.findNavController()?.popBackStack()
            }

            R.id.ivDelete -> {
                requireContext().deleteVenueDialog { dialog, layout ->
                    deleteVenue {
                        if (it) {
                            layout.progressBarApi.visible(isVisible = false)
                            venueUpdateViewModel.updatingVenueData.value = null
                            myVenueViewModel.myVenueData.removeAt(myVenueViewModel.position)
                            this@VenueDetailsFragment.view?.findNavController()?.popBackStack()
                            try {
                                dialog.cancel()
                            } catch (_: RuntimeException) {

                            }
                        } else {
                            layout.progressBarApi.visible(isVisible = false)
                        }
                    }
                }
            }

            R.id.btnAddPhotos -> {
                openAddPhotoUI()
            }

            R.id.ivEditVenue -> {
                this.view?.findNavController()?.navigate(R.id.venueTypeFragment)
            }

            R.id.viewBackground -> {
                openSinglesUI()
            }

            R.id.btnViewAlbumCheckIn -> {
                openVenueAlbumScreen()
            }

//            R.id.tvCheckIn -> {
//                val bundle = Bundle().apply {
//                    putString(Constants.VENUE_ID, venueId)
//                }
//                this.view?.findNavController()?.navigate(R.id.imageTagFragment, bundle)
//            }

            R.id.ivRoamingTimer -> {
                redirectToRoamingTimer()
            }
        }
    }

    private fun openAddPhotoUI() {
        if (roamingViewModel.roamingTimerResponse != null) {
            val bundle = Bundle().apply { putString(Constants.VENUE_ID, venueId) }
            this.view?.findNavController()
                ?.navigate(R.id.action_venueDetails_to_uploadVenuePhotoFragment, bundle)
        } else requireContext().enableRoamingTimerDialog { _, _, _ ->
            redirectToRoamingTimer()
        }
    }

    private fun openSinglesUI() {
        if (requireContext().isGpsEnabled()) {
            if (!isUserAbleToTurnOnRoamingTimer) {
                requireContext().unableToEnableRoamingTimerDialog()
                return
            } else if (roamingViewModel.roamingTimerResponse == null) {
                requireContext().enableRoamingTimerDialog { _, _, _ ->
                    redirectToRoamingTimer()
                }
                return
            } else if (roamingViewModel.roamingTimerResponse?.venue_detail?._id != venueId) {
                requireContext().unableToEnableRoamingTimerDialog(
                    isRoamingTimerEnabledForOtherVenue = true
                )
                return
            }
            val bundle = Bundle().apply {
                putBoolean(Constants.IS_MY_VENUE, isMyVenue)
                putBoolean(Constants.IS_VENUE_CHECKED_IN, isVenueCheckIn)
                putSerializable(
                    Constants.EXTRA_DATA,
                    venueUpdateViewModel.updatingVenueData.value
                )
            }
            this.view?.findNavController()
                ?.navigate(R.id.venueDetailSinglesFragment, bundle)
        } else findNavController().navigate(R.id.enableLocationFragment)
    }

    private fun openVenueAlbumScreen() {
        val bundle = Bundle().apply {
            putString(Constants.VENUE_ID, venueId)
            putSerializable(Constants.EXTRA, venueUpdateViewModel.updatingVenueData.value)
        }
        this.view?.findNavController()?.navigate(R.id.venueAlbumFragment, bundle)
//        if (roamingViewModel.roamingTimerResponse != null) {
//            val bundle = Bundle().apply {
//                putString(Constants.VENUE_ID, venueId)
//                putSerializable(Constants.EXTRA, venueUpdateViewModel.updatingVenueData.value)
//            }
//            this.view?.findNavController()?.navigate(R.id.venueAlbumFragment, bundle)
//        } else requireContext().enableRoamingTimerDialog { _, _, _ ->
//            redirectToRoamingTimer()
//        }
    }

    private fun redirectToRoamingTimer() {
        if (openFrom == FROM_ROAMING_TIMER) {
            this.view?.findNavController()?.popBackStack()
            return
        }
//                if (!isUserAbleToTurnOnRoamingTimer) {
//                    // show popup that you are not near to the venue
//                    return
//                }
        val bundle = Bundle().apply {
            putString(
                Constants.EXTRA,
                if (venuesImagesList.isNotEmpty()) venuesImagesList[0].image else ""
            )
            putString(Constants.VENUE_ID, venueId)
            putInt(Constants.FROM, RoamingTimerFragment.FROM_VENUE_DETAILS)
            putSerializable(
                Constants.EXTRA_DATA,
                venueUpdateViewModel.updatingVenueData.value
            )
        }
        this.view?.findNavController()?.navigate(R.id.fragmentRoamingTimer, bundle)
    }

    private fun deleteVenue(callback: (Boolean) -> Unit) {
        if (view != null && isAdded) {
            lifecycleScope.launch {
                venueUpdateViewModel.updatingVenueData.value?._id?.let {
                    DeleteVenue(venue_id = it)
                }?.let {
                    venueUpdateViewModel.deleteVenue(it).observe(viewLifecycleOwner) { response ->
                        when (response) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                response.message?.let { it1 -> requireContext().showToast(it1) }
                                callback(false)
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                callback(true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getRoamingTimerStatus() {
        if (view != null && isAdded) {
            roamingViewModel.getTimerStatus(lifecycleScope) { roamingTimerStatusResponse, _ ->
                binding.isVenueCheckedIn = roamingTimerStatusResponse != null
                isVenueCheckedIn = roamingTimerStatusResponse != null
                if (roamingTimerStatusResponse == null) {
                    roamingTimerNotificationManager.cancelRoamingTimerNotification()
                }
            }
        }
    }

    private fun openImageViewingDialog(response: VenueImage?) {
        response ?: return
        val dialog = Dialog(requireContext(), R.style.DialogStyleInstagram)
        val layout = LayoutImageViewingWithTagDialogBinding.inflate(
            LayoutInflater.from(requireContext()),
            null,
            false
        )

        layout.closeViewTop.setOnClickListener {
            dialog.dismiss()
        }

        layout.closeViewBottom.setOnClickListener {
            dialog.dismiss()
        }
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val dateFormatSecond = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.getDefault())
//        val localDateTime = LocalDateTime.parse(response.createdAt)

        with(layout) {
            layout.ivPostedImage.setCanWeAddTags(false)
            ivDelete.visible(isVisible = false)
            tvUserName.text = response.userInfo.name
            ivUser.loadImage(response.userInfo.profile_pic)
            venueUpdateViewModel.updatingVenueData.value?.let {
                tvLocation.text = StringBuilder().append("Venue: ").append(it.name).append(", ")
                    .append(BaseUtils.getLocationString(it.contactinfo).uppercase())
            }
            tvPostTime.text =
                StringBuilder().append("Posted on ")
                    .append(dateFormatSecond.parse(response.createdAt)
                        ?.let { dateFormat.format(it) })

            ivTaggedUsersIndicator.visible(response.tagged_users.isNotEmpty())
            ivPostedImage.tagImageView.loadVenueImage(response.image)


            layout.ivPostedImage.tagClickListener = InstaTag.TagClickListener {
                if (it == null) return@TagClickListener
                openUserProfile(it, dialog)
            }

            ivUser.setOnClickListener { openUserProfile(response.user_id, dialog) }
            tvUserName.setOnClickListener { openUserProfile(response.user_id, dialog) }
        }
        dialog.setContentView(layout.root)
        dialog.show()
    }

    private fun openUserProfile(userId: String, dialog: Dialog) {
        dialog.dismiss()
        val bundle = Bundle().apply {
            putString(Constants.USER_ID, userId)
            putSerializable(Constants.EXTRA, CloseFriendAdapter.RequestStatus.RECEIVED)
            putInt(Constants.FROM, ProfileFragment.FROM_VENUE_SINGLES)
            putBoolean(Constants.TYPE, false)
        }
        this.view?.findNavController()
            ?.navigate(R.id.profileFragment, bundle)
    }

    private fun logEvent() {
        if (isLogRegistered) return

        mixPanelWrapper.logEvent(MixPanelWrapper.VENUE_VISIT, JSONObject().apply {
            venueUpdateViewModel.updatingVenueData.value?.let {
                put(MixPanelWrapper.PropertiesKey.VENUE_NAME, it.name)
                put(MixPanelWrapper.PropertiesKey.VENUE_ID, it._id)
                HomeActivity.userCurrentLocation?.let { userLocation ->
                    val latLng = LatLng(
                        it.contactinfo.latlon?.coordinates?.get(1) ?: 0.0,
                        it.contactinfo.latlon?.coordinates?.get(0) ?: 0.0
                    )
                    val distanceInMeter = SphericalUtil.computeDistanceBetween(latLng, userLocation)
                    val decimalFormatter = DecimalFormat("#.##")
                    put(MixPanelWrapper.PropertiesKey.USER_DISTANCE, decimalFormatter.format(distanceInMeter))
                }
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
                put(
                    MixPanelWrapper.PropertiesKey.CHECKED_IN_USER_COUNT,
                    it.roamingtimeractiveuserscount
                )
                put(MixPanelWrapper.PropertiesKey.IS_ROAMING_TIMER_ENABLED, isVenueCheckedIn)
                put(MixPanelWrapper.PropertiesKey.VENUE_VISIT_TIME, Calendar.getInstance().time)
            }
        })
        isLogRegistered = true
    }
}