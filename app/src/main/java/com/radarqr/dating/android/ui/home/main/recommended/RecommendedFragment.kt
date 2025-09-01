package com.radarqr.dating.android.ui.home.main.recommended

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentRecommendedBinding
import com.radarqr.dating.android.databinding.LayoutHomeReportRemoveDialogBinding
import com.radarqr.dating.android.hotspots.helpers.showSubscriptionSheet
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.subscription.SubscriptionWrapper
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.main.adapter.RecommendationAdapter
import com.radarqr.dating.android.ui.home.main.model.GetRecommendationViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.LikeStatus
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.welcome.mobileLogin.AccountSettingsRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.LikeDislikeRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.ReportRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.UserRecommendationRequest
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.SharedPrefsHelper
import com.radarqr.dating.android.utility.Utility.openBottomSheetWithEditField
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.cardstackview.*
import com.radarqr.dating.android.utility.enums.SubscriptionPopUpType
import com.radarqr.dating.android.utility.handler.DialogClickHandler
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper.PropertiesKey
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecommendedFragment :
    BaseFragment<FragmentRecommendedBinding>(), ViewClickHandler, CardStackListener {

    private val recommendationViewModel: GetRecommendationViewModel by viewModel()
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val mixPanelWrapper: MixPanelWrapper by inject()

    var type: Boolean? = null
    var userId = ""
    var profileData: ProfileData? = null

    private lateinit var vibrator: Vibrator
    var dialog: BottomSheetDialog? = null

    private val adapter by lazy { RecommendationAdapter() }
    private val layoutManager by lazy { CardStackLayoutManager(requireContext(), this) }

    var isRejected = false

    companion object {
        var nestedScrollView: NestedScrollView? = null
    }

    private lateinit var rocketAnimation: AnimationDrawable

    /*-------------------------------------------------------------------------------------------------------*/
    /*-------------------------------- #region Fragment Methods ---------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------*/


    override fun getLayoutRes(): Int = R.layout.fragment_recommended

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.recommendedFragment = this
        binding.clickHandler = this
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        recommendationViewModel.recommendationData.setData()
        initializeProfileObserver()
        layoutManager.apply {
            setStackFrom(StackFrom.None)
            setDirections(Direction.HORIZONTAL)
            setSwipeableMethod(SwipeableMethod.Automatic)
            setOverlayInterpolator(LinearInterpolator())
        }

        /*  binding.ivChat.apply {
              setBackgroundResource(R.drawable.animation_hotspots)
              rocketAnimation = background as AnimationDrawable
          }*/

        try {
            binding.rvHome.layoutManager = layoutManager
        } catch (e: Exception) {

        }
        setRecyclerView()
//        profileDataList.clear()
//        profileDataList.addAll(recommendationViewModel.userList.values)
        with(binding) {

            initializeProfileObserver()

            layoutManager.apply {
                setStackFrom(StackFrom.None)
                setDirections(Direction.HORIZONTAL)
                setSwipeableMethod(SwipeableMethod.Automatic)
                setOverlayInterpolator(LinearInterpolator())
            }
            rvHome.layoutManager = layoutManager
            setRecyclerView()

            ivCancelRequest.visible(adapter.getList().isNotEmpty())
            ivSendRequest.visible(adapter.getList().isNotEmpty())
            rvHome.visible(adapter.getList().isNotEmpty())
            ivChat.visible(adapter.getList().isNotEmpty())
//            llError.visible(adapter.getList().isEmpty())
        }
        getRecommendations(isLastPosition = true)

        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        dialog = BottomSheetDialog(requireActivity())
        binding.tvNoInternet.setOnClickListener {
            if (BaseUtils.isInternetAvailable())
                setAdapter()
            else CommonCode.setToast(
                requireContext(),
                resources.getString(R.string.no_internet_msg)
            )
        }
        setAdapter()
        binding.ivUndo.visibility =
            if (recommendationViewModel.isUndoVisible) View.VISIBLE else View.INVISIBLE
        if (recommendationViewModel.lastCancelledId.isEmpty()) {
            disableUndo()
        } else enableUndo()

//        binding.animateToEnd.show()
    }


    override fun onResume() {
        super.onResume()

        activity?.let {
            (it as HomeActivity).hideShowIvAiChatIcon(true).setOnClickListener {
                if (BaseUtils.isInternetAvailable()) {
                    val bundle = Bundle()
                    bundle.putString(Constants.TYPE, Constants.RUN_DAY_AI)

                    findNavController().navigate(
                        R.id.action_recommendation_to_web_view_fragment,
                        bundle
                    )
                } else CommonCode.setToast(
                    requireContext(),
                    resources.getString(R.string.no_internet_msg)
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.let { (it as HomeActivity).hideShowIvAiChatIcon(false) }
//        activity?.let { (it as HomeActivity).hideShowFireIcon(isVisible = false) }
        /*recommendationViewModel.userRemovedIdsList.forEach {
            if (recommendationViewModel.userList.containsKey(it))
                recommendationViewModel.userList.remove(it)
        }*/
        recommendationViewModel.userRemovedIdsList.clear()
    }

    /*-------------------------------------------------------------------------------------------------------*/
    /*-------------------------------- #endregion Fragment Methods ------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------*/


    /*-------------------------------------------------------------------------------------------------------*/
    /*-------------------------------- #region Private Methods ---------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------*/


    private fun initializeProfileObserver() {
        /* getProfileViewModel.profileData.observe(viewLifecycleOwner) {
             it?.apply {
                 *//*images?.let {
                    if (images.isNotEmpty())
                        binding.includePauseProfile.ivUserImage.loadImage(
                            getProfileViewModel.userImages[images[0]] ?: "",
                            binding.includePauseProfile.progressBar
                        )
                }*//*
            }
        }*/
    }

    private fun setAdapter() {
        /*if (BaseUtils.isInternetAvailable()) {
         *//*   if (SharedPrefsHelper[Constants.IS_PROFILE_PAUSED, false]) {
                binding.includePauseProfile.llPauseProfile.visibility = View.VISIBLE
            } else {
//                getUserRecommendation()
                binding.includePauseProfile.llPauseProfile.visibility = View.GONE
            }
            getAccountDetails()
*//*
        } else {
            binding.tvNoInternet.visibility = View.VISIBLE
            binding.llError.tv_error.text = resources.getString(R.string.no_internet_msg)
        }*/

        binding.ivCancelRequest.setOnClickListener {
            /**
             * steps to reject request
             * 1. store user id in onCardAppeared listener of cardStackView
             * 2. store type(true, false) on click of this button
             * 3. Hit likeDislike api ->
             * if response is success then {
             *      a. set isRejected to true (used to check that user rejected and helping in tracking undo)
             *      b. start card swipe left animation
             *      c. remove user from near list if same user exist there and store user object, can be retrieved on undo
             *      d. store user data in object, can be retrieved on undo
             *      e. store rejectedUser's id used to retrieve user from api
             *      f. add removed user's id in removedUsersList(remove user in onStop method)
             *
             * }
             *
             * */
            rejectRequest()
        }
        binding.ivSendRequest.setOnClickListener {
            /**
             * steps to send request
             * 1. store user id in onCardAppeared listener of cardStackView
             * 2. store type(true, false) on click of this button
             * 3. Hit likeDislike api ->
             * if response is success then {
             *      a. set isRejected to false (used to check that user rejected and helping in tracking undo)
             *      b. start card swipe right animation
             *      c. remove user from near list if same user exist there
             *      d. add removed user's id in removedUsersList(remove user in onStop method)
             *
             * }
             *
             * */
            type = true
            likeDislike(userId, 2, null)
        }

        binding.ivUndo.setOnClickListener {
            /**
             * steps to undo request
             * 1. store user id in onCardAppeared listener of cardStackView
             * 2. store type(true, false) on click of this button
             * 3. pass last canceled user id in api
             * 3. Hit likeDislike api ->
             * if response is success then {
             *      a. set isRejected to false (used to check that user rejected and helping in tracking undo)
             *      b. restore user id from the stored object on 0 position
             *      c. refresh user's map as well so that retrieved user come on top position
             *      d. set last cancelled user id empty
             *      e. set stored user object to null as well near user object
             *      f. set adapter again (because if user switch screen then adapter bottom animation won't work)
             *      g. start card swipe bottom animation
             *      h. remove the stored user id from remove user list so that user can't be removed in onStop method
             *
             * }
             *
             * */
            if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS) {
                showSubscriptionSheet(
                    SubscriptionPopUpType.RECOMMENDATION_UNDO,
                    popBackStack = false
                ) {
                    if (it)
                        recommendationViewModel.isUndoPending = true
                }
            } else {
                type = false
                likeDislike(
                    recommendationViewModel.lastCancelledId,
                    3,
                    recommendationViewModel.lastCancelledId
                )
            }

        }

        binding.ivChat.setOnClickListener {
//            rocketAnimation.start()
//            return@setOnClickListener
            if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS) {
                showSubscriptionSheet(
                    SubscriptionPopUpType.SEND_LIKE_WITH_MESSAGE,
                    popBackStack = false
                ) {
                }
            } else {
                requireContext().openBottomSheetWithEditField(
                    "Make the first move! Catch their eye with a comment.",
                    "",
                    "Send a Hi...",
                    "",
                    "Send Comment",
                    arrayListOf(5, 5),
                    isCancelVisible = true
                ) {
                    type = true
                    likeDislike(userId, 2, null, it)
                }
            }
        }
        /*

                binding.ivPrefernce.setOnClickListener {
                    findNavController()
                        .navigate(R.id.action_home_to_prefernce)
                }

                binding.ivForword.setOnClickListener {
                    if (userList.size != 0) {
                        user_id = userList[0]._id ?: ""
                        val data = Bundle()
                        data.putString("user_id", user_id)
        //                data.putParcelable(Constants.EXTRA, loadBitmapFromView(binding.nvView))
                        findNavController()
                            .navigate(R.id.action_home_to_forword_profile, data)
                    }
                }
        */

        binding.llError.tvError.setOnClickListener {
            findNavController()
                .navigate(R.id.action_home_to_prefernce)
        }

    }

    private fun rejectRequest() {
        type = false
        likeDislike(userId, 1, null)
    }

    private fun loadBitmapFromView(v: View): Bitmap? {
        val b = Bitmap.createBitmap(
            v.measuredWidth,
            v.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        v.draw(c)
        return b
    }

    private fun getRecommendations(isLastPosition: Boolean = false) {
        if (BaseUtils.isInternetAvailable()) {
            handleProgressBarVisibility(boolean = true)
            binding.progressBar.visible(isVisible = false)
            binding.progressBarCenter.visible(isVisible = true)
            binding.tvNoInternet.visibility = View.GONE
            if (isLastPosition) {
                if (view != null)
                    lifecycleScope.launch {
                        recommendationViewModel.getRecommendation(UserRecommendationRequest())
                            .observe(viewLifecycleOwner) {
                                when (it) {
                                    is DataResult.Loading -> {

                                    }

                                    is DataResult.Success -> {
                                        binding.progressBarCenter.visible(isVisible = false)
                                        handleProgressBarVisibility(boolean = false)
                                        if (it.data.data.isEmpty()) {
//                                            recommendationViewModel.userList.clear()
                                            recommendationViewModel.recommendationData.clear()
                                            if (!recommendationViewModel.isUndoVisible || recommendationViewModel.lastCancelledId.isEmpty())
                                                binding.ivUndo.visibility = View.INVISIBLE
                                            binding.ivCancelRequest.visibility = View.GONE
                                            binding.ivSendRequest.visibility = View.GONE
                                            binding.ivChat.visibility = View.GONE
                                            binding.rvHome.visibility = View.GONE
                                            binding.llError.clErrorParent.visibility = View.VISIBLE
                                        } else {
                                            binding.ivCancelRequest.visibility = View.VISIBLE
                                            binding.ivSendRequest.visibility = View.VISIBLE
                                            binding.ivChat.visibility = View.VISIBLE
                                            binding.llError.clErrorParent.visibility = View.GONE
                                            binding.rvHome.visibility = View.VISIBLE
//                                            binding.rvHome.startAnimation(getAnimation())

                                            /* val data = it.data.data.replaceImageWithUrl(
                                                 requireContext(),
                                                 recommendationViewModel.userList
                                             )
                                             data.forEach { mapData ->
                                                 if (!recommendationViewModel.userList.containsKey(
                                                         mapData.key
                                                     )
                                                 ) {
                                                     recommendationViewModel.userList[mapData.key] =
                                                         mapData.value
                                                 } else {
                                                     if (recommendationViewModel.userList[mapData.key] != mapData.value) {
                                                         recommendationViewModel.userList[mapData.key] =
                                                             mapData.value
                                                     }
                                                 }
                                             }*/
                                            /*recommendationViewModel.userList.clear()
                                            recommendationViewModel.userList.putAll(data)*/
                                            recommendationViewModel.recommendationData =
                                                it.data.data
                                            it.data.data.setData()
                                            binding.llError.clErrorParent.visible(it.data.data.isEmpty())
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                if (recommendationViewModel.isUndoPending && recommendationViewModel.lastCancelledId.isNotEmpty()) {
                                                    recommendationViewModel.isUndoPending = false
                                                    type = false
                                                    likeDislike(
                                                        recommendationViewModel.lastCancelledId,
                                                        3,
                                                        recommendationViewModel.lastCancelledId
                                                    )
                                                }
                                            }, 300)
//                                            adapter?.refresh()
//                                            setRecyclerView()

                                        }
                                    }

                                    is DataResult.Failure -> {
                                        binding.progressBarCenter.visible(isVisible = false)
                                        handleProgressBarVisibility(boolean = false)
                                        binding.ivCancelRequest.visibility = View.GONE
                                        binding.ivSendRequest.visibility = View.GONE
                                        binding.ivChat.visible(isVisible = false)
                                        binding.rvHome.visibility = View.GONE
                                        binding.llError.clErrorParent.visibility = View.VISIBLE

                                        reportApiError(
                                            Exception().stackTrace[0].lineNumber,
                                            it.statusCode ?: 0,
                                            "user/recommendations",
                                            requireActivity().componentName.className,
                                            it.message ?: ""
                                        )

                                        FirebaseCrashlytics.getInstance()
                                            .recordException(Exception("user/recommendations Api Error"))
                                    }

                                    DataResult.Empty -> {
                                    }
                                }
                            }
                    }
            } else {
                binding.ivCancelRequest.visible(true)
                binding.ivSendRequest.visible(true)
                binding.rvHome.visible(true)
                binding.ivChat.visible(true)
            }
        } else {
            binding.tvNoInternet.visibility = View.VISIBLE
            binding.llError.clErrorParent.visible(isVisible = false)
            binding.llError.tvError.text = resources.getString(R.string.no_internet_msg)
        }
    }

    private fun ArrayList<ProfileData>.setData() {
        /*adapter.let {
            val old = it.getList()
            val new = this
            if (new.isNotEmpty())
                userId = new[0]._id ?: ""
            val callback = SpotDiffCallback(old, new)
            val result = DiffUtil.calculateDiff(callback)
            it.setList(new)
            result.dispatchUpdatesTo(it)
        }*/
        adapter.updateList(this)
    }


    private fun getAnimation(): Animation =
        AnimationUtils.loadAnimation(activity, R.anim.slide_in_top)


    private fun setRecyclerView() {
        binding.rvHome.adapter = adapter
    }


    private fun likeDislike(
        userId: String,
        value: Int,
        likeId: String?,
        senderMessage: String? = null
    ) {
        if (view != null && isAdded && isVisible && userId.isNotEmpty()) {
            handleProgressBarVisibility(boolean = true)
            lifecycleScope.launch {
                recommendationViewModel.sendRequest(
                    LikeDislikeRequest(
                        userId,
                        type,
                        Constants.ONLINE,
                        likeId,
                        senderMessage
                    )
                ).observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {

                        }

                        is DataResult.Success -> {
                            profileData?.let { it1 -> logSendLikeEvent(it1, value, senderMessage) }
                            SubscriptionWrapper.getUserInformation { _, _, _ -> }
                            when (value) {
                                3 -> { // doing undo
                                    isRejected = false
                                    /**
                                     * restoring data when user did undo to prevent api hit
                                     * */
                                    recommendationViewModel.deletedUserData?.let { deletedUserData ->
                                        /* val map: LinkedHashMap<String, ProfileData> =
                                             recommendationViewModel.userList.clone() as LinkedHashMap<String, ProfileData>
                                         recommendationViewModel.userList.clear()
                                         recommendationViewModel.userList[deletedUserData._id!!] =
                                             deletedUserData
                                         recommendationViewModel.userList.putAll(map)*/
//                                        profileDataList.add(0, deletedUserData)
//                                            setRecyclerView()
                                    }
                                    /* */
                                    /**
                                     * restoring near you data when user did undo to prevent api hit
                                     * and checking if delete near user data contains had data or not
                                     * *//*
                                    recommendationViewModel.deletedNearYouUserData?.let { profileData ->
                                        recommendationViewModel.nearYouUsersList[profileData._id!!] =
                                            profileData
                                    }*/
                                    /**
                                     * set both values to null so that duplicate user won't add
                                     * */
//                                    recommendationViewModel.deletedNearYouUserData = null
                                    recommendationViewModel.deletedUserData = null

                                    binding.ivUndo.visibility = View.INVISIBLE
                                    recommendationViewModel.isUndoVisible = false
                                    recommendationViewModel.lastCancelledId = ""
                                    disableUndo()
                                    recommendationViewModel.canceledFromProfile = false
                                    recommendationViewModel.recommendationData.clear()
                                    adapter.setList(ArrayList())
                                    adapter.notifyDataSetChanged()
                                    getRecommendations(isLastPosition = true)
                                    /*if (recommendationViewModel.canceledFromProfile || recommendationViewModel.recommendationData.isEmpty()) {

                                    } else swipeBottom()*/
                                }

                                1 -> { // rejecting the user
                                    isRejected = true
                                    binding.ivUndo.visible(true)
                                    recommendationViewModel.isUndoVisible = true
                                    recommendationViewModel.lastCancelledId = it.data.data._id
                                    if (recommendationViewModel.recommendationData.isNotEmpty())
                                        recommendationViewModel.recommendationData.removeAt(0)
                                    swipeLeft()
                                    enableUndo()
                                }

                                2 -> { // liking the user
                                    isRejected = false
                                    swipeRight()

                                    recommendationViewModel.lastCancelledId = ""
                                    if (recommendationViewModel.recommendationData.isNotEmpty())
                                        recommendationViewModel.recommendationData.removeAt(0)
                                    disableUndo()
                                    /* countDownTimer?.apply {
                                         binding.ivLikeAnime.visibility = View.VISIBLE
                                         start()
                                     }*/
                                }

                                else -> {
                                    /*swipeLeft()
                                    recommendationViewModel.lastCancelledId = ""
                                    disableUndo()*/
                                    /*countDownTimer?.apply {
                                        binding.ivCloseAnime.visibility = View.VISIBLE
                                        start()
                                    }*/
                                }
                            }
                            handleProgressBarVisibility(boolean = false)
                            if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS)
                                recommendationViewModel.showAds(this@RecommendedFragment) {
                                    getProfileViewModel.saveAdsCount(viewLifecycleOwner.lifecycleScope)
                                }
                        }

                        is DataResult.Failure -> {
                            handleProgressBarVisibility(boolean = false)
                            if (it.statusCode == 422) {
                                val likeStatus: LikeStatus? = try {
                                    Gson().fromJson(it.data, LikeStatus::class.java)
                                } catch (e: java.lang.Exception) {
                                    null
                                }
                                if (likeStatus != null) {
                                    disableUndo()
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
                    }
                }

            }
        }
    }

    private fun handleProgressBarVisibility(boolean: Boolean) {
        binding.progressBar.visible(isVisible = boolean)
        binding.ivChat.isEnabled = !boolean
        binding.ivSendRequest.isEnabled = !boolean
        binding.ivCancelRequest.isEnabled = !boolean
    }

    private fun swipeLeft() {
        Log.d("okhttp", "swipeLeft")
        val setting = SwipeAnimationSetting.Companion.Builder()
            .setDirection(Direction.Left)
            .setDuration(Duration.Slow.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        layoutManager.setSwipeAnimationSetting(setting)
        recommendationViewModel.profileSwipedCount++
        binding.rvHome.swipe()
    }

    private fun swipeRight() {
        Log.d("okhttp", "swipeRight")
        val setting = SwipeAnimationSetting.Companion.Builder()
            .setDirection(Direction.Right)
            .setDuration(Duration.Slow.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        layoutManager.setSwipeAnimationSetting(setting)
        recommendationViewModel.profileSwipedCount++
        binding.rvHome.swipe()
    }

    private fun swipeBottom() {
        Log.d("okhttp", "swipeBottom")
        val setting = SwipeAnimationSetting.Companion.Builder()
            .setDirection(Direction.Bottom)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        layoutManager.setSwipeAnimationSetting(setting)
        binding.rvHome.rewind()
    }

    private fun swipeTop() {
        val setting = SwipeAnimationSetting.Companion.Builder()
            .setDirection(Direction.Top)
            .setDuration(Duration.Normal.duration)
            .setInterpolator(AccelerateInterpolator())
            .build()
        layoutManager.setSwipeAnimationSetting(setting)
        binding.rvHome.rewind()
    }

    private fun showAlert(message: String) {
        showCustomAlert(message, "Ok", object : DialogClickHandler<Any> {
            override fun onClick(value: Any) {
                swipeTop()
            }
        })
    }

    private fun enableUndo() {
        binding.ivUndo.isEnabled = true
        ImageViewCompat.setImageTintList(
            binding.ivUndo,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black_color
                )
            )
        )
    }

    private fun disableUndo() {
        binding.ivUndo.isEnabled = false
        ImageViewCompat.setImageTintList(
            binding.ivUndo,
            ColorStateList.valueOf(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.undo_color
                )
            )
        )
    }

    private fun openBottomSheet() {
        val dialogBinding = DataBindingUtil.inflate<LayoutHomeReportRemoveDialogBinding>(
            LayoutInflater.from(requireContext()),
            R.layout.layout_home_report_remove_dialog,
            null,
            false
        )

        dialogBinding.tvRemove.setOnClickListener {
            dialog?.dismiss()
            rejectRequest()
        }

        dialogBinding.tvReport.setOnClickListener {
            dialog?.dismiss()
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

        dialogBinding.tvCancel.setOnClickListener {
            dialog?.dismiss()
        }

        dialog?.setContentView(dialogBinding.root)
        dialog?.setCancelable(false)
        dialog?.show()
    }

    private fun reportUser(request: ReportRequest) {
        if (view == null) return
        lifecycleScope.launch {
            recommendationViewModel.reportUser(request = request).observe(viewLifecycleOwner) {
                when (it) {
                    DataResult.Empty -> {
                    }

                    is DataResult.Failure -> {
                    }

                    DataResult.Loading -> {
                    }

                    is DataResult.Success -> {
                        if (it.statusCode == 200) {
//                            recommendationViewModel.deletedUserData = profileDataList[0]
                            type = false
                            likeDislike(userId, 4, null)
                        }
                    }
                }
            }
        }
    }

    private fun getAccountDetails() {
        try {
            lifecycleScope.launch {
                recommendationViewModel.getAccountSettings().observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Success -> {
                            SharedPrefsHelper.save(
                                Constants.IS_PROFILE_PAUSED,
                                it.data.data.pause_profile
                            )

                            binding.includePauseProfile.llPauseProfile.visibility =
                                if (it.data.data.pause_profile) View.VISIBLE else View.GONE

                            getProfileViewModel.data.value = it.data
                            getProfileViewModel.pauseProfileObserver.value =
                                it.data.data.pause_profile
                        }

                        DataResult.Empty -> {
                        }

                        is DataResult.Failure -> {
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/get-account-settings",
                                requireActivity().componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/get-account-settings Api Error"))
                        }

                        DataResult.Loading -> {
                        }
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

    /*-------------------------------------------------------------------------------------------------------*/
    /*--------------------------------- #endregion Private Methods ------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------*/


    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_pause_unpause_profile -> {
                getBaseActivity()?.let {
                    it.updateAccountSettings(AccountSettingsRequest(pause_profile = false)) { data ->
                        data?.let {
                            getProfileViewModel.data.value?.data?.pause_profile =
                                data.pause_profile ?: false
//                            getUserRecommendation()
                            binding.includePauseProfile.llPauseProfile.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onCardDragging(direction: Direction?, ratio: Float) {
        Log.d("okhttp", "onCardDragging")
    }

    override fun onCardSwiped(direction: Direction?) {
        Log.d("okhttp", "onCardSwiped")
        /**
         * Check if liked or disliked position is last or not
         * if last position then hit api
         * */
        userId = ""
        profileData = null
        getRecommendations(isLastPosition = (adapter.itemCount) == layoutManager.getTopPosition())
        handleProgressBarVisibility(boolean = adapter.itemCount == layoutManager.getTopPosition())
    }

    override fun onCardRewound(position: Int) {
        Log.d("okhttp", "onCardRewound $position")
        recommendationViewModel.lastCancelledId = ""
        if ((adapter.getList().size) > position) {
            if (recommendationViewModel.userRemovedIdsList.contains(
                    adapter.getList()[position]._id!!
                )
            )
                recommendationViewModel.userRemovedIdsList.remove(
                    adapter.getList()[position]._id!!
                )
            /* if (!recommendationViewModel.nearYouUsersList.containsKey(
                     adapter.getList()[position]?._id!!
                 )
             )
                 recommendationViewModel.nearYouUsersList[adapter.getList()[position]?._id!!] =
                     adapter.getList()[position]*/
        }
        disableUndo()
    }

    override fun onCardCanceled() {
        Log.d("okhttp", "onCardCanceled")
    }

    override fun onCardAppeared(view: View?, position: Int) {
        Log.d("okhttp", "onCardAppeared")
        /**
         * store user id for every appeared user
         * used to send, reject and undo request
         * */
        userId = adapter.getList()[position]._id!!
        profileData = adapter.getList()[position]
    }

    override fun onCardDisappeared(view: View?, position: Int) {
        Log.d("okhttp", "onCardDisappeared")
        /**
         * checking if user rejected
         * if rejected then store deleted user data
         * and store near user data as well if available so that we can restore over undo
         * */
        if (isRejected) {
            /*store user data*/
            if (adapter.getList().size > position)
                recommendationViewModel.deletedUserData = adapter.getList()[position]
            /*store near user data*/
            /* if (adapter.getList().size > position)
                 if (recommendationViewModel.nearYouUsersList.containsKey(
                         adapter.getList()[position]?._id!!
                     )
                 )
                     recommendationViewModel.deletedNearYouUserData =
                         recommendationViewModel.nearYouUsersList[adapter.getList()[position]?._id!!]*/
        }
        try {

            /* */
            /**
             * remove data from map when user like or disliked user's profile
             * first check for the key if found then remove
             * *//*
            if (adapter.getList().size > position)
                if (recommendationViewModel.nearYouUsersList.containsKey(adapter.getList()[position]?._id!!))
                    recommendationViewModel.nearYouUsersList.remove(
                        adapter.getList()[position]?._id!!
                    )*/

            /**
             * add removed user's id so that when user leave screen we can remove data from map
             * on behalf of id
             * */
            recommendationViewModel.userRemovedIdsList.add(adapter.getList()[position]._id!!)
        } catch (e: Exception) {
        }

    }

    /**
     * KEYS :--
     * type, age,gender,name, venue_name, user_location (lat,long), location_city, location_state, venue_location (lat,long)
     * dob, username
     * */
    private fun logSendLikeEvent(data: ProfileData, value: Int, senderMessage: String?) {
        mixPanelWrapper.logSendLikeEvent(data, JSONObject().apply {
            put(PropertiesKey.LIKE_TYPE, Constants.MixPanelFrom.RECOMMENDATION)
            put(PropertiesKey.CATEGORY, Constants.ONLINE)
            put(PropertiesKey.VENUE_NAME, Constants.MixPanelFrom.NA)
            put(
                PropertiesKey.VENUE_LOCATION,
                Constants.MixPanelFrom.NA
            )
            when (value) {
                //LIKE USER
                2 -> put(
                    PropertiesKey.ACTION,
                    if (senderMessage?.trim()
                            .isNullOrEmpty()
                    ) Constants.MixPanelFrom.ACTION_LIKE else Constants.MixPanelFrom.ACTION_LIKE_MESSAGE
                )
                // DISLIKE
                1 -> put(
                    PropertiesKey.ACTION,
                    Constants.MixPanelFrom.ACTION_DISLIKE
                )
                // UNDO
                3 -> put(
                    PropertiesKey.ACTION,
                    Constants.MixPanelFrom.ACTION_UNDO
                )
            }
        })
    }
}