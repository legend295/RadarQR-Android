package com.radarqr.dating.android.hotspots.venuealbum

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.DeleteVenueImageRequest
import com.radarqr.dating.android.data.model.VenueImage
import com.radarqr.dating.android.databinding.FragmentVenueAlbumBinding
import com.radarqr.dating.android.databinding.LayoutImageViewingWithTagDialogBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.closefriend.adapter.CloseFriendAdapter
import com.radarqr.dating.android.hotspots.createvenue.VenueUpdateViewModel
import com.radarqr.dating.android.hotspots.helpers.showSubscriptionSheet
import com.radarqr.dating.android.hotspots.helpers.venueAlbumInfo
import com.radarqr.dating.android.hotspots.model.MyVenuesData
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.utility.BaseUtils.getLocationString
import com.radarqr.dating.android.utility.PaginationScrollListener
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.loadVenueImage
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.enums.SubscriptionPopUpType
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.instatag.InstaTag
import com.radarqr.dating.android.utility.serializable
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class VenueAlbumFragment : VenueBaseFragment<FragmentVenueAlbumBinding>(), ViewClickHandler {

    private val venueUpdateViewModel: VenueUpdateViewModel by viewModel()
    private val list = ArrayList<VenueImage?>()
    private var venueId: String = ""
    private val adapter by lazy { VenueAlbumAdapter(list, adapterClickHandler) }
    private var myVenueData: MyVenuesData? = null

    //Pagination
    private var pageNo: Int = 1
    private var isLoading = false
    private var isLastPage = false

    override fun getLayoutRes(): Int = R.layout.fragment_venue_album

    override fun init(view: View, savedInstanceState: Bundle?) {
        binding.viewHandler = this

        venueId = arguments?.getString(Constants.VENUE_ID, "") ?: ""
        myVenueData = arguments?.serializable(Constants.EXTRA)

        getVenueImages()
        setAdapter()

        binding.swipeRefreshLayout.setOnRefreshListener {
            isLoading = false
            isLastPage = false
            pageNo = 1
            getVenueImages()
        }
    }

    private fun handleEmptyUi() {
        binding.tvNoImage.visible(list.isEmpty())
        binding.tvEmptyMessage.visible(list.isEmpty())
    }

    private fun setAdapter() {
        val layoutManager = GridLayoutManager(requireContext(), 3)
        binding.rvVenueAlbum.layoutManager = layoutManager
        binding.rvVenueAlbum.adapter = adapter
        binding.rvVenueAlbum.addOnScrollListener(object : PaginationScrollListener(layoutManager) {
            override fun loadMoreItems() {
                adapter.addLoadingView()
                this@VenueAlbumFragment.isLoading = true
                ++pageNo
                getVenueImages()
            }

            override val isLastPage: Boolean
                get() = this@VenueAlbumFragment.isLastPage
            override val isLoading: Boolean
                get() = this@VenueAlbumFragment.isLoading
        })
    }

    private val adapterClickHandler = { list: ArrayList<VenueImage?>, position: Int ->
        openImageViewingDialog(list[position], position)
    }

    private fun openImageViewingDialog(response: VenueImage?, position: Int) {
        response ?: return
        val dialog = Dialog(requireContext(), R.style.DialogStyleInstagram)
        val layout = LayoutImageViewingWithTagDialogBinding.inflate(
            LayoutInflater.from(requireContext()),
            null,
            false
        )

        layout.closeViewTop.setOnClickListener {
            dialog.dismiss()
        }

        layout.closeViewBottom.setOnClickListener {
            dialog.dismiss()
        }
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        val dateFormatSecond = SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.getDefault())
//        val localDateTime = LocalDateTime.parse(response.createdAt)

        with(layout) {
            ivDelete.visible(HomeActivity.loggedInUserId == myVenueData?.user_id || HomeActivity.loggedInUserId == response.user_id)
            tvUserName.text = response.userInfo.name
            ivUser.loadImage(response.userInfo.profile_pic)
            myVenueData?.let {
                tvLocation.text = StringBuilder().append("Venue: ").append(it.name).append(", ")
                    .append(getLocationString(it.contactinfo).uppercase())
            }
            tvPostTime.text =
                StringBuilder().append("Posted on ")
                    .append(dateFormatSecond.parse(response.createdAt)
                        ?.let { dateFormat.format(it) })

            ivTaggedUsersIndicator.visible(response.tagged_users.isNotEmpty())
            ivPostedImage.tagImageView.loadVenueImage(response.image)

            tvUserName.setOnClickListener { openUserProfile(response.user_id, dialog) }
            ivUser.setOnClickListener { openUserProfile(response.user_id, dialog) }

            ivTaggedUsersIndicator.setOnClickListener {
                if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.PLUS || HomeActivity.loggedInUserId == myVenueData?.user_id || HomeActivity.loggedInUserId == response.user_id) {
                    if (layout.ivPostedImage.areTagsVisible())
                        ivPostedImage.hideTags()
                    else ivPostedImage.showTags()
                } else {
                    showSubscriptionSheet(
                        SubscriptionPopUpType.SEE_TAGGED_USERS,
                        popBackStack = false
                    ) {
                        dialog.dismiss()
                    }
                }

            }

            ivDelete.setOnClickListener {
                deleteConfirmationUI(response, position, dialog)
            }

            layout.ivPostedImage.tagClickListener = InstaTag.TagClickListener {
                if (it == null) return@TagClickListener
                openUserProfile(it, dialog)
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            layout.ivPostedImage.addTagViewFromTags(response.tagged_users)
            layout.ivPostedImage.hideTags()
        }, 100)
        layout.ivPostedImage.setTaggedPhotoEvent(photoEvent(layout))
        dialog.setContentView(layout.root)
        dialog.show()
    }

    private fun LayoutImageViewingWithTagDialogBinding.deleteConfirmationUI(
        response: VenueImage,
        position: Int, dialog: Dialog
    ) {
        AlertDialog.Builder(requireContext()).setTitle("Are you sure you want to delete.")
            .setPositiveButton("Yes") { which, _ ->
                which.dismiss()
                progressBar.visible(isVisible = true)
                ivDelete.visible(isVisible = false)
                deleteVenueImage(response._id) { isSuccess ->
                    progressBar.visible(isVisible = false)
                    ivDelete.visible(isVisible = true)
                    if (isSuccess) {
                        list.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        adapter.notifyItemRangeChanged(0, list.size - 1)
                        handleEmptyUi()
                        dialog.dismiss()
                    }
                }
            }.setNegativeButton(requireContext().getText(R.string.cancel)) { which, _ ->
                which.dismiss()
            }.show()
    }

    private fun openUserProfile(userId: String, dialog: Dialog) {
        dialog.dismiss()
        val bundle = Bundle().apply {
            putString(Constants.USER_ID, userId)
            putSerializable(Constants.EXTRA, CloseFriendAdapter.RequestStatus.RECEIVED)
            putInt(Constants.FROM, ProfileFragment.FROM_VENUE_SINGLES)
            putBoolean(Constants.TYPE, false)
        }
        this.view?.findNavController()
            ?.navigate(R.id.profileFragment, bundle)
    }

    private fun photoEvent(layout: LayoutImageViewingWithTagDialogBinding) =
        object : InstaTag.PhotoEvent {
            override fun singleTapConfirmedAndRootIsInTouch(x: Int, y: Int, isDragging: Boolean) {

            }

            override fun onDoubleTap(e: MotionEvent?): Boolean {
                if (layout.ivPostedImage.areTagsVisible()) return true
                layout.ivPostedImage.showTags()
                return true
            }

            override fun onSingleTap(e: MotionEvent?): Boolean {
                if (layout.ivPostedImage.areTagsVisible())
                    layout.ivPostedImage.hideTags()
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
            R.id.ivBack -> {
                this.view?.findNavController()?.popBackStack()
            }

            R.id.ivInfo -> {
                // show bottom sheet
                requireContext().venueAlbumInfo()
            }
        }
    }

    private fun getVenueImages() {
        if (view != null && isAdded) {
            binding.progressBar.visible(isVisible = pageNo == 1 && list.isEmpty())
            lifecycleScope.launch {
                venueUpdateViewModel.getVenueImages(venueId, pageNo, 21)
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {}
                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                if (pageNo == 1) {
                                    list.clear()
                                }
                                list.addAll(it.data.data?.venueImages ?: ArrayList())
                                adapter.refreshData()
                                isLastPage = list.size >= (it.data.data?.total_count ?: 0)
                            }
                        }
                        adapter.removeLoadingView()
                        this@VenueAlbumFragment.isLoading = false
                        binding.swipeRefreshLayout.isRefreshing = false
                        binding.progressBar.visible(isVisible = false)
                        handleEmptyUi()
                    }
            }
        }
    }

    private fun deleteVenueImage(imageId: String, callback: (Boolean) -> Unit) {
        if (view != null && isAdded) {
            lifecycleScope.launch {
                venueUpdateViewModel.deleteVenueImages(DeleteVenueImageRequest(venueId, imageId))
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            DataResult.Empty -> {}
                            is DataResult.Failure -> {
                                callback(false)
                                it.message?.let { it1 -> requireContext().showToast(it1) }
                            }

                            DataResult.Loading -> {}
                            is DataResult.Success -> {
                                callback(true)
                            }
                        }
                    }
            }
        }
    }
}