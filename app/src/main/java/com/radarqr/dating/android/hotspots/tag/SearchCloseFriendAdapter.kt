package com.radarqr.dating.android.hotspots.tag

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.base.ProgressViewHolder
import com.radarqr.dating.android.data.model.SearchUser
import com.radarqr.dating.android.databinding.LayoutCloseFriendItemBinding
import com.radarqr.dating.android.databinding.ProgressBarBinding
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.visible

class SearchCloseFriendAdapter(
    val list: ArrayList<SearchUser?>,
    var tagModel: TagViewModel.TagModel?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var clickHandler: (SearchUser, Int) -> Unit

    companion object {
        const val VIEW = 0
        const val EMPTY = 1
    }

    inner class ViewHolder(val binding: LayoutCloseFriendItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun init() {
            val data = list[absoluteAdapterPosition]
            data ?: return
            binding.tvSendRequest.visible(isVisible = false)
            binding.tvRequestStatus.visible(isVisible = false)
            val image: String
            val name: String
            val userName: String
            val userId: String
            if (HomeActivity.loggedInUserId == data.requester_id) {
                // show friend info
                userId = data.friend_info._id
                image = data.friend_info.profile_pic
                name = data.friend_info.name
                userName = data.friend_info.username
            } else {
                // show requester info
                userId = data.requester_info._id
                image = data.requester_info.profile_pic
                name = data.requester_info.name
                userName = data.requester_info.username
            }
            binding.tvName.text = name
            binding.tvUserName.text = StringBuilder().append("@").append(userName)
            binding.ivUser.loadImage(image)

            binding.root.setOnClickListener {
                tagModel?.let {
                    it.id = userId
                    it.taggedUserName = userName
                    it.taggedUserId = userId
                    it.taggedUserImage = image
                    it.name = name
                }
                clickHandler(data, absoluteAdapterPosition)
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
            ViewHolder(binding)
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
        if (getItemViewType(position) == VIEW) (holder as SearchCloseFriendAdapter.ViewHolder).init()
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

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }
}