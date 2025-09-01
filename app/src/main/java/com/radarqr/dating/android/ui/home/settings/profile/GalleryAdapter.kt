package com.radarqr.dating.android.ui.home.settings.profile

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.LayoutGalleryItemBinding
import com.radarqr.dating.android.utility.Utility
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.visible

class GalleryAdapter(
    val list: ArrayList<String>,
    val activity: Activity,
    val clickHandler: (String, LayoutGalleryItemBinding) -> Unit
) :
    RecyclerView.Adapter<GalleryAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: LayoutGalleryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val data = list[absoluteAdapterPosition]
            binding.ivItem.loadImage(data)
            binding.ivPlay.visible(data.contains(Constants.MP4))
//            Utility.zoomImage(activity, binding.ivItem)
            binding.ivPlay.setOnClickListener {
                clickHandler(data, binding)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutGalleryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = list.size
}