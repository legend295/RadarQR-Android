package com.radarqr.dating.android.ui.home.settings.hobbies.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.data.model.hobbies.HobbiesAndInterestData
import com.radarqr.dating.android.databinding.LayoutAddHobbiesAndInterestItemBinding
import java.util.*

class AddHobbiesAndInterestAdapter(val list: ArrayList<HobbiesAndInterestData>) :
    RecyclerView.Adapter<AddHobbiesAndInterestAdapter.ViewHolder>() {

    //    private val list: ArrayList<HobbiesAndInterestData> = ArrayList()

    //    var selectedCount = 0
    val selectedList = TreeMap<Int, String>()

    inner class ViewHolder(var binding: LayoutAddHobbiesAndInterestItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            binding.data = list[absoluteAdapterPosition]
            binding.tvName.text = list[absoluteAdapterPosition].name
            if (list[absoluteAdapterPosition].isSelected) {
//                selectedCount += 1
                selectedList[absoluteAdapterPosition] = list[absoluteAdapterPosition]._id
            }
           /* Glide.with(binding.root)
                .load(
                    RaddarApp.getEnvironment()
                        .getUrl() + list[absoluteAdapterPosition].category + "/" + list[absoluteAdapterPosition].image
                )
                .into(binding.ivHobbies)*/

            binding.root.setOnClickListener {
                if (list[absoluteAdapterPosition].isSelected) {
                    list[absoluteAdapterPosition].isSelected = false
//                    selectedCount -= 1
                    if (selectedList.containsKey(absoluteAdapterPosition)) {
                        selectedList.remove(absoluteAdapterPosition)
                    }
                } else {
                    if (selectedList.size <= 4) {
                        list[absoluteAdapterPosition].isSelected = true
//                        selectedCount += 1
                        selectedList[absoluteAdapterPosition] = list[absoluteAdapterPosition]._id
                    }
                }

                binding.data = list[absoluteAdapterPosition]
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: LayoutAddHobbiesAndInterestItemBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.layout_add_hobbies_and_interest_item,
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int = list.size

    override fun getItemId(position: Int): Long = list[position]._id.hashCode().toLong()

    private fun add(data: HobbiesAndInterestData) {
        list.add(data)
        notifyItemInserted(list.size - 1)
    }


    fun addAll(list: ArrayList<HobbiesAndInterestData>) {
        for (value in list) {
            add(value)
        }
    }
}