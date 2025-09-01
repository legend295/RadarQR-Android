package com.radarqr.dating.android.hotspots.tag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.radarqr.dating.android.data.model.SearchCloseFriendRequest
import com.radarqr.dating.android.data.model.SearchUser
import com.radarqr.dating.android.data.repository.DataRepository

class TagViewModel(private val dataRepository: DataRepository) : ViewModel() {

    var isLastPage = false
    var isLoading = false
    var searchPageNo = 1

    var selectedSearchedUser: SearchUser? = null
    var tagModel: TagModel? = null
    var taggedUsersList = HashMap<String, TagModel>()

    fun searchCloseFriend(request: SearchCloseFriendRequest) =
        dataRepository.searchCloseFriends(request).asLiveData()


    fun clear() {
        tagModel = null
        selectedSearchedUser = null
        taggedUsersList.clear()
    }

    data class TagModel(
        var id: String = "",
        var taggedUserId: String = "",
        var taggedUserName: String = "",
        var taggedUserImage: String = "",
        var xCord: Int = 0,
        var yCord: Int = 0,
        var name: String = ""
    )
}