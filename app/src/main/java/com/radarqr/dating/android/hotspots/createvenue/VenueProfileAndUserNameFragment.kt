package com.radarqr.dating.android.hotspots.createvenue

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentVenueProfileAndUsernameBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.venue.MyVenueViewModel
import com.radarqr.dating.android.utility.BaseUtils.isInternetAvailable
import com.radarqr.dating.android.utility.Utility.openLink
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.exception.RequiredFieldException
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class VenueProfileAndUserNameFragment : VenueBaseFragment<FragmentVenueProfileAndUsernameBinding>(),
    ViewClickHandler {

    private val createVenueViewModel: CreateVenueViewModel by viewModel()
    private val venueUpdateViewModel: VenueUpdateViewModel by viewModel()
    private val myVenueViewModel: MyVenueViewModel by viewModel()
    private var isUpdating: Boolean = false

    override fun getLayoutRes(): Int = R.layout.fragment_venue_profile_and_username


    override fun init(view: View, savedInstanceState: Bundle?) {
        binding.viewHandler = this
        binding.viewModel = createVenueViewModel

        arguments?.apply {
//            createVenueViewModel.venueData = getSerializable(Constants.EXTRA_DATA) as MyVenuesData?
            isUpdating = getBoolean(Constants.IS_UPDATING, false)
            if (isUpdating)
                binding.tvNext.text = resources.getString(R.string.done)
        }

        /*  createVenueViewModel.venueData?.apply {
              createVenueViewModel.venueId.set(_id)
              createVenueViewModel.venueName.set(name)
              createVenueViewModel.uniqueName.set(uniquename)
          }*/

        if (this.view != null && isAdded && isUpdating) {
            venueUpdateViewModel.updatingVenueData.observe(viewLifecycleOwner) {
                it?.let {
                    createVenueViewModel.venueData = it
                    createVenueViewModel.venueId.set(it._id)
                    createVenueViewModel.venueName.set(it.name)
                    createVenueViewModel.uniqueName.set(it.uniquename)
                }
            }
        } else {
            venueUpdateViewModel.updatingVenueData.value = null
        }

        binding.etUsername.setOnFocusChangeListener { _, _ ->
            binding.tvErrorMessage.text = ""
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivBack -> {
                this.view?.findNavController()?.popBackStack()
            }

            R.id.tvNext -> {
                if (requireActivity().isInternetAvailable()) {
                    hideKeyboard(binding.root)
                    binding.etUsername.clearFocus()
                    binding.etFirstname.clearFocus()
                    try {
                        createVenueViewModel.isValid()
                        if (isUpdating) {
                            updateVenue()
                        } else
                            createVenue()
                    } catch (e: RequiredFieldException) {
                        e.localizedMessage?.let {
                            if (it.isNotEmpty()) {
                                binding.tvErrorMessage.text = it
                                binding.tvErrorMessage.visible(isVisible = true)
                            }

                        }
                    }
                }
            }
        }
    }

    private fun createVenue() {
        if (view != null && isAdded && isVisible) {
            handleProgressAndNext(isVisible = true)
            lifecycleScope.launch {
                createVenueViewModel.createVenue().observe(viewLifecycleOwner) {
                    handleProgressAndNext(isVisible = false)
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            it.message?.let { it1 ->
                                if (it.statusCode == 422) {
                                    handleErrorMessages(it1)
                                } else requireContext().showToast(it1)
                            }
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            it.data.data?.let { data ->
                                myVenueViewModel.myVenueData.add(0, data)
                                myVenueViewModel.position = 0
                            }
                            venueUpdateViewModel.updatingVenueData.value = it.data.data
                            val bundle = Bundle().apply {
                                putBoolean(Constants.TYPE, true)
                            }
                            this@VenueProfileAndUserNameFragment.view?.findNavController()
                                ?.navigate(R.id.action_NameFragment_to_venueTypeFragment, bundle)
                        }
                    }
                }
            }
        }
    }

    private fun handleProgressAndNext(isVisible: Boolean) {
        binding.progressBar.visible(isVisible)
        binding.tvNext.isEnabled = !isVisible
    }

    private fun handleErrorMessages(it1: String) {
        if (it1.equals("Venue subscription not found", ignoreCase = true)
            || it1.equals("Venue subscription is paused", ignoreCase = true)
            || it1.equals("Venue subscription is expired", ignoreCase = true)
            || it1.equals(
                "You are not allowed to add more than allocated venues limit",
                ignoreCase = true
            )
        ) {
            AlertDialog.Builder(requireContext())
                .setTitle(resources.getString(R.string.message))
                .setMessage("To add more venues, please purchase your Promo Packet HERE.")
                .setPositiveButton("Buy Now") { dialog, i ->
                    dialog.dismiss()
                    requireActivity().openLink(Constants.SUBSCRIPTION_URL)
                }
                .setNegativeButton(resources.getString(R.string.cancel)) { dialog, _ -> dialog.dismiss() }
                .show()
        } else {
            binding.tvErrorMessage.text = it1
            binding.tvErrorMessage.visible(isVisible = true)
        }
    }

    private fun updateVenue() {
        if (view != null && isAdded && isVisible)
            lifecycleScope.launch {
                createVenueViewModel.updateVenue().observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            it.message?.let { it1 ->
                                if (it.statusCode == 422) {
                                    binding.tvErrorMessage.text = it1
                                    binding.tvErrorMessage.visible(isVisible = true)
                                } else requireContext().showToast(it1)
                            }
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            venueUpdateViewModel.updatingVenueData.value = it.data.data
                            this@VenueProfileAndUserNameFragment.view?.findNavController()
                                ?.popBackStack()
                        }
                    }
                }
            }
    }
}