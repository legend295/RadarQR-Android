package com.radarqr.dating.android.hotspots.closefriend

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.radarqr.dating.android.data.model.*
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.hotspots.closefriend.adapter.CloseFriendAdapter
import com.radarqr.dating.android.ui.home.main.HomeActivity

class CloseFriendAndRequestViewModel(private val dataRepository: DataRepository) : ViewModel() {

    val closeFriendList = ArrayList<CloseFriendUser?>()

    val list = ArrayList<Any?>()
    var isLastPage = false
    var isLoading = false
    var searchPageNo = 1
    var closeFriendPageNo = 1
    var closeFriendTotalCount = 0



    companion object {
        // always keep key as user id
        var closeFriendPositionsMap = HashMap<String, Pair<Int, Any>>()
        var requestsPositionsMap = HashMap<String, Pair<Int, InvitedUsers>>()
    }

    var requestCountHelper: RequestCountHelper? = null

    //if accepted // store item in temporary object and when user go back to the previous screen check if item is not null then add that item into the list and null that object
    // if user stays on profile details and remove that user then we don't need to add that into the list when user go back to previous screen
    // and null that object if user remove from close friend
    // and after remove if user add that user again to close friend than also we don't need to add that user because request is sent by us
    //else // we don't need to add that user in the list
    val requesterInfo = HashMap<String, RequesterInfo>()

    // this stores clicked item position so that item can be updated in profile details to prevent api call
    var itemClickedPosition = -1

    var friendRequestPageNo = 1
    var isFriendRequestLastPage = false
    var isFriendRequestLoading = false
    var friendRequestTotalCount = 0

    val friendRequestList = ArrayList<InvitedUsers?>()
    var friendRequestItemClickedPosition = -1

    fun getCloseFriends(pageNo: Int, limit: Int) =
        dataRepository.getFriendList(pageNo, limit).asLiveData()

    fun getCloseFriendInvitations(pageNo: Int, limit: Int) =
        dataRepository.getAllInvites(pageNo, limit).asLiveData()

    fun searchUser(request: SearchByUsername) =
        dataRepository.searchByUsername(request).asLiveData()

    fun sendRequest(request: SendInviteRequest) =
        dataRepository.sendInvitation(request).asLiveData()

    fun acceptInvitation(request: AcceptInviteRequest) =
        dataRepository.acceptInvitation(request).asLiveData()

    fun undoInvite(request: SendInviteRequest) =
        dataRepository.undoInvite(request).asLiveData()

    fun removeFriend(request: SendInviteRequest) =
        dataRepository.removeFriend(request).asLiveData()

    fun rejectInvite(request: AcceptInviteRequest) =
        dataRepository.rejectInvite(request).asLiveData()

    fun addItemToCloseFriend(callback: () -> Unit) {
        requesterInfo.values.forEach {
            it.let {
                closeFriendTotalCount += 1
                val item = CloseFriendUser(
                    _id = "", FriendInfo(HomeActivity.loggedInUserId, "", "", ""),
                    it,
                    friend_action = true,
                    friend_id = HomeActivity.loggedInUserId,
                    requester_action = true,
                    requester_id = it._id,
                    requestStatus = CloseFriendAdapter.RequestStatus.ALREADY_ADDED
                )
                list.add(0, item)
                callback()
            }
        }
    }

    /*
    * User A search user B from close friend tab
    * now user B send the request to user A
    * User A switched to request tab user B loads
    * now user A again switch back to close friends
    * status for that user will change or not
    *
    * this will solve the above issue
    * */
    fun updateItem(id: String, statue: CloseFriendAdapter.RequestStatus) {
        if (closeFriendPositionsMap.containsKey(id)) {
            val item = closeFriendPositionsMap[id]?.second
            val position = closeFriendPositionsMap[id]?.first
            if (item is Users && position != null && position < list.size) {
                item.requestStatus = statue
                item.friendlist = FriendList(requester_id = id)
                list[position] = item
            }
        }
    }

    fun resetData() {
        closeFriendList.clear()

        list.clear()
        isLastPage = false
        isLoading = false
        searchPageNo = 1
        closeFriendPageNo = 1
        friendRequestPageNo = 1
        closeFriendTotalCount = 0

        itemClickedPosition = -1

        requesterInfo.clear()

        isFriendRequestLastPage = false
        isFriendRequestLoading = false
        friendRequestTotalCount = 0
        friendRequestList.clear()
        friendRequestItemClickedPosition = -1

        closeFriendPositionsMap.clear()
        requestsPositionsMap.clear()
    }

    interface RequestCountHelper {
        fun setCount(count: Int)
    }
}