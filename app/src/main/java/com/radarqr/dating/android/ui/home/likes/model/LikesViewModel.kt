package com.radarqr.dating.android.ui.home.likes.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.radarqr.dating.android.data.model.LikeRequest
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.ui.home.likenew.LikeFragment
import com.radarqr.dating.android.ui.welcome.mobileLogin.AcceptRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.utility.PreferencesHelper


class LikesViewModel constructor(
    private val dataRepository: DataRepository,
    private val preferencesHelper: PreferencesHelper
) :
    ViewModel() {

    /*#region According to new flow*/


    var allLikesRequest = LikeRequest(category = LikeFragment.LikeType.ALL_LIKES.value())
    var onlineLikeRequest = LikeRequest(category = LikeFragment.LikeType.ONLINE.value())
    var inPersonLikeRequest = LikeRequest(category = LikeFragment.LikeType.IN_PERSON.value())

//    var allLikesMap = LinkedHashMap<String, UserLikes>()
//    var onlineLikesMap = LinkedHashMap<String, UserLikes>()
//    var inPersonLikesMap = LinkedHashMap<String, UserLikes>()

    var allLikesList: ArrayList<UserLikes?> = ArrayList()
    var onlineLikesList: ArrayList<UserLikes?> = ArrayList()
    var inPersonLikesList: ArrayList<UserLikes?> = ArrayList()

    var allLikesIsLoading = false
    var onlineLikesIsLoading = false
    var inPersonLikesIsLoading = false
    var allLikesIsLastPage = false
    var onlineLikesIsLastPage = false
    var inPersonLikesIsLastPage = false
    /*#endregion*/

    var listItem: MutableList<UserLikes> = ArrayList()
    var parentList: ArrayList<UserLikes> = ArrayList()

    var tabSelectedPosition: MutableLiveData<Int> = MutableLiveData()

    var likeData = LikeData()

    var isProfileSelected: MutableLiveData<Boolean> = MutableLiveData()

    var allSize: MutableLiveData<Int> = MutableLiveData()
    var onlineSize: MutableLiveData<Int> = MutableLiveData()
    var inPersonSize: MutableLiveData<Int> = MutableLiveData()

    var allUserObject: MutableLiveData<UserLikes> = MutableLiveData()
    var onlineUserObject: MutableLiveData<UserLikes> = MutableLiveData()
    var inPersonUserObject: MutableLiveData<UserLikes> = MutableLiveData()

    //    var imageUrlHashmap: HashMap<String, LikeImageModel> = HashMap()
//    var imageHashmap: HashMap<String, LikeImageModel> = HashMap()
    var itemSelectedPosition = 0
    var isLikeRequestClicked = false
    var isAllTopViewSelected = false
    var isOnlineTopViewSelected = false
    var isInPersonTopViewSelected = false
    var totalCount: Int = 0
    var onlineCount: Int = 0
    var inPersonCount: Int = 0

    var handler: Handler? = null

    init {
//        getImagesFromSession()
    }


    fun getLikes(pageno: Int, limit: Int, category: String) =
        dataRepository.getLikesApi(pageno, limit, category).asLiveData()

    fun getProfile(getPRofileApiRequest: getProfileRequest) =
        dataRepository.getProfileApi(getPRofileApiRequest).asLiveData()

    fun acceptRequest(acceptRequest: AcceptRequest) =
        dataRepository.acceptRequest(acceptRequest).asLiveData()


    fun clearEverything() {
        listItem.clear()
        allLikesList.clear()
        onlineLikesList.clear()
        inPersonLikesList.clear()
        tabSelectedPosition.value = 0

        likeData = LikeData()

        isProfileSelected.value = false

        allSize.value = 0
        onlineSize.value = 0
        inPersonSize.value = 0

        allUserObject.value = null
        onlineUserObject.value = null
        inPersonUserObject.value = null

//        imageUrlHashmap.clear()
//        imageHashmap.clear()
        itemSelectedPosition = 0
        isLikeRequestClicked = false
        isAllTopViewSelected = false
        isOnlineTopViewSelected = false
        isInPersonTopViewSelected = false
        totalCount = 0
        onlineCount = 0
        inPersonCount = 0


        allLikesRequest = LikeRequest(category = LikeFragment.LikeType.ALL_LIKES.value())
        onlineLikeRequest = LikeRequest(category = LikeFragment.LikeType.ONLINE.value())
        inPersonLikeRequest = LikeRequest(category = LikeFragment.LikeType.IN_PERSON.value())

//        allLikesMap = LinkedHashMap()
//        onlineLikesMap = LinkedHashMap()
//        inPersonLikesMap = LinkedHashMap()

        allLikesIsLoading = false
        onlineLikesIsLoading = false
        inPersonLikesIsLoading = false
        allLikesIsLastPage = false
        onlineLikesIsLastPage = false
        inPersonLikesIsLastPage = false
    }

    interface Handler {
        fun openBottomSheet(data: UserLikes, isSuccess: () -> Unit)
    }

}





