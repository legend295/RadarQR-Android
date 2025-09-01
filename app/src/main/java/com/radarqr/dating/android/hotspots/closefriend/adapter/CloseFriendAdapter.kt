package com.radarqr.dating.android.hotspots.closefriend.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.ProgressViewHolder
import com.radarqr.dating.android.data.model.CloseFriendUser
import com.radarqr.dating.android.data.model.Users
import com.radarqr.dating.android.databinding.LayoutCloseFriendItemBinding
import com.radarqr.dating.android.databinding.ProgressBarBinding
import com.radarqr.dating.android.hotspots.closefriend.CloseFriendAndRequestViewModel
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.visible

class CloseFriendAdapter<T : Any>(
    val list: ArrayList<T?>,
    val closeFriendAndRequestViewModel: CloseFriendAndRequestViewModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var sendRequestClick: (Users, Int) -> Unit
    lateinit var openProfile: (String, RequestStatus, Int) -> Unit
    lateinit var clickHandler: (T, Int, RequestStatus) -> Unit

    enum class RequestStatus {
        ADD, SENT, RECEIVED, ALREADY_ADDED
    }

    companion object {
        const val VIEW = 0
        const val EMPTY = 1
    }

    inner class CloseFriendViewHolder(val binding: LayoutCloseFriendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun init() {
            val data = list[absoluteAdapterPosition]
            data ?: return
            data.addPositionsToMap()
            if (data is CloseFriendUser) {
                val image: String
                val name: String
                val userName: String
                if (HomeActivity.loggedInUserId == data.requester_id) {
                    // show friend info
                    image = data.friend_info?.profile_pic ?: ""
                    name =
                        data.friend_info?.name ?: binding.root.context.getString(R.string.app_name)
                    userName = data.friend_info?.username ?: ""
                } else {
                    // show requester info
                    image = data.requester_info?.profile_pic ?: ""
                    name = data.requester_info?.name
                        ?: binding.root.context.getString(R.string.app_name)
                    userName = data.requester_info?.username ?: ""
                }
                binding.ivUser.loadImage(image)
                binding.tvName.text = name
                binding.tvUserName.text = StringBuilder().append("@").append(userName)
                binding.tvSendRequest.visible(isVisible = false)
                binding.tvRequestStatus.visible(isVisible = true)
                binding.tvRequestStatus.text = binding.root.context.getString(R.string.remove)
            } else if (data is Users) {
                binding.ivUser.loadImage(data.profile_pic)
                binding.tvName.text = data.name
                binding.tvUserName.text = StringBuilder().append("@").append(data.username)
                data.handleFriendStatus()
            }

            binding.root.setOnClickListener {
                val id =
                    if (data is CloseFriendUser)
                        if (HomeActivity.loggedInUserId == data.requester_id) data.friend_id
                        else data.requester_id
                    else if (data is Users) data._id
                    else ""
                val requestInfo =
                    when (data) {
                        is CloseFriendUser -> RequestStatus.ALREADY_ADDED
                        is Users -> data.requestStatus
                            ?: RequestStatus.ALREADY_ADDED
                        else -> RequestStatus.ALREADY_ADDED
                    }
                openProfile(id, requestInfo, absoluteAdapterPosition)
            }

            binding.tvSendRequest.setOnClickListener {
                sendRequestClick(data as Users, absoluteAdapterPosition)
            }

            binding.tvRequestStatus.setOnClickListener {
                val requestInfo =
                    when (data) {
                        is CloseFriendUser -> RequestStatus.ALREADY_ADDED
                        is Users -> data.requestStatus
                            ?: RequestStatus.ALREADY_ADDED
                        else -> RequestStatus.ALREADY_ADDED
                    }
                clickHandler(data, absoluteAdapterPosition, requestInfo)
            }
        }

        private fun Users.handleFriendStatus() {
            when (requestStatus) {
                RequestStatus.ADD -> {
                    binding.tvSendRequest.visible(isVisible = true)
                    binding.tvRequestStatus.visible(isVisible = false)
                }
                RequestStatus.SENT -> {
                    binding.tvRequestStatus.text =
                        binding.root.context.getString(R.string.pending)
                    binding.tvRequestStatus.visible(isVisible = true)
                    binding.tvSendRequest.visible(isVisible = false)
                }
                RequestStatus.RECEIVED -> {
                    binding.tvRequestStatus.text =
                        binding.root.context.getString(R.string.pending)
                    binding.tvRequestStatus.visible(isVisible = true)
                    binding.tvSendRequest.visible(isVisible = false)
                }
                RequestStatus.ALREADY_ADDED -> {
                    binding.tvRequestStatus.text = binding.root.context.getString(R.string.remove)
                    binding.tvRequestStatus.visible(isVisible = true)
                    binding.tvSendRequest.visible(isVisible = false)
                }

                else -> {
                    binding.tvRequestStatus.visible(isVisible = false)
                    binding.tvSendRequest.visible(isVisible = true)
                }
            }
        }

        private fun T.addPositionsToMap() {
            val id =
                if (this is CloseFriendUser)
                    if (HomeActivity.loggedInUserId == this.requester_id) this.friend_id
                    else requester_id
                else if (this is Users) _id
                else ""
            CloseFriendAndRequestViewModel.closeFriendPositionsMap[id] =
                Pair(absoluteAdapterPosition, this)


            /*
            * user a send the request
            * user b go to the request tab user a loads
            * now user go back to close friends and search that user
            * user b accept the request from search or from profile details
            * and then go back to request tab user a still there
            * need to handle this case
            *
            * this will solve the above issue
            * */
            if (this is Users && this.requestStatus != RequestStatus.RECEIVED)
                if (CloseFriendAndRequestViewModel.requestsPositionsMap.containsKey(id)) {
                    val item = CloseFriendAndRequestViewModel.requestsPositionsMap[id]?.second
                    val position = CloseFriendAndRequestViewModel.requestsPositionsMap[id]?.first
                    if (item != null && position != null && position < closeFriendAndRequestViewModel.friendRequestList.size) {
                        closeFriendAndRequestViewModel.friendRequestList.removeAt(position)
                    }
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW) {
            val binding =
                LayoutCloseFriendItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            CloseFriendViewHolder(binding)
        } else {
            val binding =
                ProgressBarBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            ProgressViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == VIEW) (holder as CloseFriendAdapter<*>.CloseFriendViewHolder).init()
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int = if (list[position] == null) EMPTY else VIEW

    fun addLoader() {
        list.add(null)
        notifyItemInserted(list.size - 1)
    }

    fun removeLoader() {
        if (list.isEmpty()) return
        if (list[list.size - 1] == null) {
            list.removeAt(list.size - 1)
            notifyItemRemoved(list.size - 1)
        }
    }

}