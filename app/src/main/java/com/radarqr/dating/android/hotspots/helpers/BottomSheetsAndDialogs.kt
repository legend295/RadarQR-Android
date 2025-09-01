package com.radarqr.dating.android.hotspots.helpers

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.radarqr.dating.android.R
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.*
import com.radarqr.dating.android.utility.BaseUtils.isInternetAvailable
import com.radarqr.dating.android.utility.Utility.openLink
import com.radarqr.dating.android.utility.Utility.toPx
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.enums.SubscriptionPopUpType
import com.radarqr.dating.android.utility.handler.ViewClickHandler

fun Context.sendForApprovalSheet(callBack: (View, BottomSheetDialog, LayoutSendForApprovalSheetBinding) -> Unit) {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutSendForApprovalSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    layout.tvContinue.setOnClickListener {
        if (isInternetAvailable()) callBack(it, sheet, layout)
    }

    layout.tvCancel.setOnClickListener {
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.show()
}

fun Context.howVenueWorkSheet() {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutHowVenueWorkSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)
    sheet.apply {
        setContentView(layout.root)
        setCommonSettings()
        setCanceledOnTouchOutside(true)
        show()
    }
}

fun Context.venueAlbumInfo() {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutHowVenueWorkSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        tvTitle.text = getString(R.string.venue_album_info_title)
        tvFirstLine.text = getString(R.string.venue_album_first)
        tvSecondLine.text = getString(R.string.venue_album_second)
        tvThirdLine.text = getString(R.string.venue_album_third)
    }

    sheet.apply {
        setContentView(layout.root)
        setCommonSettings()
        setCanceledOnTouchOutside(true)
        show()
    }
}

fun Context.registerToBeAHotSpotFullScreenDialog(callBack: () -> Unit) {
    val dialog = Dialog(this, R.style.DialogStyle)
    val layout = LayoutRegisterToBeHotspotDialogBinding.inflate(
        LayoutInflater.from(dialog.context), null, false
    )
    layout.viewHandler = object : ViewClickHandler {
        override fun onClick(view: View) {
            when (view.id) {
                R.id.ivBack -> {
                    dialog.dismiss()
                }

                R.id.tvRegisterToBeAHotspot -> {
                    callBack()
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.cancel()
                    }, 200)
                }
            }
        }
    }
    dialog.setContentView(layout.root)
    dialog.setCustomSettings()
    dialog.show()
}

fun Context.venueSubscriptionFullScreenDialog(callBack: () -> Unit) {
    val dialog = Dialog(this, R.style.DialogStyle)
    val layout = LayoutVenueSubscriptionDialogBinding.inflate(
        LayoutInflater.from(dialog.context), null, false
    )
    layout.viewHandler = object : ViewClickHandler {
        override fun onClick(view: View) {
            when (view.id) {
                R.id.ivBack, R.id.tvNoThanks -> {
                    dialog.dismiss()
                }

                R.id.tvLetsDoIt -> {
                    callBack()
                    Handler(Looper.getMainLooper()).postDelayed({
                        dialog.cancel()
                    }, 200)
                }
            }
        }
    }
    dialog.setContentView(layout.root)
    dialog.setCustomSettings()
    dialog.show()
}

fun Context.deleteVenueDialog(callBack: (Dialog, FragmentDeleteVenueBinding) -> Unit) {
    val dialog = Dialog(this, R.style.DialogStyle)
    val layout = FragmentDeleteVenueBinding.inflate(
        LayoutInflater.from(dialog.context), null, false
    )
    val isChecked: ObservableField<Boolean> = ObservableField<Boolean>(false)
    layout.isChecked = isChecked.get()
    layout.viewHandler = object : ViewClickHandler {
        override fun onClick(view: View) {
            when (view.id) {
                R.id.ivBack -> {
                    dialog.dismiss()
                }

                R.id.tvDelete -> {
                    if (isInternetAvailable()) {
                        layout.progressBarApi.visible(isVisible = true)
                        callBack(dialog, layout)
                    }
//                    dialog.cancel()
                }
            }
        }
    }

    dialog.setContentView(layout.root)
    dialog.setCustomSettings()
    dialog.show()
}

fun Context.approvalDialog(callBack: (Dialog, LayoutApprovalDialogBinding) -> Unit) {
    val dialog = Dialog(this, R.style.DialogStyle)
    val layout = LayoutApprovalDialogBinding.inflate(
        LayoutInflater.from(dialog.context), null, false
    )
    val isChecked: ObservableField<Boolean> = ObservableField<Boolean>(false)
    layout.isChecked = isChecked.get()
    layout.viewHandler = object : ViewClickHandler {
        override fun onClick(view: View) {
            when (view.id) {
                R.id.ivBack -> {
                    dialog.dismiss()
                }

                R.id.tvContinue -> {
                    callBack(dialog, layout)
                }
            }
        }
    }

    dialog.setContentView(layout.root)
    dialog.setCustomSettings()
    dialog.show()
}

fun Context.approvalCompleteDialog(callBack: (Dialog) -> Unit) {
    val dialog = Dialog(this, R.style.DialogStyle)
    val layout = LayoutCompleteApprovalDialogBinding.inflate(
        LayoutInflater.from(dialog.context), null, false
    )
    layout.viewHandler = object : ViewClickHandler {
        override fun onClick(view: View) {
            when (view.id) {/*R.id.ivBack -> {
                    dialog.dismiss()
                }*/
                R.id.tvContinue -> {
                    callBack(dialog)
                }
            }
        }
    }

    dialog.setContentView(layout.root)
    dialog.setCustomSettings()
    dialog.show()
}

fun Context.addACloseFriend(
    username: String, callBack: (View, BottomSheetDialog, LayoutSendForApprovalSheetBinding) -> Unit
) {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutSendForApprovalSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        layout.tvCancel.visible(isVisible = false)
        ivNext.setImageDrawable(
            ContextCompat.getDrawable(
                this@addACloseFriend, R.drawable.ic_send_request
            )
        )
        tvTitle.text = getString(R.string.add_a_close_friend)
        tvContentFirst.text =
            StringBuilder().append("Would you like to request to add ").append(username)
                .append(" \"as a close friend\"?").toString()
        tvContentSecond.visible(isVisible = true)
        tvContentSecond.text =
            "Close Friends can be tagged in your photos when visiting Hot Spots! Maximizing both of your chances for meeting singles \"in the wild\" at venues you enjoy."
        tvContinue.text = "Add Close Friend"

    }

    layout.tvContinue.setOnClickListener {
        if (isInternetAvailable()) callBack(it, sheet, layout)
    }

    layout.tvCancel.setOnClickListener {
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.show()
}

fun Context.confirmCloseFriend(
    view: View?,
    username: String,
    callBack: (View, BottomSheetDialog, LayoutSendForApprovalSheetBinding) -> Unit
) {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutSendForApprovalSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        when (view?.id) {
            R.id.ivRejectRequest -> {
                ivNext.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@confirmCloseFriend, R.drawable.ic_remove_close_friend
                    )
                )
                tvTitle.text = getString(R.string.decline_request)
                tvContentFirst.text =
                    StringBuilder().append("Are you sure you want to reject this person as a close friend?")
                        .toString()
            }

            else -> {
                ivNext.setImageDrawable(
                    ContextCompat.getDrawable(
                        this@confirmCloseFriend, R.drawable.ic_pending_request
                    )
                )
                tvTitle.text = getString(R.string.confirm_close_friend)
                tvContentFirst.text =
                    StringBuilder().append("Are you sure you want to add this person as a close friend?")
                        .toString()
                tvCancel.text = getString(R.string.decline)
                tvCancel.setTextColor(
                    ContextCompat.getColor(
                        this@confirmCloseFriend, R.color.white
                    )
                )
                tvCancel.background =
                    ContextCompat.getDrawable(this@confirmCloseFriend, R.drawable.green_fill_rect)
                tvCancel.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this@confirmCloseFriend, R.color.radish
                    )
                )
                tvContinue.text = getString(R.string.accept)

            }
        }


    }

    layout.tvContinue.setOnClickListener {
        if (isInternetAvailable()) callBack(it, sheet, layout)
    }

    layout.tvCancel.setOnClickListener {
        if (view?.id == R.id.ivRejectRequest) {
            sheet.dismissWithAnimation = true
            sheet.dismiss()
            return@setOnClickListener
        }
        if (isInternetAvailable()) callBack(it, sheet, layout)
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.show()
}

fun Context.removeCloseFriend(
    username: String, callBack: (View, BottomSheetDialog, LayoutSendForApprovalSheetBinding) -> Unit
) {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutSendForApprovalSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        ivNext.setImageDrawable(
            ContextCompat.getDrawable(
                this@removeCloseFriend, R.drawable.ic_remove_close_friend
            )
        )
        tvTitle.text = getString(R.string.remove_close_friend)
        tvContentFirst.text =
            StringBuilder().append("Are you sure you want to remove this person as a close friend?")
                .toString()/* tvCancel.text = getString(R.string.reject)
         tvCancel.setTextColor(ContextCompat.getColor(this@removeCloseFriend, R.color.white))
         tvCancel.background =
             ContextCompat.getDrawable(this@removeCloseFriend, R.drawable.green_fill_rect)
         tvCancel.backgroundTintList =
             ColorStateList.valueOf(ContextCompat.getColor(this@removeCloseFriend, R.color.red))
         tvContinue.text = getString(R.string.accept)*/


    }

    layout.tvContinue.setOnClickListener {
        if (isInternetAvailable()) callBack(it, sheet, layout)
    }

    layout.tvCancel.setOnClickListener {
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.show()
}

fun Context.undoRequest(
    username: String, callBack: (View, BottomSheetDialog, LayoutSendForApprovalSheetBinding) -> Unit
) {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutSendForApprovalSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        ivNext.setImageDrawable(
            ContextCompat.getDrawable(
                this@undoRequest, R.drawable.ic_remove_close_friend
            )
        )
        tvTitle.text = "Undo sent request"
        tvContentFirst.text =
            StringBuilder().append("Are you sure you want to undo this friend request?").toString()/* tvCancel.text = getString(R.string.reject)
         tvCancel.setTextColor(ContextCompat.getColor(this@removeCloseFriend, R.color.white))
         tvCancel.background =
             ContextCompat.getDrawable(this@removeCloseFriend, R.drawable.green_fill_rect)
         tvCancel.backgroundTintList =
             ColorStateList.valueOf(ContextCompat.getColor(this@removeCloseFriend, R.color.red))
         tvContinue.text = getString(R.string.accept)*/


    }

    layout.tvContinue.setOnClickListener {
        if (isInternetAvailable()) callBack(it, sheet, layout)
    }

    layout.tvCancel.setOnClickListener {
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.show()
}

fun Context.unableToEnableRoamingTimerDialog(isRoamingTimerEnabledForOtherVenue: Boolean = false) {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutSendForApprovalSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        val ten = (15).toPx.toInt()
        val twenty = (20).toPx.toInt()
        parent.setPadding(ten, ten, ten, twenty)
        ivNext.setImageDrawable(
            ContextCompat.getDrawable(
                this@unableToEnableRoamingTimerDialog, R.drawable.ic_roaming_timer
            )
        )
        tvTitle.text =
            if (isRoamingTimerEnabledForOtherVenue) getString(R.string.roaming_timer) else getString(
                R.string.sorry_please_reach_the_venue
            )
        val text =
            if (isRoamingTimerEnabledForOtherVenue) "Your roaming timer is enabled for some other Venue. You can see singles of one venue at a time for which roaming timer is enabled."
            else "To enable the roaming timer on a Venue you should be in around 20m to 30m. Please go near the Venue to enable the timer."
        tvContentFirst.text = StringBuilder().append(text).toString()
        tvCancel.visible(isVisible = false)
        tvContinue.text = getString(R.string.okay)

    }

    layout.tvContinue.setOnClickListener {
        sheet.dismiss()
    }

    layout.tvCancel.setOnClickListener {
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.show()
}

fun Context.enableRoamingTimerDialog(callBack: (View, BottomSheetDialog, LayoutSendForApprovalSheetBinding) -> Unit) {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutSendForApprovalSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        ivNext.setImageDrawable(
            ContextCompat.getDrawable(
                this@enableRoamingTimerDialog, R.drawable.ic_roaming_timer
            )
        )
        tvTitle.text = getString(R.string.roaming_timer)
        tvContentFirst.text =
            StringBuilder().append("You can see the singles after enabling the roaming timer for this venue. Please do it now.")
                .toString()
        tvContinue.text = getString(R.string.enable_roaming_timer)

    }

    layout.tvContinue.setOnClickListener {
        callBack(it, sheet, layout)
        sheet.dismiss()
    }

    layout.tvCancel.setOnClickListener {
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.show()
}

fun Context.roamingTimerEnableMessage(callBack: (View, BottomSheetDialog, LayoutSendForApprovalSheetBinding) -> Unit) {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutSendForApprovalSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        ivNext.setImageDrawable(
            ContextCompat.getDrawable(
                this@roamingTimerEnableMessage, R.drawable.ic_roaming_timer
            )
        )
        tvTitle.text = getString(R.string.roaming_timer)
        tvContentFirst.text =
            StringBuilder().append("You can extend the timer when it's about to expire in the last 15 mins only...")
                .toString()
        tvContinue.text = getString(R.string.okay)
        tvCancel.visible(isVisible = false)

    }

    layout.tvContinue.setOnClickListener {
        callBack(it, sheet, layout)
        sheet.dismiss()
    }

    layout.tvCancel.setOnClickListener {
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.show()
}

fun Context.actionNeeded() {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutSendForApprovalSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        ivNext.setImageDrawable(
            ContextCompat.getDrawable(
                this@actionNeeded, R.drawable.ic_info_red
            )
        )

        tvTitle.text = getString(R.string.action_needed)
        tvTitle.setTextColor(ContextCompat.getColor(this@actionNeeded, R.color.redFigma))
        tvContentFirst.text =
            StringBuilder().append("Your Venue is unable to be approved at this time. Please check your email (and Junk Folder) to learn what is needed for approval. You may send for approval again once changes have been made.")
                .toString()
        tvContinue.text = getString(R.string.okay)
        tvCancel.visible(isVisible = false)

    }

    layout.tvContinue.setOnClickListener {
        sheet.dismiss()
    }

    layout.tvCancel.setOnClickListener {
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.show()
}

fun Context.venuePausedDialog(callBack: () -> Unit) {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutSendForApprovalSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        ivNext.setImageDrawable(
            ContextCompat.getDrawable(
                this@venuePausedDialog, R.drawable.pause
            )
        )

        tvTitle.text = getString(R.string.uh_oh)
        tvTitle.setTextColor(ContextCompat.getColor(this@venuePausedDialog, R.color.redFigma))
        tvContentFirst.text =
            StringBuilder().append("This “Hot Spot” is taking a quick breather from the map. Until they return, explore other Hot Spots Near You.")
                .toString()
        tvContentSecond.visible(isVisible = true)
        tvContentSecond.text =
            StringBuilder().append("Love this place to bits? Show ‘em some love while you’re still there and let them know you're excited to see them back on the map.")
                .toString()

        tvContinue.text = getString(R.string.show_me_new_venues)
        tvCancel.visible(isVisible = false)

    }

    layout.tvContinue.setOnClickListener {
        callBack()
        sheet.dismiss()
    }

    layout.tvCancel.setOnClickListener {
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.show()
}

/*#region Subscription sheets*/

fun Fragment.showSubscriptionSheet(
    subscriptionPopUpType: SubscriptionPopUpType,
    popBackStack: Boolean = true,
    callBack: (Boolean) -> Unit
) {
    val sheet = BottomSheetDialog(requireContext(), R.style.DialogStyle)
    val layout =
        LayoutSubscriptionDialogsBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        val icon: Int = subscriptionPopUpType.getIcon()
        val title: String = subscriptionPopUpType.getTitle()
        val firstMessage: String = subscriptionPopUpType.getFirstMessage()/*when (subscriptionPopUpType) {
            SubscriptionPopUpType.MARK_FAVORITE -> {
                icon = subscriptionPopUpType.getIcon()
                title = subscriptionPopUpType.getTitle()
                firstMessage = subscriptionPopUpType.getFirstMessage()
            }

            SubscriptionPopUpType.LIKE -> {
                icon = subscriptionPopUpType.getIcon()
                title = subscriptionPopUpType.getTitle()
                firstMessage = subscriptionPopUpType.getFirstMessage()
            }

            SubscriptionPopUpType.PREFERENCES -> {
                icon = subscriptionPopUpType.getIcon()
                title = subscriptionPopUpType.getTitle()
                firstMessage = subscriptionPopUpType.getFirstMessage()
            }

            SubscriptionPopUpType.RECOMMENDATION_LIMIT_REACHED -> {
                icon = subscriptionPopUpType.getIcon()
                title = subscriptionPopUpType.getTitle()
                firstMessage = subscriptionPopUpType.getFirstMessage()
            }

            SubscriptionPopUpType.SEND_LIKE_WITH_MESSAGE -> {
                icon = subscriptionPopUpType.getIcon()
                title = subscriptionPopUpType.getTitle()
                firstMessage = subscriptionPopUpType.getFirstMessage()
            }

            SubscriptionPopUpType.RECOMMENDATION_UNDO -> {

            }
        }*/

        ivNext.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(), icon
            )
        )

        tvTitle.text = title
        tvContentFirst.text = StringBuilder().append(firstMessage).toString()

    }

    layout.tvContinue.setOnClickListener {
        val bundle = Bundle()
        callBack(true)
        when (subscriptionPopUpType) {
            SubscriptionPopUpType.MARK_FAVORITE -> {
                bundle.putString(Constants.FROM, Constants.MixPanelFrom.MARK_YOUR_FAVORITE_POPUP)
            }

            SubscriptionPopUpType.LIKE -> {
                bundle.putString(Constants.FROM, Constants.MixPanelFrom.LIKE)
            }

            SubscriptionPopUpType.PREFERENCES -> {
                bundle.putString(
                    Constants.FROM,
                    Constants.MixPanelFrom.PREFERENCES_ADVANCE_FILTER_POPUP
                )
            }

            SubscriptionPopUpType.RECOMMENDATION_LIMIT_REACHED -> {
                bundle.putString(
                    Constants.FROM,
                    Constants.MixPanelFrom.RECOMMENDATION_LIMIT_REACHED
                )
            }

            SubscriptionPopUpType.SEND_LIKE_WITH_MESSAGE -> {
                bundle.putString(Constants.FROM, Constants.MixPanelFrom.FROM_SHOOT_YOUR_SHOT_POPUP)
            }

            SubscriptionPopUpType.RECOMMENDATION_UNDO -> {
                bundle.putString(Constants.FROM, Constants.MixPanelFrom.FROM_RECALL_POPUP)
            }

            SubscriptionPopUpType.SEE_TAGGED_USERS -> {
                bundle.putString(Constants.FROM, Constants.MixPanelFrom.FROM_SEE_TAGGED_USER)
            }
        }
        this.view?.findNavController()?.navigate(R.id.subscriptionFragment, bundle)
        sheet.dismiss()
    }

    layout.tvCancel.setOnClickListener {
        callBack(false)
        if (popBackStack) this.view?.findNavController()?.popBackStack()
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCancelable(false)
    sheet.setCommonSettings()
    sheet.show()
}

/*#endregion*/

fun Context.venuePausedDialog(reason: String) {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutSendForApprovalSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        ivNext.setImageDrawable(
            ContextCompat.getDrawable(
                this@venuePausedDialog, R.drawable.ic_info_red
            )
        )

        tvTitle.text = getString(R.string.venue_paused)
        tvTitle.setTextColor(ContextCompat.getColor(this@venuePausedDialog, R.color.redFigma))
        tvContentFirst.text =
            StringBuilder().append("Your venue is currently on hold due to the following reason:")
                .toString()
        tvContentSecond.visible(isVisible = true)
        tvContentSecond.text = reason
        tvContinue.text = getString(R.string.okay)
        tvCancel.visible(isVisible = false)

    }

    layout.tvContinue.setOnClickListener {
        sheet.dismiss()
    }

    layout.tvCancel.setOnClickListener {
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.show()
}

fun Fragment.showFirstAd() {
    val dialog = Dialog(requireContext(), android.R.style.Theme_NoTitleBar)
    val layout =
        LayoutFirstAdsSheetBinding.inflate(LayoutInflater.from(requireContext()), null, false)

    layout.ivClose.setOnClickListener { dialog.dismiss() }

    layout.tvUpgrade.setOnClickListener {
        this.view?.findNavController()?.navigate(R.id.subscriptionFragment, Bundle().apply {
            putString(
                Constants.FROM, Constants.MixPanelFrom.RECOMMENDATION_ADS
            )
            putString(Constants.ADS_FROM, Constants.MixPanelFrom.CHEMISTRY_SWIPE)
        })
        dialog.dismiss()
    }
    dialog.window?.setWindowAnimations(R.style.AnimationDialog)
    dialog.setContentView(layout.root)
    dialog.show()
}


fun Fragment.showSecondAd() {
    val dialog = Dialog(requireContext(), android.R.style.Theme_NoTitleBar)
    val layout =
        LayoutSecondAdsSheetBinding.inflate(LayoutInflater.from(requireContext()), null, false)
    layout.ivClose.setOnClickListener { dialog.dismiss() }

    layout.tvUpgrade.setOnClickListener {
        this.view?.findNavController()?.navigate(R.id.subscriptionFragment, Bundle().apply {
            putString(
                Constants.FROM, Constants.MixPanelFrom.RECOMMENDATION_ADS
            )
            putString(Constants.ADS_FROM, Constants.MixPanelFrom.SWAG_SHOP)
        })
        dialog.dismiss()
    }

    layout.tvOpenSwagShop.setOnClickListener {
        requireActivity().openLink(Constants.SWAG_SHOP_URL)
        dialog.dismiss()
    }
    dialog.window?.setWindowAnimations(R.style.AnimationDialog)
    dialog.setContentView(layout.root)
    dialog.show()
}

fun Fragment.showThirdAd() {
    val dialog = Dialog(requireContext(), android.R.style.Theme_NoTitleBar)
    val layout =
        LayoutThirdAdsSheetBinding.inflate(LayoutInflater.from(requireContext()), null, false)
    layout.ivClose.setOnClickListener { dialog.dismiss() }

    layout.tvUpgrade.setOnClickListener {
        this.view?.findNavController()?.navigate(R.id.subscriptionFragment, Bundle().apply {
            putString(
                Constants.FROM, Constants.MixPanelFrom.RECOMMENDATION_ADS
            )
            putString(Constants.ADS_FROM, Constants.MixPanelFrom.LIKES)
        })
        dialog.dismiss()
    }
    dialog.window?.setWindowAnimations(R.style.AnimationDialog)
    dialog.setContentView(layout.root)
    dialog.show()
}

fun Fragment.showForthAd() {
    val dialog = Dialog(requireContext(), android.R.style.Theme_NoTitleBar)
    val layout =
        LayoutForthAdsSheetBinding.inflate(LayoutInflater.from(requireContext()), null, false)
    layout.ivClose.setOnClickListener { dialog.dismiss() }

    layout.tvUpgrade.setOnClickListener {
        this.view?.findNavController()?.navigate(R.id.subscriptionFragment, Bundle().apply {
            putString(
                Constants.FROM, Constants.MixPanelFrom.RECOMMENDATION_ADS
            )
            putString(Constants.ADS_FROM, Constants.MixPanelFrom.TAGGED)
        })
        dialog.dismiss()
    }
    dialog.window?.setWindowAnimations(R.style.AnimationDialog)
    dialog.setContentView(layout.root)
    dialog.show()
}

fun Fragment.showFifthAd() {
    val dialog = Dialog(requireContext(), android.R.style.Theme_NoTitleBar)
    val layout =
        LayoutFifthAdsSheetBinding.inflate(LayoutInflater.from(requireContext()), null, false)
    layout.ivClose.setOnClickListener { dialog.dismiss() }

    layout.tvUpgrade.setOnClickListener {
        this.view?.findNavController()?.navigate(R.id.subscriptionFragment, Bundle().apply {
            putString(
                Constants.FROM, Constants.MixPanelFrom.RECOMMENDATION_ADS
            )
            putString(Constants.ADS_FROM, Constants.MixPanelFrom.HOTSPOT_MAP)
        })
        dialog.dismiss()
    }
    dialog.window?.setWindowAnimations(R.style.AnimationDialog)
    dialog.setContentView(layout.root)
    dialog.show()
}

fun Fragment.showSixthAd() {
    val dialog = Dialog(requireContext(), android.R.style.Theme_NoTitleBar)
    val layout =
        LayoutSixthAdsSheetBinding.inflate(LayoutInflater.from(requireContext()), null, false)
    layout.ivClose.setOnClickListener { dialog.dismiss() }

    layout.tvUpgrade.setOnClickListener {
        this.view?.findNavController()?.navigate(R.id.subscriptionFragment, Bundle().apply {
            putString(
                Constants.FROM, Constants.MixPanelFrom.RECOMMENDATION_ADS
            )
            putString(Constants.ADS_FROM, Constants.MixPanelFrom.HOTSPOTS)
        })
        dialog.dismiss()
    }
    dialog.window?.setWindowAnimations(R.style.AnimationDialog)
    dialog.setContentView(layout.root)
    dialog.show()
}

fun Fragment.showUnMatchConfirmation(callBack: () -> Unit) {
    val dialog = BottomSheetDialog(requireContext(), R.style.DialogStyle)
    val layout = LayoutBottomSheetUnmatchConfirmationBinding.inflate(
        LayoutInflater.from(requireContext()), null, false
    )
    layout.tvYes.setOnClickListener {
        dialog.dismiss()
        callBack()
    }

    layout.tvCancel.setOnClickListener {
        dialog.dismiss()
    }
    dialog.window?.setWindowAnimations(R.style.AnimationDialog)
    dialog.setContentView(layout.root)
    dialog.setCommonSettings()
    dialog.show()
}

fun Context.addThreeImageDialog(
    imageSize: Int = 0,
    callBack: (Boolean) -> Unit
) {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutAddThreeImageSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        val msg = "Profiles require a minimum of 3 photos, please add to continue."
        tvContentFirst.text = msg
    }

    layout.tvContinue.setOnClickListener {
        callBack(true)
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.setCancelable(false)
    sheet.show()
}

fun Context.addThreeImageDialogConfirmation(
    imageSize: Int = 0,
    callBack: (Boolean) -> Unit
) {
    val sheet = BottomSheetDialog(this, R.style.DialogStyle)
    val layout =
        LayoutAddThreeImageConfirmationDialogBinding.inflate(LayoutInflater.from(sheet.context), null, false)

    with(layout) {
        val msg = "Three photos are required to complete your profile. Boost your likes by adding even more!"
        tvContentFirst.text = msg
    }

    layout.tvContinue.setOnClickListener {
        callBack(true)
        sheet.dismiss()
    }

    layout.tvCancel.setOnClickListener {
        callBack(false)
        sheet.dismiss()
    }

    sheet.setContentView(layout.root)
    sheet.setCommonSettings()
    sheet.setCancelable(false)
    sheet.show()
}


fun Dialog.setCustomSettings() {
    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    window?.setDimAmount(0.4f)
    window?.statusBarColor = Color.TRANSPARENT
    window?.setLayout(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
    )
}

fun BottomSheetDialog.setCommonSettings() {
    behavior.skipCollapsed = true
    behavior.state = BottomSheetBehavior.STATE_EXPANDED

    dismissWithAnimation = true
    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    window?.setDimAmount(0.4f)
    window?.statusBarColor = Color.TRANSPARENT
}