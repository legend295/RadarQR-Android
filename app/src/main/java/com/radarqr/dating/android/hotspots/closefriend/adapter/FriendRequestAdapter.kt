package com.radarqr.dating.android.hotspots.closefriend.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.base.ProgressViewHolder
import com.radarqr.dating.android.data.model.FriendList
import com.radarqr.dating.android.data.model.InvitedUsers
import com.radarqr.dating.android.data.model.Users
import com.radarqr.dating.android.databinding.LayoutFriendRequestItemBinding
import com.radarqr.dating.android.databinding.ProgressBarBinding
import com.radarqr.dating.android.hotspots.closefriend.CloseFriendAndRequestViewModel
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.visible

class FriendRequestAdapter(
    val list: ArrayList<InvitedUsers?>,
    val closeFriendAndRequestViewModel: CloseFriendAndRequestViewModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var clickHandler: (View, InvitedUsers, Int) -> Unit
    lateinit var openProfile: (String, Int) -> Unit

    companion object {
        const val VIEW = 0
        const val EMPTY = 1
    }

    inner class ViewHolder(val binding: LayoutFriendRequestItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun init() {
            val data = list[absoluteAdapterPosition]
            data ?: return
            data.addPositionsToMap()
            binding.tvName.text = data.requester_info?.name
            binding.tvUserName.text =
                if (data.requester_info?.username.isNullOrEmpty()) "" else "@" + data.requester_info?.username
            binding.ivUser.loadImage(data.requester_info?.profile_pic)

            binding.ivAcceptRequest.visible(isVisible = data.requester_info != null)
            binding.ivRejectRequest.visible(isVisible = data.requester_info != null)


            binding.root.setOnClickListener {
                data.requester_info?._id?.let { it1 -> openProfile(it1, absoluteAdapterPosition) }

            }
            binding.ivAcceptRequest.setOnClickListener {
                clickHandler(it, data, absoluteAdapterPosition)
            }
            binding.ivRejectRequest.setOnClickListener {
                clickHandler(it, data, absoluteAdapterPosition)
            }


        }

        private fun InvitedUsers.addPositionsToMap() {
            // store user id
            val id = if (HomeActivity.loggedInUserId == requester_id) friend_id else requester_id
            CloseFriendAndRequestViewModel.requestsPositionsMap[id] =
                Pair(absoluteAdapterPosition, this)


            closeFriendAndRequestViewModel.updateItem(id, CloseFriendAdapter.RequestStatus.RECEIVED)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW) {
            val binding = LayoutFriendRequestItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            ViewHolder(binding)
        } else {
            val binding =
                ProgressBarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            ProgressViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW -> (holder as ViewHolder).init()
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int =
        if (list[position] == null) EMPTY else VIEW
}