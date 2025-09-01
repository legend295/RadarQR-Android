package com.radarqr.dating.android.hotspots.venue

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.View
import androidx.core.text.bold
import androidx.core.text.inSpans
import androidx.navigation.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.SubscriptionStatus
import com.radarqr.dating.android.databinding.FragmentVenuePromotionBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.venue.viewmodel.VenuePromotionViewModel
import com.radarqr.dating.android.utility.Utility.openLink
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.serializable
import org.koin.androidx.viewmodel.ext.android.viewModel

class VenuePromotionFragment : VenueBaseFragment<FragmentVenuePromotionBinding>(),
    ViewClickHandler {

    companion object {
        const val ACTIVE = 1
        const val IN_ACTIVE = 2
    }

    private var subscriptionStatus: SubscriptionStatus? = null

    private val venuePromotionViewModel: VenuePromotionViewModel by viewModel()

    override fun getLayoutRes(): Int = R.layout.fragment_venue_promotion

    override fun init(view: View, savedInstanceState: Bundle?) {
        binding.viewHandler = this
        subscriptionStatus = arguments?.serializable(Constants.EXTRA)
        venuePromotionViewModel.subscriptionStatus.postValue(subscriptionStatus)
        handleObservers()

    }

    override fun onResume() {
        super.onResume()
        binding.isProgressBarVisible = true
        venuePromotionViewModel.venueSubscriptionStatus()
    }

    private fun handleObservers() {
        venuePromotionViewModel.subscriptionStatus.observe(viewLifecycleOwner) {
            it?.handleSubscriptionStatusResponse()
        }
    }

    private fun SubscriptionStatus.handleSubscriptionStatusResponse() {
        binding.isProgressBarVisible = false
        val isActive = subscription_status.status == ACTIVE
        binding.isSubscriptionActive = isActive
        /*
        Your time on the map has expired. Please
        select a new package to reactivate your
        Hot Spot.
        */
        val text =
            if (isActive) {
                binding.tvShortMessage.visible(isVisible = true)
                SpannableStringBuilder().bold { append("Welcome Aboard! Let's Get Started.") }
                    .append("\n\n")
                    .append("We are excited to have you as part of\nour “Hot Spot” network! Begin by\nentering your venues. Please ensure\nthat you provide all the necessary\ninformation before submitting your\nvenue for approval.")
            } else {
                if (subscriptionStatus?.subscription?.expiration_date.isNullOrEmpty()) {
                    binding.tvShortMessage.visible(isVisible = true)
                    resources.getString(R.string.venue_subscription_promotion_msg)
                } else {
                    binding.tvShortMessage.visible(isVisible = false)
                    SpannableStringBuilder().bold { append("Your time on the map has expired. Please select a new package to reactivate your Hot Spot.") }
                        .append("\n\n")
                        .append("RadarQR has got you covered! Our \"Hot Spots\" advertising partnerships bring new customers straight to your venue's doorstep for some real-life match-making magic.")
                }
            }

        binding.tvLongMessage.text = text
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvLetsDoIt -> {
                if (venuePromotionViewModel.subscriptionStatus.value?.subscription_status?.status == ACTIVE) {
                    this.view?.findNavController()
                        ?.navigate(R.id.action_venuePromotion_to_venueFragment)
                } else requireActivity().openLink(Constants.SUBSCRIPTION_URL)
            }

            R.id.tvNoThanks -> this.view?.findNavController()?.popBackStack()
        }
    }
}