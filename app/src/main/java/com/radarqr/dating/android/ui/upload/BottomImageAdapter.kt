package com.radarqr.dating.android.ui.upload

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.data.model.VenueImage
import com.radarqr.dating.android.databinding.LayoutProfileImageBinding
import com.radarqr.dating.android.utility.Utility.isVideo
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.loadVenueImage
import com.radarqr.dating.android.utility.Utility.visible

class BottomImageAdapter<T>(
    val list: ArrayList<T>,
    var isDeleteVisible: Boolean = false,
    val type: Type
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    lateinit var clickHandler: (View, Any?, Int) -> Unit

    enum class Type {
        UPDATE_VENUE, EDIT_PROFILE
    }

    companion object {
        const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_EMPTY = 1
    }

    inner class ViewHolder(val binding: LayoutProfileImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            var image = ""
            binding.ivAdd1.visible(isVisible = false)
            binding.ivUser.visible(isVisible = false)
            if (list[absoluteAdapterPosition] is String) {
                image = list[absoluteAdapterPosition] as String
            } else if (list[absoluteAdapterPosition] is VenueImage) {
                val data = list[absoluteAdapterPosition] as VenueImage
                image = data.image
            }
            if (type == Type.UPDATE_VENUE) {
                binding.ivView1.loadVenueImage(image)
            } else binding.ivView1.loadImage(image)

            binding.ivVideo.visible(binding.root.context.isVideo(image))
            binding.ivClose1.visible(isVisible = if (absoluteAdapterPosition != 0) isDeleteVisible else false)
            binding.ivTransparent.visible(isVisible = if (absoluteAdapterPosition != 0) isDeleteVisible else false)


            binding.ivView1.setOnClickListener {
                clickHandler(it, list[absoluteAdapterPosition], absoluteAdapterPosition)
            }

            binding.ivClose1.setOnClickListener {
                clickHandler(it, list[absoluteAdapterPosition], absoluteAdapterPosition)
            }
        }
    }

    inner class EmptyViewHolder(val binding: LayoutProfileImageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            binding.ivPlus.visible(isVisible = isDeleteVisible)
            binding.ivUser.visible(isVisible = !isDeleteVisible)
            binding.ivAdd1.visible(isVisible = !isDeleteVisible)
            binding.root.setOnClickListener {
                if (type == Type.UPDATE_VENUE) {
                    clickHandler(
                        it, if (type == Type.UPDATE_VENUE) VenueImage(
                            "", 0.0, "",
                            ArrayList(), "", ""
                        ) else "", absoluteAdapterPosition
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            LayoutProfileImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                ViewHolder(binding)
            }

            else -> {
                EmptyViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            VIEW_TYPE_ITEM -> {
                (holder as BottomImageAdapter<*>.ViewHolder).bind()
            }

            else -> (holder as BottomImageAdapter<*>.EmptyViewHolder).bind()
        }
    }

    override fun getItemCount(): Int = 3

    override fun getItemViewType(position: Int): Int =
        if (position >= list.size) VIEW_TYPE_EMPTY else VIEW_TYPE_ITEM
}