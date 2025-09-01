package com.radarqr.dating.android.hotspots.closefriend

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.AcceptInviteRequest
import com.radarqr.dating.android.data.model.InvitedUsers
import com.radarqr.dating.android.databinding.FragmentFriendRequestBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.closefriend.adapter.CloseFriendAdapter
import com.radarqr.dating.android.hotspots.closefriend.adapter.FriendRequestAdapter
import com.radarqr.dating.android.hotspots.helpers.confirmCloseFriend
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.utility.PaginationScrollListener
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class FriendRequestFragment : VenueBaseFragment<FragmentFriendRequestBinding>() {


    private val closeFriendAndRequestViewModel: CloseFriendAndRequestViewModel by viewModel()
    private val adapter by lazy {
        FriendRequestAdapter(
            closeFriendAndRequestViewModel.friendRequestList,
            closeFriendAndRequestViewModel
        )
    }

    private val limit = 10

    override fun getLayoutRes(): Int = R.layout.fragment_friend_request

    override fun init(view: View, savedInstanceState: Bundle?) {
        bindRecyclerView()
        if (closeFriendAndRequestViewModel.friendRequestList.isEmpty()) {
            binding.progressBarData.visible(isVisible = true)
            getCloseFriendInvitations()
        } else getCloseFriendInvitations()

        binding.swipeRefreshLayout.setOnRefreshListener {
            closeFriendAndRequestViewModel.friendRequestPageNo = 1
            closeFriendAndRequestViewModel.isFriendRequestLastPage = false
            closeFriendAndRequestViewModel.isFriendRequestLoading = false
            getCloseFriendInvitations()
        }
    }

    private fun bindRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        binding.rvFriendRequest.apply {
            adapter = this@FriendRequestFragment.adapter
            layoutManager = linearLayoutManager
        }

        adapter.apply {
            openProfile = { id, position ->
                closeFriendAndRequestViewModel.friendRequestItemClickedPosition = position
                val bundle = Bundle().apply {
                    putString(Constants.USER_ID, id)
                    putSerializable(Constants.EXTRA, CloseFriendAdapter.RequestStatus.RECEIVED)
                    putInt(Constants.FROM, ProfileFragment.FROM_FRIEND_REQUEST)
                    putBoolean(Constants.TYPE, false)
                }
                this@FriendRequestFragment.view?.findNavController()
                    ?.navigate(R.id.profileFragment, bundle)
            }

            // Like-dislike click handler
            clickHandler = { view, data, position ->
                data.requester_info?.name?.let {
                    requireContext().confirmCloseFriend(
                        view,
                        it
                    ) { v, dialog, layout ->
                        if (v.id == R.id.tv_continue) {
                            if (view.id == R.id.ivAcceptRequest) {
                                data.acceptInvite(position, dialog, layout.progressBarApi)
                            } else {
                                data.rejectInvite(position, dialog, layout.progressBarApi)
                            }
                        } else {
                            data.rejectInvite(position, dialog, layout.rejectProgressBarApi)
                        }
                    }
                }
            }
        }

        binding.rvFriendRequest.addOnScrollListener(object :
            PaginationScrollListener(linearLayoutManager) {
            override fun loadMoreItems() {
                closeFriendAndRequestViewModel.friendRequestPageNo += 1
                getCloseFriendInvitations()
            }

            override val isLastPage: Boolean
                get() = closeFriendAndRequestViewModel.isFriendRequestLastPage
            override val isLoading: Boolean
                get() = closeFriendAndRequestViewModel.isFriendRequestLoading
        })
    }

    private fun InvitedUsers.rejectInvite(
        position: Int,
        dialog: BottomSheetDialog,
        view: ProgressBar
    ) {
        //reject close friend request
        view.visible(isVisible = true)
        requester_id.rejectInvite { isSuccess ->
            view.visible(isVisible = false)
            if (isSuccess) {
                val id =
                    if (HomeActivity.loggedInUserId == requester_id) friend_id else requester_id
                closeFriendAndRequestViewModel.updateItem(id, CloseFriendAdapter.RequestStatus.ADD)
                position.removeItemFromAdapter()
                dialog.dismiss()
            }
        }
    }

    private fun InvitedUsers.acceptInvite(
        position: Int,
        dialog: BottomSheetDialog,
        view: ProgressBar
    ) {
        // accept close friend request
        view.visible(isVisible = true)
        requester_id.acceptCloseFriendRequest { isSuccess, statusCode, message ->
            view.visible(isVisible = false)
            if (isSuccess) {
                //
                val id =
                    if (HomeActivity.loggedInUserId == requester_id) friend_id else requester_id
                closeFriendAndRequestViewModel.updateItem(
                    id,
                    CloseFriendAdapter.RequestStatus.ALREADY_ADDED
                )

                requester_info?._id?.let {
                    closeFriendAndRequestViewModel.requesterInfo[requester_info._id] =
                        requester_info
                }
                position.removeItemFromAdapter()
                dialog.dismiss()
            } else {
                statusCode?.let {
                    if (it == 422) {
                        message?.let { message ->
                            requireContext().showToast(message)
                        }
                        position.removeItemFromAdapter()
                        dialog.dismiss()
                    }
                }
            }
        }
    }

    private fun Int.removeItemFromAdapter() {
        closeFriendAndRequestViewModel.friendRequestList.removeAt(this)
        adapter.notifyItemRemoved(this)
        if (closeFriendAndRequestViewModel.friendRequestTotalCount > 0)
            closeFriendAndRequestViewModel.friendRequestTotalCount -= 1
        closeFriendAndRequestViewModel.isFriendRequestLastPage =
            closeFriendAndRequestViewModel.friendRequestList.size >= closeFriendAndRequestViewModel.friendRequestTotalCount

        closeFriendAndRequestViewModel.requestCountHelper?.setCount(
            closeFriendAndRequestViewModel.friendRequestTotalCount
        )

        binding.tvEmptyMessage.visible(closeFriendAndRequestViewModel.friendRequestList.isEmpty())
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getCloseFriendInvitations() {
        if (view != null && isAdded) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.getCloseFriendInvitations(
                    closeFriendAndRequestViewModel.friendRequestPageNo,
                    limit
                )
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                if (it.statusCode == 404) {
                                    closeFriendAndRequestViewModel.requestCountHelper?.setCount(0)
                                    closeFriendAndRequestViewModel.friendRequestPageNo = 1
                                    CloseFriendAndRequestViewModel.requestsPositionsMap.clear()
                                    closeFriendAndRequestViewModel.friendRequestList.clear()
                                    adapter.notifyDataSetChanged()
                                }
                                binding.tvEmptyMessage.visible(closeFriendAndRequestViewModel.friendRequestList.isEmpty())
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                // set total count to the variable
                                closeFriendAndRequestViewModel.friendRequestTotalCount =
                                    it.data.data?.total_count ?: 0

                                // set count to the interface which will help to update the request count
                                closeFriendAndRequestViewModel.requestCountHelper?.setCount(
                                    closeFriendAndRequestViewModel.friendRequestTotalCount
                                )
                                if (closeFriendAndRequestViewModel.friendRequestPageNo == 1) {
                                    closeFriendAndRequestViewModel.friendRequestList.clear()
                                    CloseFriendAndRequestViewModel.requestsPositionsMap.clear()
                                }

                                closeFriendAndRequestViewModel.friendRequestList.addAll(
                                    it.data.data?.users ?: ArrayList()
                                )

                                adapter.notifyDataSetChanged()

                                closeFriendAndRequestViewModel.isFriendRequestLastPage =
                                    closeFriendAndRequestViewModel.friendRequestList.size >= closeFriendAndRequestViewModel.friendRequestTotalCount

                                binding.tvEmptyMessage.visible(closeFriendAndRequestViewModel.friendRequestList.isEmpty())
                            }
                        }
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.progressBarData.visible(isVisible = false)
                    }
            }
        }
    }

    private fun String.acceptCloseFriendRequest(callback: (Boolean, Int?, String?) -> Unit) {
        if (view != null && isAdded && isVisible) {
            lifecycleScope.launch {
                closeFriendAndRequestViewModel.acceptInvitation(AcceptInviteRequest(requester_id = this@acceptCloseFriendRequest))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                callback(false, it.statusCode, it.message)
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                callback(true, null, null)
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

}