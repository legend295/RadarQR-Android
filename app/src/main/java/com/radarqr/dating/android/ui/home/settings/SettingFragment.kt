package com.radarqr.dating.android.ui.home.settings

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quickblox.auth.QBAuth
import com.quickblox.auth.session.QBSettings
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.users.QBUsers
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.base.HomeBaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.constant.Constants.SWAG_SHOP_URL
import com.radarqr.dating.android.databinding.FragmentSettingBinding
import com.radarqr.dating.android.hotspots.closefriend.CloseFriendAndRequestViewModel
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerNotificationManager
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerViewModel
import com.radarqr.dating.android.hotspots.venue.VenuePromotionFragment
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.subscription.SubscriptionWrapper
import com.radarqr.dating.android.subscription.SubscriptionWrapper.loginRCAppUserId
import com.radarqr.dating.android.ui.home.likes.model.LikesViewModel
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.main.model.GetRecommendationViewModel
import com.radarqr.dating.android.ui.home.quickBlox.ChatViewModel
import com.radarqr.dating.android.ui.home.settings.model.SettingsOptionData
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.mobileLogin.SendOtpViewModel
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.updateTokenRequest
import com.radarqr.dating.android.ui.welcome.registerScreens.ImageUploadViewModel
import com.radarqr.dating.android.utility.*
import com.radarqr.dating.android.utility.BaseUtils.getLatLngFromProfile
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.openLink
import com.radarqr.dating.android.utility.Utility.share
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.toPx
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.introduction.IntroductionScreenType
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.StringBuilder


class SettingFragment : HomeBaseFragment<FragmentSettingBinding>(), View.OnClickListener,
    ViewClickHandler {

    private val preferencesHelper: PreferencesHelper by inject()
    private val getProfileViewModel: GetProfileViewModel by viewModel()

    //    private val quickBloxManager: QuickBloxManager by inject()
    private val chatViewModel: ChatViewModel by viewModel()
    private val likesViewModel: LikesViewModel by viewModel()
    private val imageViewModel: ImageUploadViewModel by viewModel()
    private val sendOtpViewModel: SendOtpViewModel by viewModel()
    private val recommendationViewModel: GetRecommendationViewModel by viewModel()
    private val closeFriendAndRequestViewModel: CloseFriendAndRequestViewModel by viewModel()
    private val roamingTimerViewModel: RoamingTimerViewModel by viewModel()
    private val roamingTimerNotificationManager: RoamingTimerNotificationManager by inject()
    private val mixPanelWrapper: MixPanelWrapper by inject()

    private var quickBloxId = ""
    var userId = ""
    private var imageKey = ""
    var image = ""

    private val settingsOptionsList = listOf(
        "Tips & Tricks",
        "Preferences",
        "Account",
//        "Restore Subscription",
        "Manage Venues",
        "Close Friends",
        "Logout"
    )

    private val settingsOptionsDrawableList = listOf(
        R.drawable.ic_help,
        R.drawable.ic_preference,
        R.drawable.ic_setting,
//        R.drawable.ic_setting,
        R.drawable.ic_fire_icon,
        R.drawable.ic_close_friend_icon_settings,
        R.drawable.ic_logout,
    )

    private val settingsOptionsDrawableTye = listOf(
        SettingsOptionData.SettingsTYpe.TIPS_TRICKS,
        SettingsOptionData.SettingsTYpe.PREFERENCES,
        SettingsOptionData.SettingsTYpe.ACCOUNT,
//        SettingsOptionData.SettingsTYpe.RESTORE_SUBSCRIPTION,
        SettingsOptionData.SettingsTYpe.VENUES,
        SettingsOptionData.SettingsTYpe.CLOSE_FRIENDS,
        SettingsOptionData.SettingsTYpe.LOGOUT,
    )

    private val adapter by lazy { SettingsOptionsAdapters(getProfileViewModel.settingsList) }


    /*-------------------------------------------------------------------------------------------------------*/
    /*-------------------------------- #region Fragment Methods ---------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------*/

    override fun getLayoutRes(): Int = R.layout.fragment_setting

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (getProfileViewModel.settingsList.isEmpty())
            settingsOptionsList.forEachIndexed { index, s ->
                getProfileViewModel.settingsList.add(
                    SettingsOptionData(
                        s,
                        index, 0,
                        ContextCompat.getDrawable(
                            requireContext(),
                            settingsOptionsDrawableList[index]
                        ),
                        settingsOptionsDrawableTye[index]
                    )
                )
            }


    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        setSettingsOptions()
        binding.settingFragment = this
        showToolbarLayout(false)
        showNavigation(true)
        if (!HomeActivity.isPromo)
            lifecycleScope.launch {
                val subscriptionUserId =
                    preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.SUBSCRIPTION_USER_ID)
                        .first()
                subscriptionUserId.loginRCAppUserId {
                    binding.currentSubscription = RaddarApp.getSubscriptionStatus()
                }
            }
        else {
            binding.currentSubscription = RaddarApp.getSubscriptionStatus()
        }

        getCloseFriendInvitations()
        binding.viewHandler = this

        initializeObserver()

        /* binding.reload.setOnClickListener {
             if (BaseUtils.isInternetAvailable()) {
                 binding.reload.visibility = View.GONE
                 binding.progressBar.visibility = View.VISIBLE
                 getData()
             } else CommonCode.setToast(
                 requireContext(),
                 resources.getString(R.string.no_internet_msg)
             )
         }
 */
        getData()

        runBlocking {
            quickBloxId =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_QUICK_BLOX_ID)
                    .first()
                    ?: ""
            imageKey =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_Image).first()
                    ?: ""
            image =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_IMAGE_URL).first()
                    ?: ""

            userId =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_USERID).first()
                    ?: ""

            binding.tvName.text =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_FIRSTNAME).first()
                    ?: ""

            binding.profileProgress.progress =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_PERCENTAGE).first()
                    ?: 0

            binding.tvProfileComplete.text =
                (preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_PERCENTAGE)
                    .first() ?: "0").toString() + "% complete"

        }
        Handler(Looper.getMainLooper()).postDelayed({
            handleIntroductoryUI()
        }, 50)
    }


    /*-------------------------------------------------------------------------------------------------------*/
    /*-------------------------------- #endregion Fragment Methods ------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------*/


    /*-------------------------------------------------------------------------------------------------------*/
    /*-------------------------------- #region Private Methods ---------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------*/

    private fun handleIntroductoryUI() {
        val isShown = SharedPrefsHelper.get(
            Constants.IntroductionConstants.SETTINGS,
            defValue = false
        )

        (activity as HomeActivity?)?.introductionHandler?.showIntroductoryUI(
            IntroductionScreenType.SETTINGS,
            hasShown = isShown,
            Pair(
                binding.ivSwagBackground.x + binding.ivSwagBackground.width / 2 - (30).toPx,
                binding.ivSwagBackground.y + binding.ivSwagBackground.height / 2 + (30).toPx
            )
        ) {
            SharedPrefsHelper.save(Constants.IntroductionConstants.SETTINGS, true)
            if (it) {
                requireActivity().openLink(SWAG_SHOP_URL)
            }
        }

    }

    private fun setSettingsOptions() {
        binding.rvSettings.adapter = adapter

        adapter.clickHandler = { data, position ->
            if (!BaseUtils.isInternetAvailable()) {
                CommonCode.setToast(
                    requireContext(),
                    resources.getString(R.string.no_internet_msg)
                )

            } else {
                when (data.type) {
                    SettingsOptionData.SettingsTYpe.TIPS_TRICKS -> {
                        val bundle = Bundle()
                        bundle.putString(Constants.TYPE, Constants.HELP_CENTER)
                        this.view?.findNavController()?.navigate(
                            R.id.action_account_details_to_web_view_fragment,
                            bundle
                        )
                    }

                    SettingsOptionData.SettingsTYpe.PREFERENCES -> {
                        this.view?.findNavController()
                            ?.navigate(R.id.action_setting_to_preferenceFragment)
                    }

                    SettingsOptionData.SettingsTYpe.ACCOUNT -> {
                        val bundle = Bundle()
                        bundle.putString(Constants.EXTRA_DATA, image)
                        this.view?.findNavController()
                            ?.navigate(R.id.action_setting_to_accountDetailsFragment, bundle)
                    }

                    SettingsOptionData.SettingsTYpe.VENUES -> {
//                            this.view?.findNavController()?.navigate(R.id.yourVenueFragment)
                        if (getProfileViewModel.profileData.value?.venue_subscription?.subscription_status?.status == VenuePromotionFragment.ACTIVE) {
                            this.view?.findNavController()?.navigate(R.id.yourVenueFragment)
                        } else {
                            val bundle = Bundle().apply {
                                putSerializable(
                                    Constants.EXTRA,
                                    getProfileViewModel.profileData.value?.venue_subscription
                                )
                            }
                            this.view?.findNavController()
                                ?.navigate(R.id.action_setting_to_venuePromotionFragment, bundle)
                        }
                    }

                    SettingsOptionData.SettingsTYpe.CLOSE_FRIENDS -> {
                        this.view?.findNavController()?.navigate(R.id.closeFriendAndRequestFragment)
                    }

                    SettingsOptionData.SettingsTYpe.LOGOUT -> {
                        showAlertConfirmationDialog(
                            "Are you sure you want to logout?",
                            yesDismissClick
                        )
                    }

//                    SettingsOptionData.SettingsTYpe.RESTORE_SUBSCRIPTION -> {
//                        SubscriptionWrapper.restoreSubscription()
//                    }
                }
            }
        }
    }


    private fun getData() {
        /*when (getProfileViewModel.profileData.value) {
            null -> {

            }
        }*/
        try {
            getBaseActivity()?.getProfile(getProfileRequest()) { data, _ ->
                if (isAdded)
                    data?.let {
                        /*it.replaceProfileImagesWithUrl(requireContext()) { data ->
                            getProfileViewModel.profileData.value = data
                        }*/
                        recommendationViewModel.adsShowMax = it.ads_interval ?: Constants.ADS_COUNT
                        if (HomeActivity.isPromo) {
                            binding.currentSubscription = RaddarApp.getSubscriptionStatus()
                        } else
                            it.subscription_user_id.loginRCAppUserId {
                                binding.currentSubscription = RaddarApp.getSubscriptionStatus()
                            }
                        getProfileViewModel.profileData.value = it
                        HomeActivity.userProfileLocation = it.getLatLngFromProfile()
                    }
            }
            if (!BaseUtils.isInternetAvailable())
                loadImage()
        } catch (e: java.lang.Exception) {

        }
    }

    @SuppressLint("SetTextI18n")
    private fun initializeObserver() {
        getProfileViewModel.profileData.observe(viewLifecycleOwner) {
            it.images?.apply {
                /*getProfileViewModel.storeImages(
                    it.images,
                    requireContext()
                )*/

                if (it.images.isNotEmpty()) {
//                    image = getProfileViewModel.userImages[it.images[0]] ?: ""
                    image = it.images[0]
                    loadImage()
                }

                runBlocking {
                    preferencesHelper.saveImage(
                        image,
                        it.images[0]
                    )
                }
            }

            runBlocking(Dispatchers.IO) {
                preferencesHelper.saveUserData(it)

            }


            userId = it._id!!
            binding.tvProfileComplete.text =
                it.profileCompletness.toString() + "% complete"
            binding.tvName.text = it.name ?: ""
            binding.profileProgress.progress = it.profileCompletness!!
        }
    }

    private fun loadImage() {
        binding.ivUser.loadImage(image, isThumb = true)
        (activity as HomeActivity?)?.setUserImage(image)
    }

    private val yesDismissClick = { _: DialogInterface, _: Int ->
        if (view != null && isVisible && isAdded)
            BaseUtils.showProgressbar(requireContext())
        QuickBloxManager.logout {
            QuickBloxManager.deleteSession {
                logOut()
            }

        }

    }


    private fun logOut() {
        if (view != null && isVisible && isAdded)
            lifecycleScope.launch {
                getProfileViewModel.Logout(updateTokenRequest(device_token = SharedPrefsHelper[Constants.DEVICE_TOKEN, ""]))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {

                            }

                            is DataResult.Success -> {
                                BaseUtils.hideProgressbar()
                                /*if (quickBloxId.isEmpty()) {
                                    clear()
                                } else {
                                    if (SharedPrefsHelper.hasQbUser())
                                        userLogout()
                                    else clear()
                                }*/
                                clear()

                            }

                            is DataResult.Failure -> {
                                BaseUtils.hideProgressbar()
                                if ((it.statusCode == 404 && it.message == "User doesn't exist.") || it.statusCode == 401) clear()
                                else
                                    it.message?.let { it1 -> requireContext().showToast(it1) }
                                reportApiError(
                                    Exception().stackTrace[0].lineNumber,
                                    it.statusCode ?: 0,
                                    "user/logout",
                                    requireActivity().componentName.className,
                                    it.message ?: ""
                                )

                                FirebaseCrashlytics.getInstance()
                                    .recordException(Exception("user/logout Api Error"))

//                            clear()
                            }

                            DataResult.Empty -> {}
                        }
                    }

            }
    }

    private fun userLogout() {
        Log.d("TAG", "SignOut")
        BaseUtils.showProgressbar(requireContext())
        QBUsers.signOut().performAsync(object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                clear()
            }

            override fun onError(e: QBResponseException?) {
                reportApiError(
                    Exception().stackTrace[0].lineNumber,
                    e?.httpStatusCode ?: 0,
                    "QBUsers.signOut()",
                    requireActivity().componentName.className,
                    e?.localizedMessage ?: ""
                )

                FirebaseCrashlytics.getInstance()
                    .recordException(e ?: java.lang.Exception("Quickblox logout exception"))
                clear()
            }
        })
    }

    private fun deleteSession() {
        QBAuth.deleteSession().performAsync(object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {

            }

            override fun onError(e: QBResponseException?) {

            }
        })
    }

    /**
     * Add same logics while account deleting in AccountDetailsFragment
     * */
    private fun clear() {
        QBSettings.getInstance().isEnablePushNotification = false
        SubscriptionWrapper.logOut()
        roamingTimerNotificationManager.cancelRoamingTimerNotification()
//        deleteSession()
        runBlocking {
            preferencesHelper.clearAllPreferences()
        }
        SharedPrefsHelper.delete(Constants.DEVICE_TOKEN)
        SharedPrefsHelper.removeQbUser()
        SharedPrefsHelper.clearSession()
//        QbDialogHolder.clear()

        imageViewModel.clearEverything()
        chatViewModel.clearEverything()
        likesViewModel.clearEverything()
        recommendationViewModel.clear()
        sendOtpViewModel.clear()
//        getProfileViewModel.imageUrlHashMap.clear()
        getProfileViewModel.clearEverything()
        roamingTimerViewModel.clear()
        mixPanelWrapper.resetProperties()
        QuickBloxManager.qbChatService.destroy()
        BaseUtils.hideProgressbar()
        val intent = Intent(requireActivity(), InitialActivity::class.java)
        startActivity(intent)
        requireActivity().finishAffinity()
    }

    /*-------------------------------------------------------------------------------------------------------*/
    /*--------------------------------- #endregion Private Methods ------------------------------------------*/
    /*-------------------------------------------------------------------------------------------------------*/

    override fun onClick(view: View) {
        when (view.id) {
            R.id.cvBarCode -> {
                this.findNavController().navigate(R.id.showQR_fragment)
            }

            R.id.ivEdit, R.id.cvParent -> {
                try {

                    this.view?.findNavController()
                        ?.navigate(R.id.action_setting_to_editProfileFragment)
                } catch (ignore: Exception) {

                }
            }

            R.id.tvShareProfile -> {
                requireActivity().share(
                    "Let's connect on RadarQR!\n" +
                            "The below message will navigate you to my profile. Check me out! ${
                                RaddarApp.getEnvironment().getShareUrl()
                            }" + userId
                )
                /*val intent = Intent(Intent.ACTION_SEND)
                val shareBody = "Let's connect on RadarQR!\n" +
                        "Here is my profile link: ${
                            RaddarApp.getEnvironment().getShareUrl()
                        }" + userId
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(
                    Intent.createChooser(
                        intent,
                        "Share Profile"
                    )
                )*/

            }
            /*

                        R.id.tvLogOut -> {

                        }

                        R.id.tvAccount -> {

                        }
            */

            /*R.id.tvPreferences -> {
                this.findNavController().navigate(R.id.action_setting_to_preferenceFragment)

            }*/

            /*  R.id.tvTipsAndTricks -> {

              }
  */
            /* R.id.tvHotspotsVenueAccount -> {
                 this.view?.findNavController()?.navigate(R.id.yourVenueFragment)
             }*/

            R.id.ivSwagBackground -> {
                requireActivity().openLink(SWAG_SHOP_URL)
            }

            /*   R.id.tvCloseFriends -> {
                   this.view?.findNavController()?.navigate(R.id.closeFriendAndRequestFragment)
               }*/

            R.id.ivSubscriptionPlaceholder, R.id.tvSubscriptionNotActiveMsg, R.id.tvSubscriptionActiveMsg -> {
                this.view?.findNavController()?.navigate(
                    R.id.subscriptionFragment,
                    Bundle().apply { putString(Constants.FROM, Constants.MixPanelFrom.SETTINGS) })
            }
        }
    }

    private fun getCloseFriendInvitations() {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.getCloseFriendInvitations(1, 1)
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                (0).setRequestCount()
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                (it.data.data?.total_count ?: 0).setRequestCount()
                            }
                        }
                    }
            }
        }
    }

    private fun Int.setRequestCount() {
        try {
            getProfileViewModel.settingsList[4].endValue = this
            adapter.notifyItemChanged(4)
        } catch (ignore: Exception) {

        }
//        if (this > 0) {
//            binding.tvRequestsCount.visible(isVisible = true)
//            binding.tvRequestsCount.text = this.toString()
//        } else binding.tvRequestsCount.visible(isVisible = false)
    }

}
