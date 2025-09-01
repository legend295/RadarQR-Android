package com.radarqr.dating.android.ui.home.settings.profile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.radarqr.dating.android.utility.chipslayoutmanager.ChipsLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.*
import com.radarqr.dating.android.data.model.profile.BasicInfoData
import com.radarqr.dating.android.databinding.FragmentProfileBinding
import com.radarqr.dating.android.hotspots.closefriend.CloseFriendAndRequestViewModel
import com.radarqr.dating.android.hotspots.closefriend.adapter.CloseFriendAdapter
import com.radarqr.dating.android.hotspots.helpers.addACloseFriend
import com.radarqr.dating.android.hotspots.helpers.confirmCloseFriend
import com.radarqr.dating.android.hotspots.helpers.removeCloseFriend
import com.radarqr.dating.android.hotspots.helpers.showSubscriptionSheet
import com.radarqr.dating.android.hotspots.helpers.undoRequest
import com.radarqr.dating.android.hotspots.model.MyVenuesData
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.subscription.SubscriptionWrapper
import com.radarqr.dating.android.ui.home.likes.model.LikesViewModel
import com.radarqr.dating.android.ui.home.likes.model.UserLikes
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.main.adapter.BasicCommonAdapter
import com.radarqr.dating.android.ui.home.main.model.GetRecommendationViewModel
import com.radarqr.dating.android.ui.home.quickBlox.ChatViewModel
import com.radarqr.dating.android.ui.home.settings.adapter.HobbyAdapter
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.HobbiesData
import com.radarqr.dating.android.ui.home.settings.prodileModel.LikeStatus
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.welcome.mobileLogin.AcceptRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.LikeDislikeRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.ReportRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.utility.*
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.openBottomSheet
import com.radarqr.dating.android.utility.Utility.openBottomSheetWithEditField
import com.radarqr.dating.android.utility.Utility.share
import com.radarqr.dating.android.utility.Utility.showItsAMatchDialog
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.toPx
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.enums.SubscriptionPopUpType
import com.radarqr.dating.android.utility.handler.DialogClickHandler
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.introduction.IntroductionScreenType
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper.PropertiesKey
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileFragment : BaseFragment<FragmentProfileBinding>(), ViewClickHandler {

    companion object {
        const val FROM_HOME = 0
        const val FROM_NEAR_YOU = 8
        const val FROM_LIKE = 1
        const val FROM_SCAN = 2
        const val FROM_CHAT = 3
        const val FROM_EDIT = 4
        const val FROM_CLOSE_FRIEND = 5
        const val FROM_FRIEND_REQUEST = 6
        const val FROM_VENUE_SINGLES = 7
        const val EXIST = "exist"
        const val SEND = "send"
        const val ACCEPT = "accept"
        const val REQUEST_SENT = "request_sent"
        const val REQUEST_RECEIVE = "request_revceive"
        const val MATCH = "match"
        const val UN_MATCH = "un_match"
        const val DECLINE = "decline"
    }

    private val mixPanelWrapper: MixPanelWrapper by inject()

    private var profileData: ProfileData? = null
    private var userLikes: UserLikes? = null
    private var venueData: MyVenuesData? = null
    private var userId: String = ""
    private var apiType = ""
    private var canUserSendRequest = false

    var requestStatus = CloseFriendAdapter.RequestStatus.ALREADY_ADDED

    /**
     * used to check if already matched then hide send and cancel requests button
     * value is coming from the previous screen in constant TYPE
     * */
    private var isMatching = false

    /**
     * used to track the previous screen
     * value is coming from the previous screen in constant FROM
     * */
    private var type: Int = -1
    private var basicInfoAdapter: BasicCommonAdapter? = null
    private var wordAdapter: BasicCommonAdapter? = null
    private var hobbyAdapter: HobbyAdapter? = null
    private var profileGridImagesAdapter: ProfileGridImagesAdapter? = null
    private var profileGridImagesAdapterSecond: ProfileGridImagesAdapter? = null

    private var basicInfoList = ArrayList<BasicInfoData>()
    private var workItemList = ArrayList<BasicInfoData>()
    private var hobbiesList = ArrayList<HobbiesData>()

    private val recommendationViewModel: GetRecommendationViewModel by viewModel()
    private val likesViewModel: LikesViewModel by viewModel()
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val closeFriendAndRequestViewModel: CloseFriendAndRequestViewModel by viewModel()

    //    private val quickBloxManager: QuickBloxManager by inject()
    private val chatViewModel: ChatViewModel by viewModel()
    private var imageSelectedPosition = 0

    override fun getLayoutRes(): Int = R.layout.fragment_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageSelectedPosition = 0
        binding.viewClickHandler = this
        HomeActivity.activeFragment.value = this
        arguments?.apply {
            userId = getString(Constants.USER_ID, "")
            isMatching = getBoolean(Constants.TYPE, false)
            type = getInt(Constants.FROM, -1)
            addPadding(if (type == FROM_EDIT) 20 else 95)

            when (type) {
                FROM_LIKE -> {
                    userLikes = serializable(Constants.EXTRA) as UserLikes?
                }

                FROM_CLOSE_FRIEND, FROM_FRIEND_REQUEST -> {
                    requestStatus = serializable(Constants.EXTRA)!!
//                    requestStatus.handleFriendStatus()
                }

                FROM_VENUE_SINGLES -> {
                    venueData = serializable(Constants.EXTRA)
                }
            }
            if (profileData == null) {
                profileData = serializable(Constants.EXTRA_DATA) as ProfileData?
                profileData?.let {
                    binding.data = it
                    it.setData()
                } ?: kotlin.run {
                    getProfile()
                }
            } else {
                binding.data = profileData
                profileData?.setData()
            }
        }
        if (userId == HomeActivity.loggedInUserId) {
            binding.isMatching = false
            binding.ivMore.visible(isVisible = false)
        } else
            binding.isMatching = isMatching
    }


    private fun addPadding(padding: Int) {
        binding.clParent.setPadding(0, 0, 0, padding.toPx.toInt())
    }

    private fun getProfile() {
        if (view != null && isAdded && userId.isNotEmpty()) {
            if (BaseUtils.isInternetAvailable()) {
                try {
                    getBaseActivity()?.getProfile(getProfileRequest(user_id = arrayListOf(userId))) { data, pair ->
                        data?.let {
                            profileData = it
                            binding.data = it
                            it.setData()
                        } ?: kotlin.run {
                            if (pair.first == 404) {
                                if (pair.second == "User not found.") {
                                    AlertDialog.Builder(requireContext())
                                        .setTitle("User account is deleted.")
                                        .setPositiveButton("Ok") { _, _ ->
                                            this.view?.findNavController()?.popBackStack()
                                        }.show()
                                } else {
                                    pair.second?.let { requireContext().showToast(it) }
                                    this.view?.findNavController()?.popBackStack()
                                }
                            }
                        }
                    }
                } catch (e: java.lang.Exception) {
                }
            } else CommonCode.setToast(
                requireContext(),
                resources.getString(R.string.no_internet_msg)
            )
        }
    }

    private fun ProfileData.setData() {
        if (view != null && isAdded && isVisible) {
            logProfileVisitEvent(this)
            if (userId != HomeActivity.loggedInUserId)
                binding.ivMore.visible(isVisible = true)
            if (type == FROM_SCAN || type == FROM_CLOSE_FRIEND || type == FROM_FRIEND_REQUEST) {
                requestStatus = if (friendlist?.close_friend == true) {
                    CloseFriendAdapter.RequestStatus.ALREADY_ADDED
                } else if (friendlist?.request_sent == true) {
                    CloseFriendAdapter.RequestStatus.SENT
                } else if (friendlist?.request_received == true) {
                    CloseFriendAdapter.RequestStatus.RECEIVED
                } else CloseFriendAdapter.RequestStatus.ADD
                if (requestStatus == CloseFriendAdapter.RequestStatus.ADD && userId != HomeActivity.loggedInUserId)
                    handleIntroductoryUI()
                requestStatus.handleFriendStatus()
            } else {
                binding.ivRequestStatus.visible(isVisible = false)
                canUserSendRequest = false
            }

            if (type == FROM_VENUE_SINGLES) {
                if (HomeActivity.loggedInUserId != this._id)
                    binding.ivChatVenueSingle.visible(isVisible = true)
            }

            if (view != null && isAdded && isVisible) {
                setImage()
                setGridImagesAdapter()
                setBasicInfoAdapter()
                setWorkAdapter()
                setHobbiesAdapter()
                setGridImagesAdapterSecond()
            }

            val requestSent = like_status.request_sent
            val requestReceive = like_status.request_receive
            val match = like_status.is_match
            val unMatch = like_status.is_unmatch
            val isDeclined = like_status.is_decline
            apiType = when {
                unMatch -> UN_MATCH
                match -> MATCH
                isDeclined -> DECLINE
                requestReceive -> REQUEST_RECEIVE
                requestSent -> REQUEST_SENT
                else -> SEND
            }
        }
    }

    private fun CloseFriendAdapter.RequestStatus.handleFriendStatus() {
        if (userId != HomeActivity.loggedInUserId)
            when (this) {
                CloseFriendAdapter.RequestStatus.ADD -> {
                    binding.ivRequestStatus.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_send_request
                        )
                    )
                    canUserSendRequest = true
                    binding.ivRequestStatus.visible(isVisible = true)
                }

                CloseFriendAdapter.RequestStatus.SENT, CloseFriendAdapter.RequestStatus.RECEIVED -> {
                    binding.ivRequestStatus.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_pending_request
                        )
                    )
                    canUserSendRequest = true
                    binding.ivRequestStatus.visible(isVisible = true)
                }

                CloseFriendAdapter.RequestStatus.ALREADY_ADDED -> {
                    binding.ivRequestStatus.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.ic_close_friend_profile
                        )
                    )
                    canUserSendRequest = true
                    binding.ivRequestStatus.visible(isVisible = true)
                }
            }

    }

    private fun handleIntroductoryUI() {
        Handler(Looper.getMainLooper()).postDelayed({
            val isShown = SharedPrefsHelper.get(
                Constants.IntroductionConstants.PROFILE,
                defValue = false
            )
            (activity as HomeActivity?)?.introductionHandler?.showIntroductoryUI(
                IntroductionScreenType.PROFILE,
                hasShown = isShown,
                Pair(
                    binding.ivRequestStatus.x - (50).toPx,
                    binding.ivRequestStatus.y + (10).toPx
                )
            ) {
                SharedPrefsHelper.save(Constants.IntroductionConstants.PROFILE, true)
                if (it)
                    handleRequestClick()
            }
        }, 50)

    }

    private fun ProfileData.setImage() {
        if (images?.isNotEmpty() == true) {
            /* val firstImageUrl = if (imageDataMap.containsKey(images[0]))
                 (imageDataMap[images[0]]?.url) ?: requireContext().getImageUrl(images[0])
             else requireContext().getImageUrl(images[0])*/
            binding.ivUser.loadImage(images[0])
//            binding.ivVideoIcon.visible(images[0].contains(Constants.MP4))

            if (images.size > 1) {
                /*val secondImageImageUrl = if (imageDataMap.containsKey(images[1]))
                    (imageDataMap[images[1]]?.url) ?: requireContext().getImageUrl(images[1])
                else requireContext().getImageUrl(images[1])*/
                binding.ivUserSecond.loadImage(images[1])
                binding.ivVideoIconSecond.visible(images[1].contains(Constants.MP4))
            }

            if (images.size > 2) {
                /* val thirdImageImageUrl = if (imageDataMap.containsKey(images[2]))
                     (imageDataMap[images[2]]?.url) ?: requireContext().getImageUrl(images[2])
                 else requireContext().getImageUrl(images[2])*/
                binding.ivUserThird.loadImage(images[2])
                binding.ivVideoIconThird.visible(images[2].contains(Constants.MP4))
            }
        }
    }

    private fun ProfileData.setGridImagesAdapter() {
        images?.apply {
            if (images.size > 3) {
                val lastItem = if (images.size > 9) 9 else images.size
                val list = ArrayList(images.subList(3, lastItem))
                binding.rvExtraImages.visible(isVisible = list.size >= 3)
                /*val imageList = ArrayList<String>()
                list.forEach {
                    if (imageDataMap.containsKey(it))
                        imageList.add(imageDataMap[it]?.url ?: it)
                    else imageList.add(it)
                }*/
                profileGridImagesAdapter = ProfileGridImagesAdapter(list) { _, position ->
                    imageSelectedPosition = 3 + position
                    openGalleryFragment()
                }
                binding.rvExtraImages.adapter = profileGridImagesAdapter
            }
        }

    }

    private fun ProfileData.setGridImagesAdapterSecond() {
        images?.apply {
            if (images.size > 11) {
                val list = ArrayList(images.subList(9, images.size))
                /*val imageList = ArrayList<String>()
                list.forEach {
                    if (imageDataMap.containsKey(it))
                        imageList.add(imageDataMap[it]?.url ?: it)
                    else imageList.add(it)
                }*/
                binding.rvExtraImagesSecond.visible(isVisible = true)
                profileGridImagesAdapterSecond =
                    ProfileGridImagesAdapter(list) { _, position ->
                        imageSelectedPosition = 9 + position
                        openGalleryFragment()
                    }
                binding.rvExtraImagesSecond.adapter = profileGridImagesAdapterSecond
            }
        }

    }

    private fun ProfileData.setBasicInfoAdapter() {
        BaseUtils.getListItem(this) { list ->
            basicInfoList.clear()
            basicInfoList.addAll(list)
        }
        binding.tvBasicInfo.visible(basicInfoList.isNotEmpty())
        basicInfoAdapter = BasicCommonAdapter(basicInfoList, requireActivity())
        val basicInfoChipsLayoutManager =
            ChipsLayoutManager.newBuilder(requireActivity())
                .setChildGravity(Gravity.TOP)
                .setScrollingEnabled(true)
                .setGravityResolver { Gravity.CENTER }
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .build()
        binding.rvBasicInfo.layoutManager = basicInfoChipsLayoutManager

        binding.rvBasicInfo.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = basicInfoAdapter
        }
    }

    private fun ProfileData.setWorkAdapter() {
        BaseUtils.getWorkItem(this) { list ->
            workItemList.clear()
            workItemList.addAll(list)
        }

        binding.tvWorkAndEducation.visible(workItemList.isNotEmpty())

        wordAdapter = BasicCommonAdapter(workItemList, requireActivity(), fromWork = true)
        binding.rvWork.apply {
            itemAnimator = DefaultItemAnimator()
            adapter = wordAdapter
        }
    }

    private fun ProfileData.setHobbiesAdapter() {
        hobbiesList.clear()
        hobbies_interest?.let { hobbiesList.addAll(it) }

        binding.tvHobbiesAndInterests.visible(hobbiesList.isNotEmpty())

        val chipsLayoutManager =
            ChipsLayoutManager.newBuilder(requireActivity())
                .setChildGravity(Gravity.TOP)
                .setScrollingEnabled(true)
                .setGravityResolver { Gravity.CENTER }
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .build()

        hobbyAdapter = HobbyAdapter(
            hobbiesList,
            requireActivity(), "1", fromProfile = true
        )

        binding.rvHobbiesAndInterests.apply {
            layoutManager = chipsLayoutManager
            itemAnimator = DefaultItemAnimator()
            adapter = hobbyAdapter
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivBack -> {
                this.view?.findNavController()?.popBackStack()
            }

            R.id.iv_send_request -> {
                when (type) {
                    FROM_HOME, FROM_NEAR_YOU, FROM_SCAN -> {
                        likeDislike(userId, type = true) {}
//                        if (checkForLikeDislike()) {
//                        }
                    }

                    FROM_LIKE -> {
                        acceptReject(
                            AcceptRequest(
                                sender_id = userLikes?.sender_id,
                                receiver_option_choosed = true,
                                category = userLikes?.category
                            )
                        ) {
//                            if (it) this.view?.findNavController()?.popBackStack()
                        }
                    }
                }
            }

            R.id.iv_cancel_request -> {
                when (type) {
                    FROM_HOME, FROM_NEAR_YOU, FROM_SCAN -> {
//                        if (checkForLikeDislike())
                        likeDislike(userId, type = false) {}
                    }

                    FROM_LIKE -> {
                        acceptReject(
                            AcceptRequest(
                                sender_id = userLikes?.sender_id,
                                receiver_option_choosed = false,
                                category = userLikes?.category
                            )
                        ) {
//                            if (it) this.view?.findNavController()?.popBackStack()
                        }
                    }
                }

            }

            R.id.ivChat -> {
                if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS) {
                    showSubscriptionSheet(
                        SubscriptionPopUpType.SEND_LIKE_WITH_MESSAGE,
                        popBackStack = false
                    ) {
                    }
                } else {
                    sendChatRequest()
                }

            }

            R.id.ivChatVenueSingle -> {
                if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS) {
                    showSubscriptionSheet(
                        SubscriptionPopUpType.SEND_LIKE_WITH_MESSAGE,
                        popBackStack = false
                    ) {}
                } else {
                    requireContext().openBottomSheetWithEditField(
                        "Make the first move! Catch their eye with a comment.",
                        "",
                        "Send a Hi...",
                        "",
                        "Send Comment",
                        arrayListOf(5, 5),
                        isCancelVisible = true,
                        forVenueSingles = true
                    ) {
                        likeDislike(userId, senderMessage = it, type = true) {}
                    }
                }
            }

            R.id.ivRequestStatus -> {
                handleRequestClick()
            }

            R.id.ivUser -> {
                imageSelectedPosition = 0
                openGalleryFragment()
            }

            R.id.ivUserSecond -> {
                imageSelectedPosition = 1
                openGalleryFragment()
            }

            R.id.ivUserThird -> {
                imageSelectedPosition = 2
                openGalleryFragment()
            }

            R.id.ivMore -> {
                showPopup()
            }
        }
    }

    private fun sendChatRequest() {
        when (type) {
            FROM_HOME, FROM_NEAR_YOU, FROM_SCAN -> {
//                if (checkForLikeDislike())
                requireContext().openBottomSheetWithEditField(
                    "Make the first move! Catch their eye with a comment.",
                    "",
                    "Send a Hi...",
                    "",
                    "Send Comment",
                    arrayListOf(5, 5),
                    isCancelVisible = true
                ) {
                    likeDislike(userId, senderMessage = it, type = true) {}
                }
            }

            FROM_LIKE -> {
                userLikes?.let {
                    requireContext().openBottomSheet(it) { msg, dialog, layout ->
                        dialog.setCanceledOnTouchOutside(false)
                        dialog.setCancelable(false)
                        layout.progressBar.visible(isVisible = true)
                        acceptReject(
                            AcceptRequest(
                                sender_id = it.sender_id,
                                receiver_option_choosed = true,
                                receiver_response_message = msg.trim(),
                                category = it.category
                            )
                        ) { isSuccess ->
                            layout.progressBar.visible(false)
                            dialog.behavior.isHideable = true
                            dialog.setCanceledOnTouchOutside(true)
                            dialog.setCancelable(true)
                            if (isSuccess) {
                                dialog.dismiss()
//                                        this.view?.findNavController()?.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleRequestClick() {
        when (requestStatus) {
            CloseFriendAdapter.RequestStatus.ADD -> {
                addCloseFriend()
            }

            CloseFriendAdapter.RequestStatus.SENT -> {
                undoInvitation()
            }

            CloseFriendAdapter.RequestStatus.RECEIVED -> {
                acceptRejectInvitation()
            }

            CloseFriendAdapter.RequestStatus.ALREADY_ADDED -> {
                removeCloseFriend()
            }
        }
    }

    private fun showPopup() {
        val popup = PopupMenu(requireContext(), binding.ivMore)
        if (canUserSendRequest) {
            val text = when (requestStatus) {
                CloseFriendAdapter.RequestStatus.ADD -> "Add to Close Friend"
                CloseFriendAdapter.RequestStatus.SENT -> "Cancel Friend Request"
                CloseFriendAdapter.RequestStatus.RECEIVED -> "Accept Friend Request"
                CloseFriendAdapter.RequestStatus.ALREADY_ADDED -> "Remove Close Friend"
            }
            popup.menu.add(0, 2, Menu.NONE, text)
        }

        popup.menu.add(0, 1, Menu.NONE, "Forward")
        popup.menu.add(0, 0, Menu.NONE, getString(R.string.report))

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                0 -> {
                    openReportDialog { data, subOption, child, reason ->
                        val reportRequest = ReportRequest(
                            userId,
                            data._id,
                            suboption_id = subOption._id,
                            sub_suboption_id = child._id,
                            other_info = reason
                        )
                        Log.d("Request", "$reportRequest")
                        reportUser(reportRequest)
                    }
                }

                1 -> {
                    requireActivity().share(
                        "Check out ${profileData?.name} on RadarQR. Here is the profile link: ${
                            RaddarApp.getEnvironment().getShareUrl()
                        }$userId"
                    )
                }

                2 -> {
                    handleRequestClick()
                }
            }
            true
        }

        popup.show()
    }

    private fun checkForLikeDislike(): Boolean {
        when (apiType) {
            SEND -> {
                return true
            }

            REQUEST_RECEIVE -> {
                showAlert(Constants.REQUEST_RECEIVE)
                return false
            }

            REQUEST_SENT -> {
                showAlert(Constants.REQUEST_SENT)
                return false
            }

            MATCH -> {
                showAlert(Constants.MATCH_MESSAGE)
                return false
            }

            UN_MATCH -> {
                showAlert(Constants.UN_MATCH_MESSAGE)
                return false
            }

            DECLINE -> {
                showAlert(Constants.DECLINE)
                return false
            }

            else -> {
                return true
            }
        }
    }

    private fun likeDislike(
        userId: String,
        likeId: String? = null,
        senderMessage: String? = null,
        type: Boolean,
        response: (Boolean) -> Unit
    ) {
        enableDisableButtons(isEnabled = false)
        if (view != null && isVisible && isAdded)
            lifecycleScope.launch {
                recommendationViewModel.sendRequest(
                    LikeDislikeRequest(
                        userId,
                        type,
                        if (this@ProfileFragment.type == FROM_HOME || this@ProfileFragment.type == FROM_NEAR_YOU) Constants.ONLINE else if (this@ProfileFragment.type == FROM_VENUE_SINGLES) Constants.VENUE else Constants.IN_PERSON,
                        likeId,
                        senderMessage
                    )
                )
                    .observe(viewLifecycleOwner) {
                        SubscriptionWrapper.getUserInformation { _, _, _ -> }
                        when (it) {
                            is DataResult.Loading -> {

                            }

                            is DataResult.Success -> {
                                recommendationViewModel.profileSwipedCount++
                                profileData?.let { it1 ->
                                    logSendLikeEvent(
                                        data = it1,
                                        type,
                                        senderMessage
                                    )
                                }
                                if (!type && (this@ProfileFragment.type == FROM_HOME || this@ProfileFragment.type == FROM_NEAR_YOU)) {
                                    recommendationViewModel.isUndoVisible = true
                                    recommendationViewModel.lastCancelledId = it.data.data._id
                                    recommendationViewModel.canceledFromProfile = true
                                    response(true)
                                }
                                if (this@ProfileFragment.type == FROM_VENUE_SINGLES) {
                                    requireContext().showToast(it.data.message)
                                } else {
                                    if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS && this@ProfileFragment.type == FROM_HOME)
                                        recommendationViewModel.showAds(this@ProfileFragment) {
                                            getProfileViewModel.saveAdsCount(viewLifecycleOwner.lifecycleScope)
                                            this@ProfileFragment.view?.findNavController()
                                                ?.popBackStack()
                                        }
                                    else {
                                        this@ProfileFragment.view?.findNavController()
                                            ?.popBackStack()
                                    }
                                }
                            }

                            is DataResult.Failure -> {
                                response(false)
                                enableDisableButtons(isEnabled = true)
                                if (it.statusCode == 422) {
                                    val likeStatus: LikeStatus? = try {
                                        Gson().fromJson(it.data, LikeStatus::class.java)
                                    } catch (e: java.lang.Exception) {
                                        null
                                    }
                                    if (likeStatus != null) {
                                        val requestSent = likeStatus.request_sent
                                        val requestReceive = likeStatus.request_receive
                                        val match = likeStatus.is_match
                                        val unMatch = likeStatus.is_unmatch
                                        val isDeclined = likeStatus.is_decline
                                        when {
                                            unMatch -> {
                                                showAlert(Constants.UN_MATCH_MESSAGE)
                                            }

                                            match -> {
                                                showAlert(Constants.MATCH_MESSAGE)
                                            }

                                            isDeclined -> {
                                                showAlert(Constants.DECLINE)
                                            }

                                            requestReceive -> {
                                                showAlert(Constants.REQUEST_RECEIVE)
                                            }

                                            requestSent -> {
                                                showAlert(Constants.REQUEST_SENT)
                                            }

                                        }
                                    }
                                } else if (it.statusCode == 429) {
                                    RaddarApp.getInstance()
                                        .setSubscriptionStatus(SubscriptionStatus.NON_PLUS)
                                    showSubscriptionSheet(
                                        SubscriptionPopUpType.RECOMMENDATION_LIMIT_REACHED,
                                        popBackStack = false
                                    ) {}
                                } else {
                                    reportApiError(
                                        Exception().stackTrace[0].lineNumber,
                                        it.statusCode ?: 0,
                                        "user/send-request",
                                        requireActivity().componentName.className,
                                        it.message ?: ""
                                    )

                                    FirebaseCrashlytics.getInstance()
                                        .recordException(Exception("user/send-request Api Error"))
                                }
                            }

                            DataResult.Empty -> {
                            }

                            else -> {}
                        }
                    }

            }
    }

    private fun showAlert(message: String) {
        showCustomAlert(message, "Ok", object : DialogClickHandler<Any> {
            override fun onClick(value: Any) {
//                view?.findNavController()?.popBackStack()
            }
        })
    }

    private fun acceptReject(request: AcceptRequest, isSuccess: (Boolean) -> Unit) {
        enableDisableButtons(isEnabled = false)
        if (view != null && isVisible && isAdded) {
            if (BaseUtils.isInternetAvailable()) {
                BaseUtils.showProgressbar(requireContext())
                lifecycleScope.launch {
                    likesViewModel.acceptRequest(request)
                        .observe(viewLifecycleOwner) {
                            when (it) {
                                is DataResult.Loading -> {
                                }

                                is DataResult.Success -> {
                                    BaseUtils.hideProgressbar()
                                    profileData?.let { it1 ->
                                        logAcceptLikeEvent(
                                            data = it1,
                                            request
                                        )
                                    }
                                    binding.isMatching = false
                                    arguments?.putBoolean(Constants.TYPE, false)
                                    it.data.data.qb_dialog_id?.let { dialogId ->
                                        requireContext().showItsAMatchDialog(
                                            userLikes?.user_detail?.profile_pic,
                                            QuickBloxManager,
                                            dialogId
                                        ) { _, _, type ->
                                            if (type == 0) {
                                                this@ProfileFragment.view?.findNavController()
                                                    ?.popBackStack()
                                            } else {
                                                val bundle = Bundle().apply {
                                                    putString(Constants.DIALOG_ID, dialogId)
                                                    putString(Constants.FROM, "")
                                                    putString(
                                                        Constants.NAME,
                                                        userLikes?.user_detail?.name
                                                    )
                                                    putString(
                                                        Constants.PROFILE_PIC,
                                                        userLikes?.user_detail?.profile_pic
                                                    )
                                                    putString(Constants.TYPE, Constants.FROM_HOME)
                                                }
                                                this@ProfileFragment.view?.findNavController()
                                                    ?.navigate(
                                                        R.id.action_profile_to_chatFragment,
                                                        bundle
                                                    )
                                            }
                                        }

                                    }
                                    enableDisableButtons(isEnabled = true)
                                    isSuccess(true)
                                }

                                is DataResult.Failure -> {
                                    BaseUtils.hideProgressbar()
                                    enableDisableButtons(isEnabled = true)
                                    isSuccess(false)
                                    it.message?.let { message ->
                                        requireContext().showToast(message)
                                    }
                                    reportApiError(
                                        Exception().stackTrace[0].lineNumber,
                                        it.statusCode ?: 0,
                                        "user/accept-request",
                                        requireActivity().componentName.className,
                                        it.message ?: ""
                                    )

                                    FirebaseCrashlytics.getInstance()
                                        .recordException(Exception("user/accept-request Api Error"))
                                }

                                is DataResult.Empty -> {
                                }

                                else -> {}
                            }
                        }

                }
            } else {
                requireContext().showToast(getString(R.string.no_internet_msg))
            }
        }
    }

    private fun enableDisableButtons(isEnabled: Boolean) {
        binding.ivCancelRequest.isEnabled = isEnabled
        binding.ivSendRequest.isEnabled = isEnabled
        binding.ivChat.isEnabled = isEnabled
    }

    private fun openGalleryFragment() {
        profileData?.let {
            val bundle = Bundle().apply {
                putSerializable(Constants.EXTRA_DATA, profileData)
                putInt(Constants.POSITION, imageSelectedPosition)
            }
            this.view?.findNavController()
                ?.navigate(R.id.action_profileFragment_to_galleryFragment, bundle)
        }
    }

    private fun reportUser(request: ReportRequest) {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                getProfileViewModel.reportUser(request = request).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {
                        }

                        is DataResult.Failure -> {
                        }

                        DataResult.Loading -> {
                        }

                        is DataResult.Success -> {
                            if (it.statusCode == 200) {
                                when (type) {
                                    FROM_HOME, FROM_NEAR_YOU, FROM_SCAN -> {
//                                        if (checkForLikeDislike())
                                        likeDislike(userId, type = false) {
                                            recommendationViewModel.isUndoVisible = false
                                        }
                                    }

                                    FROM_LIKE -> {
                                        acceptReject(
                                            AcceptRequest(
                                                sender_id = userLikes?.sender_id,
                                                receiver_option_choosed = false,
                                                category = userLikes?.category
                                            )
                                        ) {
                                            view?.findNavController()?.popBackStack()
                                        }
                                    }

                                    else -> {
                                        view?.context?.showToast(it.data.message)
                                    }
                                }
                            }
                        }

                        else -> {}
                    }
                }
            }
        }
    }


    /*
    * CLOSE FRIENDS API CALLS
    * */

    private fun updateItemInList() {
        if (type == FROM_CLOSE_FRIEND && closeFriendAndRequestViewModel.itemClickedPosition >= 0 && closeFriendAndRequestViewModel.list.size > closeFriendAndRequestViewModel.itemClickedPosition) {
            val item =
                closeFriendAndRequestViewModel.list[closeFriendAndRequestViewModel.itemClickedPosition]
            if (item is Users) {
                item.requestStatus = requestStatus
                closeFriendAndRequestViewModel.list[closeFriendAndRequestViewModel.itemClickedPosition] =
                    item
            } else if (item is CloseFriendUser) {
                if (closeFriendAndRequestViewModel.itemClickedPosition < closeFriendAndRequestViewModel.list.size)
                    closeFriendAndRequestViewModel.list.removeAt(closeFriendAndRequestViewModel.itemClickedPosition)
            }
        }
    }

    private fun updateItemInFriendRequestList() {
        if (type == FROM_FRIEND_REQUEST
            && closeFriendAndRequestViewModel.friendRequestItemClickedPosition >= 0
            && closeFriendAndRequestViewModel.friendRequestList.size > closeFriendAndRequestViewModel.friendRequestItemClickedPosition
        ) {
            closeFriendAndRequestViewModel.friendRequestList.removeAt(
                closeFriendAndRequestViewModel.friendRequestItemClickedPosition
            )
            closeFriendAndRequestViewModel.friendRequestItemClickedPosition = -1
        }
    }

    private fun updateItem(status: CloseFriendAdapter.RequestStatus) {
        if (type == FROM_FRIEND_REQUEST)
            closeFriendAndRequestViewModel.updateItem(profileData?._id!!, status)
    }

    private fun addCloseFriend() {
        requireContext().addACloseFriend(
            profileData?.name ?: "this person"
        ) { _, dialog, layout ->
            layout.progressBarApi.visible(isVisible = true)
            profileData?._id?.sendInvitation {
                layout.progressBarApi.visible(isVisible = false)
                closeFriendAndRequestViewModel.requesterInfo.clear()
                dialog.dismiss()
            }
        }
    }

    private fun undoInvitation() {
        requireContext().undoRequest("") { _, dialog, layout ->
            layout.progressBarApi.visible(isVisible = true)
            profileData?.friendlist?.friend_id?.undoInvite { isSuccess ->
                layout.progressBarApi.visible(isVisible = false)
                if (isSuccess) {
                    // after undo make the status to ADD so that user can again send request
                    requestStatus =
                        CloseFriendAdapter.RequestStatus.ADD
                    requestStatus.handleFriendStatus()
                    updateItemInList()
                    dialog.dismiss()
                }
            }
        }
    }

    private fun acceptRejectInvitation() {
        requireContext().confirmCloseFriend(null, "") { v, dialog, layout ->
            if (v.id == R.id.tv_continue) {
                // accept close friend request
                acceptCloseFriendRequest(layout.progressBarApi, dialog)
            } else {
                //reject close friend request dialog
                rejectInvite(layout.rejectProgressBarApi, dialog)

            }
        }
    }

    private fun removeCloseFriend() {
        requireContext().removeCloseFriend("") { _, dialog, layout ->
            layout.progressBarApi.visible(isVisible = true)
            profileData?._id?.removeFriend {
                layout.progressBarApi.visible(isVisible = false)
                if (it) {
                    requestStatus = CloseFriendAdapter.RequestStatus.ADD
                    requestStatus.handleFriendStatus()
                    updateItemInList()
                    closeFriendAndRequestViewModel.requesterInfo.clear()
                    dialog.dismiss()
                }
            }

        }
    }

    private fun acceptCloseFriendRequest(
        view: View,
        dialog: BottomSheetDialog
    ) {
        view.visible(isVisible = true)
        // API CALL FOR ACCEPT CLOSE FRIEND
        profileData?.friendlist?.requester_id?.acceptCloseFriendRequest { isSuccess ->
            view.visible(isVisible = false)
            if (isSuccess) {
                // update status to ALREADY_ADDED so that user will not able to send request as user is already added
                requestStatus =
                    CloseFriendAdapter.RequestStatus.ALREADY_ADDED
                requestStatus.handleFriendStatus()
                updateItemInList()
                if (type == FROM_FRIEND_REQUEST || type == FROM_CLOSE_FRIEND)
                    closeFriendAndRequestViewModel.requesterInfo[profileData?._id!!] =
                        RequesterInfo(
                            profileData?._id!!,
                            profileData?.name!!,
                            profileData?.profile_pic!!,
                            profileData?.username!!
                        )
                updateItem(CloseFriendAdapter.RequestStatus.ALREADY_ADDED)
                updateItemInFriendRequestList()

                dialog.dismiss()
            }
        }
    }

    private fun rejectInvite(
        view: View,
        dialog: BottomSheetDialog
    ) {
        view.visible(isVisible = true)
        // API CALL FOR REJECT THE INVITATION
        profileData?.friendlist?.requester_id?.rejectInvite { isSuccess ->
            view.visible(isVisible = false)
            if (isSuccess) {
                // update status to ADD if user reject the request so that user can send request again
                requestStatus =
                    CloseFriendAdapter.RequestStatus.ADD
                requestStatus.handleFriendStatus()
                updateItemInList()
                closeFriendAndRequestViewModel.requesterInfo.clear()
                updateItem(CloseFriendAdapter.RequestStatus.ADD)
                updateItemInFriendRequestList()
                dialog.dismiss()
            }
        }
    }

    private fun String.sendInvitation(callback: (Boolean) -> Unit) {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.sendRequest(SendInviteRequest(this@sendInvitation))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                callback(false)
                                it.message?.let { it1 -> requireContext().showToast(it1) }
                                if (it.statusCode == 422 && it.message == "Invite already exists") {
                                    requestStatus = CloseFriendAdapter.RequestStatus.RECEIVED
                                    updateItemInList()
                                    requestStatus.handleFriendStatus()
                                }
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                requestStatus = CloseFriendAdapter.RequestStatus.SENT
                                it.data.data?.apply {
                                    val friendList = FriendList(
                                        _id = this._id,
                                        friend_id = this.friend_id,
                                        requester_id = this.requester_id,
                                        request_sent = true
                                    )
                                    if (type == FROM_CLOSE_FRIEND && closeFriendAndRequestViewModel.itemClickedPosition >= 0 && closeFriendAndRequestViewModel.list.size > closeFriendAndRequestViewModel.itemClickedPosition) {
                                        val item =
                                            closeFriendAndRequestViewModel.list[closeFriendAndRequestViewModel.itemClickedPosition]
                                        if (item is Users) {
                                            item.friendlist = friendList
                                            item.requestStatus = requestStatus
                                            closeFriendAndRequestViewModel.list[closeFriendAndRequestViewModel.itemClickedPosition] =
                                                item
                                        }
                                    }
                                    profileData?.friendlist = friendList
                                }
                                profileData?.let { it1 -> logSendCloseFriendRequest(data = it1) }
                                requestStatus.handleFriendStatus()
                                callback(true)
                            }

                            else -> {}
                        }
                    }
            }
        }
    }

    private fun String.undoInvite(callback: (Boolean) -> Unit) {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.undoInvite(SendInviteRequest(friend_id = this@undoInvite))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                callback(false)
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                callback(true)
                            }

                            else -> {}
                        }
                    }
            }
        }
    }

    private fun String.acceptCloseFriendRequest(callback: (Boolean) -> Unit) {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.acceptInvitation(AcceptInviteRequest(requester_id = this@acceptCloseFriendRequest))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                callback(false)
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                callback(true)
                            }

                            else -> {}
                        }
                    }
            }
        }
    }

    private fun String.rejectInvite(callback: (Boolean) -> Unit) {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.rejectInvite(AcceptInviteRequest(requester_id = this@rejectInvite))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                callback(false)
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                callback(true)
                            }

                            else -> {}
                        }
                    }
            }
        }
    }

    private fun String.removeFriend(callback: (Boolean) -> Unit) {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.removeFriend(SendInviteRequest(friend_id = this@removeFriend))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                callback(false)
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                callback(true)
                                profileData?.let { it1 -> logRemoveFriendRequest(data = it1) }
                            }

                            else -> {}
                        }
                    }
            }
        }
    }

    private fun logProfileVisitEvent(profileData: ProfileData) {
        mixPanelWrapper.logEvent(MixPanelWrapper.PROFILE_VISIT, JSONObject().apply {
            put(
                PropertiesKey.FROM_SCREEN, when (type) {
                    FROM_HOME -> Constants.MixPanelFrom.RECOMMENDATION
                    FROM_NEAR_YOU -> Constants.MixPanelFrom.NEAR_YOU
                    FROM_LIKE -> Constants.MixPanelFrom.LIKE
                    FROM_SCAN -> Constants.MixPanelFrom.QR_CODE
                    FROM_CHAT -> Constants.MixPanelFrom.CHAT
                    FROM_VENUE_SINGLES -> Constants.MixPanelFrom.HOTSPOTS
                    FROM_CLOSE_FRIEND, FROM_FRIEND_REQUEST -> Constants.MixPanelFrom.CLOSE_FRIENDS
                    else -> ""
                }
            )
            put(PropertiesKey.PROFILE_USERNAME, profileData.username)
            put(PropertiesKey.PROFILE_NAME, profileData.name)
            profileData.location.city?.let {
                put(PropertiesKey.PROFILE_LOCATION_CITY, it)
            }
            profileData.location.state?.let {
                put(PropertiesKey.PROFILE_LOCATION_STATE, it)
            }
            profileData.age?.let {
                put(PropertiesKey.PROFILE_AGE, it)
            }
            profileData.gender?.let {
                put(PropertiesKey.PROFILE_GENDER, it)
            }
            profileData.birthday?.let {
                put(PropertiesKey.PROFILE_DOB, it)
            }
        })
    }

    /**
     * KEYS :--
     * type, age,gender,name, venue_name, user_location (lat,long), location_city, location_state, venue_location (lat,long)
     * dob, username
     * */
    private fun logAcceptLikeEvent(data: ProfileData, request: AcceptRequest) {
        mixPanelWrapper.logAcceptLikeEvent(data, JSONObject().apply {
            put(
                PropertiesKey.LIKE_TYPE, when (type) {
                    FROM_HOME -> Constants.MixPanelFrom.RECOMMENDATION
                    FROM_NEAR_YOU -> Constants.MixPanelFrom.NEAR_YOU
                    FROM_LIKE -> Constants.MixPanelFrom.LIKE
                    FROM_SCAN -> Constants.MixPanelFrom.QR_CODE
                    FROM_CHAT -> Constants.MixPanelFrom.CHAT
                    FROM_VENUE_SINGLES -> Constants.MixPanelFrom.HOTSPOTS
                    FROM_CLOSE_FRIEND, FROM_FRIEND_REQUEST -> Constants.MixPanelFrom.CLOSE_FRIENDS
                    else -> ""
                }
            )
            put(
                PropertiesKey.ACTION,
                if (request.receiver_option_choosed == true) Constants.MixPanelFrom.ACTION_ACCEPT else Constants.MixPanelFrom.ACTION_REJECT
            )
            if (type == FROM_VENUE_SINGLES) {
                venueData?.let {
                    put(PropertiesKey.VENUE_NAME, it.name)
                    put(
                        PropertiesKey.VENUE_LOCATION,
                        it.contactinfo.latlon?.coordinates
                    )
                }
            }
        })
    }

    /**
     * KEYS :--
     * type, age,gender,name, venue_name, user_location (lat,long), location_city, location_state, venue_location (lat,long)
     * dob, username
     * */
    private fun logSendLikeEvent(data: ProfileData, type: Boolean, senderMessage: String?) {
        mixPanelWrapper.logSendLikeEvent(data, JSONObject().apply {
            put(
                PropertiesKey.LIKE_TYPE, when (this@ProfileFragment.type) {
                    FROM_HOME -> Constants.MixPanelFrom.RECOMMENDATION
                    FROM_NEAR_YOU -> Constants.MixPanelFrom.NEAR_YOU
                    FROM_LIKE -> Constants.MixPanelFrom.LIKE
                    FROM_SCAN -> Constants.MixPanelFrom.QR_CODE
                    FROM_CHAT -> Constants.MixPanelFrom.CHAT
                    FROM_VENUE_SINGLES -> Constants.MixPanelFrom.HOTSPOTS
                    FROM_CLOSE_FRIEND, FROM_FRIEND_REQUEST -> Constants.MixPanelFrom.CLOSE_FRIENDS
                    else -> ""
                }
            )
            put(
                PropertiesKey.CATEGORY,
                if (this@ProfileFragment.type == FROM_HOME || this@ProfileFragment.type == FROM_NEAR_YOU) Constants.ONLINE else if (this@ProfileFragment.type == FROM_VENUE_SINGLES) Constants.VENUE else Constants.IN_PERSON,
            )
            put(
                PropertiesKey.ACTION,
                if (type) if (senderMessage?.trim().isNullOrEmpty())
                    Constants.MixPanelFrom.ACTION_LIKE else Constants.MixPanelFrom.ACTION_LIKE_MESSAGE
                else Constants.MixPanelFrom.ACTION_DISLIKE
            )
            venueData?.let {
                put(PropertiesKey.VENUE_NAME, it.name)
                put(
                    PropertiesKey.VENUE_LOCATION,
                    it.contactinfo.latlon?.coordinates
                )
            }?:run {
                put(PropertiesKey.VENUE_NAME, Constants.MixPanelFrom.NA)
                put(
                    PropertiesKey.VENUE_LOCATION,
                    Constants.MixPanelFrom.NA
                )
            }
        })
    }

    private fun logSendCloseFriendRequest(data: ProfileData) {
        mixPanelWrapper.logSendCloseFriendRequest(JSONObject().apply {
            put(
                PropertiesKey.FROM_SCREEN,
                if (type == FROM_VENUE_SINGLES) Constants.MixPanelFrom.HOTSPOTS else Constants.MixPanelFrom.OTHERS
            )
            data.let {
                put(PropertiesKey.PROFILE_AGE, it.age)
                put(PropertiesKey.PROFILE_GENDER, it.gender)
                put(PropertiesKey.PROFILE_USERNAME, it.username)
                put(PropertiesKey.PROFILE_NAME, it.name)
                put(PropertiesKey.PROFILE_USER_LOCATION, it.location.latlon?.coordinates)
                put(PropertiesKey.PROFILE_LOCATION_CITY, it.location.city)
                put(PropertiesKey.PROFILE_LOCATION_STATE, it.location.state)
                put(PropertiesKey.PROFILE_DOB, it.birthday)
            }
        })
    }

    private fun logRemoveFriendRequest(data: ProfileData) {
        mixPanelWrapper.logRemoveFriendRequest(JSONObject().apply {
            put(PropertiesKey.FROM_SCREEN, Constants.MixPanelFrom.OTHERS)
            data.let {
                put(PropertiesKey.PROFILE_AGE, it.age)
                put(PropertiesKey.PROFILE_GENDER, it.gender)
                put(PropertiesKey.PROFILE_USERNAME, it.username)
                put(PropertiesKey.PROFILE_NAME, it.name)
                put(PropertiesKey.PROFILE_USER_LOCATION, it.location.latlon?.coordinates)
                put(PropertiesKey.PROFILE_LOCATION_CITY, it.location.city)
                put(PropertiesKey.PROFILE_LOCATION_STATE, it.location.state)
                put(PropertiesKey.PROFILE_DOB, it.birthday)
            }
        })
    }
}