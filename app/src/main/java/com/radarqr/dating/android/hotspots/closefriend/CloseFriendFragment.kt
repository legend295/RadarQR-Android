package com.radarqr.dating.android.hotspots.closefriend

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.*
import com.radarqr.dating.android.databinding.FragmentCloseFriendBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.closefriend.adapter.CloseFriendAdapter
import com.radarqr.dating.android.hotspots.helpers.addACloseFriend
import com.radarqr.dating.android.hotspots.helpers.confirmCloseFriend
import com.radarqr.dating.android.hotspots.helpers.removeCloseFriend
import com.radarqr.dating.android.hotspots.helpers.undoRequest
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment.Companion.FROM_VENUE_SINGLES
import com.radarqr.dating.android.utility.DebouncingQueryTextListener
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper.PropertiesKey
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class CloseFriendFragment : VenueBaseFragment<FragmentCloseFriendBinding>() {

    private val closeFriendAndRequestViewModel: CloseFriendAndRequestViewModel by viewModel()
    private val mixPanelWrapper: MixPanelWrapper by inject()

    val adapter by lazy {
        CloseFriendAdapter(
            closeFriendAndRequestViewModel.list,
            closeFriendAndRequestViewModel
        )
    }
    var searchText = ""

    override fun getLayoutRes(): Int = R.layout.fragment_close_friend

    override fun init(view: View, savedInstanceState: Bundle?) {
        // if list is empty then fetch close friends data
        if (closeFriendAndRequestViewModel.list.isEmpty()) {
            // if list is already empty then we don't need this object so make sure to make this null so that duplicate item won't add into the list
            closeFriendAndRequestViewModel.requesterInfo.clear()
            binding.progressBarData.visible(isVisible = true)
            getCloseFriends()
        } else {
            /**
             * if accepted - store item in temporary object and when user go back to the previous screen check if item is not null then add that item into the list and null that object
             * if user stays on profile details and remove that user then we don't need to add that into the list when user go back to previous screen
             * and null that object if user remove from close friend
             * and after remove if user add that user again to close friend than also we don't need to add that user because request is sent by us
             * else - we don't need to add that user in the list
             */
            closeFriendAndRequestViewModel.requesterInfo.isNotEmpty().let {
                // if user is searching then we don't need to add item
                // because if user clear the search field then api will run automatically so we don't need to add item
                // if we still add item then 2 item will be visible in search data when we come back from profile details screen
                if (searchText.isEmpty() && it)
                    closeFriendAndRequestViewModel.addItemToCloseFriend {
                        adapter.notifyItemInserted(0)
                    }
                closeFriendAndRequestViewModel.requesterInfo.clear()
            }
        }
        // this position is being reset to -1 so that we do not update any item mistakenly
        //this stores clicked item position so that item can be updated in profile details to prevent api call
        closeFriendAndRequestViewModel.itemClickedPosition = -1

        bindRecyclerView()

        // Search view
        binding.searchViewFriend.addTextChangedListener(DebouncingQueryTextListener(lifecycle) {
            // added hasFocus() check, to prevent unwanted api call when switching through the tabs
            searchText = it ?: ""
            if ((it?.length ?: 0) >= 3 && binding.searchViewFriend.hasFocus()) {
                it?.apply {
                    closeFriendAndRequestViewModel.searchPageNo = 1
                    closeFriendAndRequestViewModel.isLastPage = false
                    binding.progressBarSearch.visible(isVisible = true)
                    searchUser(it.toString())
                }
            }

            if (it.isNullOrEmpty() && binding.searchViewFriend.hasFocus()) {
                closeFriendAndRequestViewModel.closeFriendPageNo = 1
                closeFriendAndRequestViewModel.isLastPage = false
                getCloseFriends()
            }
        })
        /* binding.searchViewFriend.doAfterTextChanged {
             if ((it?.length ?: 0) >= 3) {
                 it?.apply {
                     closeFriendAndRequestViewModel.searchPageNo = 1
                     closeFriendAndRequestViewModel.isLastPage = false
                     binding.progressBarSearch.visible(isVisible = true)
                     searchUser(it.toString())
                 }
             }

             if (it.isNullOrEmpty() && binding.searchViewFriend.hasFocus()) {
                 closeFriendAndRequestViewModel.closeFriendPageNo = 1
                 closeFriendAndRequestViewModel.isLastPage = false
                 getCloseFriends()
             }
         }*/


        //Swipe refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            // reset all the viewModel fields
            closeFriendAndRequestViewModel.closeFriendPageNo = 1
            closeFriendAndRequestViewModel.isLastPage = false
            closeFriendAndRequestViewModel.isLoading = false
            closeFriendAndRequestViewModel.searchPageNo = 1
            closeFriendAndRequestViewModel.list.clear()
            closeFriendAndRequestViewModel.closeFriendList.clear()

            binding.searchViewFriend.setText("")
            if (!binding.searchViewFriend.hasFocus())
                getCloseFriends()
        }
    }

    private fun bindRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvFriends.apply {
            adapter = this@CloseFriendFragment.adapter
            this.layoutManager = layoutManager
        }

        adapter.apply {
            // open dialog when user want to add friend to close friend
            sendRequestClick = { user, position ->
                hideKeyboard(binding.rvFriends)
                Handler(Looper.getMainLooper()).postDelayed({
                    requireContext().addACloseFriend(user.name) { _, dialog, layout ->
                        layout.progressBarApi.visible(isVisible = true)
                        user.sendInvitation(position) { isSuccess ->
                            layout.progressBarApi.visible(isVisible = false)
                            dialog.dismiss()
                        }
                    }
                }, 100)

            }

            // redirect to profile detail screen
            openProfile = { id, requestStatus, position ->
                closeFriendAndRequestViewModel.itemClickedPosition = position
                val bundle = Bundle().apply {
                    putString(Constants.USER_ID, id)
                    putSerializable(Constants.EXTRA, requestStatus)
                    putInt(Constants.FROM, ProfileFragment.FROM_CLOSE_FRIEND)
                    putBoolean(Constants.TYPE, false)
                }
                this@CloseFriendFragment.view?.findNavController()
                    ?.navigate(R.id.profileFragment, bundle)
            }

            // remove closed friends
            clickHandler = { data, position, requestStatus ->
                hideKeyboard(binding.rvFriends)
                // START THIS ACTION AFTER SOMETIME TO PREVENT FLUCTUATION BECAUSE OF KEYBOARD HIDE
                Handler(Looper.getMainLooper()).postDelayed({
                    when (requestStatus) {
                        CloseFriendAdapter.RequestStatus.ADD -> {}
                        CloseFriendAdapter.RequestStatus.SENT -> {
                            if (data is Users) data.undoRequest(position)
                        }

                        CloseFriendAdapter.RequestStatus.RECEIVED -> {
                            if (data is Users) data.confirmCloseFriendDialog(position)
                        }

                        CloseFriendAdapter.RequestStatus.ALREADY_ADDED -> {
                            data.removeFriend(position)
                        }
                    }
                }, 100)
            }
        }

        // THIS IS DONE LIKE THIS BECAUSE WE NEED TO HIDE KEYBOARD WHEN USER STARTS TO SCROLLING ELSE WE WILL USER PAGINATION-SCROLL-LISTENER CLASS
        binding.rvFriends.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!closeFriendAndRequestViewModel.isLoading && !closeFriendAndRequestViewModel.isLastPage) {
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                        && firstVisibleItemPosition >= 0
                    ) {
                        closeFriendAndRequestViewModel.isLoading = true
                        adapter.addLoader()
                        if (binding.searchViewFriend.text.isNullOrEmpty()) {
                            closeFriendAndRequestViewModel.closeFriendPageNo += 1
                            getCloseFriends()
                        } else {
                            closeFriendAndRequestViewModel.searchPageNo += 1
                            searchUser(binding.searchViewFriend.text.toString())
                        }
                    }
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    // IN TIME OF DRAGGING AND SETTLING  HIDE THE KEYBOARD
                    RecyclerView.SCROLL_STATE_DRAGGING, RecyclerView.SCROLL_STATE_SETTLING -> {
                        hideKeyboard(recyclerView)
                    }
                }
            }
        })
    }

    // UNDO THE SEND REQUEST BY OPENING THE CONFIRMATION DIALOG
    private fun Users.undoRequest(position: Int) {
        requireContext().undoRequest("") { _, dialog, layout ->
            layout.progressBarApi.visible(isVisible = true)
            friendlist?.friend_id?.undoInvite { isSuccess ->
                layout.progressBarApi.visible(isVisible = false)
                if (isSuccess) {
                    // after undo make the status to ADD so that user can again send request
                    requestStatus =
                        CloseFriendAdapter.RequestStatus.ADD
                    position.updateItemInAdapter(this)
                    dialog.dismiss()
                }
            }
        }
    }

    private fun Any.removeFriend(position: Int) {
        requireContext().removeCloseFriend("") { _, dialog, layout ->
            layout.progressBarApi.visible(isVisible = true)
            val id =
                if (this is Users) this._id
                else if (this is CloseFriendUser)
                    if (HomeActivity.loggedInUserId == this.requester_id) this.friend_id
                    else this.requester_id
                else ""
            id.removeFriend(this) { isSuccess, statusCode ->
                layout.progressBarApi.visible(isVisible = false)
                if (isSuccess || statusCode == 422) {
                    if (this is Users) {
                        requestStatus = CloseFriendAdapter.RequestStatus.ADD
                        position.updateItemInAdapter(this)
                    } else {
                        // remove item from list, refresh adapter and decrease total count by 1 so that pagination will work fine
                        closeFriendAndRequestViewModel.list.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        if (closeFriendAndRequestViewModel.closeFriendTotalCount > 0)
                            closeFriendAndRequestViewModel.closeFriendTotalCount -= 1
                        adapter.notifyItemRangeChanged(
                            position,
                            closeFriendAndRequestViewModel.list.size
                        )
                        closeFriendAndRequestViewModel.list.isEmpty().setEmptyMessage()
                    }
                    dialog.dismiss()
                }
            }
        }

    }

    // OPEN
    private fun Users.confirmCloseFriendDialog(position: Int) {
        requireContext().confirmCloseFriend(null, "") { v, dialog, layout ->
            if (v.id == R.id.tv_continue) {
                // accept close friend request
                acceptCloseFriendRequest(position, layout.progressBarApi, dialog)
            } else {
                //reject close friend request dialog
                rejectInvite(position, layout.rejectProgressBarApi, dialog)

            }
        }
    }

    // OPEN BOTTOM-SHEET FOR ACCEPT AND DECLINE FRIEND REQUEST CONFIRMATION
    private fun Users.acceptCloseFriendRequest(
        position: Int,
        view: View,
        dialog: BottomSheetDialog
    ) {
        _id.let {
            view.visible(isVisible = true)
            // API CALL FOR ACCEPT CLOSE FRIEND
            it.acceptCloseFriendRequest { isSuccess ->
                view.visible(isVisible = false)
                if (isSuccess) {
                    // update status to ALREADY_ADDED so that user will not able to send request as user is already added
                    requestStatus =
                        CloseFriendAdapter.RequestStatus.ALREADY_ADDED
                    position.updateItemInAdapter(this)
                    dialog.dismiss()
                }
            }
        }

    }

    private fun Users.rejectInvite(
        position: Int,
        view: View,
        dialog: BottomSheetDialog
    ) {
        friendlist?.requester_id?.let {
            view.visible(isVisible = true)

            // API CALL FOR REJECT THE INVITATION
            it.rejectInvite { isSuccess ->
                view.visible(isVisible = false)
                if (isSuccess) {
                    // update status to ADD if user reject the request so that user can send request again
                    requestStatus =
                        CloseFriendAdapter.RequestStatus.ADD
                    position.updateItemInAdapter(this)
                    dialog.dismiss()
                }
            }
        }
    }

    // UPDATE ITEM IN ADAPTER ON THE GIVEN POSITION
    private fun Int.updateItemInAdapter(item: Any) {
        if (item is Users) {
            closeFriendAndRequestViewModel.list[this] = item
            adapter.notifyItemChanged(this)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getCloseFriends() {
        false.setSearchEmptyMessage()
        if (view != null && isAdded) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.getCloseFriends(
                    closeFriendAndRequestViewModel.closeFriendPageNo,
                    limit = 10
                ).observe(viewLifecycleOwner) {
                    binding.swipeRefreshLayout.isRefreshing = false
                    adapter.removeLoader()
                    closeFriendAndRequestViewModel.isLoading = false
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            binding.progressBarData.visible(isVisible = false)
                            if (it.statusCode == 404) {
                                closeFriendAndRequestViewModel.closeFriendPageNo = 1
                                closeFriendAndRequestViewModel.list.clear()
                                CloseFriendAndRequestViewModel.closeFriendPositionsMap.clear()
                                adapter.notifyDataSetChanged()
                            }
                            closeFriendAndRequestViewModel.list.isEmpty().setEmptyMessage()
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            closeFriendAndRequestViewModel.closeFriendTotalCount =
                                (it.data.data?.total_count ?: 0)

                            // IF PAGE NUMBER IF 1 THEN CLEAR THE LIST BECAUSE WE WILL CLEAR ITEM WHEN LOADING FROM START
                            if (closeFriendAndRequestViewModel.closeFriendPageNo == 1) {
                                closeFriendAndRequestViewModel.list.clear()
                                CloseFriendAndRequestViewModel.closeFriendPositionsMap.clear()
                                closeFriendAndRequestViewModel.closeFriendList.clear()
                            }
                            closeFriendAndRequestViewModel.closeFriendList.addAll(
                                it.data.data?.users ?: ArrayList()
                            )

                            closeFriendAndRequestViewModel.list.addAll(
                                it.data.data?.users ?: ArrayList()
                            )

                            closeFriendAndRequestViewModel.isLastPage =
                                closeFriendAndRequestViewModel.list.size >= closeFriendAndRequestViewModel.closeFriendTotalCount
                            binding.progressBarData.visible(isVisible = false)
                            adapter.notifyDataSetChanged()
                            closeFriendAndRequestViewModel.list.isEmpty().setEmptyMessage()
                        }
                    }
                }
            }
        }
    }

    // SET EMPTY MESSAGE WHEN THERE ARE NO FRIENDS FOUND
    private fun Boolean.setEmptyMessage() {
        binding.ivEmptyHolder.visible(isVisible = this)
        binding.tvEmptyFirst.visible(isVisible = this)
        binding.tvEmptySecond.visible(isVisible = this)
    }

    // SET THIS EMPTY MESSAGE WHEN THERE ARE NO USER DURING SEARCH
    private fun Boolean.setSearchEmptyMessage() {
        binding.tvEmptyMessage.visible(isVisible = this)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchUser(value: String) {
        false.setEmptyMessage()
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.searchUser(
                    SearchByUsername(
                        value,
                        pageNo = closeFriendAndRequestViewModel.searchPageNo,
                        limit = 20
                    )
                ).observe(viewLifecycleOwner) {
                    binding.swipeRefreshLayout.isRefreshing = false
                    adapter.removeLoader()
                    closeFriendAndRequestViewModel.isLoading = false
                    binding.progressBarSearch.visible(isVisible = false)
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            if (it.statusCode == 404) {
                                closeFriendAndRequestViewModel.searchPageNo = 1
                                closeFriendAndRequestViewModel.list.clear()
                                CloseFriendAndRequestViewModel.closeFriendPositionsMap.clear()
                                adapter.notifyDataSetChanged()
                            }
                            closeFriendAndRequestViewModel.list.isEmpty().setSearchEmptyMessage()
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            if (closeFriendAndRequestViewModel.searchPageNo == 1) {
                                closeFriendAndRequestViewModel.list.clear()
                                CloseFriendAndRequestViewModel.closeFriendPositionsMap.clear()
                            }
                            it.data.data?.users?.forEach { user ->
                                user.requestStatus = if (user.friendlist?.close_friend == true) {
                                    CloseFriendAdapter.RequestStatus.ALREADY_ADDED
                                } else if (user.friendlist?.request_sent == true) {
                                    CloseFriendAdapter.RequestStatus.SENT
                                } else if (user.friendlist?.request_received == true) {
                                    CloseFriendAdapter.RequestStatus.RECEIVED
                                } else CloseFriendAdapter.RequestStatus.ADD
                            }
                            closeFriendAndRequestViewModel.list.addAll(
                                it.data.data?.users ?: ArrayList()
                            )
                            closeFriendAndRequestViewModel.isLastPage =
                                closeFriendAndRequestViewModel.list.size >= (it.data.data?.total_count
                                    ?: 0)
                            adapter.notifyDataSetChanged()
                            closeFriendAndRequestViewModel.list.isEmpty().setSearchEmptyMessage()
                        }
                    }
                }
            }
        }
    }

    private fun Users.sendInvitation(position: Int, callback: (Boolean) -> Unit) {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.sendRequest(SendInviteRequest(this@sendInvitation._id))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                callback(false)
                                it.message?.let { it1 -> requireContext().showToast(it1) }
                                if (it.statusCode == 422 && it.message == "Invite already exists") {
                                    this@sendInvitation.requestStatus =
                                        CloseFriendAdapter.RequestStatus.RECEIVED
                                    position.updateItemInAdapter(this@sendInvitation)
                                }
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                this@sendInvitation.requestStatus =
                                    CloseFriendAdapter.RequestStatus.SENT
                                it.data.data?.apply {
                                    this@sendInvitation.friendlist = FriendList(
                                        _id = this._id,
                                        friend_id = this.friend_id,
                                        requester_id = this.requester_id,
                                        request_sent = true
                                    )
                                }
                                closeFriendAndRequestViewModel.list[position] = this@sendInvitation
                                adapter.notifyItemChanged(position)
                                logSendCloseFriendRequest(this@sendInvitation)
                                callback(true)
                            }
                        }
                    }
            }
        }
    }

    private fun String.acceptCloseFriendRequest(callback: (Boolean) -> Unit) {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.acceptInvitation(AcceptInviteRequest(requester_id = this@acceptCloseFriendRequest))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                callback(false)
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                callback(true)
                            }
                        }
                    }
            }
        }
    }

    private fun String.rejectInvite(callback: (Boolean) -> Unit) {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.rejectInvite(AcceptInviteRequest(requester_id = this@rejectInvite))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                callback(false)
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                callback(true)
                            }
                        }
                    }
            }
        }
    }

    private fun String.undoInvite(callback: (Boolean) -> Unit) {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.undoInvite(SendInviteRequest(friend_id = this@undoInvite))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                callback(false)
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                callback(true)
                            }
                        }
                    }
            }
        }
    }

    private fun String.removeFriend(data: Any, callback: (Boolean, Int?) -> Unit) {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.removeFriend(SendInviteRequest(friend_id = this@removeFriend))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                callback(false, it.statusCode)
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                callback(true, it.statusCode)
                                logRemoveFriendRequest(data)
                            }
                        }
                    }
            }
        }
    }

    private fun logSendCloseFriendRequest(data: Users) {
        mixPanelWrapper.logSendCloseFriendRequest(JSONObject().apply {
            put(PropertiesKey.FROM_SCREEN, Constants.MixPanelFrom.OTHERS)
            data.let {
                put(PropertiesKey.PROFILE_AGE, Constants.MixPanelFrom.NA)
                put(PropertiesKey.PROFILE_GENDER, Constants.MixPanelFrom.NA)
                put(PropertiesKey.PROFILE_USERNAME, it.username)
                put(PropertiesKey.PROFILE_NAME, it.name)
                put(PropertiesKey.PROFILE_USER_LOCATION, Constants.MixPanelFrom.NA)
                put(PropertiesKey.PROFILE_LOCATION_CITY, Constants.MixPanelFrom.NA)
                put(PropertiesKey.PROFILE_LOCATION_STATE, Constants.MixPanelFrom.NA)
                put(PropertiesKey.PROFILE_DOB, Constants.MixPanelFrom.NA)
            }
        })
    }

    private fun logRemoveFriendRequest(data: Any?) {
        mixPanelWrapper.logRemoveFriendRequest(JSONObject().apply {
            put(PropertiesKey.FROM_SCREEN, Constants.MixPanelFrom.OTHERS)
            put(PropertiesKey.PROFILE_AGE, Constants.MixPanelFrom.NA)
            put(PropertiesKey.PROFILE_GENDER, Constants.MixPanelFrom.NA)
            if (data is Users) {
                put(PropertiesKey.PROFILE_USERNAME, data.username)
                put(PropertiesKey.PROFILE_NAME, data.name)
            } else if (data is CloseFriendUser) {
                if (HomeActivity.loggedInUserId == data.requester_id) {
                    // send friend info
                    put(
                        PropertiesKey.PROFILE_USERNAME,
                        data.friend_info?.username ?: Constants.MixPanelFrom.NA
                    )
                    put(
                        PropertiesKey.PROFILE_NAME,
                        data.friend_info?.name ?: Constants.MixPanelFrom.NA
                    )
                } else {
                    // send requester info
                    put(
                        PropertiesKey.PROFILE_USERNAME,
                        data.requester_info?.username ?: Constants.MixPanelFrom.NA
                    )
                    put(
                        PropertiesKey.PROFILE_NAME,
                        data.requester_info?.name ?: Constants.MixPanelFrom.NA
                    )
                }

            } else {
                put(PropertiesKey.PROFILE_USERNAME, Constants.MixPanelFrom.NA)
                put(PropertiesKey.PROFILE_NAME, Constants.MixPanelFrom.NA)
            }
            put(PropertiesKey.PROFILE_USER_LOCATION, Constants.MixPanelFrom.NA)
            put(PropertiesKey.PROFILE_LOCATION_CITY, Constants.MixPanelFrom.NA)
            put(PropertiesKey.PROFILE_LOCATION_STATE, Constants.MixPanelFrom.NA)
            put(PropertiesKey.PROFILE_DOB, Constants.MixPanelFrom.NA)

        })
    }
}