package com.radarqr.dating.android.ui.welcome.registerScreens

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentIdentityBinding
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.Utility.color
import com.radarqr.dating.android.utility.Utility.drawable
import com.radarqr.dating.android.utility.Utility.font
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class IdentityFragment : BaseFragment<FragmentIdentityBinding>(), View.OnClickListener {
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    var value = Constants.MALE
    private val preferencesHelper: PreferencesHelper by inject()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.identityFragment = this
//        showToolbarLayout(true)
//        showBackButton(true)
//        showToolbar(true)
//        showToolbarWhite(false)
//        showBackButtonWhite(false)
//        showProgress(true)
//        setProgress(56)
//        showSkip(false)
        binding.clContinue.setOnClickListener {
            if (value == "") {

            } else {
                binding.progressBar.visibility = View.VISIBLE
                editProfile()
            }
        }
        binding.clMen.setOnClickListener(this)
        binding.clWomen.setOnClickListener(this)
        binding.clNon.setOnClickListener(this)
        handleView()
    }


    override fun getLayoutRes(): Int = R.layout.fragment_identity

    override fun onResume() {
        super.onResume()
        activity?.let {
            (it as InitialActivity).hideShowWholeToolbar(isVisible = true)
            it.hideShowSkip(isVisible = false)
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.cl_men -> {
//                value = Constants.MAN
                value = Constants.MALE
                handleView()
            }
            R.id.cl_women -> {
//                value = Constants.WOMAN
                value = Constants.FEMALE
                handleView()
            }
            R.id.cl_non -> {
                value = Constants.NON_BINARY
                handleView()
            }
        }
    }

    private fun handleView() {
        binding.clWomen.background =
            requireContext().drawable(if (value == Constants.FEMALE) R.drawable.green_fill_rect else R.drawable.rect_grey)
        binding.tvWomen.setTextColor(requireContext().color(if (value == Constants.FEMALE) R.color.white else R.color.black))
        binding.ivWomen.backgroundTintList =
            ColorStateList.valueOf(requireContext().color(if (value == Constants.FEMALE) R.color.white else R.color.teal_200))
        binding.tvWomen.typeface =
            requireContext().font(if (value == Constants.FEMALE) R.font.poppins_bold else R.font.poppins_regular)

        binding.clMen.background =
            requireContext().drawable(if (value == Constants.MALE) R.drawable.green_fill_rect else R.drawable.rect_grey)
        binding.tvMen.setTextColor(requireContext().color(if (value == Constants.MALE) R.color.white else R.color.black))
        binding.ivMen.backgroundTintList =
            ColorStateList.valueOf(requireContext().color(if (value == Constants.MALE) R.color.white else R.color.teal_200))
        binding.tvMen.typeface =
            requireContext().font(if (value == Constants.MALE) R.font.poppins_bold else R.font.poppins_regular)

        binding.clNon.background =
            requireContext().drawable(if (value == Constants.NON_BINARY) R.drawable.green_fill_rect else R.drawable.rect_grey)
        binding.tvNon.setTextColor(requireContext().color(if (value == Constants.NON_BINARY) R.color.white else R.color.black))
        binding.ivNon.backgroundTintList =
            ColorStateList.valueOf(requireContext().color(if (value == Constants.NON_BINARY) R.color.white else R.color.teal_200))
        binding.tvNon.typeface =
            requireContext().font(if (value == Constants.NON_BINARY) R.font.poppins_bold else R.font.poppins_regular)

    }


    @ExperimentalCoroutinesApi
    private fun editProfile() {
        lifecycleScope.launch {
            getProfileViewModel.editProfile(
                EditProfileApiRequest(
                    gender = value.lowercase(),
                    StepProgress = 4
                )
            )
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is DataResult.Success -> {
                            getProfileViewModel.profileData.value = it.data.data
                            runBlocking {

                                preferencesHelper.setValue(
                                    key = PreferencesHelper.PreferencesKeys.STEP_PROGRESS,
                                    value = 4
                                )
                            }
                            it.data.data.let {
                                runBlocking(Dispatchers.IO) {
                                    preferencesHelper.saveDataEditProfile(
                                        it
                                    )
                                }
                            }
                            binding.progressBar.visibility = View.GONE
                            findNavController()
                                .navigate(R.id.action_identityFragment_to_interestedInFragment)
                        }
                        is DataResult.Failure -> {
                            binding.progressBar.visibility = View.GONE
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
