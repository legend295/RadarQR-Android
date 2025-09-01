package com.radarqr.dating.android.ui.home.settings

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.databinding.FragmentUpdateUsernameBinding
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.utility.BaseUtils.isInternetAvailable
import com.radarqr.dating.android.utility.BaseUtils.showKeyboard
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class UpdateUsernameFragment : BaseFragment<FragmentUpdateUsernameBinding>() {

    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val preferencesHelper: PreferencesHelper by inject()

    override fun getLayoutRes(): Int = R.layout.fragment_update_username

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HomeActivity.activeFragment.value = this
        //set username if user has provided that
        binding.etUsername.setText(getProfileViewModel.profileData.value?.username)

        // Request focus and open keyboard
        binding.etUsername.requestFocus()
        binding.etUsername.post {
            binding.etUsername.setSelection(
                getProfileViewModel.profileData.value?.username?.length ?: 0
            )
        }
        binding.etUsername.showKeyboard()

        binding.tvContinue.setOnClickListener {
            binding.etUsername.clearFocus()
            hideKeyboard(it)
            if (binding.etUsername.text.isNullOrEmpty()) return@setOnClickListener
            if (getProfileViewModel.profileData.value?.username == binding.etUsername.text.toString()) return@setOnClickListener
            // check if char length is greater than 3
            if (binding.etUsername.text!!.length <= 3) {
                requireContext().showToast("Username length must be at least 3 characters long")
                return@setOnClickListener
            }
            if (requireActivity().isInternetAvailable()) {
                binding.progressBar.visible(isVisible = true)
                editProfile(EditProfileApiRequest(username = binding.etUsername.text.toString()))
            }
        }

        binding.etUsername.setOnFocusChangeListener { _, _ ->
            binding.tvErrorMessage.text = ""
        }
    }

    private fun editProfile(
        editProfileRequest: EditProfileApiRequest
    ) {
        if (view != null && isAdded && isVisible)
            try {
                lifecycleScope.launch {
                    getProfileViewModel.editProfile(editProfileRequest)
                        .observe(viewLifecycleOwner) {
                            when (it) {
                                is DataResult.Loading -> {}
                                is DataResult.Success -> {
                                    it.data.data.let {
                                        runBlocking(Dispatchers.IO) {
                                            preferencesHelper.saveDataEditProfile(
                                                it
                                            )
                                        }
                                    }
                                    /*it.data.data.replaceProfileImagesWithUrl(requireContext()) { data ->
                                        getProfileViewModel.profileData.value = data
                                    }*/
                                    getProfileViewModel.profileData.value = it.data.data
                                    binding.progressBar.visible(isVisible = false)
                                    this@UpdateUsernameFragment.view?.findNavController()
                                        ?.popBackStack()

                                }

                                is DataResult.Failure -> {
                                    binding.progressBar.visible(isVisible = false)
                                    if (it.statusCode == 422) {
                                        binding.tvErrorMessage.text = it.message
                                        binding.tvErrorMessage.visible(isVisible = true)
                                    } else {
                                        binding.tvErrorMessage.visible(isVisible = false)
                                        it.message?.let { msg ->
                                            requireContext().showToast(msg)
                                        }
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
                                }

                                DataResult.Empty -> {}
                            }
                        }

                }
            } catch (e: Exception) {

            }
    }
}