package com.radarqr.dating.android.ui.home.main.adapter

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil
import com.radarqr.dating.android.R
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.LayoutRecommendationItemBinding
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.main.recommended.SpotDiffCallback
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.utility.SharedPrefsHelper
import com.radarqr.dating.android.utility.Utility.visible

class RecommendationAdapter() :
    RecyclerView.Adapter<RecommendationAdapter.ViewHolder>() {
    private var list: ArrayList<ProfileData> = ArrayList()

    inner class ViewHolder(val binding: LayoutRecommendationItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            val data = list[absoluteAdapterPosition]
            binding.data = data
            if (data.location.latlon?.coordinates != null) {
                val lat = data.location.latlon.coordinates[1]
                val lng = data.location.latlon.coordinates[0]
                if (lat == 0.0 || lng == 0.0) {
                    binding.tvDistance.visible(isVisible = false)
                } else {
                    val userLocation = LatLng(lat, lng)
//                val myLocation = HomeActivity.userLocation ?: SharedPrefsHelper.getLastLocation()
                    val myLocation = HomeActivity.userProfileLocation
                        ?: SharedPrefsHelper.getLastEditProfileLocation()
                    myLocation?.let {
                        binding.tvDistance.visible(isVisible = true)
                        val miles = ((SphericalUtil.computeDistanceBetween(
                            it,
                            userLocation
                        ) * 0.62137) / 1000).toInt()
                        binding.tvDistance.text = if (miles <= 1) "Within one mile" else
                            buildString {
                                append(miles)
                                append(" ")
                                append(binding.root.context?.getString(R.string.mi_away))
                            }
                    }
                } ?: kotlin.run {
                    binding.tvDistance.visible(isVisible = false)
                }
            } else binding.tvDistance.visible(isVisible = false)
            binding.root.setOnClickListener {
                val bundle = Bundle().apply {
                    putSerializable(Constants.EXTRA_DATA, data)
                    putString(Constants.USER_ID, data._id)
                    putInt(Constants.FROM, ProfileFragment.FROM_HOME)
                    putBoolean(Constants.TYPE, true)
                }
                binding.root.findNavController().navigate(R.id.profileFragment, bundle)
            }

            binding.executePendingBindings()
            binding.invalidateAll()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutRecommendationItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemId(position: Int): Long =
        list[position]._id?.hashCode()?.toLong() ?: list[position].hashCode().toLong()

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }

    fun setList(list: ArrayList<ProfileData>) {
        this.list = list
    }

    fun getList(): ArrayList<ProfileData> = list

    fun updateList(list: List<ProfileData>) {
        val diffCallback = SpotDiffCallback(this.list, list)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.list.clear()
        this.list.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }
}