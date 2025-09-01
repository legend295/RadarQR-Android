package com.radarqr.dating.android.ui.welcome.registerScreens

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentAddMorePhotoBinding
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.mobileLogin.DeleteImagesRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.ui.welcome.registerScreens.adapter.ImageStringAdapter
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.S3Uploader
import com.radarqr.dating.android.utility.Utility.getImageUrl
import com.radarqr.dating.android.utility.Utility.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


var pos = 0

class AddMorePictureFragment : BaseFragment<FragmentAddMorePhotoBinding>() {
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val imageViewModel: ImageUploadViewModel by viewModel()
    var image_list: ArrayList<String> = ArrayList()
    var tag = 0
    var first = 0

    val PERMISSION_ID = 42
    var imageUri: Uri? = null
    lateinit var imageAdapter: ImageStringAdapter
    private var s3uploaderObj: S3Uploader? = null
    var new_image_list: ArrayList<Uri> = ArrayList()
    private val preferencesHelper: PreferencesHelper by inject()
    var userId = ""
    val list = ArrayList<String>()
    val pathList = ArrayList<String>()
    val deleteImageList: ArrayList<String> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addMorePhotoFragment = this
        s3uploaderObj = S3Uploader(requireActivity())
        showToolbarLayout(true)
        showBackButton(true)
        showToolbar(true)
        showToolbarWhite(false)
        showBackButtonWhite(false)
        showProgress(true)
        setProgress(28)
        showSkip(false)
        runBlocking(Dispatchers.IO) {
            userId =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_USERID).first()
                    ?: ""
        }

        binding.clContinue.setOnClickListener {
            /*if (image_list.size == 0) {
                CommonCode.setToast(requireActivity(), "Please choose at least one image")
            } else {
            }*/
            var data = Bundle()
            data.putString("tag", "1")
            findNavController()
                .navigate(R.id.action_addmorePhoto_to_ageFragment, data)


        }

        imageAdapter = ImageStringAdapter(
            list,
            requireActivity(), "1", "0",
            object : ImageStringAdapter.ImageListener {
                override fun emptyClick(position: Int) {
                    back_press = ""
                    imageViewModel.clickedPosition = list.size
                    openImageUploadFragment(pos = imageViewModel.clickedPosition)
                }

                override fun itemClick(url: String, pos: Int) {
                    imageViewModel.clickedPosition = pos
                    /*var uri = url
                    if (uri.contains(Constants.THUMB)) {
                        uri =
                            getProfileViewModel().viewModeUserImages[getProfileViewModel().profileData.value?.images?.get(
                                pos
                            )].toString()
                    }*/
                    val uri = RaddarApp.imagesMap[url] ?: requireContext().getImageUrl(url)
                    openImageUploadFragment(uri, pos)
                }

                override fun removeImage(url: String, position: Int,view: View, isRemoved: () -> Unit) {
                    AlertDialog.Builder(requireContext())
                        .setMessage("Are you sure you want to delete this picture?")
                        .setPositiveButton(
                            "Yes"
                        ) { dialog, _ ->
                            view.visible(isVisible = true)
                            imageAdapter.isDeleteInProgress = true
                            imageViewModel.clickedPosition = position
                            val removeList = ArrayList<String>()
                            removeList.add(deleteImageList[position])
                            deleteImages(DeleteImagesRequest(removeList), position, isRemoved)
                            dialog.dismiss()
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }.show()


                }
            }
        )

        binding.rvPhoto.adapter = imageAdapter

        /*if (newimageUri1 != null) {
            binding.clContinue.isEnabled = true
            binding.clContinue.setBackgroundResource(R.drawable.green_fill_rect)
        }*/

        binding.clAddMore.setOnClickListener {
            if (imageAdapter.listItem.size < 15)
                if (imageAdapter.listItem.size == imageAdapter.size) {
                    imageAdapter.size += 3
                    imageAdapter.refresh()
                }
        }

        initializeObserver()
//        dragDropImages()
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            (it as InitialActivity).hideShowWholeToolbar(isVisible = true)
            it.hideShowSkip(isVisible = false)
            it.hideShowBack(isVisible = true)
        }
    }

    private fun dragDropImages() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
            override fun isLongPressDragEnabled() = true
            override fun isItemViewSwipeEnabled() = false

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val dragFlags =
                    ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
                val swipeFlags =
                    if (isItemViewSwipeEnabled) ItemTouchHelper.START or ItemTouchHelper.END else 0
                return makeMovementFlags(dragFlags, swipeFlags)
            }

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                if (viewHolder.itemViewType != target.itemViewType)
                    return false
                val fromPosition = viewHolder.absoluteAdapterPosition
                val toPosition = target.absoluteAdapterPosition
                val item = list.removeAt(fromPosition)
                list.add(toPosition, item)
                val item2 = pathList.removeAt(fromPosition)
                pathList.add(toPosition, item2)
                recyclerView.adapter!!.notifyItemMoved(fromPosition, toPosition)
                Log.d("IMAGE_LIST", "$list")
                Log.d("IMAGE_LIST", "$pathList")
                return true
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_IDLE -> {
                        /* if (dragFromPosition != -1 && dragToPosition != -1 && dragFromPosition != dragToPosition) {
                             // Item successfully dragged
                             listener.onItemDragged(dragFromPosition, dragToPosition)
                             // Reset drag positions
                             dragFromPosition = -1
                             dragToPosition = -1
                         }*/
                        imageAdapter.refresh()
                        editProfile()

                        /*
                        * delete subscription
                        * subscription
                        * */
                    }
                }
//                imageAdapter.refresh()
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

        })
        itemTouchHelper.attachToRecyclerView(binding.rvPhoto)
    }

    private fun editProfile() {
        getBaseActivity()?.let {
            it.editProfile(EditProfileApiRequest(images = pathList)) { data ->
                getProfileViewModel.profileData.value = data
            }
        }
    }

    fun openImageUploadFragment(url: String? = "", pos: Int? = 0) {
        val data = Bundle()
        data.putInt(Constants.TYPE, Constants.FROM_EDIT)
        data.putBoolean(Constants.FROM_ADD_MORE_PICTURE_SCREEN, true)
        data.putString(Constants.EXTRA_DATA, url)
        data.putInt(Constants.POSITION, pos ?: 0)

        findNavController()
            .navigate(R.id.action_addmorePhoto_to_imageUploadFragment, data)
    }

    override fun getLayoutRes(): Int = R.layout.fragment_add_more_photo

    private fun initializeObserver() {
        getProfileViewModel.profileData.observe(viewLifecycleOwner) {
            it?.let {
                it.images?.apply {

                    if (it.images.isNotEmpty()) {
                        for (i in it.images.indices) {
                            imageViewModel.imageUrlList[i] = it.images[i]
                        }

                        pathList.clear()
                        pathList.addAll(it.images)

                        /* getProfileViewModel().storeImages(
                             it.images as ArrayList<String>,
                             requireContext()
                         )*/

                        deleteImageList.clear()
                        deleteImageList.addAll(it.images)
                        list.clear()
                        /*for (data in it.images) {
                            getProfileViewModel().userImages[data]?.let { image -> list.add(image) }
                        }*/
                        list.addAll(this)
                        if (list.size != 0) {
                            binding.clContinue.isEnabled = true
                            binding.clContinue.setBackgroundResource(R.drawable.green_fill_rect)
                        }
                        imageAdapter.setSize()
                    }
                }

                setUploadText()

            }
        }
    }

    private fun setUploadText() {
        binding.tvUpload.text = "Upload(${15 - (imageAdapter.listItem.size)} Remaining)"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {

            PERMISSION_ID -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    openBottomSheet()
                } else {

                }
            }
        }
    }

    fun openBottomSheet(pos: Int? = 0) {
        imageUri = null

        val data = Bundle()
        data.putInt(Constants.TYPE, Constants.FROM_REGISTER)
        data.putInt(Constants.POSITION, pos!!)
        findNavController()
            .navigate(R.id.action_addmorePhoto_to_imageUploadFragment, data)

    }

    private fun deleteImages(
        deleteImagesRequest: DeleteImagesRequest,
        posi: Int,
        isRemoved: () -> Unit
    ) {
        try {
            lifecycleScope.launch {
                getProfileViewModel.deleteImage(deleteImagesRequest)
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {

                            }
                            is DataResult.Success -> {
                                isRemoved()
                                list.removeAt(imageViewModel.clickedPosition)
                                pathList.removeAt(imageViewModel.clickedPosition)
                                deleteImageList.removeAt(imageViewModel.clickedPosition)
                                imageAdapter.refresh()
                                imageAdapter.isDeleteInProgress = false
                                (getProfileViewModel.profileData.value?.images as ArrayList).removeAt(
                                    posi
                                )
                                if (imageAdapter.listItem.size == 0) {
                                    binding.clContinue.isEnabled = false
                                    binding.clContinue.setBackgroundResource(R.drawable.lightgreen_rect)
                                }

                                setUploadText()

                            }
                            is DataResult.Failure -> {
                                imageAdapter.isDeleteInProgress = false
                                reportApiError(
                                    Exception().stackTrace[0].lineNumber,
                                    it.statusCode ?: 0,
                                    "user/delete-image",
                                    requireActivity().componentName.className,
                                    it.message ?: ""
                                )

                                FirebaseCrashlytics.getInstance()
                                    .recordException(Exception("user/delete-image Api Error"))
                            }

                            else -> {}
                        }
                    }

            }
        } catch (e: Exception) {

        }
    }

}
