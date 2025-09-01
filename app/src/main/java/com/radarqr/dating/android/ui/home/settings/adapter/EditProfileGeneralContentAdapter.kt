package com.radarqr.dating.android.ui.home.settings.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.databinding.LayoutEditProfileGeneralContentItemBinding
import com.radarqr.dating.android.ui.home.settings.model.EditProfileGeneralContentData

class EditProfileGeneralContentAdapter(
    var list: ArrayList<EditProfileGeneralContentData>,
    val fromEditProfile: Boolean = false,
    val clickHandler: (EditProfileGeneralContentData) -> Unit
) :
    RecyclerView.Adapter<EditProfileGeneralContentAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: LayoutEditProfileGeneralContentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(editProfileGeneralContentData: EditProfileGeneralContentData) {
            binding.fromEditProfile = fromEditProfile
            binding.data = editProfileGeneralContentData
            binding.tvData.text =
                HtmlCompat.fromHtml("<u>Select</u>", HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.root.setOnClickListener { clickHandler(editProfileGeneralContentData) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutEditProfileGeneralContentItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    override fun getItemId(position: Int): Long =
        list[position].id.toLong()


    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }

    fun addData(list: ArrayList<EditProfileGeneralContentData>) {
        list.forEach {
            this.list.add(it)

        }
    }
}