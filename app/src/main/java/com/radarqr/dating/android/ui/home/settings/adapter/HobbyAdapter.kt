package com.radarqr.dating.android.ui.home.settings.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.databinding.LayoutHobbyItemBinding
import com.radarqr.dating.android.ui.home.settings.prodileModel.HobbiesData


class HobbyAdapter(
    private val listItem: List<HobbiesData> = ArrayList(),
    val requireActivity: Activity, var tag: String,
    val fromProfile: Boolean = false
) :
    RecyclerView.Adapter<HobbyAdapter.ViewHolder>() {

    lateinit var clickHandler: (Int) -> Unit

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutHobbyItemBinding.inflate(LayoutInflater.from(parent.context),parent,false);
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItem[position]
        if (item.equals("") || item == null) {
            holder.binding.tvItem.visibility = View.GONE
        } else {
            holder.binding.tvItem.visibility = View.VISIBLE
            /*Glide.with(holder.itemView)
                .load(RaddarApp.getEnvironment().getUrl() + item.category + "/" + item.image)
                .into(holder.itemView.iv_sign)*/
            holder.binding.tvItem.text = item.name
        }
        if (fromProfile) {
            holder.binding.llMain.background =
                ContextCompat.getDrawable(holder.itemView.context, R.drawable.bg_round_stroke)
            holder.binding.tvItem.typeface =
                ResourcesCompat.getFont(holder.itemView.context, R.font.poppins_bold)
        }
        if (tag == "0") {
            holder.binding.ivClose.visibility = View.VISIBLE
        } else {
            holder.binding.ivClose.visibility = View.GONE
        }
        holder.binding.ivClose.setOnClickListener {
            (listItem as ArrayList).remove(listItem[position])
            notifyItemRemoved(position)
            notifyItemRangeChanged(0, listItem.size)
            clickHandler.invoke(position)
        }

    }

    private fun add(value: HobbiesData) {
        (listItem as ArrayList).add(value)
        notifyItemInserted(listItem.size - 1)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear() {
        (listItem as ArrayList).clear()
        notifyDataSetChanged()
    }

    fun addAll(value: ArrayList<HobbiesData>) {
        for (data in value) {
            add(data)
        }
    }

    fun getList(): ArrayList<HobbiesData> = (listItem as ArrayList<HobbiesData>)

    override fun getItemId(position: Int) = listItem[position]._id.hashCode().toLong()

    override fun getItemCount(): Int = listItem.size

    fun refresh() {
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: LayoutHobbyItemBinding) : RecyclerView.ViewHolder(binding.root)

}