package com.radarqr.dating.android.ui.subscription.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.databinding.LayoutSubscriptionVerticalFeaturesItemBinding
import com.radarqr.dating.android.ui.subscription.SubscriptionViewModel
import com.radarqr.dating.android.utility.Utility.visible

class SubscriptionFeaturesVerticalAdapter(val list: ArrayList<SubscriptionViewModel.SubscriptionVerticalFeaturesData>) :
    RecyclerView.Adapter<SubscriptionFeaturesVerticalAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: LayoutSubscriptionVerticalFeaturesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.data = list[absoluteAdapterPosition]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutSubscriptionVerticalFeaturesItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }
}