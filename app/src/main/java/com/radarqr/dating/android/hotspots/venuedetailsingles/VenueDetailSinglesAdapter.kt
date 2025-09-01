package com.radarqr.dating.android.hotspots.venuedetailsingles

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.ProgressViewHolder
import com.radarqr.dating.android.data.model.SinglesUser
import com.radarqr.dating.android.databinding.LayoutVenueDetailSinglesItemBinding
import com.radarqr.dating.android.databinding.ProgressBarBinding
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.handler.ViewClickHandler

class VenueDetailSinglesAdapter(
    val list: ArrayList<SinglesUser?>,
    val clickHandler: (SinglesUser) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM = 0
        const val LOADING = 1
    }

    inner class ViewHolder(val binding: LayoutVenueDetailSinglesItemBinding) :
        RecyclerView.ViewHolder(binding.root), ViewClickHandler {
        fun bind() {
            val data = list[absoluteAdapterPosition]
            data ?: return
            binding.tvName.text = data.name
            binding.tvUserName.text = StringBuilder().append("@").append(data.username)
            binding.ivUser.loadImage(data.profile_pic)

            binding.viewClickHandler = object : ViewClickHandler {
                override fun onClick(view: View) {
                    when (view.id) {
                        R.id.ivSend, R.id.ivUser, R.id.tvName -> {
                            clickHandler(data)
                        }
                    }
                }

            }

        }

        override fun onClick(view: View) {

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM -> {
                val binding = LayoutVenueDetailSinglesItemBinding.inflate(
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
        if (getItemViewType(position) == ITEM) {
            (holder as ViewHolder).bind()
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemId(position: Int): Long = list[position]?._id.hashCode().toLong()

    override fun getItemViewType(position: Int): Int = if (list[position] == null) LOADING else ITEM

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
        if (list.isNotEmpty() && list[list.size - 1] == null) {
            list.removeAt(list.size - 1)
            notifyItemRemoved(list.size)
        }
    }
}