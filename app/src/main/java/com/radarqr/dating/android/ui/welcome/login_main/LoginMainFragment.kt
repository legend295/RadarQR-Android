package com.radarqr.dating.android.ui.welcome.login_main

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.facebook.CallbackManager
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.quickblox.auth.session.QBSettings
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.SocialRequest
import com.radarqr.dating.android.databinding.FragmentLoginMainBinding
import com.radarqr.dating.android.hotspots.closefriend.CloseFriendAndRequestViewModel
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerNotificationManager
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerViewModel
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.ui.home.likes.model.LikesViewModel
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.main.model.GetRecommendationViewModel
import com.radarqr.dating.android.ui.home.quickBlox.ChatViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.mobileLogin.SendOtpViewModel
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.ui.welcome.otpVerify.VerifyOtpApiResponse
import com.radarqr.dating.android.ui.welcome.registerScreens.ImageUploadViewModel
import com.radarqr.dating.android.utility.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class LoginMainFragment : BaseFragment<FragmentLoginMainBinding>() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var facebookCallbackManager: CallbackManager
    private val sendOtpViewModel: SendOtpViewModel by viewModel()
    private val preferencesHelper: PreferencesHelper by inject()
    private val getProfileViewModel: GetProfileViewModel by viewModel()

    private val chatViewModel: ChatViewModel by viewModel()
    private val likesViewModel: LikesViewModel by viewModel()
    private val imageViewModel: ImageUploadViewModel by viewModel()
    private val recommendationViewModel: GetRecommendationViewModel by viewModel()
    private val roamingTimerViewModel: RoamingTimerViewModel by viewModel()
    private val roamingTimerNotificationManager: RoamingTimerNotificationManager by inject()
    val bundle = Bundle()
    var token = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (arguments == null) {
            arguments = Bundle()
        }

        binding.loginMainFragment = this
//        showToolbarLayout(isVisible = false)
        clear()
        binding.clMobile.setOnClickListener {
            SharedPrefsHelper.save(Constants.LOGIN_TYPE, Constants.PHONE)
//            arguments?.putString(Constants.TYPE, Constants.FROM_MOBILE)
            arguments?.putString(Constants.LOGIN_TYPE, Constants.FROM_MOBILE)
            getProfileViewModel.firstName.value = ""
            sendOtpViewModel.socialRequest = MutableLiveData(SocialRequest())
            Log.d("ARGUMENTS", "$arguments")
            this.findNavController()
                .navigate(R.id.action_loginMainFragment_to_mobileFragment, arguments)
        }

        initializeGoogleLogin()
        facebookCallbackManager = CallbackManager.Factory.create()
        binding.tvTerms.setOnClickListener {
            if (BaseUtils.isInternetAvailable()) {
                val bundle = Bundle()
                bundle.putString(Constants.TYPE, Constants.TERMS_OF_SERVICES)

                findNavController().navigate(
                    R.id.action_account_details_to_web_view_fragment,
                    bundle
                )
            } else CommonCode.setToast(
                requireContext(),
                resources.getString(R.string.no_internet_msg)
            )
        }

        /*binding.tvPolicy.setOnClickListener {
            if (BaseUtils.isInternetAvailable()) {
                val bundle = Bundle()
                bundle.putString(Constants.TYPE, Constants.PRIVACY_POLICY)

                findNavController().navigate(
                    R.id.action_account_details_to_web_view_fragment,
                    bundle
                )
            } else CommonCode.setToast(
                requireContext(),
                resources.getString(R.string.no_internet_msg)
            )
        }*/

        binding.clGoogle.setOnClickListener {
            if (BaseUtils.isInternetAvailable()) {
                SharedPrefsHelper.save(Constants.LOGIN_TYPE, Constants.GOOGLE)
                val signInIntent: Intent = googleSignInClient.signInIntent
                googleLauncher.launch(signInIntent)
            } else CommonCode.setToast(
                requireContext(),
                resources.getString(R.string.no_internet_msg)
            )
        }

        binding.clFacebook.setOnClickListener {
            if (BaseUtils.isInternetAvailable()) {
                try {
                    LoginManager.getInstance().logOut()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                SharedPrefsHelper.save(Constants.LOGIN_TYPE, Constants.FACEBOOK)
                activity?.let {
                    beginLoginToFacebook(it, facebookCallbackManager)
                }
            } else CommonCode.setToast(
                requireContext(),
                resources.getString(R.string.no_internet_msg)
            )
        }

        val text =
            "By signing up for RadarQR, you agree to our Terms & Conditions. Learn how we process your data in our Privacy Policy."
        val spannableString = SpannableString(text)

        val termsAndCondition = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (BaseUtils.isInternetAvailable()) {
                    val bundle = Bundle()
                    bundle.putString(Constants.TYPE, Constants.TERMS_OF_SERVICES)

                    findNavController().navigate(
                        R.id.action_account_details_to_web_view_fragment,
                        bundle
                    )
                } else CommonCode.setToast(
                    requireContext(),
                    resources.getString(R.string.no_internet_msg)
                )
            }

        }

        val policyClickable = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (BaseUtils.isInternetAvailable()) {
                    val bundle = Bundle()
                    bundle.putString(Constants.TYPE, Constants.PRIVACY_POLICY)

                    findNavController().navigate(
                        R.id.action_account_details_to_web_view_fragment,
                        bundle
                    )
                } else CommonCode.setToast(
                    requireContext(),
                    resources.getString(R.string.no_internet_msg)
                )
            }
        }

        //42
        spannableString.setSpan(termsAndCondition, 44, 62, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannableString.setSpan(policyClickable, 102, 116, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        binding.cbTermsAndPolicy.highlightColor = Color.TRANSPARENT
        binding.cbTermsAndPolicy.text = spannableString
        binding.cbTermsAndPolicy.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            (it as InitialActivity).hideShowWholeToolbar(isVisible = false)
            it.hideShowSkip(isVisible = false)
        }
    }

    private fun initializeGoogleLogin() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(resources.getString(R.string.web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        val account = GoogleSignIn.getLastSignedInAccount(requireContext())
        account?.let {
            googleSignInClient.signOut()
        }
    }

    @ExperimentalCoroutinesApi
    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                activity?.let {
                    lifecycleScope.launchWhenStarted {
                        try {
                            val data = sendOtpViewModel.handleSignInResult(
                                GoogleSignIn.getSignedInAccountFromIntent(
                                    result.data
                                )
                            )

                            Log.d("GOOGLE_TOKEN", data.id ?: "")

                            sendOtpViewModel.socialRequest.value?.name = data.displayName ?: ""
                            sendOtpViewModel.socialRequest.value?.social_id = data.id ?: ""
                            sendOtpViewModel.socialRequest.value?.email = data.email ?: ""
                            sendOtpViewModel.socialRequest.value?.social_type = Constants.GOOGLE
                            getProfileViewModel.firstName.value = data.displayName
                            socialLogin()

                        } catch (e: ApiException) {
                            Log.e("TAG", "Api Exception ${e.localizedMessage}")
                        }
                    }
                }
            }
        }

    private fun beginLoginToFacebook(
        activity: FragmentActivity,
        facebookCallbackManager: CallbackManager
    ) {
        activity.let { fragmentActivity ->
            fragmentActivity.finishFacebookLoginToThirdParty(facebookCallbackManager) { loginResult ->
                Log.d("Facebook_login", "$loginResult")
                Log.d("Facebook_login", "Access token -- ${loginResult.accessToken.token}")

                sendOtpViewModel.getFacebookData(loginResult) {
                    getProfileViewModel.firstName.value = sendOtpViewModel.socialRequest.value?.name

                    socialLogin()
                }
            }

            LoginManager.getInstance()
                .logInWithReadPermissions(
                    fragmentActivity,
                    facebookCallbackManager,
                    listOf("email", "public_profile")
                )
        }
    }

    private fun FragmentActivity.finishFacebookLoginToThirdParty(
        facebookCallbackManager: CallbackManager,
        onCredential: suspend (LoginResult) -> Unit
    ) {
        this.lifecycleScope.launchWhenStarted {
            try {
                val loginResult: LoginResult =
                    sendOtpViewModel.getFacebookToken(facebookCallbackManager = facebookCallbackManager)
                onCredential(loginResult)
            } catch (e: FacebookException) {
                Log.e("Facebook Error", e.toString())
            }
        }
    }

    /*override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }*/

    override fun getLayoutRes(): Int = R.layout.fragment_login_main

    private fun socialLogin() {
        if (sendOtpViewModel.socialRequest.value == null) return

        getBaseActivity()?.let {
            it.socialLogin(sendOtpViewModel.socialRequest.value!!) { response ->
                response?.let { data ->
                    sendOtpViewModel.verifyOtpApiResponse.value = data
                    SharedPrefsHelper.save(
                        Constants.SOCIAL_ID_FACEBOOK,
                        data.data.user.social_id.facebook
                    )
                    if (data.data.user.phone_number.isEmpty()) {
                        runBlocking {
                            preferencesHelper.setValue(
                                key = PreferencesHelper.PreferencesKeys.KEY_AUTH,
                                value = data.data.token
                            )
                        }
                        if (arguments == null) {
                            arguments = Bundle()
                        }
                        arguments?.putString(Constants.LOGIN_TYPE, Constants.FROM_SOCIAL)
                        this@LoginMainFragment.findNavController()
                            .navigate(
                                R.id.action_loginMainFragment_to_mobileFragment,
                                arguments
                            )
                    } else
                        handleResponse(data)
                }
            }
        }
    }

    private fun handleResponse(it: VerifyOtpApiResponse) {
        token = it.data.token
        runBlocking {
            preferencesHelper.setValue(
                key = PreferencesHelper.PreferencesKeys.KEY_AUTH,
                value = token
            )
            preferencesHelper.setValue(
                key = PreferencesHelper.PreferencesKeys.KEY_MOBILE,
                value = it.data.user.phone_number
            )
            preferencesHelper.setValue(
                key = PreferencesHelper.PreferencesKeys.KEY_QUICK_BLOX_ID,
                value = it.data.user.quickblox_user_id
            )
            preferencesHelper.setValue(
                key = PreferencesHelper.PreferencesKeys.STEP_PROGRESS,
                value = it.data.StepProgress
            )
            SharedPrefsHelper.save(Constants.IS_USER_VERIFIED, true)
            when (it.data.StepProgress) {
                0 -> {
                    val data = Bundle()
                    data.putString("tag", "1")
                    data.putString(Constants.EXTRA, sendOtpViewModel.socialRequest.value?.name)
                    findNavController()
                        .navigate(
                            R.id.action_otp_to_firstName, data
                        )
                }

                1 -> {
                    findNavController()
                        .navigate(
                            R.id.action_otp_to_addPhotoFragment
                        )
                }

                2 -> {
                    val data = Bundle()
                    data.putString("tag", "1")
                    findNavController()
                        .navigate(
                            R.id.action_otp_to_ageFragment, data
                        )

                }

                3 -> {
                    findNavController()
                        .navigate(
                            R.id.action_otp_to_identityFragment
                        )

                }

                4 -> {
                    findNavController()
                        .navigate(
                            R.id.action_otp_to_interestedInFragment
                        )

                }

                else -> {
                    getProfile()
                }
            }

            it.data.let {
                preferencesHelper.saveData(it)
            }

        }
    }

    private fun getProfile() {
        lifecycleScope.launch {
            getProfileViewModel.getProfile(getProfileRequest())
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {

                        }

                        is DataResult.Success -> {
                            it.data.data.let {
                                runBlocking(Dispatchers.IO) { preferencesHelper.saveUserData(it) }
                            }
                            try {
                                it.data.data.images?.apply {
                                    val urlFromS3 = S3Utils.generatesShareUrl(
                                        requireActivity(), it.data.data.images[0]
                                    )

                                    val image = urlFromS3.replace(" ", "%20")

                                    runBlocking {
                                        preferencesHelper.saveImage(
                                            image,
                                            it.data.data.images[0]
                                        )
                                    }
                                }
                            } catch (e: Exception) {

                            }

                            val intent = Intent(requireActivity(), HomeActivity::class.java)
                            intent.putExtra("show_image_popup",true)
                            startActivity(intent)
                            requireActivity().finish()


                        }

                        is DataResult.Failure -> {
                            val intent = Intent(requireActivity(), HomeActivity::class.java)
                            intent.putExtra("show_image_popup",true)
                            startActivity(intent)
                            requireActivity().finish()
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/get-profile",
                                requireActivity().componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/get-profile Api Error"))
                        }

                        else -> {}
                    }
                }

        }
    }

    private fun clear() {
        QBSettings.getInstance().isEnablePushNotification = false
        roamingTimerNotificationManager.cancelRoamingTimerNotification()
        RaddarApp.getInstance().setSubscriptionStatus(SubscriptionStatus.NON_PLUS)
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

}
