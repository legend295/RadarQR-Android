package com.radarqr.dating.android.ui.home.settings.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.LayoutProfileGridImagesItemBinding
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.visible

class ProfileGridImagesAdapter(
    val list: ArrayList<String>,
    val clickHandler: (ArrayList<String>, Int) -> Unit
) :
    RecyclerView.Adapter<ProfileGridImagesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: LayoutProfileGridImagesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            if (absoluteAdapterPosition < list.size) {
                binding.ivUser.loadImage(list[absoluteAdapterPosition])
                binding.ivVideoIcon.visible(list[absoluteAdapterPosition].contains(Constants.MP4))
                binding.root.setOnClickListener {
                    clickHandler(list, absoluteAdapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutProfileGridImagesItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return if (list.size <= 5) {
            3
        } else if (list.size <= 8) {
            6
        } else {
            9
        }
    }
}