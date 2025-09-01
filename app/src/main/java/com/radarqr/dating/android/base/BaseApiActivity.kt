package com.radarqr.dating.android.base

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.SocialRequest
import com.radarqr.dating.android.subscription.SubscriptionWrapper
import com.radarqr.dating.android.subscription.SubscriptionWrapper.loginRCAppUserId
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.welcome.mobileLogin.AccountSettingsRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.ui.welcome.otpVerify.VerifyOtpApiResponse
import com.radarqr.dating.android.utility.SharedPrefsHelper
import org.koin.androidx.viewmodel.ext.android.viewModel

open class BaseApiActivity : AppCompatActivity() {

    private val baseViewModel: BaseViewModel by viewModel()

    fun getProfile(request: getProfileRequest, data: (ProfileData?, Pair<Int?, String?>) -> Unit) {
        lifecycleScope.launchWhenStarted {
            baseViewModel.getProfile(request).observe(this@BaseApiActivity) {
                when (it) {
                    DataResult.Empty -> {
                    }

                    is DataResult.Failure -> {
                        baseViewModel.reportApiError(
                            Exception().stackTrace[0].lineNumber,
                            it.statusCode ?: 0,
                            "user/get-profile",
                            componentName.className,
                            it.message ?: ""
                        )

                        FirebaseCrashlytics.getInstance()
                            .recordException(Exception("user/get-profile Api Error"))
                        data(null, Pair(it.statusCode, it.message))
                    }

                    DataResult.Loading -> {
                    }

                    is DataResult.Success -> {
                        HomeActivity.rcSubscriptionUserId =
                            it.data.data.subscription_user_id ?: ""

                        data(it.data.data, Pair(it.statusCode, null))
                    }
                }
            }
        }
    }

    fun socialLogin(request: SocialRequest, data: (VerifyOtpApiResponse?) -> Unit) {
        lifecycleScope.launchWhenStarted {
            baseViewModel.socialLogin(request).observe(this@BaseApiActivity) {
                when (it) {
                    DataResult.Empty -> {}
                    is DataResult.Failure -> {
                        data(null)
                    }

                    DataResult.Loading -> {}
                    is DataResult.Success -> {
                        data(it.data)
                    }
                }
            }
        }
    }

    fun editProfile(
        editProfileApiRequest: EditProfileApiRequest,
        response: (ProfileData) -> Unit
    ) {
        lifecycleScope.launchWhenStarted {
            baseViewModel.editProfile(
                editProfileApiRequest
            )
                .observe(this@BaseApiActivity) {
                    when (it) {
                        is DataResult.Loading -> {

                        }

                        is DataResult.Success -> {
                            response(it.data.data)
                        }

                        is DataResult.Failure -> {
                            baseViewModel.reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/edit-profile",
                                componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/edit-profile Api Error"))
                        }

                        DataResult.Empty -> {}
                    }
                }

        }
    }

    fun updateAccountSettings(
        request: AccountSettingsRequest,
        response: (AccountSettingsRequest?) -> Unit
    ) {
        try {
            lifecycleScope.launchWhenCreated {
                baseViewModel.accountSettings(request).observe(this@BaseApiActivity) {
                    when (it) {
                        DataResult.Empty -> {
                        }

                        is DataResult.Failure -> {
                            baseViewModel.reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/account-settings",
                                componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/account-settings Api Error"))
                            response(null)
                        }

                        DataResult.Loading -> {
                        }

                        is DataResult.Success -> {
                            SharedPrefsHelper.save(
                                Constants.IS_PROFILE_PAUSED,
                                request.pause_profile ?: false
                            )
                            response(it.data)
                        }
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

}