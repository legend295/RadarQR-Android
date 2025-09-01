package com.radarqr.dating.android.hotspots.venuealbum

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.base.ProgressViewHolder
import com.radarqr.dating.android.data.model.VenueImage
import com.radarqr.dating.android.databinding.LayoutProfileGridImagesItemBinding
import com.radarqr.dating.android.databinding.ProgressBarBinding
import com.radarqr.dating.android.utility.Utility.loadVenueImage

class VenueAlbumAdapter(
    val list: ArrayList<VenueImage?>,
    val clickHandler: (ArrayList<VenueImage?>, Int) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM = 0
        const val ITEM_LOADING = 1
    }

    inner class ViewHolder(val binding: LayoutProfileGridImagesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val data = list[absoluteAdapterPosition]
            data ?: return
            binding.ivUser.loadVenueImage(data.image)

            binding.root.setOnClickListener {
                clickHandler(list, absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM -> {
                val binding = LayoutProfileGridImagesItemBinding.inflate(
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
        when (getItemViewType(position)) {
            ITEM -> (holder as ViewHolder).bind()
        }
    }


    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int =
        if (list[position] == null) ITEM_LOADING else ITEM

    @SuppressLint("NotifyDataSetChanged")
    fun refreshData() {
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