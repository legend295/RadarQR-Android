package com.radarqr.dating.android.hotspots.createvenue

import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.VenueImage
import com.radarqr.dating.android.databinding.FragmentCreateVenuePhotoBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.model.MyVenuesData
import com.radarqr.dating.android.ui.upload.BottomImageAdapter
import com.radarqr.dating.android.ui.welcome.mobileLogin.DeleteImagesRequest
import com.radarqr.dating.android.utility.BaseUtils.isInternetAvailable
import com.radarqr.dating.android.utility.Utility.getImageUrl
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateVenuePhotoFragment : VenueBaseFragment<FragmentCreateVenuePhotoBinding>(),
    ViewClickHandler {

    private val venueUpdateViewModel: VenueUpdateViewModel by viewModel()
    private var threeImagesPairList = ArrayList<VenueImage>()
    private var venueData: MyVenuesData? = null
    val adapter by lazy {
        BottomImageAdapter(
            threeImagesPairList,
            isDeleteVisible = false,
            BottomImageAdapter.Type.UPDATE_VENUE
        )
    }

    override fun getLayoutRes(): Int = R.layout.fragment_create_venue_photo

    override fun init(view: View, savedInstanceState: Bundle?) {
        binding.viewHandler = this
        if (this.view != null && isAdded) {
            venueUpdateViewModel.updatingVenueData.observe(viewLifecycleOwner) {
                it?.let {
                    venueData = it
//                    adapter.isDeleteVisible = !(it.status == 2 || it.status == 1)
                    threeImagesPairList.clear()
                    threeImagesPairList.addAll(it.images ?: ArrayList())
                }
            }
        }
        adapter.clickHandler = { adapterView, value, position ->
            if (requireContext().isInternetAvailable()) {
                value as VenueImage?
                value?.let {
                    when (adapterView.id) {
                        R.id.iv_close1 -> {
                            if (value.image.isNotEmpty()) {
                                value.openUploadFragment(position)
                            } else {
                                // TODO
                            }
                        }
                        else -> {
//                            val image = ArrayList<String>()
//                            threeImagesPairList.forEach { image.add(it.image) }
                            value.openUploadFragment(position)
                        }
                    }
                }

            }
        }
        binding.rvImage.adapter = adapter
    }

    private fun VenueImage.openUploadFragment(position: Int) {
        val uri =
            if (image.isNotEmpty()) RaddarApp.imagesMap[image]
                ?: requireContext().getImageUrl(image)
            else ""
        val data = Bundle()
        data.putInt(Constants.TYPE, Constants.FROM_VENUE_PHOTO)
        data.putString(Constants.EXTRA_DATA, uri)
        data.putInt(Constants.POSITION, position)
        data.putSerializable(Constants.EXTRA, ArrayList(threeImagesPairList))
        this@CreateVenuePhotoFragment.view?.findNavController()?.navigate(R.id.uploadFragment, data)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvDone -> {
                this.view?.findNavController()?.popBackStack()
            }
        }
    }

    private fun String.deleteImage(position: Int) {
        if (view != null && isVisible) {
            lifecycleScope.launch {
                venueUpdateViewModel.deleteImage(
                    DeleteImagesRequest(images = ArrayList<String>().apply { add(this@deleteImage) })
                ).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            it.message?.let { it1 -> requireContext().showToast(it1) }
                        }
                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            threeImagesPairList.removeAt(position)
                            adapter.notifyItemRemoved(position)
//                            updateVenue()
                        }
                    }
                }
            }
        }
    }

//    private fun updateVenue() {
//        if (view != null && isVisible) {
//            lifecycleScope.launch {
//                venueUpdateViewModel.updateVenue(
//                    UpdateVenueRequest(
//                        venue_id = venueUpdateViewModel.updatingVenueData.value?._id,
//                        images = threeImagesPairList
//                    )
//                ).observe(viewLifecycleOwner) {
//                    when (it) {
//                        DataResult.Empty -> {}
//                        is DataResult.Failure -> {
//                            it.message?.let { it1 -> requireContext().showToast(it1) }
//                        }
//                        DataResult.Loading -> {}
//                        is DataResult.Success -> {
//                            venueUpdateViewModel.updatingVenueData.value = it.data.data
//                        }
//                    }
//                }
//            }
//        }
//    }
}