package com.radarqr.dating.android.hotspots.venuedetail

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.data.model.VenueImage
import com.radarqr.dating.android.databinding.LayoutVenueImagesItemBinding
import com.radarqr.dating.android.utility.Utility.loadVenueImage
import com.radarqr.dating.android.utility.Utility.visible

class VenueImagesAdapter(
    val list: ArrayList<VenueImage>,
    val callBack: (VenueImage?, Boolean, Int) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_EMPTY = 1
    }

    inner class VenueViewHolder(val binding: LayoutVenueImagesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.ivAdd1.visible(isVisible = false)
            binding.ivUser.loadVenueImage(list[absoluteAdapterPosition].image)
            binding.root.setOnClickListener {
                callBack(list[absoluteAdapterPosition], false, absoluteAdapterPosition)
            }
        }
    }

    inner class ViewHolderEmpty(val binding: LayoutVenueImagesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.ivUser.setBackgroundColor(Color.parseColor("#1A0CDEC2"))
//            binding.ivUserIcon.visible(isVisible = true)
            binding.root.setOnClickListener {
                callBack(null, true, absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = LayoutVenueImagesItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return if (viewType == VIEW_TYPE_ITEM) VenueViewHolder(binding)
        else ViewHolderEmpty(binding)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_ITEM -> (holder as VenueViewHolder).bind()
            VIEW_TYPE_EMPTY -> (holder as ViewHolderEmpty).bind()
        }
    }

    override fun getItemViewType(position: Int): Int =
        if (position >= list.size) VIEW_TYPE_EMPTY else VIEW_TYPE_ITEM

    override fun getItemCount(): Int = 3
}