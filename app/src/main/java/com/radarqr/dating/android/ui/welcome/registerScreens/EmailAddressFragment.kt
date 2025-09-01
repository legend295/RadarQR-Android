package com.radarqr.dating.android.ui.welcome.registerScreens

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentEmailBinding
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.utility.CommonCode
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.ui.welcome.mobileLogin.SendOtpViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class EmailAddressFragment : BaseFragment<FragmentEmailBinding>() {
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val sendOtpViewModel: SendOtpViewModel by viewModel()
    private var type: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        type = arguments?.getInt(Constants.TYPE, 0) ?: 0

        when (type) {
            Constants.ACCOUNT_DETAILS_FRAGMENT -> {
                initialSetup(false)
                binding.etName.setText(arguments?.getString(Constants.EXTRA_DATA, "") ?: "")
            }

            else -> {
                initialSetup(true)
                /*sendOtpViewModel.socialRequest.value?.let {
                    binding.etName.setText(it.email)
                }*/
            }
        }
        binding.emailFragment = this



        binding.clContinue.setOnClickListener {
            if (binding.etName.text?.isEmpty() == true) {
                CommonCode.setToast(requireContext(), "Please enter valid email")
            } else {
                val validemail =
                    Patterns.EMAIL_ADDRESS.matcher(
                        binding.etName.text.toString().trim()
                    )
                        .matches()
                if (validemail) {
                    binding.progressBar.visibility = View.VISIBLE
                    editProfile()
                } else {
                    CommonCode.setToast(requireContext(), "Please enter valid email")
                }
            }
        }
//
//        binding.activityToolbarBack.setOnClickListener {
//            onBackPress()
//        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            if (it is InitialActivity) {
                it.hideShowWholeToolbar(isVisible = true)
                it.hideShowSkip(isVisible = true)
            }
        }
    }


    private fun initialSetup(value: Boolean) {
//        showToolbarLayout(value)
//        showBackButton(value)
//        showToolbar(value)
//        showToolbarWhite(value)
//        showBackButtonWhite(value)
//        showProgress(value)
//        setProgress(84)
//        showSkip(value)
//        binding.activityToolbarBack.visibility = if (value) View.GONE else View.VISIBLE
    }

    override fun getLayoutRes(): Int = R.layout.fragment_email

    private fun editProfile() {
        if (view != null && isVisible && isAdded)
            lifecycleScope.launch {
                getProfileViewModel.editProfile(
                    EditProfileApiRequest(
                        StepProgress = 6,
                        email = binding.etName.text.toString()
                    )
                )
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {
                                binding.progressBar.visibility = View.VISIBLE
                            }
                            is DataResult.Success -> {
                                getProfileViewModel.profileData.value = it.data.data
                                when (type) {
                                    Constants.ACCOUNT_DETAILS_FRAGMENT -> {
                                        onBackPress()
                                    }

                                    else -> {
                                        binding.progressBar.visibility = View.GONE
                                        findNavController()
                                            .navigate(R.id.action_emailAddress_to_wecomeSignupFragment)
                                    }
                                }
                            }
                            is DataResult.Failure -> {
                                binding.progressBar.visibility = View.GONE
                                CommonCode.setToast(
                                    requireContext(),
                                    it.message ?: "Email Already Exist"
                                )

                                reportApiError(
                                    Exception().stackTrace[0].lineNumber,
                                    it.statusCode ?: 0,
                                    "user/edit-profile",
                                    requireActivity().componentName.className,
                                    it.message ?: ""
                                )

                                FirebaseCrashlytics.getInstance()
                                    .recordException(Exception("user/edit-profile Api Error"))
                            }

                            else -> {}
                        }
                    }

            }
    }

}
