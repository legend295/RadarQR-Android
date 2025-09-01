package com.radarqr.dating.android.ui.welcome.otpVerify

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.BuildConfig
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.constant.Constants.COUNTRYCODE
import com.radarqr.dating.android.constant.Constants.FROM_SOCIAL
import com.radarqr.dating.android.constant.Constants.MOBILE
import com.radarqr.dating.android.databinding.FragmentOtpVerficationBinding
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.mobileLogin.*
import com.radarqr.dating.android.utility.*
import com.radarqr.dating.android.utility.Utility.openKeyboard
import com.radarqr.dating.android.utility.otp.OTPListener
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class OtpVerifyFragment : BaseFragment<FragmentOtpVerficationBinding>() {

    private val verifyOtpViewModel: VerifyOtpViewModel by viewModel()
    private val sendOtpViewModel: SendOtpViewModel by viewModel()
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val mixPanelWrapper: MixPanelWrapper by inject()
    var timer: CountDownTimer? = null
    var mobile = ""
    var code = ""
    var otp = ""
    var token = ""
    private var type = 0
    private var loginType = ""
    private val preferencesHelper: PreferencesHelper by inject()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.otpFragment = this
//        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//        StrictMode.setThreadPolicy(policy)
//        showToolbarLayout(true)
//        showProgress(false)
//        showBackButton(true)
//        showToolbar(true)
//        showToolbarWhite(false)
//        showBackButtonWhite(false)
//        showSkip(false)
        val data: Bundle? = arguments
        if (arguments != null) {
            type = arguments?.getInt(Constants.TYPE) ?: 0
            loginType = arguments?.getString(Constants.LOGIN_TYPE) ?: ""
        }

        binding.otpView.requestFocusOTP()
        binding.otpView.openKeyBoard()

        mobile = data?.getString(MOBILE)!!

        code = data.getString(COUNTRYCODE)!!
        editorListener()

//         var number = mobile.replaceFirst("(\\d{3})(\\d{3})(\\d+)", "$1 $2 $3")
        binding.tvBody.text = HtmlCompat.fromHtml(
            getString(R.string.send_code)
                    + "<br></br>" + "<b>" + BuildConfig.COUNTRY_CODE + " " + Utility.phoneFormat(
                mobile
            ) + "</b>",
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )





        binding.clContinue.setOnClickListener {
            gotToHome()
        }
        binding.tvTimer.setOnClickListener {
//            binding.otpView.isEnabled = false
            binding.clContinue.isEnabled = false

            sendOtp(mobile)
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_otp_verfication

    private fun editorListener() {
        timer()

        binding.otpView.otpListener = object : OTPListener {
            override fun onInteractionListener() {

            }

            override fun onOTPComplete(otp: String) {
                hideKeyboard(binding.otpView)
            }
        }

//        setTextChangeListener(binding.etOtp1, binding.etOtp2, binding.etOtp1)
//        setTextChangeListener(binding.etOtp2, binding.etOtp3, binding.etOtp1)
//        setTextChangeListener(binding.etOtp3, binding.etOtp4, binding.etOtp2)
//        setTextChangeListener(binding.etOtp4, binding.etOtp4, binding.etOtp3)
    }

    private fun setTextChangeListener(
        fromEditText: EditText,
        toEditText: EditText,
        backEditText: EditText
    ) {

        fromEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                if (s.isEmpty()) {
                    backEditText.requestFocus()
                } else if (s.length == 1) {
                    toEditText.requestFocus()
                }

            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
                if (s.isEmpty()) {
                    backEditText.requestFocus()
                }
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.length == 1) {
                    focusChange(toEditText)
                }
            }
        })
    }

    fun focusChange(editText: EditText) {
        editText.requestFocus()

        val imm: InputMethodManager? =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?

        imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }


    private fun gotToHome() {
        if (!binding.otpView.otp.isNullOrEmpty() && (binding.otpView.otp ?: "").length >= 4) {
//            val enteredOtpNumber =
//                "${binding.etOtp1.text}${binding.etOtp2.text}${binding.etOtp3.text}${binding.etOtp4.text}"
            val enteredOtpNumber = binding.otpView.otp ?: "0"


            val enteredOtpInInt = enteredOtpNumber.toInt()
//            binding.etOtp1.isEnabled = false
//            binding.etOtp2.isEnabled = false
//            binding.etOtp3.isEnabled = false
//            binding.etOtp4.isEnabled = false
//            binding.otpView.isEnabled = false
            binding.clContinue.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE
            if (type == Constants.ACCOUNT_DETAILS_FRAGMENT || loginType == FROM_SOCIAL) {
                confirmPhoneOtp(
                    ConfirmOtpRequest(
                        mobile,
                        BuildConfig.COUNTRY_CODE,
                        enteredOtpInInt.toString()
                    )
                )
            } else {
                when (loginType) {
                    FROM_SOCIAL -> {

//                        socialLogin()
                    }

                    else -> {
                        verifyOtp(mobile, enteredOtpInInt.toString())
                    }
                }
            }

        } else {
            Toast.makeText(requireContext(), "Please enter valid OTP", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun timer() {
        binding.tvTimer.isEnabled = false
        timer = object : CountDownTimer(61000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                try {
                    binding.tvTimer.text = HtmlCompat.fromHtml(
                        "<u>Resend(" + millisUntilFinished / 1000 + ")</u>",
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                } catch (_: Exception) {

                }
            }

            override fun onFinish() {
                binding.tvTimer.isEnabled = true
                binding.tvTimer.text = resources.getString(R.string.resend)
            }
        }.start()
    }

    override fun onStop() {
        super.onStop()
        timer?.cancel()
    }

    private fun confirmPhoneOtp(request: ConfirmOtpRequest) {
        if (view != null && isAdded && isVisible)
            lifecycleScope.launch {
                sendOtpViewModel.confirmPhoneOtp(request).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {
                        }

                        DataResult.Loading -> {
                        }

                        is DataResult.Success -> {
                            QuickBloxManager.qbChatService.destroy()
                            if (QuickBloxManager.qbSessionManager.activeSession != null) {
                                QuickBloxManager.deleteSession {

                                }
                            }
                            if (it.statusCode == 200) {
                                SharedPrefsHelper.save(Constants.IS_USER_VERIFIED, true)
                                runBlocking {
                                    preferencesHelper.setValue(
                                        key = PreferencesHelper.PreferencesKeys.KEY_MOBILE,
                                        value = request.phone_number
                                    )
                                }
                                if (loginType != FROM_SOCIAL)
                                    CommonCode.setToast(
                                        requireContext(),
                                        resources.getString(R.string.phone_updated_successfully_message)
                                    )

                                if (loginType == FROM_SOCIAL) {
                                    sendOtpViewModel.verifyOtpApiResponse.value.let { data ->
                                        if (data != null)
                                            handleResponse(data)
                                    }
                                } else {
                                    findNavController().popBackStack(
                                        R.id.accountDetailsFragment,
                                        false
                                    )
                                }
                            }

                        }

                        is DataResult.Failure -> {
                            if (it.statusCode == 422) {
                                CommonCode.setToast(requireContext(), it.message ?: "")
                            }

                            if (it.statusCode == 200) {
                                findNavController().popBackStack(R.id.accountDetailsFragment, false)
                            }
                        }
                    }
                }
            }
    }

    private fun socialLogin() {
        getBaseActivity()?.let {
            it.socialLogin(sendOtpViewModel.socialRequest.value!!) { response ->
                response?.let { data ->
                    handleResponse(data)
                }
            }
        }
    }

    private fun verifyOtp(mobile: String, otp: String) {
        if (view != null && isAdded) {
            lifecycleScope.launch {
                verifyOtpViewModel.verifyOtp(
                    VerifyOtpApiRequest(
                        mobile,
                        BuildConfig.COUNTRY_CODE,
                        otp
                    )
                )
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {

                            }

                            is DataResult.Success -> {
                                handleResponse(it.data)

                            }

                            is DataResult.Failure -> {
//                                binding.etOtp1.isEnabled = true
//                                binding.etOtp2.isEnabled = true
//                                binding.etOtp3.isEnabled = true
//                                binding.etOtp4.isEnabled = true
//                                binding.otpView.isEnabled = true
                                binding.clContinue.isEnabled = true
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT)
                                    .show()


                                reportApiError(
                                    java.lang.Exception().stackTrace[0].lineNumber,
                                    it.statusCode ?: 0,
                                    "verify-otp",
                                    requireActivity().componentName.className,
                                    it.message ?: ""
                                )

                                FirebaseCrashlytics.getInstance()
                                    .recordException(java.lang.Exception("verify-otp Api Error"))
                            }

                            else -> {}
                        }
                    }

            }
        }
    }

    private fun handleResponse(it: VerifyOtpApiResponse) {
        token = it.data.token
        SharedPrefsHelper.save(Constants.IS_USER_VERIFIED, true)
        runBlocking {
            preferencesHelper.setValue(
                key = PreferencesHelper.PreferencesKeys.KEY_AUTH,
                value = token
            )
            preferencesHelper.setValue(
                key = PreferencesHelper.PreferencesKeys.KEY_MOBILE,
                value = mobile
            )
            preferencesHelper.setValue(
                key = PreferencesHelper.PreferencesKeys.KEY_QUICK_BLOX_ID,
                value = it.data.user.quickblox_user_id
            )
            preferencesHelper.setValue(
                key = PreferencesHelper.PreferencesKeys.STEP_PROGRESS,
                value = it.data.StepProgress
            )
            binding.progressBar.visibility = View.GONE
            when (it.data.StepProgress) {
                0 -> {
                    val data = Bundle()
                    data.putString("tag", "1")
                    data.putString(Constants.LOGIN_TYPE, loginType)
//                    data.putString(Constants.EXTRA, sendOtpViewModel.socialRequest.value?.name)
//                    getProfileViewModel.firstName.value = sendOtpViewModel.socialRequest.value?.name
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
                    binding.progressBar.visibility = View.VISIBLE
                    getProfile()
                }
            }

            it.data.let {
                preferencesHelper.saveData(it)
            }

        }
//        binding.otpView.isEnabled = true
//        binding.etOtp1.isEnabled = true
//        binding.etOtp2.isEnabled = true
//        binding.etOtp3.isEnabled = true
//        binding.etOtp4.isEnabled = true
        binding.clContinue.isEnabled = true
    }

    private fun sendOtp(mobile: String) {
        if (view != null && isAdded) {
            lifecycleScope.launch {
                sendOtpViewModel.SendOtp(SendOtpApiRequest(mobile, BuildConfig.COUNTRY_CODE))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {

                            }

                            is DataResult.Success -> {
//                            binding.tvContent.visibility = View.GONE
//                                binding.etOtp1.setText("")
//                                binding.etOtp2.setText("")
//                                binding.etOtp3.setText("")
//                                binding.etOtp4.setText("")
//                                binding.etOtp1.isEnabled = true
//                                binding.etOtp2.isEnabled = true
//                                binding.etOtp3.isEnabled = true
//                                binding.etOtp4.isEnabled = true
                                binding.otpView.setOTP("")
                                binding.otpView.isEnabled = true
                                binding.clContinue.isEnabled = true
//                            binding.tvContent.isEnabled = false
                                binding.tvTimer.text = Html.fromHtml(
                                    ""
                                            + "<font color=\"#A1DFD7\"><b><u>" + "Resend" + "</u></b></font><br><br>"
                                )
                                timer()
                            }

                            is DataResult.Failure -> {
//                                binding.etOtp1.isEnabled = true
//                                binding.etOtp2.isEnabled = true
//                                binding.etOtp3.isEnabled = true
//                                binding.etOtp4.isEnabled = true
                                binding.otpView.isEnabled = true
                                binding.clContinue.isEnabled = true
                                Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT)
                                    .show()


                                reportApiError(
                                    java.lang.Exception().stackTrace[0].lineNumber,
                                    it.statusCode ?: 0,
                                    "send-otp",
                                    requireActivity().componentName.className,
                                    it.message ?: ""
                                )

                                FirebaseCrashlytics.getInstance()
                                    .recordException(java.lang.Exception("Send Otp Api Error"))
                            }

                            else -> {}
                        }
                    }

            }
        }
    }

    private fun getProfile() {
        if (view != null && isVisible && isAdded)
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

                                        val Image = urlFromS3.replace(" ", "%20")

                                        runBlocking {
                                            preferencesHelper.saveImage(
                                                Image,
                                                it.data.data.images[0]
                                            )
                                        }
                                    }
                                } catch (e: Exception) {

                                }
                                binding.progressBar.visibility = View.GONE
                                logLoginEvent(it.data.data._id)
                                val intent = Intent(requireActivity(), HomeActivity::class.java)
                                intent.putExtra("show_image_popup",true)
                                startActivity(intent)
                                requireActivity().finish()


                            }

                            is DataResult.Failure -> {
                                binding.progressBar.visibility = View.GONE
                                logLoginEvent(null)
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

    private fun logLoginEvent(userId: String?) {
        mixPanelWrapper.logLoginEvent(userId)
    }

}
