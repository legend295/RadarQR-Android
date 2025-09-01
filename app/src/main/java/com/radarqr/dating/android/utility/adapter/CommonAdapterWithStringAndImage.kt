package com.radarqr.dating.android.utility.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.databinding.LayoutCommonAdapterStringWithImageBinding
import com.radarqr.dating.android.hotspots.helpers.VenueUtils

class CommonAdapterWithStringAndImage(
    val list: ArrayList<CommonAdapterModel>,
    val callback: (CommonAdapterModel, Int) -> Unit
) :
    RecyclerView.Adapter<CommonAdapterWithStringAndImage.CommonViewHolder>() {

    inner class CommonViewHolder(val binding: LayoutCommonAdapterStringWithImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val data = list[absoluteAdapterPosition]
            binding.icon =
                ContextCompat.getDrawable(binding.root.context, data.icon)
            binding.tvValue.text = data.name

            binding.root.setOnClickListener {
                callback(data, absoluteAdapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder {
        val binding = LayoutCommonAdapterStringWithImageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CommonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = list.size

    data class CommonAdapterModel(
        val icon: Int,
        val name: String
    )
}