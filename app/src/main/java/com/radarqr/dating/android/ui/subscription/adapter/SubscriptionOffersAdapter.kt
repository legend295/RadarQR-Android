package com.radarqr.dating.android.ui.subscription.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.R
import com.radarqr.dating.android.databinding.LayoutSubscriptionPriceItemBinding
import com.radarqr.dating.android.ui.subscription.SubscriptionViewModel.Companion.WEEK
import com.radarqr.dating.android.ui.subscription.model.StoreProducts

class SubscriptionOffersAdapter :
    RecyclerView.Adapter<SubscriptionOffersAdapter.ViewHolder>() {
    val list: ArrayList<StoreProducts> = ArrayList()

    var selectedPosition = 0
    lateinit var itemSelectionCallback: (StoreProducts) -> Unit

    inner class ViewHolder(private val binding: LayoutSubscriptionPriceItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.data = list[absoluteAdapterPosition]
            val savingAmount =
                if (list[absoluteAdapterPosition].key == "1$WEEK") binding.root.context.getString(R.string.new_txt) else {
                    "Save ${list[absoluteAdapterPosition].savingPercentage}%"
                }
            binding.tvOffer.text = savingAmount

            if (list[absoluteAdapterPosition].isSelected) {
                selectedPosition = absoluteAdapterPosition
                itemSelectionCallback(list[absoluteAdapterPosition])
            }

            binding.root.setOnClickListener {
                if (absoluteAdapterPosition != selectedPosition) {
                    list[selectedPosition].isSelected = false
                    notifyItemChanged(selectedPosition)

                    selectedPosition = absoluteAdapterPosition

                    list[absoluteAdapterPosition].isSelected =
                        !list[absoluteAdapterPosition].isSelected
                    notifyItemChanged(selectedPosition)
                    itemSelectionCallback(list[absoluteAdapterPosition])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutSubscriptionPriceItemBinding.inflate(
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

    fun updateList(list: ArrayList<StoreProducts>) {
        val diffCallback = Diff(this.list, list)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.list.clear()
        this.list.addAll(list)
        diffResult.dispatchUpdatesTo(this)
    }


    internal class Diff(
        private val oldItems: ArrayList<StoreProducts>,
        private val newItems: ArrayList<StoreProducts>
    ) :
        DiffUtil.Callback() {


        override fun getOldListSize(): Int {
            return oldItems.size
        }

        override fun getNewListSize(): Int {
            return newItems.size
        }

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition].storeProduct.id == newItems[newItemPosition].storeProduct.id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }
    }

}