package com.radarqr.dating.android.hotspots.createvenue.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.data.model.VenueImage
import com.radarqr.dating.android.databinding.LayoutProfileImageBinding
import com.radarqr.dating.android.utility.Utility.loadVenueImage
import com.radarqr.dating.android.utility.Utility.visible

class VenueDetailImagesAdapter(val list: ArrayList<VenueImage>, var isDeleteVisible: Boolean = false) :
    RecyclerView.Adapter<VenueDetailImagesAdapter.ViewHolder>() {

    lateinit var clickHandler: (View, VenueImage, Int) -> Unit

    inner class ViewHolder(val binding: LayoutProfileImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.ivView1.loadVenueImage(list[absoluteAdapterPosition].image)
            binding.ivAdd1.visible(isVisible = false)
            binding.ivUser.visible(isVisible = false)
            binding.ivClose1.visible(isVisible = if (absoluteAdapterPosition != 0) isDeleteVisible else false)
            binding.ivTransparent.visible(isVisible = if (absoluteAdapterPosition != 0) isDeleteVisible else false)
            binding.ivVideo.visible(isVisible = false)

            binding.ivView1.setOnClickListener {
                clickHandler(it, list[absoluteAdapterPosition], absoluteAdapterPosition)
            }

            binding.ivClose1.setOnClickListener {
                clickHandler(it, list[absoluteAdapterPosition], absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutProfileImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = list.size
}