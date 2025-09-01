package com.radarqr.dating.android.ui.home.settings

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentPauseProfileBinding
import com.radarqr.dating.android.ui.welcome.mobileLogin.AccountSettingsRequest
import com.radarqr.dating.android.utility.SharedPrefsHelper
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PauseProfileFragment : BaseFragment<FragmentPauseProfileBinding>(), ViewClickHandler {

    private val getProfileViewModel: GetProfileViewModel by viewModel()


    override fun getLayoutRes(): Int = R.layout.fragment_pause_profile

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.clickHandler = this
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_pause_unpause_profile -> {
                val request = AccountSettingsRequest(
                    pause_profile = false
                )
                updateAccountSettings(request)
            }
        }
    }

    private fun updateAccountSettings(request: AccountSettingsRequest) {
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
                            getProfileViewModel.data.value?.data?.pause_profile =
                                request.pause_profile ?: false
                            SharedPrefsHelper.save(
                                Constants.IS_PROFILE_PAUSED,
                                request.pause_profile ?: false
                            )

                            findNavController().popBackStack()
                        }
                    }
                }
            }
        } catch (e: Exception) {

        }
    }
}