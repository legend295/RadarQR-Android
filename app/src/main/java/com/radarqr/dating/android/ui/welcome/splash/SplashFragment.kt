package com.radarqr.dating.android.ui.welcome.splash

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.quickblox.auth.QBAuth
import com.quickblox.auth.session.QBSettings
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.chat.kotlin.utils.qb.QbDialogHolder
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentSplashBinding
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerNotificationManager
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerViewModel
import com.radarqr.dating.android.ui.home.likes.model.LikesViewModel
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.main.model.GetRecommendationViewModel
import com.radarqr.dating.android.ui.home.quickBlox.ChatViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.mobileLogin.SendOtpViewModel
import com.radarqr.dating.android.ui.welcome.registerScreens.ImageUploadViewModel
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.QuickBloxManager
import com.radarqr.dating.android.utility.SharedPrefsHelper
import com.radarqr.dating.android.utility.chat.ChatHelper
import com.radarqr.dating.android.utility.environment.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class SplashFragment : BaseFragment<FragmentSplashBinding>() {
    var user_id = ""
    var StepProgress = 0
    var quickBloxId = ""
    private val preferencesHelper: PreferencesHelper by inject()
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val chatViewModel: ChatViewModel by viewModel()
    private val likesViewModel: LikesViewModel by viewModel()
    private val roamingTimerNotificationManager: RoamingTimerNotificationManager by inject()
    private val imageViewModel: ImageUploadViewModel by viewModel()
    private val recommendationViewModel: GetRecommendationViewModel by viewModel()
    private val sendOtpViewModel: SendOtpViewModel by viewModel()
    private val roamingTimerViewModel: RoamingTimerViewModel by viewModel()

    private val isTesting: Boolean = false // set false if not testing

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val mainIntent = requireActivity().intent
        val type = mainIntent.getIntExtra(Constants.TYPE, 0)
        /**
         * Checking user is deactivated from admin after report
         * so handling the deactivated user here
         * and logging out user from quickblox so that he/she won't be able to get notification

         */
        if (type == Constants.REPORT) {
            runBlocking {
                quickBloxId =
                    preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_QUICK_BLOX_ID)
                        .first()
                        ?: ""
            }
            QBSettings.getInstance().isEnablePushNotification =
                false // Disabling push so that after logout user won't get notifications
            if (quickBloxId.isEmpty()) {
                clear()
            } else {
                if (SharedPrefsHelper.hasQbUser()) {
                    deleteSession()
                    clear()
                } else clear()
            }
        }

        QuickBloxManager.qbChatService.destroy()
        if (QuickBloxManager.qbSessionManager.activeSession != null) {
            QuickBloxManager.deleteSession {

            }
        }

        runBlocking(Dispatchers.IO) {
            try {
                user_id =
                    preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_AUTH).first()
                        ?: ""
                StepProgress =
                    preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.STEP_PROGRESS)
                        .first() ?: 0

            } catch (e: Exception) {

            }
        }


        Log.d("NOTIFICATION_DATA", mainIntent.getStringExtra(Constants.TYPE).toString())
        Log.d("NOTIFICATION_DATA", mainIntent.getStringExtra(Constants.USER_ID).toString())
        Log.d("NOTIFICATION_DATA", mainIntent.getStringExtra(Constants.CATEGORY).toString())
        Log.d("NOTIFICATION_DATA", mainIntent.getStringExtra(Constants.DIALOG_ID).toString())
        if (mainIntent != null) {
            if (mainIntent.data != null && mainIntent.data!!.scheme.equals("https")) {
                val data: Uri? = mainIntent.data
                val pathSegments = data.toString()
                val userArray = pathSegments.split("/")
                val userId = try {
                    userArray[4]
                } catch (e: IndexOutOfBoundsException) {
                    try {
                        userArray[3]
                    } catch (e: Exception) {
                        ""
                    }
                }
                if (userId == "") {
                    goToLogin()
                } else {
                    SharedPrefsHelper.save(Constants.IS_DEEP_LINK, true)
                    SharedPrefsHelper.save(Constants.SHARED_USER_ID, userId)
                    if (user_id == "") {
                        val bundle = Bundle()
                        bundle.putString(Constants.USER_ID, userId)
                        bundle.putString(Constants.TAG, "1")

                        this.findNavController()
                            .navigate(R.id.action_splashFragment_to_goThroughFragment, bundle)
                    } else {
                        val intent = Intent(requireActivity(), HomeActivity::class.java)
                        intent.putExtra("show_image_popup",true)
                        intent.putExtra("user_id", userId)
                        intent.putExtra("tag", "1")
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            } else {
                goToLogin()
            }
        } else {
            goToLogin()
        }

        requireContext().externalCacheDir?.deleteRecursively()
        requireActivity().cacheDir.deleteRecursively()
        getFacebookHashKey()
        return inflater.inflate(R.layout.fragment_splash, container, false)

    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            (it as InitialActivity).hideShowWholeToolbar(isVisible = false)
        }
    }

    private fun getFacebookHashKey() {
        val info: PackageInfo
        try {
            info =
                requireActivity().packageManager.getPackageInfo(
                    requireContext().packageName,
                    PackageManager.GET_SIGNATURES
                )
            for (signature in info.signatures) {
                var md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val something = String(Base64.encode(md.digest(), 0))
//String something = new Strixng(Base64.encodeBytes(md.digest()));
                Log.e("facebook_hash", "hash key $something")
            }
        } catch (e1: PackageManager.NameNotFoundException) {
            Log.e("facebook_hash", "name not found$e1")
        } catch (e: NoSuchAlgorithmException) {
            Log.e("facebook_hash", "no such an algorithm $e")
        } catch (e: Exception) {
            Log.e("facebook_hash", "exception $e")
        }
    }

    private fun deleteSession() {
        QBAuth.deleteSession().performAsync(object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                clear()
            }

            override fun onError(e: QBResponseException?) {
                clear()
            }
        })
    }

    private fun clear() {
        chatViewModel.clearEverything()
        likesViewModel.clearEverything()
        getProfileViewModel.clearEverything()

        ChatHelper.destroy()
        QBSettings.getInstance().isEnablePushNotification = false
        roamingTimerNotificationManager.cancelRoamingTimerNotification()
        runBlocking {
            preferencesHelper.clearAllPreferences()
        }
        SharedPrefsHelper.delete(Constants.DEVICE_TOKEN)
        SharedPrefsHelper.removeQbUser()
        SharedPrefsHelper.clearSession()

        imageViewModel.clearEverything()
        chatViewModel.clearEverything()
        likesViewModel.clearEverything()
        recommendationViewModel.clear()
        sendOtpViewModel.clear()
        getProfileViewModel.clearEverything()
        roamingTimerViewModel.clear()

        QuickBloxManager.qbChatService.destroy()
    }


    private fun goToLogin() {
        // Permission Granted-System will work
        if (isTesting && (RaddarApp.getEnvironment() == Environment.PRODUCTION || RaddarApp.getEnvironment() == Environment.RELEASE)) {
            runBlocking {
                preferencesHelper.setValue(
                    key = PreferencesHelper.PreferencesKeys.KEY_AUTH,
                    value = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiNjNhMWQyNTY5YjYwMmEzYzY3ZjM1Y2JjIiwibG9naW5faWQiOiJjYmRjYTdmM2U5NmJhMjgiLCJ1c2VyX3R5cGUiOiJ1c2VyIiwiaWF0IjoxNjczNzIyODcxLCJleHAiOjEwMDE2NzM3MjI4NzF9.R3xyers3FCFkFlN_thRKyzeCyK0uFECNkvYnIJzsL7g"
                )

                preferencesHelper.setValue(
                    key = PreferencesHelper.PreferencesKeys.KEY_MOBILE,
                    value = "3053930792"
                )

                activity?.let {
                    val intent = Intent(requireActivity(), HomeActivity::class.java)
                    intent.putExtra("show_image_popup",true)
                    it.startActivity(intent)
                    it.finish()
                }
            }


        } else {
            try {

                SharedPrefsHelper.save(Constants.IS_DEEP_LINK, false)
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    if (SharedPrefsHelper[Constants.IS_USER_VERIFIED, false]) {
                        if (user_id != "") {
                            SharedPrefsHelper.save(Constants.IS_USER_VERIFIED, true)
                            when (StepProgress) {
                                0 -> {
                                    val data = Bundle()
                                    data.putString("tag", "1")
                                    this.view?.findNavController()
                                        ?.navigate(
                                            R.id.action_splash_to_firstName, data
                                        )
                                }

                                1 -> {
                                    this.findNavController()
                                        .navigate(
                                            R.id.action_splash_to_addPhotoFragment
                                        )
                                }

                                2 -> {
                                    val data = Bundle()
                                    data.putString("tag", "1")
                                    this.view?.findNavController()
                                        ?.navigate(
                                            R.id.action_splash_to_ageFragment, data
                                        )

                                }

                                3 -> {
                                    this.view?.findNavController()
                                        ?.navigate(
                                            R.id.action_splash_to_identityFragment
                                        )

                                }

                                4 -> {
                                    this.view?.findNavController()
                                        ?.navigate(
                                            R.id.action_splash_to_interestedInFragment
                                        )

                                }

                                else -> {
                                    activity?.let {
                                        val intent =
                                            Intent(requireActivity(), HomeActivity::class.java)
                                        intent.putExtra("show_image_popup",true)
                                        intent.putExtras(requireActivity().intent)
                                        it.startActivity(intent)
                                        it.finish()
                                    }
                                }
                            }

                        } else {
                            this.view?.findNavController()
                                ?.navigate(R.id.action_splashFragment_to_goThroughFragment)
                        }
                    } else {
                        this.view?.findNavController()
                            ?.navigate(R.id.action_splashFragment_to_goThroughFragment)

                    }


                }, 2000)
            } catch (e: Exception) {
                this.view?.findNavController()
                    ?.navigate(R.id.action_splashFragment_to_goThroughFragment)
            }
        }
    }


    override fun getLayoutRes(): Int = R.layout.fragment_splash

}
