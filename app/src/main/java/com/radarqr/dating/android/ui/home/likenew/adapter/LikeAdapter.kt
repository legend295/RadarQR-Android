package com.radarqr.dating.android.ui.home.likenew.adapter

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.ProgressViewHolder
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.LayoutNearYouItemBinding
import com.radarqr.dating.android.databinding.ProgressBarBinding
import com.radarqr.dating.android.ui.home.likes.model.UserLikes
import com.radarqr.dating.android.ui.home.settings.prodileModel.LocationDetail
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.Utility.toPx
import com.radarqr.dating.android.utility.Utility.visible

class LikeAdapter(var list: ArrayList<UserLikes?>, val callBack: (UserLikes?, Int) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var isLoaderVisible = false

    companion object {
        const val ITEM = 0
        const val ITEM_LOADING = 1
    }

    inner class LikeViewHolder(var binding: LayoutNearYouItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.llBottomLocation.setPadding(10.toPx.toInt(), 0, 0, 0)
            val data = list[absoluteAdapterPosition]
            data ?: return
            binding.subscriptionStatus = RaddarApp.getSubscriptionStatus()
            binding.ivChat.visible(isVisible = false)
            binding.ivLikeIcon.visible(isVisible = !data.sender_message.isNullOrEmpty() || data.category == Constants.VENUE)
            binding.data = data.getProfileData()
            binding.ivLikeIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    binding.root.context,
                    if (data.category == Constants.VENUE) R.drawable.ic_like_fire else R.drawable.ic_vector_chat_red
                )
            )

            /* binding.ivUser.setOnClickListener {
                 val bundle = Bundle().apply {
                     putSerializable(Constants.EXTRA, data)
                     putString(Constants.USER_ID, data.sender_id)
                     putInt(Constants.FROM, ProfileFragment.FROM_LIKE)
                     putBoolean(Constants.TYPE, true)
                 }
                 binding.root.findNavController()
                     .navigate(R.id.action_likeFragment_to_profileFragment, bundle)
             }*/

            binding.root.setOnClickListener {
                callBack(data, absoluteAdapterPosition)
            }

            /* binding.ivChat.setOnClickListener {
                 callBack(data, absoluteAdapterPosition)
             }*/
        }

        private fun UserLikes.getProfileData(): ProfileData {
            return ProfileData(
                profile_pic = user_detail?.profile_pic,
                name = user_detail?.name,
                gender = user_detail?.gender,
                location = user_detail?.location ?: LocationDetail(),
                age = BaseUtils.convertAge(user_detail?.birthday),
                show_age = user_detail?.show_age ?: false
            )
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM -> {
                val binding = LayoutNearYouItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return LikeViewHolder(binding)
            }

            else -> {
                val progressBinding =
                    ProgressBarBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                ProgressViewHolder(progressBinding)
                return ProgressViewHolder(progressBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = list[position]
        if (holder is LikeViewHolder) {
            val staggeredHolder: LikeViewHolder = holder
            staggeredHolder.bind()
        }
        if (data == null) {
            // Span the item if active
            val lp = holder.itemView.layoutParams
            if (lp is StaggeredGridLayoutManager.LayoutParams) {
                lp.isFullSpan = true
                holder.itemView.layoutParams = lp
            }
        } else {
            // Span the item if active
            val lp = holder.itemView.layoutParams
            if (lp is StaggeredGridLayoutManager.LayoutParams) {
                lp.isFullSpan = false
                holder.itemView.layoutParams = lp
            }
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (list[position] == null) ITEM_LOADING else ITEM

    override fun getItemId(position: Int): Long = list[position]?._id.hashCode().toLong()

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }


    fun addLoadingView() {
        //add loading item
        Handler(Looper.getMainLooper()).post {
            list.add(null)
            notifyItemInserted(list.size - 1)
        }
    }

    fun setLikeList(list: List<UserLikes?>) {
        this.list = ArrayList(list)
    }

    fun removeLoadingView() {
        //Remove loading item
        if (list.isNotEmpty() && list[list.size - 1] == null) {
            list.removeAt(list.size - 1)
            notifyItemRemoved(list.size)
        }
    }

}