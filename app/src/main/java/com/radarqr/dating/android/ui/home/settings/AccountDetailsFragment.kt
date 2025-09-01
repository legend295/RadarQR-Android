package com.radarqr.dating.android.ui.home.settings

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quickblox.auth.session.QBSettings
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.sample.chat.kotlin.utils.qb.QbDialogHolder
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.constant.Constants.ORDER_PROFILE_QR
import com.radarqr.dating.android.constant.Constants.SUPPORT_EMAIL
import com.radarqr.dating.android.data.model.accountdetails.AccountDetailsResponse
import com.radarqr.dating.android.databinding.FragmentAccountDetailsBinding
import com.radarqr.dating.android.databinding.LayoutDeleteAccountBinding
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerNotificationManager
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerViewModel
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.subscription.SubscriptionWrapper
import com.radarqr.dating.android.ui.home.likes.model.LikesViewModel
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.main.model.GetRecommendationViewModel
import com.radarqr.dating.android.ui.home.quickBlox.ChatViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.mobileLogin.AccountSettingsRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.DeleteAccount
import com.radarqr.dating.android.ui.welcome.mobileLogin.SendOtpViewModel
import com.radarqr.dating.android.ui.welcome.registerScreens.ImageUploadViewModel
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.QuickBloxManager
import com.radarqr.dating.android.utility.SharedPrefsHelper
import com.radarqr.dating.android.utility.Utility.checkInternet
import com.radarqr.dating.android.utility.Utility.openEmail
import com.radarqr.dating.android.utility.Utility.openLink
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.chat.ChatHelper
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class AccountDetailsFragment : BaseFragment<FragmentAccountDetailsBinding>(), ViewClickHandler {

    private val preferencesHelper: PreferencesHelper by inject()
    private val chatViewModel: ChatViewModel by viewModel()
    private val likesViewModel: LikesViewModel by viewModel()
    private val sendOtpViewModel: SendOtpViewModel by viewModel()
    private val recommendationViewModel: GetRecommendationViewModel by viewModel()
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val roamingTimerViewModel: RoamingTimerViewModel by viewModel()
    private val roamingTimerNotificationManager: RoamingTimerNotificationManager by inject()
    private val imageViewModel: ImageUploadViewModel by viewModel()
    private val mixPanelWrapper: MixPanelWrapper by inject()
//    private val quickBloxManager: QuickBloxManager by inject()

    var quickBloxId = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clickHandler = this
        binding.viewModel = getProfileViewModel
        showNavigation(false)
        showToolbarLayout(false)
        /* Glide.with(binding.root)
             .load(arguments?.getString(Constants.EXTRA_DATA) ?: R.drawable.unpaused_user_profile)
             .apply(RequestOptions.circleCropTransform())
             .into(binding.includePauseProfile.ivUserImage)*/

        /* binding.ivBack.setOnClickListener {
             if (binding.progressBarMedium.isVisible) return@setOnClickListener
             showNavigation(true)
             findNavController().navigateUp()
         }*/

        handleActiveSubscription()

        runBlocking {
            quickBloxId =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_QUICK_BLOX_ID)
                    .first()
                    ?: ""
        }

        setClickListener()
        init()
    }

    private fun handleActiveSubscription() {
        if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.PLUS) {
            binding.tvSubscriptionStatus.text = getString(R.string.subscribed_to_radarqr_plus)
        } else {
            binding.tvSubscriptionStatus.text = getString(R.string.subscribe_to_radarqr_plus)
        }
    }

    override fun onResume() {
        super.onResume()
        getAccountDetails()
    }

    @SuppressLint("SetTextI18n")
    private fun init() {
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(
                requireContext().packageName, 0
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                binding.tvVersion.text = "${pInfo.versionName} (${pInfo.longVersionCode})"
            } else {
                binding.tvVersion.text = "${pInfo.versionName} (${pInfo.versionCode})"
            }
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }

//        PhoneNumberUtils.formatNumber(getProfileViewModel.data.value?.data?.phone_number?:"",)
    }

    override fun getLayoutRes(): Int = R.layout.fragment_account_details


    private fun getAccountDetails() {
        try {
            lifecycleScope.launch {
                getProfileViewModel.getAccountSettings().observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Success -> {
                            getProfileViewModel.data.value = it.data
                            SharedPrefsHelper.save(
                                Constants.IS_PROFILE_PAUSED,
                                it.data.data.pause_profile
                            )
                            if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS || HomeActivity.isPromo)
                                if (it.data.data.is_promo) {
                                    HomeActivity.isPromo = true
                                    RaddarApp.getInstance()
                                        .setSubscriptionStatus(SubscriptionStatus.PLUS)
                                    binding.llPromotion.visible(isVisible = true)
                                    handleActiveSubscription()
                                } else {
                                    RaddarApp.getInstance()
                                        .setSubscriptionStatus(SubscriptionStatus.NON_PLUS)
                                    HomeActivity.isPromo = false
                                    handleActiveSubscription()
                                }
                            binding.isPausedOrNot =
                                (getProfileViewModel.data.value
                                    ?: AccountDetailsResponse()).data.pause_profile
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


    private fun updateAccountSettings(request: AccountSettingsRequest, type: Int) {
        try {
            lifecycleScope.launch {
                getProfileViewModel.accountSettings(request).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {
                        }

                        is DataResult.Failure -> {
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/account-settings",
                                requireActivity().componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/account-settings Api Error"))
                        }

                        DataResult.Loading -> {
                        }

                        is DataResult.Success -> {
                            when (type) {
                                0 -> {
                                    binding.isPausedOrNot = binding.isPausedOrNot != true
                                    getProfileViewModel.data.value?.data?.pause_profile =
                                        request.pause_profile ?: false

                                    SharedPrefsHelper.save(
                                        Constants.IS_PROFILE_PAUSED,
                                        request.pause_profile ?: false
                                    )
                                }

                                1 -> {
                                    getProfileViewModel.data.value?.data?.email_notification =
                                        request.email_notification ?: false
                                }

                                2 -> {
                                    getProfileViewModel.data.value?.data?.push_notification =
                                        request.push_notification ?: false
                                }
                            }

                        }
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

    private fun setClickListener() {
        binding.scEmail.setOnClickListener {
            if (getProfileViewModel.data.value?.data?.email?.isEmpty() == true) {
                requireContext().showToast("Please add email to enable email notification")
                if (getProfileViewModel.data.value?.data?.email_notification == true) {
                    updateAccountSettings(
                        AccountSettingsRequest(email_notification = false),
                        1
                    )
                }
                binding.scEmail.isChecked = false
                return@setOnClickListener
            }
            val request = AccountSettingsRequest(
                email_notification = binding.scEmail.isChecked
            )
            updateAccountSettings(request, 1)
        }
        binding.scPauseProfile.setOnClickListener {
            binding.isToggleOn = true
        }
        binding.scPushNotification.setOnClickListener {
            val request = AccountSettingsRequest(
                push_notification = binding.scPushNotification.isChecked
            )
            updateAccountSettings(request, 2)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ll_email -> {
                val bundle = Bundle()
                bundle.putInt(Constants.TYPE, Constants.ACCOUNT_DETAILS_FRAGMENT)
                bundle.putString(
                    Constants.EXTRA_DATA,
                    ((getProfileViewModel.data.value ?: AccountDetailsResponse()).data.email)
                )
                findNavController().navigate(R.id.action_account_details_to_email_fragment, bundle)
            }

            R.id.ivBack -> {
                this.view?.findNavController()?.popBackStack()
            }

            R.id.clOrderProfileQrCode -> {
                requireActivity().openLink(ORDER_PROFILE_QR)
            }

            R.id.llFeedback -> {
                requireActivity().openEmail(SUPPORT_EMAIL)
            }

            else -> {
                requireContext().checkInternet {
                    when (view.id) {
                        R.id.ll_phone_number -> {
                            val bundle = Bundle()
                            bundle.putInt(Constants.TYPE, Constants.ACCOUNT_DETAILS_FRAGMENT)
                            bundle.putString(
                                Constants.EXTRA_DATA,
                                ((getProfileViewModel.data.value
                                    ?: AccountDetailsResponse()).data.phone_number)
                            )
                            findNavController().navigate(
                                R.id.action_account_details_to_mobile_fragment,
                                bundle
                            )

                        }

                        R.id.llRestoreSubscription -> {
                            binding.progressBarRestore.show()
                            SubscriptionWrapper.restoreSubscription {
                                binding.progressBarRestore.hide()
                                if (it.activeSubscriptions.isEmpty()) {
                                    requireContext().showToast("No active subscription found.")
                                } else {
                                    mixPanelWrapper.logEvent(MixPanelWrapper.SUBSCRIPTION_RESTORE,
                                        JSONObject().apply {
                                            put(
                                                MixPanelWrapper.PropertiesKey.SUBSCRIPTION_ID,
                                                it.activeSubscriptions.first()
                                            )
                                        }
                                    )
                                    requireContext().showToast("Subscription restored.")
                                    handleActiveSubscription()
                                }
                            }
                        }

                        R.id.llBuySubscription -> {
                            this.view?.findNavController()?.navigate(
                                R.id.subscriptionFragment,
                                Bundle().apply {
                                    putString(
                                        Constants.FROM,
                                        Constants.MixPanelFrom.ACCOUNT
                                    )
                                })
                        }

                        R.id.ll_privacy_policy -> {
                            val bundle = Bundle()
                            bundle.putString(Constants.TYPE, Constants.PRIVACY_POLICY)
                            openWebViewActivity(bundle)
                        }

                        R.id.ll_term_of_services -> {
                            val bundle = Bundle()
                            bundle.putString(Constants.TYPE, Constants.TERMS_OF_SERVICES)
                            openWebViewActivity(bundle)

                        }

                        R.id.ll_privacy_preferences -> {
                            val bundle = Bundle()
                            bundle.putString(Constants.TYPE, Constants.RADAR_WEBSITE)
                            openWebViewActivity(bundle)

                        }

                        R.id.ll_download_my_data -> {
                            val bundle = Bundle()
                            bundle.putString(Constants.TYPE, "download_my_data")
                            openWebViewActivity(bundle)
                        }

                        R.id.tv_pause_unpause_profile -> {
                            val request = AccountSettingsRequest(
                                pause_profile = binding.scPauseProfile.isChecked
                            )
                            updateAccountSettings(request, 0)
                            binding.isToggleOn = false
                        }

                        R.id.tv_delete_account -> {
                            showDeleteDialog()
                        }
                    }
                }
            }
        }
    }

    private fun showDeleteDialog() {
        val dialog = Dialog(requireContext(), R.style.AlertStyle)
        val deleteBinding: LayoutDeleteAccountBinding = DataBindingUtil.inflate(
            LayoutInflater.from(requireContext()),
            R.layout.layout_delete_account,
            null,
            false
        )
        deleteBinding.tvConfirm.setOnClickListener {
            binding.tvDeleteAccount.isEnabled = false
            dialog.cancel()
            if (BaseUtils.isInternetAvailable()) {
                binding.progressBarMedium.visibility = View.VISIBLE
                deleteDialogs()
            } else requireActivity().showToast(resources.getString(R.string.no_internet_msg))

        }
        deleteBinding.tvNowNow.setOnClickListener {
            dialog.cancel()
        }
        dialog.setContentView(deleteBinding.root)
        dialog.setCancelable(false)
        dialog.show()
    }

    private fun deleteDialogs() {
        // variable to store all dialogs ids
//        val dialogIds = StringifyArrayList<String>()
        // get all chat dialogs from quickblox
        /* QuickBloxManager.getAllChatDialogs { arrayList, _ ->
             arrayList.forEach { dialog ->
                 // store the dialog id in list
                 dialogIds.add(dialog?.dialogId)
             }
             // delete all the dialogs using dialog ids
             if (dialogIds.isNotEmpty())
                 QuickBloxManager.deleteAllDialog(dialogIds) {
                     // Delete user from backend
                     deleteUser()
                 }
             else {
                 QuickBloxManager.deleteSession {
                     deleteUser()
                 }
             }
         }*/

        // delete quickblox session
        QuickBloxManager.logout {
            QuickBloxManager.deleteSession {
                deleteUser()
            }

        }
    }

    private fun deleteUser() {
        activity?.let { activity ->
            activity.lifecycleScope.launch {
                getProfileViewModel.deleteUser(
                    DeleteAccount(
                        account_deleted = true,
                        device_token = SharedPrefsHelper[Constants.DEVICE_TOKEN, ""]
                    )
                ).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {
                        }

                        is DataResult.Failure -> {
                            it.message?.let { it1 -> requireContext().showToast(it1) }
                            binding.tvDeleteAccount.isEnabled = true
                            binding.progressBarMedium.visibility = View.GONE
                        }

                        DataResult.Loading -> {
                        }

                        is DataResult.Success -> {
                            binding.progressBarMedium.visibility = View.GONE
                            clear()
                        }
                    }
                }
            }
        }
    }

    private fun clear() {
        SubscriptionWrapper.logOut()
        QBSettings.getInstance().isEnablePushNotification = false
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


    fun handleBack() {
        if (binding.isToggleOn == true) {
            binding.isToggleOn = false
            binding.scPauseProfile.isChecked = !binding.scPauseProfile.isChecked
        } else {
            if (!binding.progressBarMedium.isVisible)
                findNavController().popBackStack()
        }
//        findNavController().popBackStack()
    }

    private fun openWebViewActivity(data: Bundle?) {
        findNavController().navigate(R.id.action_account_details_to_web_view_fragment, data)
    }

}