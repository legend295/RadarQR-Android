package com.radarqr.dating.android.ui.home.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.databinding.LayoutSettingsOptionsItemBinding
import com.radarqr.dating.android.ui.home.settings.model.SettingsOptionData

class SettingsOptionsAdapters(val list: ArrayList<SettingsOptionData>) :
    RecyclerView.Adapter<SettingsOptionsAdapters.ViewHolder>() {

    lateinit var clickHandler: (SettingsOptionData, Int) -> Unit

    inner class ViewHolder(val binding: LayoutSettingsOptionsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun init() {
            binding.data = list[absoluteAdapterPosition]

            binding.root.setOnClickListener {
                clickHandler(list[absoluteAdapterPosition], absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutSettingsOptionsItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.init()
    }

    override fun getItemCount(): Int = list.size


    override fun getItemId(position: Int): Long = list[position].position.toLong()
}