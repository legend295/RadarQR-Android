package com.radarqr.dating.android.hotspots.venue

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.ProgressViewHolder
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.LayoutVenuesItemBinding
import com.radarqr.dating.android.databinding.ProgressBarBinding
import com.radarqr.dating.android.hotspots.model.MyVenuesData
import com.radarqr.dating.android.utility.Utility.toPx

class VenuesListAdapter(
    var list: ArrayList<MyVenuesData?>,
    val callBack: (Int, MyVenuesData, Int) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val ITEM = 0
        const val ITEM_LOADING = 1
    }

    /**venue status - (4 - Send for Approval 3 - in-progress, 2 - submitted, 1 - active/approved
     *
     * there would be 5 statues to it:
     * - Fill Required Information
     * - Send for Approval - 4
     * - Submitted - 2
     * - Approved - 1
     * - Action needed - 3
     *
     * By Amit --
     * 1 - active
     * 2 - disapproved
     * 3 - sent for approval
     * 4 - In progress
     * */

    inner class VenuesViewHolder(val binding: LayoutVenuesItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {
            if (list.isEmpty()) return
            val data = list[absoluteAdapterPosition]
            data ?: return
            binding.tvCount.text = String.format("${absoluteAdapterPosition + 1}")
            binding.data = data

            when (data.status) {
                Constants.VenueStatus.APPROVED -> {
                    binding.tvStatus.backgroundTintList =
                        ContextCompat.getColorStateList(binding.root.context, R.color.teal_200)
                    binding.tvStatus.text = binding.root.context?.getText(R.string.approved)
                    binding.tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        null,
                        null
                    )
                }

                Constants.VenueStatus.IN_PROGRESS -> {
                    binding.tvStatus.backgroundTintList =
                        ContextCompat.getColorStateList(binding.root.context, R.color.teal_200)
                    binding.tvStatus.text =
                        binding.root.context?.getText(R.string.waiting_for_review)
                }

                Constants.VenueStatus.DISAPPROVED -> {
                    binding.tvStatus.backgroundTintList =
                        ContextCompat.getColorStateList(binding.root.context, R.color.fire_opal)
                    binding.tvStatus.text =
                        binding.root.context?.getText(R.string.action_needed)

                    val drawable = ContextCompat.getDrawable(
                        binding.root.context,
                        R.drawable.ic_white_next_round
                    )
                    binding.tvStatus.setPadding(
                        6.toPx.toInt(),
                        4.toPx.toInt(),
                        8.toPx.toInt(),
                        4.toPx.toInt()
                    )

                    binding.tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        drawable,
                        null
                    )
                }

                Constants.VenueStatus.VENUE_PAUSED -> {
                    binding.tvStatus.backgroundTintList =
                        ContextCompat.getColorStateList(binding.root.context, R.color.fire_opal)
                    binding.tvStatus.text = binding.root.context?.getText(R.string.venue_paused)

                    if (!data.pauseComment.isNullOrEmpty()) {
                        val drawable = ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.ic_white_next_round
                        )
                        binding.tvStatus.setPadding(
                            6.toPx.toInt(),
                            4.toPx.toInt(),
                            8.toPx.toInt(),
                            4.toPx.toInt()
                        )

                        binding.tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            drawable,
                            null
                        )
                    }
                }

                Constants.VenueStatus.SEND_FOR_APPROVAL -> {
                    val isReadyForApproval = data.isReadyForApproval()
                    binding.tvStatus.backgroundTintList =
                        ContextCompat.getColorStateList(
                            binding.root.context,
                            if (isReadyForApproval) R.color.teal_200 else R.color.fire_opal
                        )
                    binding.tvStatus.text =
                        binding.root.context?.getText(if (isReadyForApproval) R.string.send_for_approval else R.string.fill_required_information)
                    list[absoluteAdapterPosition]?.isReadyForApproval = isReadyForApproval
                    if (isReadyForApproval) {
                        val drawable = ContextCompat.getDrawable(
                            binding.root.context,
                            R.drawable.ic_white_next_round
                        )
                        binding.tvStatus.setPadding(
                            6.toPx.toInt(),
                            4.toPx.toInt(),
                            8.toPx.toInt(),
                            4.toPx.toInt()
                        )
                        binding.tvStatus.setCompoundDrawablesWithIntrinsicBounds(
                            null,
                            null,
                            drawable,
                            null
                        )
                    }
                }
            }

            binding.tvStatus.setOnClickListener {
                callBack(it.id, data, absoluteAdapterPosition)
            }

            binding.tvViewVenue.setOnClickListener {
                callBack(it.id, data, absoluteAdapterPosition)
            }
            binding.clParent.setOnClickListener {
                callBack(it.id, data, absoluteAdapterPosition)
            }
        }

        private fun MyVenuesData.isReadyForApproval(): Boolean {
            when {
                name.isEmpty() -> return false
                description.isNullOrEmpty() -> return false
                contactinfo.address.isNullOrEmpty() -> return false
                images.isNullOrEmpty() || images.size < 3 -> return false
            }
            return true
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        when (viewType) {
            ITEM -> {
                val binding =
                    LayoutVenuesItemBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                return VenuesViewHolder(binding)
            }

            else -> {
                val progressBinding =
                    ProgressBarBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                ProgressViewHolder(progressBinding)
                return ProgressViewHolder(progressBinding)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            ITEM -> {
                (holder as VenuesViewHolder).bind()
            }
        }
    }

    override fun getItemCount(): Int = list.size

    override fun getItemId(position: Int): Long = list[position]?._id.hashCode().toLong()

    override fun getItemViewType(position: Int): Int =
        if (list[position] == null) ITEM_LOADING else ITEM

    fun addLoadingView() {
        //add loading item
        Handler(Looper.getMainLooper()).post {
            list.add(null)
            notifyItemInserted(list.size - 1)
        }
    }

    fun removeLoadingView() {
        //Remove loading item
        if (list.isNotEmpty() && list[list.size - 1] == null) {
            list.removeAt(list.size - 1)
            notifyItemRemoved(list.size)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refresh() {
        notifyDataSetChanged()
    }
}