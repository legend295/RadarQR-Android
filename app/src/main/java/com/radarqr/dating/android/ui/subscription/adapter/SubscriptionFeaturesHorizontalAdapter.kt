package com.radarqr.dating.android.ui.subscription.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.databinding.LayoutSubscriptionHorizontalFeaturesItemBinding
import com.radarqr.dating.android.ui.subscription.SubscriptionViewModel

class SubscriptionFeaturesHorizontalAdapter(val list: ArrayList<SubscriptionViewModel.SubscriptionHorizontalFeaturesData>) :
    RecyclerView.Adapter<SubscriptionFeaturesHorizontalAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: LayoutSubscriptionHorizontalFeaturesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.data = list[absoluteAdapterPosition]
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutSubscriptionHorizontalFeaturesItemBinding.inflate(
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