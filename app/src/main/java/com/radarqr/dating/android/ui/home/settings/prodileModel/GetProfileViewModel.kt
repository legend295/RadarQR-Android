package com.radarqr.dating.android.ui.home.settings.prodileModel

import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.data.model.accountdetails.AccountDetailsResponse
import com.radarqr.dating.android.data.model.report.ReportData
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.ui.adapter.ReportType
import com.radarqr.dating.android.ui.home.settings.model.SettingsOptionData
import com.radarqr.dating.android.ui.welcome.mobileLogin.*
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.PreferencesHelper.PreferencesKeys.ADS_COUNT
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


class GetProfileViewModel constructor(
    private val dataRepository: DataRepository,
    private val preferencesHelper: PreferencesHelper
) :
    ViewModel() {

    var getPRofileApiRequest: getProfileRequest = getProfileRequest()
    val _eventProfileData: MutableLiveData<ProfileData> = MutableLiveData()
    var stateSaved: Boolean = false
    var data: MutableLiveData<AccountDetailsResponse> = MutableLiveData()
    val firstName: MutableLiveData<String> = MutableLiveData()
    var saveData: SavePrefernceApiResponse? = null
    var adsCountForMixpanelEvent = 0


    val settingsList = ArrayList<SettingsOptionData>()

    /** tracking that location is updated or not
     * if updated then change only location in list
     *
     * value is updating in Location fragment
     *
     * used in edit profile and preferences
     * */
    var isLocationUpdated = false

    /**
     * tracking location update position in list
     * updating in edit profile and preferences
     * */
    var locationPosition = 5

    var profileData: MutableLiveData<ProfileData> = MutableLiveData()
    val reportAdapterObserver: MutableLiveData<Pair<Boolean, ReportType>> = MutableLiveData()
    var reportData: ReportData? = null
//    val userImages: LinkedHashMap<String, String> = LinkedHashMap()
//    val viewModeUserImages: LinkedHashMap<String, String> = LinkedHashMap()

    val pauseProfileObserver: MutableLiveData<Boolean> = MutableLiveData()
    val profileDataObserver: MutableLiveData<Boolean> = MutableLiveData()

    init {
        callGetProfileApi()
        // getImagesFromSession()
        getReportOptionsFromPreferences()
        viewModelScope.launch {
            adsCountForMixpanelEvent = preferencesHelper.getValue(ADS_COUNT).first() ?: 0
        }
    }

    fun callGetProfileApi() {
        val response = dataRepository.getProfileApi(getPRofileApiRequest).asLiveData()
        when (response.value) {
            is DataResult.Loading -> {

            }

            is DataResult.Success -> {
                _eventProfileData.postValue((response.value as DataResult.Success<getProfileApiResponse>).data.data)
            }

            else -> {}
        }
    }

    fun getProfile(getPRofileApiRequest: getProfileRequest) =
        dataRepository.getProfileApi(getPRofileApiRequest).asLiveData()

    fun editProfile(editProfileApiRequest: EditProfileApiRequest) =
        dataRepository.editProfileApi(editProfileApiRequest).asLiveData()

    fun getOtherProfile(getRequest: getMatchData) =
        dataRepository.getOtherProfileApi(getRequest).asLiveData()

    fun updatePhoneNumber(request: UpdatePhoneNumberRequest) =
        dataRepository.updatePhoneNumber(request).asLiveData()

    fun savePreferences(savePreferenceApiRequest: SavePreferenceApiRequest) =
        dataRepository.savePreferences(savePreferenceApiRequest).asLiveData()

    fun getPreferences() =
        dataRepository.getPreferences().asLiveData()

    fun getLikes(pageNo: Int, limit: Int, category: String) =
        dataRepository.getLikesApi(pageNo, limit, category).asLiveData()

    fun deleteImage(deleteImagesRequest: DeleteImagesRequest) =
        dataRepository.deleteImage(deleteImagesRequest).asLiveData()

    fun updateToken(updateTokenRequest: updateTokenRequest) =
        dataRepository.updateTokenApi(updateTokenRequest).asLiveData()

    fun Logout(updateTokenRequest: updateTokenRequest) =
        dataRepository.LogoutApi(updateTokenRequest).asLiveData()

    fun getAccountSettings() = dataRepository.getAccountDetails().asLiveData()

    fun accountSettings(request: AccountSettingsRequest) =
        dataRepository.accountSettings(request).asLiveData()

    fun reportUser(request: ReportRequest) = dataRepository.reportUser(request).asLiveData()

    fun getAllHobbies() = dataRepository.getAllHobbies().asLiveData()

    fun deleteUser(request: DeleteAccount) = dataRepository.deleteUser(request).asLiveData()

    fun venueSubscriptionStatus() = dataRepository.venueSubscriptionStatus()

    /*  fun storeImages(list: ArrayList<String>, context: Context) {
          for (value in list) {
              val image = if (value.contains(Constants.MP4)) BaseUtils.getImageUrl(
                  context,
                  imageName = "${value.split(".")[0]}_thumb.webp"
              ) else BaseUtils.getImageUrl(context, value)
              val viewMode = BaseUtils.getImageUrl(context, value)
              if (!userImages.containsKey(value)) {
                  userImages[value] = image
              }

              if (!viewModeUserImages.containsKey(value)) {
                  viewModeUserImages[value] = viewMode
              }
              Log.d("USER_IMAGE", "key $userImages value${userImages[value]}")
          }

      }*/

    fun saveAdsCount(lifecycleOwner: LifecycleCoroutineScope) {
        lifecycleOwner.launch {
            adsCountForMixpanelEvent++
            preferencesHelper.setValue(ADS_COUNT, adsCountForMixpanelEvent)
        }
    }

    fun clearEverything() {
        saveData = null
        isLocationUpdated = false
        profileData = MutableLiveData()
//        userImages.clear()
//        viewModeUserImages.clear()
        locationPosition = 5
        adsCountForMixpanelEvent = 0
        settingsList.clear()
    }

    fun getReportOptions() = dataRepository.getReportOptions().asLiveData()

    private fun getReportOptionsFromPreferences() {
        runBlocking {
            val data =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.REPORT_OPTIONS).first()
                    ?: ""
            if (data.isNotEmpty())
                reportData = Gson().fromJson(data, ReportData::class.java) as ReportData
        }
    }

}





