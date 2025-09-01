package com.radarqr.dating.android.hotspots.tag

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.databinding.LayoutTaggedUsersItemBinding
import com.radarqr.dating.android.utility.Utility.loadImage

class TaggedUsersAdapter(val list: ArrayList<TagViewModel.TagModel>) :
    RecyclerView.Adapter<TaggedUsersAdapter.ViewHolder>() {

    lateinit var removeClickHandler: (TagViewModel.TagModel, Int) -> Unit

    inner class ViewHolder(val binding: LayoutTaggedUsersItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun init() {
            val data = list[absoluteAdapterPosition]
            binding.ivUser.loadImage(data.taggedUserImage)
            binding.tvName.text = data.name
            binding.tvUserName.text = StringBuilder().append("@").append(data.taggedUserName)

            binding.ivRemove.setOnClickListener {
                removeClickHandler(data, absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutTaggedUsersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.init()
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }
}