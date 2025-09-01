package com.radarqr.dating.android.hotspots.tag

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.VenueImageAddRequest
import com.radarqr.dating.android.databinding.FragmentShareTaggedImageBinding
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

class ShareTaggedImageFragment : VenueBaseFragment<FragmentShareTaggedImageBinding>(),
    ViewClickHandler {

    private var venueId = ""
    private var compressedImagePath = ""
    private val venuePhotoViewModel: VenuePhotoViewModel by viewModel()
    private val tagViewModel: TagViewModel by viewModel()
    private val list = ArrayList<TagViewModel.TagModel>()
    private val adapter by lazy { TaggedUsersAdapter(list) }


    override fun getLayoutRes(): Int = R.layout.fragment_share_tagged_image

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tagViewModel.clear()
    }

    override fun init(view: View, savedInstanceState: Bundle?) {
        arguments?.apply {
            venueId = getString(Constants.VENUE_ID, "") ?: ""
            compressedImagePath = getString(Constants.EXTRA, "") ?: ""

            Glide.with(binding.root).load(compressedImagePath).into(binding.instaTag.tagImageView)
        }

        handleMessageUI()

        setAdapter()
        setTag()
    }

    private fun handleMessageUI() {
        binding.tvMessage.visible(list.isEmpty())
    }

    private fun setAdapter() {
        binding.rvTaggedUsers.adapter = adapter
        adapter.removeClickHandler = { data, position ->
            binding.instaTag.removeTagListener?.onRemove(data.id)
            tagViewModel.taggedUsersList.remove(data.id)
            list.removeAt(position)
            adapter.notifyItemRemoved(position)
            adapter.notifyItemRangeChanged(0, adapter.list.size)
            Handler(Looper.getMainLooper()).postDelayed({
                handleMessageUI()
            }, 100)
        }
    }

    private fun setTag() {
        binding.instaTag.setTaggedPhotoEvent(photoEvent)
        binding.viewHandler = this
        Handler(Looper.getMainLooper()).postDelayed({
            tagViewModel.tagModel?.let {
                if (it.taggedUserName.isNotEmpty()) {
                    if (!tagViewModel.taggedUsersList.containsKey(it.id)) {
                        list.add(it)
                        adapter.notifyItemInserted(if (list.isEmpty()) 0 else list.size - 1)
                    }
                    tagViewModel.taggedUsersList[it.taggedUserId] = it
                }
                tagViewModel.taggedUsersList.forEach { data ->
                    if (data.value.taggedUserId.isNotEmpty()) {
                        binding.instaTag.addTag(
                            data.value.taggedUserId,
                            data.value.xCord,
                            data.value.yCord,
                            data.value.taggedUserName
                        )
                    }
                }

                tagViewModel.tagModel = null
            }
            handleMessageUI()
            binding.instaTag.showTags()
        }, 100)
    }

    private val photoEvent = object : InstaTag.PhotoEvent {
        override fun singleTapConfirmedAndRootIsInTouch(x: Int, y: Int, isDragging: Boolean) {
            if (binding.instaTag.tagListSize < 20) {
                Log.d("TAGUPDATING", "x ---- $x, y ---- $y")
                tagViewModel.tagModel = TagViewModel.TagModel("", "", "", "", x, y)
                if (!isDragging)
                    this@ShareTaggedImageFragment.view?.findNavController()
                        ?.navigate(R.id.fragmentSearchCloseFriend)
            } else {
                requireContext().showToast("You can only add 20 tags per post.")
            }
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            return true
        }

        override fun onSingleTap(e: MotionEvent?): Boolean {
            return true
        }

        override fun onDoubleTapEvent(e: MotionEvent?): Boolean {
            return true
        }

        override fun onLongPress(e: MotionEvent?) {

        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tvShare -> {
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
                        binding.instaTag.tags,
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
                                    this@ShareTaggedImageFragment.view?.findNavController()
                                        ?.popBackStack(R.id.hotspotsFragment, true)
                                    this@ShareTaggedImageFragment.view?.findNavController()
                                        ?.navigate(R.id.hotspotsFragment)
                                }
                            }
                        }

                        DataResult.Loading -> {}
                        is DataResult.Success -> {
                            this@ShareTaggedImageFragment.view?.findNavController()?.popBackStack()
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