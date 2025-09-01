package com.radarqr.dating.android.ui.home.main.model

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.hotspots.helpers.showFifthAd
import com.radarqr.dating.android.hotspots.helpers.showFirstAd
import com.radarqr.dating.android.hotspots.helpers.showForthAd
import com.radarqr.dating.android.hotspots.helpers.showSecondAd
import com.radarqr.dating.android.hotspots.helpers.showSixthAd
import com.radarqr.dating.android.hotspots.helpers.showThirdAd
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.welcome.mobileLogin.*
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.SharedPrefsHelper


class GetRecommendationViewModel(val dataRepository: DataRepository) :
    ViewModel() {

    var isUndoVisible = false
    var lastCancelledId: String = ""
    var recommendationData: ArrayList<ProfileData> = ArrayList()

    //    var userList: LinkedHashMap<String, ProfileData?> = LinkedHashMap()
//    var nearYouUsersList: LinkedHashMap<String, ProfileData?> = LinkedHashMap()
    var nearYouList = ArrayList<ProfileData?>()
    var userRemovedIdsList = ArrayList<String>()
    var deletedUserData: ProfileData? = null
    var deletedNearYouUserData: ProfileData? = null
    var nearYouUsersImageUrl: LinkedHashMap<String, String> = LinkedHashMap()
    var isUndoPending = false

    var isLoading = false
    var isLastPage = false
    var profileSwipedCount = 0
    private var adsShowCount = arrayListOf(1, 2, 3, 4, 5, 6)
    private var currentAdsCount = 0
    var adsShowMax = 3

    /*if canceled from profile then while doing undo from recommendation we need to hit api so that list can be updated
    * if true hit api else don't hit api
    *  */
    var canceledFromProfile = false
    var request = NearYouRequest()

    fun getRecommendation(userRecommendationRequest: UserRecommendationRequest) =
        dataRepository.userRecomandation(userRecommendationRequest).asLiveData()

    fun getNearYouUsers() =
        dataRepository.getNearYouUsers(request).asLiveData()

    fun sendRequest(likeDislikeRequest: LikeDislikeRequest) =
        dataRepository.sendRequest(likeDislikeRequest).asLiveData()

    fun acceptRequest(acceptRequest: AcceptRequest) =
        dataRepository.acceptRequest(acceptRequest).asLiveData()

    fun reportUser(request: ReportRequest) =
        dataRepository.reportUser(request).asLiveData()

    fun getAccountSettings() = dataRepository.getAccountDetails().asLiveData()


    fun clear() {
//        userList = LinkedHashMap()
        recommendationData.clear()
        userRemovedIdsList = ArrayList()
        nearYouList = ArrayList()
        request = NearYouRequest()
        isUndoVisible = false
        lastCancelledId = ""
//        nearYouUsersList = LinkedHashMap()
        isLastPage = false
        isLoading = false
        isUndoPending = false
        deletedUserData = null
        deletedNearYouUserData = null
        nearYouUsersImageUrl = LinkedHashMap()
    }

    fun showAds(fragment: Fragment, callback: () -> Unit) {
        if (profileSwipedCount >= adsShowMax) {
            profileSwipedCount = 0
            //reset and shuffle
            if (currentAdsCount > adsShowCount.size - 1) {
                currentAdsCount = 0
                adsShowCount.shuffle()
            }
            fragment.showAdsPopUp(adsShowCount[currentAdsCount])
            currentAdsCount++
            callback()
        }else{
            callback()
        }
    }

    private fun Fragment.showAdsPopUp(count: Int) {
        when (count) {
            1 -> showFirstAd()
            2 -> showSecondAd()
            3 -> showThirdAd()
            4 -> showForthAd()
            5 -> showFifthAd()
            6 -> showSixthAd()
        }
    }
}





