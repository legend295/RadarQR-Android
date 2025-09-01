package com.radarqr.dating.android.ui.home.likenew

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.base.HomeBaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentLikeBinding
import com.radarqr.dating.android.databinding.LayoutNearYouBottomSheetBinding
import com.radarqr.dating.android.hotspots.helpers.showSubscriptionSheet
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.ui.home.likes.AllLikesFragment
import com.radarqr.dating.android.ui.home.likes.InPersonLikesFragment
import com.radarqr.dating.android.ui.home.likes.OnlineLikesFragment
import com.radarqr.dating.android.ui.home.likes.model.LikesViewModel
import com.radarqr.dating.android.ui.home.likes.model.UserLikes
import com.radarqr.dating.android.ui.home.quickBlox.ChatViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.LocationDetail
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.ui.welcome.mobileLogin.AcceptRequest
import com.radarqr.dating.android.utility.QuickBloxManager
import com.radarqr.dating.android.utility.Utility.openBottomSheet
import com.radarqr.dating.android.utility.Utility.setDimBackground
import com.radarqr.dating.android.utility.Utility.showItsAMatchDialog
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.enums.SubscriptionPopUpType
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class LikeFragment : HomeBaseFragment<FragmentLikeBinding>(), SwipeRefreshLayout.OnRefreshListener,
    ViewClickHandler {

    var likeType: LikeType = LikeType.ALL_LIKES

    var fragment: Fragment? = null
    var itsAMatchDialog: Dialog? = null
    private val likesViewModel: LikesViewModel by viewModel()
    private val chatViewModel: ChatViewModel by viewModel()
    private val mixPanelWrapper: MixPanelWrapper by inject()

    override fun getLayoutRes(): Int = R.layout.fragment_like

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        implementInterfaces()
        binding.clickHandler = this
        handleView(type = likeType)
        checkSubscription()
    }

    private fun checkSubscription() {
        if (this.view != null && !RaddarApp.getSubscriptionStatus().canViewLikes()) {
            showSubscriptionSheet(SubscriptionPopUpType.LIKE, popBackStack = false) {

            }
        }
    }

    private fun implementInterfaces() {
        likesViewModel.handler = object : LikesViewModel.Handler {
            override fun openBottomSheet(data: UserLikes, isSuccess: () -> Unit) {
                if (data.sender_message.isNullOrEmpty() && RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS) {
                    checkSubscription()
                } else
                    this@LikeFragment.openBottomSheet(data, isSuccess)
            }
        }
    }

    private fun handleView(type: LikeType) {
        this.likeType = type
        binding.type = type
        Handler(Looper.getMainLooper()).postDelayed({
            loadFragment()
        }, 100)
    }

    private fun loadFragment() {
        fragment = when (likeType) {
            LikeType.ALL_LIKES -> AllLikesFragment()
            LikeType.ONLINE -> OnlineLikesFragment()
            LikeType.IN_PERSON -> InPersonLikesFragment()
        }
        try {
            fragment?.let {
                val transaction = childFragmentManager.beginTransaction()
                transaction.replace(R.id.container, it)
                transaction.commit()
            }
        } catch (_: Exception) {
        }
    }

    override fun onRefresh() {

    }

    fun openBottomSheet(data: UserLikes, isSuccess: () -> Unit) {
        val sheet = BottomSheetDialog(requireContext(), R.style.DialogStyle)
        val layoutBinding =
            LayoutNearYouBottomSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)
        val profileData = ProfileData(
            _id = data.sender_id,
            name = data.user_detail?.name,
            gender = data.user_detail?.gender,
            profile_pic = data.user_detail?.profile_pic,
            location = data.user_detail?.location ?: LocationDetail()
        )
        layoutBinding.subscriptionStatus = RaddarApp.getSubscriptionStatus()
        layoutBinding.data = profileData
        sheet.dismissWithAnimation = true

        layoutBinding.tvProfileComment.visible(data.sender_message?.isNotEmpty() == true)
        layoutBinding.tvComment.visible(data.sender_message?.isNotEmpty() == true)
        layoutBinding.tvComment.text = data.sender_message

        layoutBinding.ivUser.setOnClickListener {
            layoutBinding.openProfile(sheet, data)
        }

        layoutBinding.tvName.setOnClickListener {
            layoutBinding.openProfile(sheet, data)
        }



        layoutBinding.ivChat.setOnClickListener {
            if (!layoutBinding.progressBar.isVisible) {
                sheet.cancel()
                if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS) {
                    sheet.dismiss()
                    showSubscriptionSheet(
                        SubscriptionPopUpType.LIKE,
                        popBackStack = false
                    ) {}
                } else {
                    requireContext().openBottomSheet(data) { msg, dialog, layout ->
                        if (!layout.progressBar.isVisible) {
                            dialog.setCanceledOnTouchOutside(false)
                            dialog.setCancelable(false)
                            layout.progressBar.visible(isVisible = true)
                            acceptReject(
                                AcceptRequest(
                                    sender_id = data.sender_id,
                                    receiver_option_choosed = true,
                                    receiver_response_message = msg.trim(),
                                    category = data.category
                                ), data
                            ) {
                                dialog.behavior.isHideable = true
                                dialog.setCanceledOnTouchOutside(true)
                                dialog.setCancelable(true)
                                layout.progressBar.visible(isVisible = false)
                                if (it) {
                                    dialog.dismiss()
                                    isSuccess()
                                }
                            }
                        }
                    }
                }
            }
        }

        layoutBinding.ivSendRequest.setOnClickListener {
            if (!layoutBinding.progressBar.isVisible) {
                if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS) {
                    sheet.dismiss()
                    showSubscriptionSheet(
                        SubscriptionPopUpType.LIKE,
                        popBackStack = false
                    ) {}
                } else {
                    sheet.setCancelable(false)
                    layoutBinding.progressBar.visible(isVisible = true)
                    acceptReject(
                        AcceptRequest(
                            sender_id = data.sender_id,
                            receiver_option_choosed = true,
                            category = data.category
                        ), data
                    ) {
                        layoutBinding.progressBar.visible(isVisible = false)
                        sheet.setCancelable(true)
                        if (it) {
                            sheet.cancel()
                            isSuccess()
                        }
                    }
                }
            }
        }

        layoutBinding.ivCancelRequest.setOnClickListener {
            if (!layoutBinding.progressBar.isVisible) {
                sheet.setCancelable(false)
                layoutBinding.progressBar.visible(isVisible = true)
                acceptReject(
                    AcceptRequest(
                        sender_id = data.sender_id,
                        receiver_option_choosed = false,
                        category = data.category
                    ), data
                ) {
                    layoutBinding.progressBar.visible(isVisible = false)
                    sheet.setCancelable(true)
                    if (it) {
                        sheet.cancel()
                        isSuccess()
                    }
                }
            }
        }

        sheet.setDimBackground()
        sheet.setContentView(layoutBinding.root)
        sheet.show()
    }

    private fun LayoutNearYouBottomSheetBinding.openProfile(
        sheet: BottomSheetDialog,
        data: UserLikes
    ) {
        if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS) {
            checkSubscription()
        } else {
            if (!progressBar.isVisible) {
                sheet.cancel()
                val bundle = Bundle().apply {
                    putSerializable(Constants.EXTRA, data)
                    putString(Constants.USER_ID, data.sender_id)
                    putInt(Constants.FROM, ProfileFragment.FROM_LIKE)
                    putBoolean(Constants.TYPE, true)
                }
                binding.root.findNavController()
                    .navigate(R.id.action_likeFragment_to_profileFragment, bundle)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvAllLikes ->
                if (likeType == LikeType.ALL_LIKES) return
                else likeType = LikeType.ALL_LIKES

            R.id.tvOnline ->
                if (likeType == LikeType.ONLINE) return
                else likeType = LikeType.ONLINE

            R.id.tvInPerson ->
                if (likeType == LikeType.IN_PERSON) return
                else likeType = LikeType.IN_PERSON
        }

        handleView(likeType)
    }

    private fun acceptReject(
        request: AcceptRequest,
        data: UserLikes,
        isSuccess: (Boolean) -> Unit
    ) {
        if (view != null && isAdded && isVisible)
            lifecycleScope.launch {
                likesViewModel.acceptRequest(request)
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {
                            }

                            is DataResult.Success -> {
                                logAcceptLikeEvent(data, request)
                                it.data.data.qb_dialog_id?.let { dialogId ->
                                    requireContext().showItsAMatchDialog(
                                        data.user_detail?.profile_pic,
                                        QuickBloxManager,
                                        dialogId
                                    ) { _, _, type ->
                                        if (type == 0) return@showItsAMatchDialog
                                        val bundle = Bundle().apply {
                                            putString(Constants.DIALOG_ID, dialogId)
                                            putString(Constants.FROM, "")
                                            putString(Constants.NAME, data.user_detail?.name)
                                            putString(
                                                Constants.PROFILE_PIC,
                                                data.user_detail?.profile_pic
                                            )
                                            putString(Constants.TYPE, Constants.FROM_HOME)
                                        }
                                        this@LikeFragment.view?.findNavController()
                                            ?.navigate(R.id.chat_fragment, bundle)
                                    }
                                }
                                likesViewModel.allLikesRequest.page = 1
                                likesViewModel.onlineLikeRequest.page = 1
                                likesViewModel.inPersonLikeRequest.page = 1
                                isSuccess(true)
                            }

                            is DataResult.Failure -> {
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
                        }
                    }

            }
    }

    enum class LikeType : LikeTypeInterface {
        ALL_LIKES {
            override fun value(): String = "all"
        },
        ONLINE {
            override fun value(): String = "online"
        },
        IN_PERSON {
            override fun value(): String = "inperson"
        }
    }

    interface LikeTypeInterface {
        fun value(): String
    }

    class SpotDiffCallback(
        private val old: ArrayList<UserLikes?>,
        private val new: List<UserLikes?>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return old.size
        }

        override fun getNewListSize(): Int {
            return new.size
        }

        override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            return old[oldPosition]?._id == new[newPosition]?._id
        }

        override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            return old[oldPosition] == new[newPosition]
        }

    }

    /**
     * KEYS :--
     * type, age,gender,name, venue_name, user_location (lat,long), location_city, location_state, venue_location (lat,long)
     * dob, username
     * */
    private fun logAcceptLikeEvent(data: UserLikes, request: AcceptRequest) {
        mixPanelWrapper.logAcceptLikeEvent(data, JSONObject().apply {
            put(MixPanelWrapper.PropertiesKey.LIKE_TYPE, Constants.MixPanelFrom.LIKE)
            put(
                MixPanelWrapper.PropertiesKey.ACTION,
                if (request.receiver_option_choosed == true)
                    if (request.receiver_response_message?.trim().isNullOrEmpty())
                        Constants.MixPanelFrom.ACTION_ACCEPT else Constants.MixPanelFrom.ACTION_ACCEPT_MESSAGE
                else Constants.MixPanelFrom.ACTION_REJECT
            )
        })
    }
}