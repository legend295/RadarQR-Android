package com.radarqr.dating.android.hotspots.helpers

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.utility.chipslayoutmanager.ChipsLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.radarqr.dating.android.R
import com.radarqr.dating.android.databinding.LayoutVenueBottomSheetBinding
import com.radarqr.dating.android.databinding.LayoutVenueUtilsItemBinding
import com.radarqr.dating.android.hotspots.createvenue.VenueUpdateViewModel
import com.radarqr.dating.android.hotspots.model.Ambiance
import com.radarqr.dating.android.hotspots.model.Type
import com.radarqr.dating.android.hotspots.model.UpdateVenueRequest
import com.radarqr.dating.android.hotspots.model.VenueTypeAndAmbianceResponse
import com.radarqr.dating.android.utility.handler.ViewClickHandler

object VenueUtils {

    fun Context.openBottomSheet(
        venue: Venue,
        venueUpdateViewModel: VenueUpdateViewModel,
        callBack: (Venue, Boolean, UpdateVenueRequest?) -> Unit // Boolean - used for tracking that we are updating the address or not if true than we will open autoComplete in VenueUpdateFragment
    ) {
        val sheet = BottomSheetDialog(this, R.style.DialogStyle)
        val layout =
            LayoutVenueBottomSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)
        layout.viewModel = venueUpdateViewModel
        layout.venue = venue
        layout.title = venue.getTitle()
        val updateVenueRequest = UpdateVenueRequest()
        layout.viewHandler = object : ViewClickHandler {
            override fun onClick(view: View) {
                when (view.id) {
                    R.id.tvAddressValue -> {
                        callBack(venue, true, null)
                    }

                    R.id.tv_continue -> {
                        callBack(venue, false, updateVenueRequest)
                        sheet.dismiss()
                    }
                }
            }
        }
        val list = ArrayList<VenueTypeAndAmbianceResponse>()
        if (venue == Venue.VENUE_TYPE) {
            //Update isSelected value if type matches with the venueTypes list
            venueUpdateViewModel.venueTypeList.forEach {
                it.isSelected =
                    it.value == venueUpdateViewModel.updatingVenueData.value?.type?.value
            }
            list.clear()
            list.addAll(venueUpdateViewModel.venueTypeList)
        } else {
            venueUpdateViewModel.venueAmbianceList.forEach {
                it.isSelected =
                    it.value == venueUpdateViewModel.updatingVenueData.value?.ambiance?.value
            }
            list.clear()
            list.addAll(venueUpdateViewModel.venueAmbianceList)
        }
        val venueUtilsAdapter = VenueUtilAdapter(list) {
            // Set selected value to the updating request so that we can update that via api in VenueUpdateFragment
            if (venue == Venue.VENUE_TYPE) {
                updateVenueRequest.type = Type(it.name, it.value)
            } else {
                updateVenueRequest.ambiance = Ambiance(it.name, it.value)
            }
        }
        layout.rvVenueItems.apply {
            layoutManager = ChipsLayoutManager.newBuilder(this@openBottomSheet)
                .setChildGravity(Gravity.TOP)
                .setMaxViewsInRow(5)
                .setGravityResolver { Gravity.CENTER }
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .build()

            setHasFixedSize(true)

            adapter = venueUtilsAdapter
        }

        layout.layoutVenueDescriptionAndOffer.etInput.doAfterTextChanged {
            val amount = "(${200 - (it?.length ?: 0)})"
            layout.layoutVenueDescriptionAndOffer.tvMessage.text =
                sheet.context.getString(R.string.character_remaining, amount)
        }

        sheet.apply {
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            dismissWithAnimation = true
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setDimAmount(0.4f)
            window?.statusBarColor = Color.TRANSPARENT
            setContentView(layout.root)
            setCanceledOnTouchOutside(true)
            show()
        }
    }

    enum class Venue : VenueHelper {
        VENUE_TYPE {
            override fun getTitle(): String = "Venue Type"
        },
        DESCRIPTION {
            override fun getTitle(): String = "Description About"
        },
        AMBIANCE {
            override fun getTitle(): String = "Ambiance"
        },
        SPECIAL_OFFERS {
            override fun getTitle(): String = "Special Offers"
        },
        UPCOMING_EVENTS {
            override fun getTitle(): String = "Upcoming Events"
        },
        CONTACT_INFO {
            override fun getTitle(): String = "Contact Info"
        },
        VENUE_NAME {
            override fun getTitle(): String = "Venue Name"
        },
        VENUE_PHOTOS {
            override fun getTitle(): String = "Venue Photos"
        },
        EVENTS {
            override fun getTitle(): String = "Events"
        },
        DEALS {
            override fun getTitle(): String = "Deals"
        };

    }


    interface VenueHelper {
        fun getTitle(): String
    }

    class VenueUtilAdapter(
        val list: ArrayList<VenueTypeAndAmbianceResponse>,
        val callback: (VenueTypeAndAmbianceResponse) -> Unit
    ) :
        RecyclerView.Adapter<VenueUtilAdapter.ViewHolder>() {
        var lastSelectedPosition = -1

        inner class ViewHolder(val binding: LayoutVenueUtilsItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            fun init() {
                binding.data = list[absoluteAdapterPosition]
                // If updating the already selected value then update lastSelectedPosition here as we are allowing to select only 1 value
                if (list[absoluteAdapterPosition].isSelected) {
                    lastSelectedPosition = absoluteAdapterPosition
                }
                binding.root.setOnClickListener {
                    // if the item is already selected than return
                    if (list[absoluteAdapterPosition].isSelected) return@setOnClickListener
                    // check that lastSelectedPosition is greater than 0 and smaller than the size of list
                    // if true then mark isSelected value to false because we are only allowing to select 1 value
                    if (lastSelectedPosition >= 0 && lastSelectedPosition < list.size) {
                        list[lastSelectedPosition].isSelected = false
                        notifyItemChanged(lastSelectedPosition, list[lastSelectedPosition])
                    }
                    //mark the selected item to true and store the lastSelectedPosition to the selected position
                    if (!list[absoluteAdapterPosition].isSelected && lastSelectedPosition != absoluteAdapterPosition) {
                        list[absoluteAdapterPosition].isSelected = true
                        lastSelectedPosition = absoluteAdapterPosition
                        callback(list[absoluteAdapterPosition])
                        notifyItemChanged(absoluteAdapterPosition, list[absoluteAdapterPosition])
                    }
                }
            }
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): VenueUtilAdapter.ViewHolder {
            val binding = LayoutVenueUtilsItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: VenueUtilAdapter.ViewHolder, position: Int) {
            holder.init()
        }

        override fun getItemCount(): Int = list.size
    }
}