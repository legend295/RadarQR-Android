package com.radarqr.dating.android.ui.welcome.mobileLogin

import android.os.Bundle
import android.view.View
import android.view.View.FOCUS_RIGHT
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.BuildConfig
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.constant.Constants.COUNTRYCODE
import com.radarqr.dating.android.constant.Constants.MOBILE
import com.radarqr.dating.android.databinding.FragmentPhoneNumberBinding
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.Utility.openKeyboard
import com.radarqr.dating.android.utility.Utility.showToast
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MobileFragment : BaseFragment<FragmentPhoneNumberBinding>() {

    private val sendOtpViewModel: SendOtpViewModel by viewModel()
    private var phoneNumber: String = ""
    private var type = 0
    private var loginType = ""
    private var sharedUserId = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mobileFragment = this
        showSystemUI()

        if (arguments != null) {
            type = arguments?.getInt(Constants.TYPE) ?: 0
            loginType = arguments?.getString(Constants.LOGIN_TYPE) ?: ""
            sharedUserId = arguments?.getString(Constants.USER_ID, "") ?: ""
            if (type == Constants.ACCOUNT_DETAILS_FRAGMENT) {
                phoneNumber = arguments?.getString(Constants.EXTRA_DATA, "") ?: ""
                binding.etPhone.setText(phoneNumber)
                binding.tvContinue.text = resources.getString(R.string.update)
            }
        }

        binding.etPhone.requestFocus(FOCUS_RIGHT)
        binding.etPhone.openKeyboard()

        /*  if (type != Constants.ACCOUNT_DETAILS_FRAGMENT) {
              showToolbarLayout(true)
              showBackButton(true)
              showToolbar(true)
              showToolbarWhite(false)
              showProgress(false)
              showBackButtonWhite(false)
              showSkip(false)
          }*/

//        val tm = requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//        binding.ccpCode.setAutoDetectedCountry(true)
//        Log.e("COUNTRY_CODE", tm.networkCountryIso)


        binding.clContinue.setOnClickListener {
            if (phoneNumber.isNotEmpty()) {
                if (binding.etPhone.text.toString().trim() == phoneNumber) {
                    return@setOnClickListener
                }
            }
            gotToHome()
        }
        binding.etPhone.doAfterTextChanged {
            if (it?.toString()?.length == 10)
                hideKeyboard(binding.etPhone)
        }

    }


    override fun getLayoutRes(): Int = R.layout.fragment_phone_number

    override fun onResume() {
        super.onResume()
        activity?.let {
            if (it is InitialActivity) {
                it.hideShowWholeToolbar(isVisible = type != Constants.ACCOUNT_DETAILS_FRAGMENT)
                it.hideShowSkip(isVisible = false)
            }
        }
    }

    private fun gotToHome() {

        if (binding.etPhone.text?.isEmpty() == true) {
            CommonCode.setToast(requireActivity(), "Enter Mobile Number")
        } else {
            if (binding.etPhone.text?.length!! < 10) {
                CommonCode.setToast(requireActivity(), "Please enter valid phone number")
            } else {
                binding.etPhone.isEnabled = false
                binding.clContinue.isEnabled = false
                binding.progressBar.visibility = View.VISIBLE
                hideKeyboard(binding.etPhone)
                if (type == Constants.ACCOUNT_DETAILS_FRAGMENT || loginType == Constants.FROM_SOCIAL) {
                    updatePhoneNumber(
                        UpdatePhoneNumberRequest(
                            binding.etPhone.text.toString().trim(),
                            BuildConfig.COUNTRY_CODE
                        )
                    )
                } else/* handleSendOtpSuccess()*/
                    SendOtp(
                        binding.etPhone.text.toString(),
                        BuildConfig.COUNTRY_CODE//binding.ccpCode.selectedCountryCodeWithPlus
                    )//+ binding.ccpCode.selectedCountryCode)//+ binding.ccpCode.selectedCountryCode)
            }
        }
    }

    private fun updatePhoneNumber(request: UpdatePhoneNumberRequest) {
        lifecycleScope.launch {
            sendOtpViewModel.updatePhoneNumber(request).observe(viewLifecycleOwner) {
                when (it) {
                    DataResult.Empty -> {
                    }

                    DataResult.Loading -> {
                    }

                    is DataResult.Success -> {
                        binding.clContinue.isEnabled = true
                        binding.etPhone.isEnabled = true
                        binding.progressBar.visibility = View.GONE
                        if (it.statusCode == 200) {
                            val data = Bundle()

                            data.putString(
                                MOBILE, binding.etPhone.text.toString().trim()
                            )

                            data.putString(COUNTRYCODE, BuildConfig.COUNTRY_CODE)
                            if (loginType == Constants.FROM_SOCIAL) {
                                data.putString(Constants.LOGIN_TYPE, Constants.FROM_SOCIAL)
                            } else {
                                if (type == Constants.ACCOUNT_DETAILS_FRAGMENT)
                                    data.putInt(Constants.TYPE, Constants.ACCOUNT_DETAILS_FRAGMENT)
                            }
                            findNavController()
                                .navigate(
                                    R.id.action_mobileFragment_to_otpFragment, data
                                )
                        }
                    }

                    is DataResult.Failure -> {
                        binding.progressBar.visibility = View.GONE
                        binding.clContinue.isEnabled = true
                        binding.etPhone.isEnabled = true
                        if (it.statusCode == 422) {
                            CommonCode.setToast(requireContext(), it.message ?: "")
                        }
                        if (it.statusCode == 200) {
                            val data = Bundle()

                            data.putString(
                                MOBILE, binding.etPhone.text.toString().trim()
                            )

                            data.putString(COUNTRYCODE, BuildConfig.COUNTRY_CODE)
                            if (type == Constants.ACCOUNT_DETAILS_FRAGMENT)
                                data.putInt(Constants.TYPE, Constants.ACCOUNT_DETAILS_FRAGMENT)
                            findNavController()
                                .navigate(
                                    R.id.action_mobileFragment_to_otpFragment, data
                                )
                        }
                    }
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    private fun SendOtp(mobile: String, code: String) {
        lifecycleScope.launch {
            sendOtpViewModel.SendOtp(SendOtpApiRequest(mobile, code))
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {

                        }

                        is DataResult.Success -> {
                            handleSendOtpSuccess()
                        }

                        is DataResult.Failure -> {
                            binding.etPhone.isEnabled = true
                            binding.clContinue.isEnabled = true
                            binding.progressBar.visibility = View.GONE
                            if (it.statusCode == Constants.USER_REPORTED) {
                                showReportDialog()
                                return@observe
                            } /*else if (it.statusCode == Constants.TOO_MANY_ATTEMPTS) {
                                requireContext().showToast("Too many requests. Please wait and try again later.")
                                return@observe
                            }*/
                            Toast.makeText(requireContext(), it.message, Toast.LENGTH_SHORT)
                                .show()
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "send-otp",
                                requireActivity().componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("Send Otp Api Error"))
                        }

                        else -> {}
                    }
                }

        }
    }

    private fun handleSendOtpSuccess() {
        binding.etPhone.isEnabled = true
        binding.clContinue.isEnabled = true
        binding.progressBar.visibility = View.GONE
        val data = Bundle()

        data.putString(
            MOBILE, binding.etPhone.text.toString().trim()
        )

        data.putString(COUNTRYCODE, binding.ccpCode.selectedCountryCodeWithPlus)
        data.putString(Constants.LOGIN_TYPE, loginType)
        if (type == Constants.ACCOUNT_DETAILS_FRAGMENT)
            data.putInt(Constants.TYPE, Constants.ACCOUNT_DETAILS_FRAGMENT)
        findNavController()
            .navigate(
                R.id.action_mobileFragment_to_otpFragment, data
            )
    }
}
