package com.radarqr.dating.android.hotspots.tag

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.VenueImageAddRequest
import com.radarqr.dating.android.databinding.FragmentImageTagBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.helpers.venuePausedDialog
import com.radarqr.dating.android.hotspots.upload.VenuePhotoViewModel
import com.radarqr.dating.android.ui.welcome.mobileLogin.DeleteImagesRequest
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.deleteVenueImage
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.instatag.InstaTag
import com.radarqr.dating.android.utility.uploadVenueImageWithTransferUtility
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File

class ImageTagFragment : VenueBaseFragment<FragmentImageTagBinding>(), ViewClickHandler {

    private var venueId = ""
    private var compressedImagePath = ""
    private val venuePhotoViewModel: VenuePhotoViewModel by viewModel()
    private val tagViewModel: TagViewModel by viewModel()

    override fun getLayoutRes(): Int = R.layout.fragment_image_tag
    var i = 0

    override fun init(view: View, savedInstanceState: Bundle?) {

        arguments?.apply {
            venueId = getString(Constants.VENUE_ID, "") ?: ""
            compressedImagePath = getString(Constants.EXTRA, "") ?: ""

            Glide.with(binding.root).load(compressedImagePath).into(binding.instaTag.tagImageView)
        }

//        binding.instaTag.setTaggedPhotoEvent(photoEvent)
        binding.viewHandler = this
//        Handler(Looper.getMainLooper()).postDelayed({
//            tagViewModel.tagModel?.let {
//                tagViewModel.taggedUsersList[it.taggedUserId] = it
//                tagViewModel.taggedUsersList.forEach { data ->
//                    if (data.value.taggedUserId.isNotEmpty()) {
//                        binding.instaTag.addTag(
//                            data.value.taggedUserId,
//                            data.value.xCord,
//                            data.value.yCord,
//                            data.value.taggedUserName
//                        )
//                    }
//                }
//                tagViewModel.tagModel = null
//            }
//            binding.instaTag.showTags()
//        }, 100)

    }

    /* private val photoEvent = object : InstaTag.PhotoEvent {
         override fun singleTapConfirmedAndRootIsInTouch(x: Int, y: Int) {
             tagViewModel.tagModel = TagViewModel.TagModel("", "", x, y)
             this@ImageTagFragment.view?.findNavController()
                 ?.navigate(R.id.fragmentSearchCloseFriend)
         }

         override fun onDoubleTap(e: MotionEvent?): Boolean {
             return true
         }

         override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
             return true
         }

         override fun onLongPress(e: MotionEvent?) {

         }
     }*/

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvLetsTag -> {
                val bundle = Bundle().apply {
                    putString(Constants.VENUE_ID, venueId)
                    putString(Constants.EXTRA, compressedImagePath)
                }
                this.view?.findNavController()
                    ?.navigate(
                        R.id.action_from_imageTagFragment_to_shareTaggedImageFragment,
                        bundle
                    )
            }

            R.id.tvNoJustSharing -> {
                uploadImageToS3()
            }
        }
    }

    private fun uploadImageToS3() {
        if (view != null && isAdded) {
            if (compressedImagePath.isNotEmpty())
                lifecycleScope.launch {
                    binding.progressBar.visible(isVisible = true)
                    val response =
                        uploadVenueImageWithTransferUtility(File(compressedImagePath), "")
                    val urlFromS3 = response.first
                    if (urlFromS3.isNotEmpty()) {
                        uploadVenuePost(urlFromS3)
                    } else {
                        requireActivity().showToast("Error in uploading image.")
                        binding.progressBar.visibility = View.GONE
                    }
                }
            else {
                requireContext().showToast("Image not found. Please try again.")
            }
        }
    }

    private fun uploadVenuePost(uploadImage: String) {
        if (view != null && isAdded) {
            lifecycleScope.launch {
                venuePhotoViewModel.uploadVenuePost(
                    VenueImageAddRequest(
                        uploadImage,
                        ArrayList(),
                        venueId
                    )
                ).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {
                            if (it.statusCode == 422) {
                                it.message?.let { it1 -> requireContext().showToast(it1) }
                            } else if (it.statusCode == 423) {
                                // Venue is paused
                                uploadImage.deleteVenueImage()
                                requireContext().venuePausedDialog {
                                    this@ImageTagFragment.view?.findNavController()
                                        ?.popBackStack(R.id.hotspotsFragment, true)
                                    this@ImageTagFragment.view?.findNavController()
                                        ?.navigate(R.id.hotspotsFragment)
                                }
                            }
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            this@ImageTagFragment.view?.findNavController()?.popBackStack()
                        }
                    }
                    binding.progressBar.visible(isVisible = false)
                }
            }
        } else {
            binding.progressBar.visible(isVisible = false)
        }
    }

    private fun String.deleteImage() {
        if (view != null && isAdded && isVisible) {
            venuePhotoViewModel.deleteImage(DeleteImagesRequest(ArrayList<String>().apply { add(this@deleteImage) }))
                .observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {}
                        is DataResult.Failure -> {

                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {

                        }
                    }
                }
        }
    }
}