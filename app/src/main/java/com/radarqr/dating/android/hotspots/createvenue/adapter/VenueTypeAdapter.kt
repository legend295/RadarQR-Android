package com.radarqr.dating.android.hotspots.createvenue.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.databinding.LayoutVenueTypeItemBinding
import com.radarqr.dating.android.hotspots.helpers.VenueUtils
import com.radarqr.dating.android.hotspots.model.MyVenuesData
import com.radarqr.dating.android.utility.Utility.visible

class VenueTypeAdapter(
    val list: ArrayList<VenueTypeModel>,
    val callback: (VenueTypeModel, Int) -> Unit
) :
    RecyclerView.Adapter<VenueTypeAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: LayoutVenueTypeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val data = list[absoluteAdapterPosition]
            binding.icon =
                ContextCompat.getDrawable(binding.root.context, data.icon)
            binding.tvValue.text = data.name
            binding.tvRequired.visible(isVisible = data.isFieldRequired)
            binding.root.setOnClickListener {
                callback(data, absoluteAdapterPosition)
            }
            binding.ivTick.visible(data.isDataFilled)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            LayoutVenueTypeItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = list.size

    override fun getItemId(position: Int): Long =
        list[position].venue.getTitle().hashCode().toLong()

    data class VenueTypeModel(
        val icon: Int,
        val name: String,
        val venue: VenueUtils.Venue,
        val isFieldRequired: Boolean = false,
        val venueData: MyVenuesData?,
        val isDataFilled: Boolean = false,
    )
}