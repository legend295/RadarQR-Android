package com.radarqr.dating.android.ui.home.main.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.ProgressViewHolder
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.LayoutNearYouItemBinding
import com.radarqr.dating.android.databinding.ProgressBarBinding
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.utility.Utility.visible

class NearYouAdapter(val list: ArrayList<ProfileData?>, val callBack: (ProfileData?, Int) -> Unit) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isLoading = false

    companion object {
        const val ITEM = 0
        const val ITEM_LOADING = 1
    }

    inner class ViewHolder(val binding: LayoutNearYouItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val data = list[absoluteAdapterPosition]
            data ?: return
            binding.data = data
            binding.ivChat.visibility = View.INVISIBLE
            binding.ivUser.setOnClickListener {
                val bundle = Bundle().apply {
                    putSerializable(Constants.EXTRA_DATA, data)
                    putString(Constants.USER_ID, data._id)
                    putInt(Constants.FROM, ProfileFragment.FROM_NEAR_YOU)
                    putBoolean(Constants.TYPE, true)
                }
                binding.root.findNavController().navigate(R.id.profileFragment, bundle)
            }
            binding.ivChat.setOnClickListener {
                callBack(data, absoluteAdapterPosition)
            }
            binding.ivLikeIcon.setOnClickListener {
                callBack(data, absoluteAdapterPosition)
            }
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
                return ViewHolder(binding)
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
        if (holder is ViewHolder) {
            val staggeredHolder: ViewHolder = holder
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

    override fun getItemCount(): Int = list.size

    override fun getItemId(position: Int): Long = list[position]?._id.hashCode().toLong()

    override fun getItemViewType(position: Int): Int =
        if (list[position] == null) ITEM_LOADING else ITEM

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

    fun removeLoadingView() {
        //Remove loading item
        if (list.isNotEmpty()) {
            list.removeAt(list.size - 1)
            notifyItemRemoved(list.size)
        }
    }
}