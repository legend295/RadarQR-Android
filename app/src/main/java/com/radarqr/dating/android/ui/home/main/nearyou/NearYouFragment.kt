package com.radarqr.dating.android.ui.home.main.nearyou

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentNearYouBinding
import com.radarqr.dating.android.databinding.LayoutNearYouBottomSheetBinding
import com.radarqr.dating.android.hotspots.helpers.showSubscriptionSheet
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.subscription.SubscriptionWrapper
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.main.adapter.NearYouAdapter
import com.radarqr.dating.android.ui.home.main.model.GetRecommendationViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.LikeStatus
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.ui.welcome.mobileLogin.LikeDislikeRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.NearYouRequest
import com.radarqr.dating.android.utility.BaseUtils.isInternetAvailable
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.PaginationScrollListener
import com.radarqr.dating.android.utility.StaggeredItemDecoration
import com.radarqr.dating.android.utility.Utility.openBottomSheetWithEditField
import com.radarqr.dating.android.utility.Utility.setDimBackground
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.toPx
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.enums.SubscriptionPopUpType
import com.radarqr.dating.android.utility.handler.DialogClickHandler
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper.PropertiesKey
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class NearYouFragment :
    BaseFragment<FragmentNearYouBinding>() {

    var adapter: NearYouAdapter? = null
    var latLng: LatLng? = null
    private val recommendationViewModel: GetRecommendationViewModel by viewModel()
    private val mixPanelWrapper: MixPanelWrapper by inject()
    var page = 0
    var userId = 0
    var timer: CountDownTimer? = null


    override fun getLayoutRes(): Int = R.layout.fragment_near_you

    /*-------------------------------------------------------------------------------------------------------*/
    /*-------------------------------- #region Fragment Methods ---------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        latLng = HomeActivity.userLocation /*?: SharedPrefsHelper.getLastLocation()*/

        setLocation()
        page = recommendationViewModel.request.page
        binding.rvNearYou.visible(isVisible = isInternetAvailable())
        binding.tvNoInternet.visible(isVisible = !isInternetAvailable())

//        recommendationViewModel.nearYouList.clear()
//        nearYouList.addAll(recommendationViewModel.nearYouUsersList.values)
//        nearYouList.clear()
        binding.progressBar.visible(recommendationViewModel.nearYouList.isEmpty() && isInternetAvailable())
        setAdapter()
        getNearYouUsers()

        binding.llError.tvError.setOnClickListener {
            findNavController()
                .navigate(R.id.preference_main)
        }

        binding.tvNoInternet.setOnClickListener {
            binding.rvNearYou.visible(isVisible = isInternetAvailable())
            binding.tvNoInternet.visible(isVisible = !isInternetAvailable())
            if (isInternetAvailable()) {
                handleLocationEmpty()
                getNearYouUsers()
            } else CommonCode.setToast(
                requireContext(),
                resources.getString(R.string.no_internet_msg)
            )
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            getNearYouUsers()
        }
    }

    override fun onStop() {
        super.onStop()
        timer?.cancel()
    }


    /*-------------------------------------------------------------------------------------------------------*/
    /*-------------------------------- #endregion Fragment Methods ------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------*/


    /*-------------------------------------------------------------------------------------------------------*/
    /*-------------------------------- #region Private Methods ---------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------*/

    private fun getAnimation(): Animation =
        AnimationUtils.loadAnimation(activity, R.anim.slide_in_top)


    private fun handleLocationEmpty() {
        if (latLng == null) {
            getLocation {}
            timer = object : CountDownTimer(10000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    Log.d("okhttp", "$latLng")
                    if (latLng == null) {
                        if (!binding.tvLocationGet.isVisible)
                            binding.tvLocationGet.visible(true)
                        binding.llError.clErrorParent.visible(false)
                        latLng = HomeActivity.userLocation
                    } else {
                        cancel()
                        setLocation()
                        getNearYouUsers()
                        binding.tvLocationGet.visible(false)
                    }
                }

                override fun onFinish() {
                    binding.tvLocationGet.visible(false)
                    binding.llError.clErrorParent.visible(isInternetAvailable())
                    binding.progressBar.visible(isVisible = false)
                    if (latLng == null)
                        requireContext().showToast("unable to get location")
                }
            }.start()
        }
    }

    private fun setLocation() {
        recommendationViewModel.request = NearYouRequest(
            lat = latLng?.latitude?.toString() ?: "",
            latLng?.longitude?.toString() ?: ""
        )
    }


    private fun setAdapter() {
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        adapter = NearYouAdapter(recommendationViewModel.nearYouList) { data, position ->
            data?.apply {
                openBottomSheet(position)
            }
        }
        adapter?.setHasStableIds(true)
        binding.rvNearYou.setHasFixedSize(true)
        binding.rvNearYou.addItemDecoration(StaggeredItemDecoration(25.toPx.toInt()))
        binding.rvNearYou.layoutManager = layoutManager
        binding.rvNearYou.adapter = adapter
//        binding.rvNearYou.startAnimation(getAnimation())

        binding.rvNearYou.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                recommendationViewModel.isLoading = true
                adapter?.addLoadingView()
                page += 1
                recommendationViewModel.request.page = page
                getNearYouUsers(fromPagination = true)

            }

            override val isLastPage: Boolean
                get() = recommendationViewModel.isLastPage
            override val isLoading: Boolean
                get() = recommendationViewModel.isLoading
        })
    }

    private fun getNearYouUsers(fromPagination: Boolean = false) {
        if (!isInternetAvailable()) {
            binding.rvNearYou.visible(isVisible = isInternetAvailable())
            binding.tvNoInternet.visible(isVisible = !isInternetAvailable())
            return
        }
        if (isAdded && isVisible && view != null) {
            lifecycleScope.launchWhenCreated {
                recommendationViewModel.getNearYouUsers().observe(viewLifecycleOwner) {
                    binding.swipeRefreshLayout.isRefreshing = false
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            binding.progressBar.visible(isVisible = false)
                            binding.llError.clErrorParent.visible(recommendationViewModel.nearYouList.isEmpty() && isInternetAvailable())
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            binding.progressBar.visible(isVisible = false)
                            if (!fromPagination)
                                recommendationViewModel.nearYouList.clear()
                            else adapter?.removeLoadingView()
                            recommendationViewModel.nearYouList.addAll(it.data.data.users)
                            recommendationViewModel.isLastPage =
                                it.data.data.total_count <= recommendationViewModel.nearYouList.size
                            adapter?.refresh()
                            recommendationViewModel.isLoading = false
                            binding.llError.clErrorParent.visible(recommendationViewModel.nearYouList.isEmpty() && isInternetAvailable())
                        }
                    }
                }

            }
        } else {
            binding.llError.clErrorParent.visible(recommendationViewModel.nearYouList.isEmpty() && isInternetAvailable())
        }
    }

    private fun ProfileData.openBottomSheet(clickedPosition: Int) {
        val sheet = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        val layoutBinding =
            LayoutNearYouBottomSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)
        layoutBinding.data = this
        sheet.dismissWithAnimation = true

        layoutBinding.tvProfileComment.visible(about_me?.isNotEmpty() == true)
        layoutBinding.tvComment.visible(about_me?.isNotEmpty() == true)
        layoutBinding.tvProfileComment.text = resources.getString(R.string.about_me)
        layoutBinding.tvComment.text = about_me

        layoutBinding.ivChat.setOnClickListener {
            if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS) {
                sheet.dismiss()
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
                    isCancelVisible = true
                ) { msg ->
                    likeDislike(this._id!!, type = true, msg, clickedPosition, sheet, layoutBinding)
                }
            }
        }

        layoutBinding.ivUser.setOnClickListener {
            sheet.dismiss()
            val bundle = Bundle().apply {
                putSerializable(Constants.EXTRA_DATA, this@openBottomSheet)
                putString(Constants.USER_ID, this@openBottomSheet._id)
                putInt(Constants.FROM, ProfileFragment.FROM_HOME)
                putBoolean(Constants.TYPE, true)
            }
            this@NearYouFragment.view?.findNavController()?.navigate(R.id.profileFragment, bundle)
        }

        layoutBinding.ivSendRequest.setOnClickListener {
            likeDislike(this._id!!, type = true, "", clickedPosition, sheet, layoutBinding)
        }

        layoutBinding.ivCancelRequest.setOnClickListener {
            likeDislike(this._id!!, type = false, "", clickedPosition, sheet, layoutBinding)

        }

        sheet.setDimBackground()
        sheet.setContentView(layoutBinding.root)
        sheet.show()
    }

    private fun ProfileData.likeDislike(
        userId: String,
        type: Boolean,
        senderMessage: String? = null,
        clickedPosition: Int,
        sheet: BottomSheetDialog,
        layoutBinding: LayoutNearYouBottomSheetBinding
    ) {
        if (requireContext().isInternetAvailable() && view != null && isAdded && isVisible) {
            layoutBinding.progressBar.visible(isVisible = true)
            lifecycleScope.launch {
                recommendationViewModel.sendRequest(
                    LikeDislikeRequest(
                        userId,
                        type,
                        Constants.ONLINE,
                        null,
                        senderMessage
                    )
                ).observe(viewLifecycleOwner) {
                    SubscriptionWrapper.getUserInformation { _, _, _ -> }
                    when (it) {
                        is DataResult.Loading -> {

                        }

                        is DataResult.Success -> {
                            logSendLikeEvent(this@likeDislike, type, senderMessage)
                            clickedPosition.removeFromPosition(userId)
                        }

                        is DataResult.Failure -> {
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
                                            userId.showAlert(
                                                Constants.UN_MATCH_MESSAGE,
                                                clickedPosition
                                            )
                                        }

                                        match -> {
                                            userId.showAlert(
                                                Constants.MATCH_MESSAGE,
                                                clickedPosition
                                            )
                                        }

                                        isDeclined -> {
                                            userId.showAlert(Constants.DECLINE, clickedPosition)
                                        }

                                        requestReceive -> {
                                            userId.showAlert(
                                                Constants.REQUEST_RECEIVE,
                                                clickedPosition
                                            )
                                        }

                                        requestSent -> {
                                            userId.showAlert(
                                                Constants.REQUEST_SENT,
                                                clickedPosition
                                            )
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
                    layoutBinding.progressBar.visible(isVisible = false)
                    sheet.dismiss()
                }

            }
        } else {
            sheet.dismiss()
        }
    }

    private fun String.showAlert(message: String, clickedPosition: Int) {
        showCustomAlert(message, "Ok", object : DialogClickHandler<Any> {
            override fun onClick(value: Any) {
                clickedPosition.removeFromPosition(this@showAlert)
            }
        })
    }

    private fun Int.removeFromPosition(userId: String) {
        if (this < recommendationViewModel.nearYouList.size)
            recommendationViewModel.nearYouList.removeAt(this)
        /* if (recommendationViewModel.nearYouUsersList.containsKey(userId))
             recommendationViewModel.nearYouUsersList.remove(userId)
 */
        adapter?.notifyItemRemoved(this)
        binding.llError.clErrorParent.visible(recommendationViewModel.nearYouList.isEmpty())
    }

    /**
     * KEYS :--
     * type, age,gender,name, venue_name, user_location (lat,long), location_city, location_state, venue_location (lat,long)
     * dob, username
     * */
    private fun logSendLikeEvent(data: ProfileData, type: Boolean, senderMessage: String?) {
        mixPanelWrapper.logSendLikeEvent(data, JSONObject().apply {
            put(PropertiesKey.LIKE_TYPE, Constants.MixPanelFrom.NEAR_YOU)
            put(PropertiesKey.CATEGORY, Constants.ONLINE)
            put(PropertiesKey.VENUE_NAME, Constants.MixPanelFrom.NA)
            put(
                PropertiesKey.VENUE_LOCATION,
                Constants.MixPanelFrom.NA
            )
            put(
                PropertiesKey.ACTION,
                if (type) if (senderMessage?.trim().isNullOrEmpty())
                    Constants.MixPanelFrom.ACTION_LIKE else Constants.MixPanelFrom.ACTION_LIKE_MESSAGE
                else Constants.MixPanelFrom.ACTION_DISLIKE
            )
        })
    }


    /*-------------------------------------------------------------------------------------------------------*/
    /*--------------------------------- #endregion Private Methods ------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------*/


}