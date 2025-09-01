package com.radarqr.dating.android.hotspots.createvenue

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.radarqr.dating.android.BuildConfig
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.constant.Constants.VenueStatus.VENUE_PAUSED
import com.radarqr.dating.android.databinding.FragmentVenueTypeBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.createvenue.adapter.VenueTypeAdapter
import com.radarqr.dating.android.hotspots.helpers.VenueUtils
import com.radarqr.dating.android.hotspots.helpers.VenueUtils.openBottomSheet
import com.radarqr.dating.android.hotspots.model.ContactInfo
import com.radarqr.dating.android.hotspots.model.MyVenuesData
import com.radarqr.dating.android.hotspots.model.UpdateVenueRequest
import com.radarqr.dating.android.utility.BaseUtils.isInternetAvailable
import com.radarqr.dating.android.utility.Utility.getCityAndState
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.environment.Environment
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class VenueUpdateFragment : VenueBaseFragment<FragmentVenueTypeBinding>(), ViewClickHandler {

    private val itemList = ArrayList<VenueTypeAdapter.VenueTypeModel>()
    private val adapter by lazy { VenueTypeAdapter(itemList, adapterCallback) }
    private var venueData: MyVenuesData? = null
    private var fromVenueNameScreen = false
    private val venueUpdateViewModel: VenueUpdateViewModel by viewModel()
//    private val contactInfo: ContactInfo = ContactInfo()

    override fun getLayoutRes(): Int = R.layout.fragment_venue_type

    @SuppressLint("NotifyDataSetChanged")
    override fun init(view: View, savedInstanceState: Bundle?) {
        binding.viewHandler = this

//        venueData = arguments?.getSerializable(Constants.EXTRA_DATA) as MyVenuesData?
        fromVenueNameScreen = arguments?.getBoolean(Constants.TYPE, false) ?: false
        /* venueData?.let {
             binding.venueProgress.progress = it.completepercentage
             binding.tvVenueComplete.text = StringBuilder().append(it.completepercentage)
                 .append(binding.root.context?.getString(R.string.percentage_complete)).toString()
             venueUpdateViewModel.setContactInfo(it)
             venueUpdateViewModel.updateVenueRequest.value?.setData(it)
         }*/

        if (this.view != null && isAdded) {
            venueUpdateViewModel.updatingVenueData.observe(viewLifecycleOwner) {
                it?.let {
                    venueData = it
                    itemList.clear()
                    itemList.addAll(getVenueList())
                    adapter.notifyDataSetChanged()
                    binding.venueProgress.progress = it.completepercentage
                    binding.tvVenueComplete.text = StringBuilder().append(it.completepercentage)
                        .append(binding.root.context?.getString(R.string.percentage_complete))
                        .toString()
                    venueUpdateViewModel.setContactInfo(it)
                    venueUpdateViewModel.description.set(it.description)
                    venueUpdateViewModel.updateVenueRequest.value?.setData(it)
                }
            }
        }
        if (!adapter.hasObservers())
            adapter.setHasStableIds(true)
        binding.rvVenueType.setHasFixedSize(true)
        binding.rvVenueType.adapter = adapter
    }

    private val adapterCallback =
        { data: VenueTypeAdapter.VenueTypeModel, _: Int ->
            if (view != null && isAdded) {
                if (requireContext().isInternetAvailable())
                    when (data.venue) {
                        VenueUtils.Venue.VENUE_NAME -> {
                            val bundle = Bundle().apply {
                                putSerializable(Constants.EXTRA_DATA, venueData)
                                putBoolean(Constants.IS_UPDATING, true)
                            }
                            this.view?.findNavController()
                                ?.navigate(R.id.venueProfileAndUserNameFragment, bundle)
                        }

                        VenueUtils.Venue.VENUE_PHOTOS -> {
                            this.view?.findNavController()?.navigate(R.id.createVenuePhotoFragment)
                        }

                        VenueUtils.Venue.VENUE_TYPE -> {
                            if (venueUpdateViewModel.venueTypeList.isEmpty()) {
                                venueUpdateViewModel.getVenueTypes {
                                    venueUpdateViewModel.venueTypeList.clear()
                                    venueUpdateViewModel.venueTypeList.addAll(it)
                                    data.openSheet()
                                }
                            } else data.openSheet()
                        }

                        VenueUtils.Venue.AMBIANCE -> {
                            if (venueUpdateViewModel.venueAmbianceList.isEmpty()) {
                                venueUpdateViewModel.getVenueAmbiance {
                                    venueUpdateViewModel.venueAmbianceList.clear()
                                    venueUpdateViewModel.venueAmbianceList.addAll(it)
                                    data.openSheet()
                                }
                            } else data.openSheet()
                        }

                        VenueUtils.Venue.CONTACT_INFO -> {
                            // This is used to prevent that location can't be changed after approval or submission for approval
                            if (venueData?.status == Constants.VenueStatus.APPROVED || venueData?.status == VENUE_PAUSED) {
                                // show dialog that you can't update location
                                AlertDialog.Builder(requireContext())
                                    .setMessage(getString(R.string.venue_approved_can_not_update_location))
                                    .setPositiveButton(
                                        "Ok"
                                    ) { dialog, _ ->
                                        dialog.dismiss()
                                    }.show()

                            } else {
                                data.openSheet()
                            }
                        }

                        else -> data.openSheet()
                    }
            }
        }

    private fun VenueTypeAdapter.VenueTypeModel.openSheet() {
        requireContext().openBottomSheet(
            venue,
            venueUpdateViewModel,
            bottomSheetClickHandler
        )
    }

    private val bottomSheetClickHandler =
        { venue: VenueUtils.Venue, updatingAddress: Boolean, updateVenueRequest: UpdateVenueRequest? ->
            handle(venue, updatingAddress, updateVenueRequest)
        }

    private fun handle(
        venue: VenueUtils.Venue,
        updatingAddress: Boolean,
        updateVenueRequest: UpdateVenueRequest?
    ) {
        when (venue) {
            VenueUtils.Venue.VENUE_TYPE -> {
                venueData?.let {
                    updateVenue(
                        UpdateVenueRequest(
                            venue_id = it._id,
                            type = updateVenueRequest?.type
                        )
                    )
                }
            }

            VenueUtils.Venue.DESCRIPTION -> {
                venueData?.let {
                    venueUpdateViewModel.description.get()?.let { it1 ->
                        if (it1.isNotEmpty()) {
                            updateVenue(
                                UpdateVenueRequest(
                                    venue_id = it._id,
                                    description = it1
                                )
                            )
                        } else venueUpdateViewModel.description.set(venueUpdateViewModel.updateVenueRequest.value?.description)

                    }
                }
            }

            VenueUtils.Venue.AMBIANCE -> {
                venueData?.let {
                    updateVenue(
                        UpdateVenueRequest(
                            venue_id = it._id,
                            ambiance = updateVenueRequest?.ambiance
                        )
                    )
                }
            }

            VenueUtils.Venue.SPECIAL_OFFERS -> {
                venueData?.let {
                    updateVenue(
                        UpdateVenueRequest(
                            venue_id = it._id,
                            specialoffer = venueUpdateViewModel.updateVenueRequest.value?.specialoffer
                                ?: ""
                        )
                    )
                }
            }

            VenueUtils.Venue.UPCOMING_EVENTS -> {}
            VenueUtils.Venue.CONTACT_INFO -> {

                if (updatingAddress) {
                    val fields = listOf(
                        Place.Field.ID,
                        Place.Field.NAME,
                        Place.Field.LAT_LNG,
                        Place.Field.PHONE_NUMBER,
                        Place.Field.WEBSITE_URI,
                        Place.Field.ADDRESS
                    )
                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                            .setCountries(
                                arrayListOf(
                                    if (RaddarApp.getEnvironment() == Environment.RELEASE) "USA" else
                                        "USA", "in"
                                )
                            )
                            .build(requireContext())
                    launcher.launch(intent)
                } else if (!venueUpdateViewModel.address.get().isNullOrEmpty()) {
                    venueUpdateViewModel.updateVenueRequest.value?.contactinfo?.phonenumber =
                        venueUpdateViewModel.phoneNumber.get()
                    venueUpdateViewModel.updateVenueRequest.value?.contactinfo?.website =
                        venueUpdateViewModel.website.get()
                    venueData?.let {
                        if (venueUpdateViewModel.updateVenueRequest.value?.contactinfo?.isContactInfoValid() == true) {
                            venueUpdateViewModel.updateVenueRequest.value?.contactinfo?.latlon =
                                null
                            updateVenue(
                                UpdateVenueRequest(
                                    venue_id = it._id,
                                    contactinfo = venueUpdateViewModel.updateVenueRequest.value?.contactinfo
                                )
                            )
                        } else showContactInfoNotValidAlert()
                    }
                }
            }

            VenueUtils.Venue.VENUE_NAME -> {}
            VenueUtils.Venue.VENUE_PHOTOS -> {}
            VenueUtils.Venue.EVENTS -> {}
            VenueUtils.Venue.DEALS -> {}
        }
    }

    private fun ContactInfo.isContactInfoValid(): Boolean {
        val latFinal: Double
        val longFinal: Double
        if (lat != null && lng != null) {
            latFinal = lat ?: 0.0
            longFinal = lng ?: 0.0
        } else if ((latlon?.coordinates?.size ?: 0) >= 2) {
            latFinal = latlon?.coordinates?.get(1) ?: 0.0
            longFinal = latlon?.coordinates?.get(0) ?: 0.0
        } else {
            latFinal = 0.0
            longFinal = 0.0
        }
        Log.d(VenueUpdateFragment::class.simpleName, "Lat - $latFinal, Lng - $longFinal")
        return longFinal != 0.0 && latFinal != 0.0
    }

    private fun showContactInfoNotValidAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle(resources.getString(R.string.message))
            .setMessage("Something went wrong. Please try to add the contact info again.")
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getContactInfo(): ContactInfo? {
        val contactInfo = ContactInfo()
        val coder = Geocoder(requireContext())
        venueUpdateViewModel.place.value?.latLng?.let {
            try {
                if (Build.VERSION.SDK_INT >= 33) {
                    coder.getFromLocation(it.latitude, it.longitude, 1) { address ->
                        address.setAddress(it, contactInfo)
                    }
                } else {
                    coder.getFromLocation(it.latitude, it.longitude, 1)?.setAddress(it, contactInfo)
                }

            } catch (e: Exception) {
                return null
            }

        }
        return contactInfo
    }

    private fun List<Address>.setAddress(latLng: LatLng, contactInfo: ContactInfo) {
        Log.d("Address-----", venueUpdateViewModel.place.value?.address.toString())
        Log.d("Address-----", venueUpdateViewModel.address.get().toString())
        if (isNotEmpty()) this[0].getCityAndState(requireContext()) { city, state, country ->
            contactInfo.id = venueUpdateViewModel.place.value?.id
            contactInfo.name = venueUpdateViewModel.place.value?.name
            contactInfo.address = venueUpdateViewModel.place.value?.address
            contactInfo.city = city
            contactInfo.state = state
            contactInfo.country = country
            contactInfo.lat = latLng.latitude
            contactInfo.lng = latLng.longitude
            contactInfo.phonenumber = if (venueUpdateViewModel.phoneNumber.get()
                    .isNullOrEmpty()
            ) null else venueUpdateViewModel.phoneNumber.get()?.replace(" ", "")
            contactInfo.website = if (venueUpdateViewModel.website.get()
                    .isNullOrEmpty()
            ) null else venueUpdateViewModel.website.get()
        }
    }

    private val launcher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.apply {
                    val place = Autocomplete.getPlaceFromIntent(this)
                    println("place data - $place")
                    venueUpdateViewModel.place.value = place
                    venueUpdateViewModel.address.set(place.address)
                    place.phoneNumber?.let {
                        val regex = Regex("[^0-9+]")
                        venueUpdateViewModel.phoneNumber.set(
                            regex.replace(place.phoneNumber ?: "", "")
                        )
                    }
                    venueUpdateViewModel.website.set(place.websiteUri?.toString())
                    venueUpdateViewModel.updateVenueRequest.value?.contactinfo = getContactInfo()
                }
            }
        }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivBack -> {
                this.view?.findNavController()?.popBackStack()
            }
        }
    }

    private fun updateVenue(request: UpdateVenueRequest) {
        if (view != null && isAdded && isVisible) {
            Log.d("UpdateRequest", "$request")
            lifecycleScope.launch {
                venueUpdateViewModel.updateVenue(request).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            it.message?.let { it1 ->
                                requireContext().showToast(it1)
                            }
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            venueUpdateViewModel.updatingVenueData.value = it.data.data
                        }
                    }
                }
            }
        }
    }

    private fun getVenueList(): ArrayList<VenueTypeAdapter.VenueTypeModel> {
        return ArrayList<VenueTypeAdapter.VenueTypeModel>().apply {
            if (!fromVenueNameScreen)
                add(
                    VenueTypeAdapter.VenueTypeModel(
                        R.drawable.ic_location,
                        "Venue Name",
                        VenueUtils.Venue.VENUE_NAME,
                        isFieldRequired = true,
                        venueData,
                        isDataFilled = venueData?.name?.isNotEmpty() == true
                    )
                )
            add(
                VenueTypeAdapter.VenueTypeModel(
                    R.drawable.ic_ios_album,
                    "Venue Photos",
                    VenueUtils.Venue.VENUE_PHOTOS,
                    isFieldRequired = true,
                    venueData,
                    isDataFilled = (venueData?.images?.size ?: 0) >= 3
                )
            )
            add(
                VenueTypeAdapter.VenueTypeModel(
                    R.drawable.ic_contact_info,
                    "Contact Info",
                    VenueUtils.Venue.CONTACT_INFO,
                    isFieldRequired = true,
                    venueData,
                    isDataFilled = venueData?.contactinfo?.address?.isNotEmpty() == true
                )
            )
            add(
                VenueTypeAdapter.VenueTypeModel(
                    R.drawable.ic_description_about_venue,
                    "Description/About",
                    VenueUtils.Venue.DESCRIPTION,
                    isFieldRequired = true,
                    venueData,
                    isDataFilled = venueData?.description?.isNotEmpty() == true
                )
            )
            add(
                VenueTypeAdapter.VenueTypeModel(
                    R.drawable.ic_venue_type_icon,
                    "Venue Type",
                    VenueUtils.Venue.VENUE_TYPE,
                    isFieldRequired = false,
                    venueData,
                    isDataFilled = venueData?.type?.name?.isNotEmpty() == true
                )
            )

            add(
                VenueTypeAdapter.VenueTypeModel(
                    R.drawable.ic_ambiance,
                    "Ambiance",
                    VenueUtils.Venue.AMBIANCE,
                    isFieldRequired = false,
                    venueData,
                    isDataFilled = venueData?.ambiance?.name?.isNotEmpty() == true
                )
            )
            add(
                VenueTypeAdapter.VenueTypeModel(
                    R.drawable.ic_special_offer,
                    getString(R.string.special_offers),
                    VenueUtils.Venue.SPECIAL_OFFERS,
                    isFieldRequired = false,
                    venueData,
                    isDataFilled = venueData?.specialoffer?.isNotEmpty() == true
                )
            )
            /*add(
                VenueTypeAdapter.VenueTypeModel(
                    R.drawable.ic_extra_info,
                    "Extra Info",
                    VenueUtils.Venue.UPCOMING_EVENTS
                )
            )*/

        }
    }


}