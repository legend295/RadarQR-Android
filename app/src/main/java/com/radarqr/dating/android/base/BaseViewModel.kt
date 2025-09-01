package com.radarqr.dating.android.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.radarqr.dating.android.data.model.SocialRequest
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.ui.welcome.mobileLogin.AccountSettingsRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.utility.PreferencesHelper
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys

class BaseViewModel(
    private val dataRepository: DataRepository,
    private val preferencesHelper: PreferencesHelper
) : ViewModel() {

    fun getProfile(request: getProfileRequest) =
        dataRepository.getProfileApi(request).asLiveData()

    fun socialLogin(request: SocialRequest) =
        dataRepository.socialLogin(request).asLiveData()

    fun editProfile(editProfileApiRequest: EditProfileApiRequest) =
        dataRepository.editProfileApi(editProfileApiRequest).asLiveData()

    fun accountSettings(request: AccountSettingsRequest) =
        dataRepository.accountSettings(request).asLiveData()


    fun reportApiError(
        lineNumber: Int,
        apiStatusCode: Int,
        apiName: String,
        className: String,
        errorMessage: String
    ) {
        FirebaseCrashlytics.getInstance().setCustomKeys {
            key("Line Number", lineNumber)
            key("Api Error Code", apiStatusCode)
            key("Api Name", apiName)
            key("Class Name", className)
            key("Error Message", errorMessage)
        }
    }

}