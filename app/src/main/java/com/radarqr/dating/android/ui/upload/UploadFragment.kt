package com.radarqr.dating.android.ui.upload

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.gowtham.library.utils.TrimType
import com.gowtham.library.utils.TrimVideo
import com.gowtham.library.utils.TrimmerUtils
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.UpdateVenueImages
import com.radarqr.dating.android.data.model.VenueImage
import com.radarqr.dating.android.databinding.FragmentUploadBinding
import com.radarqr.dating.android.databinding.LayoutBottomSheetBinding
import com.radarqr.dating.android.hotspots.createvenue.VenueUpdateViewModel
import com.radarqr.dating.android.hotspots.model.UpdateVenueRequest
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.ui.welcome.registerScreens.back_press
import com.radarqr.dating.android.utility.*
import com.radarqr.dating.android.utility.BaseUtils.openApplicationDetailsSettings
import com.radarqr.dating.android.utility.Utility.createImageFile
import com.radarqr.dating.android.utility.Utility.isVideo
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.toPx
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.cropper.InstaCropperView
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.error.InstanceCreationException
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.text.SimpleDateFormat
import java.util.*


class UploadFragment : BaseFragment<FragmentUploadBinding>(), ViewClickHandler {

    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val preferencesHelper: PreferencesHelper by inject()
    private val venueUpdateViewModel: VenueUpdateViewModel by viewModel()
    private var userId = ""
    private var s3uploaderObj: S3Uploader? = null
    private var type = 0
    private var clickedPosition = -1
    private var url = ""
    private var fromAddMorePictureScreen = false
    private var player: SimpleExoPlayer? = null
    private var isMuted = false
    private var selectedUri: Uri? = null
    private var actualImage: File? = null
    private var videoOutputFile: File? = null
    private var compressedPath = ""
    private var cameraUri: Uri? = null
    private var videoBitmap: Bitmap? = null
    private var tempVideoUri: Uri? = null
    private var threeImagesPairList = ArrayList<String>()
    private var venueImages = ArrayList<VenueImage>()

    private var isDoneClicked = false // prevent Continue click multiple times

    override fun getLayoutRes(): Int = R.layout.fragment_upload

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewHandler = this
        HomeActivity.activeFragment.value = this
        arguments?.let {
            threeImagesPairList =
                (it.getStringArrayList(Constants.EXTRA) as ArrayList<String>?) ?: ArrayList()
        }

        showToolbarLayout(false)
        init()

        /* Disable scroll-view scroll so that cropper can work fine */
        binding.instacropper.touchEvent = InstaCropperView.OnTouch {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    binding.scrollView.setScrollingEnabled(enabled = false)
                }

                MotionEvent.ACTION_UP -> {
                    binding.scrollView.setScrollingEnabled(enabled = true)
                }
            }
        }
    }

    private fun init() {
        s3uploaderObj = S3Uploader(requireActivity())

        runBlocking(Dispatchers.IO) {
            userId =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_USERID).first()
                    ?: ""
        }

        arguments?.let {
            /**
             * type is used to check that from where this fragment is opening
             * we are opening this from EditProfileFragment, AddMorePictureFragment and AddPictureFragment
             * */

            type = it.getInt(Constants.TYPE, 0)

            /** Clicked Position is used to check that user won't be able to add video on first position
             * and used to update the existing video/image as well
             * */

            clickedPosition = it.getInt(Constants.POSITION, -1)

            when (type) {
                Constants.FROM_REGISTER -> {
                    //Handle UI visibility
                    binding.rvImage.visible(isVisible = false)
                    binding.tvBottomMessage.visible(isVisible = false)
                    hideBackAndText(isVisible = false)

                    // handle data parsing
                    selectedUri = it.getString(Constants.EXTRA, "").toUri()
                    binding.isUrlEmpty = selectedUri == null
                    binding.isVideo = selectedUri.toString().contains(Constants.MP4)
                    if (selectedUri != null) if (selectedUri.toString()
                            .contains(Constants.MP4)
                    ) initializePlayer(uri = selectedUri!!)
                    else binding.instacropper.setImageUri(selectedUri)
                }

                Constants.FROM_EDIT -> {
                    // handle data parsing
                    url = it.getString(Constants.EXTRA_DATA, "")
                    fromAddMorePictureScreen = it.getBoolean(Constants.FROM_ADD_MORE_PICTURE_SCREEN)

                    //Handle UI visibility
                    hideBackAndText(isVisible = !fromAddMorePictureScreen)

                    if (clickedPosition <= 2 || fromAddMorePictureScreen) {
                        binding.rvImage.visible(isVisible = false)
                        binding.tvBottomMessage.visible(isVisible = false)
                    } else {
                        setBottomImagesAdapter()
                    }


                    binding.isUrlEmpty = url.isEmpty()
                    binding.isVideo = url.contains(Constants.MP4) or url.contains(Constants.THUMB)
                    if (url.isNotEmpty()) {
                        if (url.contains(Constants.MP4) || url.contains(Constants.THUMB)) {
                            binding.progressBarVideo.visibility = View.VISIBLE
                            initializePlayer(uri = Uri.parse(url))
                        } else Glide.with(binding.root).load(url).into(binding.instacropper)
                    }
                }

                Constants.FROM_VENUE_PHOTO -> {
                    binding.tvMessage.text =
                        requireContext().getString(R.string.upload_image_venue_message)
                    url = it.getString(Constants.EXTRA_DATA, "")
                    venueImages = arguments?.serializable(Constants.EXTRA) ?: ArrayList()
                    binding.isUrlEmpty = url.isEmpty()
                    binding.tvBottomMessage.visible(isVisible = false)
                    binding.isVideo = url.contains(Constants.MP4) or url.contains(Constants.THUMB)
                    if (url.isNotEmpty()) {
                        if (url.contains(Constants.MP4) || url.contains(Constants.THUMB)) {
                            binding.progressBarVideo.visibility = View.VISIBLE
                            initializePlayer(uri = Uri.parse(url))
                        } else Glide.with(binding.root).load(url).into(binding.instacropper)
                    }
                }
            }

        }
    }

    private fun hideBackAndText(isVisible: Boolean) {
        binding.ivBack.visible(isVisible = isVisible)
        binding.tvEditPhoto.visible(isVisible = isVisible)
    }

    private fun setBottomImagesAdapter() {
        val adapter =
            BottomImageAdapter(threeImagesPairList, type = BottomImageAdapter.Type.EDIT_PROFILE)
        binding.rvImage.adapter = adapter
    }

    private fun initializePlayer(uri: Uri) {
        player = SimpleExoPlayer.Builder(requireActivity()).build().also { exoPlayer ->
            isMuted = true
            binding.ivVolume.visibility = View.VISIBLE
            binding.videoView.player = exoPlayer
            val mediaItem = MediaItem.fromUri(uri)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
        player!!.volume = 0f

        player?.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    ExoPlayer.STATE_BUFFERING -> {
                        binding.progressBarVideo.visible(isVisible = true)
                    }

                    ExoPlayer.STATE_READY -> {
                        binding.progressBarVideo.visible(isVisible = false)
                    }

                    ExoPlayer.STATE_ENDED -> {
                        player?.apply {
                            if (playWhenReady) {
                                binding.progressBarVideo.visible(isVisible = false)
                                playWhenReady = false
                                release()
                            }
                        }
                    }

                    else -> {

                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.d("VIDEO_PLAY_ERROR", " onPlayerError $error")
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
                Log.d("VIDEO_PLAY_ERROR", " onPlayerErrorChanged $error")
            }
        })

    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            if (it is InitialActivity) {
                it.hideShowBack(isVisible = true)
                it.hideShowSkip(isVisible = false)
                binding.ivBack.setPadding(20.toPx.toInt(), 0, 0, 0)
            }

        }
    }

    override fun onPause() {
        super.onPause()
        player?.let {
            it.playWhenReady = false
            it.release()
        }
    }

    override fun onStop() {
        super.onStop()
        activity?.let {
            if (it is HomeActivity) {
                it.binding.homeToolbar.clHomeToolbar.visible(true)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivBack -> {
                this.view?.findNavController()?.popBackStack()
            }

            R.id.ivVolume -> {
                if (isMuted) {
                    isMuted = false
                    player!!.volume = 100f
                    binding.ivVolume.setImageResource(R.drawable.ic_unmute)
                } else {
                    isMuted = true
                    player!!.volume = 0f
                    binding.ivVolume.setImageResource(R.drawable.ic_baseline_volume_mute_24)
                }
            }

            R.id.iv_view1, R.id.tv_replace, R.id.ff_image, R.id.iv_close1 -> {
                if (checkStorageAndCameraPermission()) {
                    openBottomSheet()
                } else {
                    requestStorageAndCameraPermission()
                }
            }

            R.id.tv_continue -> {
                Log.d("SELECTED_URI", "$selectedUri")
                if (!BaseUtils.isInternetAvailable()) {
                    CommonCode.setToast(
                        requireContext(), resources.getString(R.string.no_internet_msg)
                    )
                    return
                }

                if (selectedUri != null && !isDoneClicked) {
                    isDoneClicked = true
                    selectedUri?.let {
                        binding.ivBack.isEnabled = false
                        binding.progressBar1.visibility = View.VISIBLE
                        if (it.toString().contains(Constants.MP4)) {
                            compressedPath = videoOutputFile?.absolutePath ?: ""
                            uploadImageVideoTos3()
                        } else {
                            binding.instacropper.crop(
                                View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.AT_MOST),
                                View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.AT_MOST)
                            ) { bitmap ->
                                runBlocking {
                                    if (this@UploadFragment.view != null && isAdded) {
                                        val file = Compressor.compress(
                                            requireContext(), getImageFile(
                                                bitmap,
                                                "${userId}_${System.currentTimeMillis()}.webp"
                                            )
                                        ) {
                                            quality(50)
//                                        this.format(Bitmap.CompressFormat.WEBP)
                                            size(897_152)
                                            /*this.resolution(720,720)*/
                                        }
                                        compressedPath = /*getImageFile(
                                        bitmap,
                                        "${userId}_${System.currentTimeMillis()}.webp"
                                    ).absolutePath*/ file.absolutePath
                                        uploadImageVideoTos3()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun openBottomSheet() {
        val dialog = BottomSheetDialog(requireActivity())
        val view = DataBindingUtil.inflate<LayoutBottomSheetBinding>(
            LayoutInflater.from(requireContext()), R.layout.layout_bottom_sheet, null, false
        )

        if (url.isNotEmpty()) {
            view.tvFind.text = resources.getString(R.string.find_a_replacement)
        } else {
            view.tvFind.text = resources.getString(R.string.upload_photo_video)

        }

        view.tvVideo.visibility =
            if (clickedPosition <= 0 || type == Constants.FROM_VENUE_PHOTO) View.GONE else View.VISIBLE

        view.tvGallery.setOnClickListener {
            openGallery()
            dialog.dismiss()
        }

        view.tvVideo.setOnClickListener {
            openVideoCamera()
            dialog.dismiss()
        }
        view.tvCamera.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            var photoFile: File? = null
            try {
                photoFile = requireContext().createImageFile()
            } catch (ex: IOException) {
                Log.d("DEBUG", "Exception while creating file: $ex")
            }
            if (photoFile != null) {
                Log.d("DEBUG", "Photo file not null")
                cameraUri = FileProvider.getUriForFile(
                    requireActivity(), "${requireActivity().packageName}.provider", photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
                cameraImageLauncher.launch(takePictureIntent)
            }

            dialog.dismiss()
        }
        dialog.setCancelable(true)

        dialog.setContentView(view.root)

        dialog.show()

    }

    /* private fun createImageFile(): File? {
         // Create an image file name
         val timeStamp: String = System.currentTimeMillis().toString()
         val imageFileName = "JPEG_" + timeStamp + "_"
         val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
         val image = File.createTempFile(
             imageFileName,  *//* prefix *//*
            ".jpg",  *//* suffix *//*
            storageDir *//* directory *//*
        )

        // Save a file: path for use with ACTION_VIEW intents
        val actualImage = image.absolutePath
        Log.d("DEBUG", "Path: $actualImage")
        return image
    }*/

    private var cameraImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    clearPhotoFromCropper()
                    selectedUri = cameraUri
                    binding.isUrlEmpty = false
                    binding.isVideo = false
                    binding.instacropper.setImageUri(selectedUri)

                    try {
                        actualImage = FileUtil.from(requireActivity(), selectedUri!!)
                    } catch (e: IOException) {
                        Utility.showToast(requireActivity(), "Failed to read picture data!")
                        e.printStackTrace()
                    }


                } catch (e: Exception) {

                }
            } else {
                handleNoMediaSelection()
            }
        }

    private fun openGallery() {
        val mime = if (clickedPosition <= 0) arrayOf("image/*") else arrayOf("image/*", "video/*")
        val intent = Intent()
        intent.type = if (clickedPosition <= 0) "image/*" else "image/* video/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mime)
        intent.action = Intent.ACTION_GET_CONTENT
        galleryImageLauncher.launch(intent)
    }

    private var galleryImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.tvReplace.visibility = View.VISIBLE
                val data: Intent? = result.data
                try {
                    val url = data?.data
                    url?.let { uri ->
                        binding.isUrlEmpty = false
                        if (!requireActivity().isVideo(uri)) {
                            binding.isVideo = false
                            clearPhotoFromCropper()
                            selectedUri = uri
                            binding.instacropper.setImageUri(uri)
                            try {
                                actualImage = FileUtil.from(requireActivity(), data.data!!)
                            } catch (e: IOException) {
                                BaseUtils.showMessage(
                                    requireContext(), message = "Failed to read picture data!"
                                )
                                e.printStackTrace()
                            }
                        } else {
                            tempVideoUri = uri
                            TrimVideo.activity(uri.toString())
                                .setTrimType(TrimType.MIN_MAX_DURATION).setMinToMax(1, 30)
                                .setHideSeekBar(true).start(this, videoTrimmerLauncher)
                        }
                    }


                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                handleNoMediaSelection()
            }
        }

    private fun handleNoMediaSelection() {
        binding.isUrlEmpty = (selectedUri == null && url.trim().isEmpty())
        selectedUri?.let {
//            if (it.toString().contains(Constants.MP4)) {
            if (requireActivity().isVideo(it)) {
                binding.isVideo = true
                initializePlayer(Uri.fromFile(videoOutputFile))
            } else {
                binding.isVideo = false
                binding.instacropper.setImageUri(selectedUri)
            }
        }
    }

    private val videoTrimmerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
//                isNew = true
                binding.isUrlEmpty = false
                binding.isVideo = true
                videoOutputFile = result.data?.extras?.get(TrimVideo.CROPPED_VIDEO_PATH) as File
                binding.progressBarVideo.visible(isVisible = true)
                initializePlayer(Uri.fromFile(videoOutputFile))
                selectedUri = Uri.fromFile(videoOutputFile)
                Log.d("TAG", "Trimmed path:: $selectedUri")
                loadThumbnails(selectedUri!!)
            } else if (result.resultCode == 100) {
                tempVideoUri?.let {
                    binding.isUrlEmpty = false
                    binding.isVideo = true
                    videoOutputFile = FileUtil.from(requireContext(), it)
                    initializePlayer(it)
                    selectedUri = it
                    Log.d("TAG", "Trimmed path:: $selectedUri")
                    loadThumbnails(selectedUri!!)
                } ?: kotlin.run {
                    handleNoMediaSelection()
                }

            } else {
                handleNoMediaSelection()
            }
        }

    private fun loadThumbnails(uri: Uri) {
        try {
            val totalDuration = TrimmerUtils.getDuration(requireActivity(), uri)
            val diff: Long = totalDuration / 4
            val sec = 1
            val interval = diff * sec * 10000000
            val options = RequestOptions().frame(interval)
            Glide.with(this).asBitmap().load(uri)
//                .apply(options)
                /*.transition(DrawableTransitionOptions.withCrossFade(300))*/
                .into(object : CustomTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap, transition: Transition<in Bitmap>?
                    ) {
                        Log.d("Bitmap", "$resource")
                        videoBitmap = resource
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {

                    }
                })
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun openVideoCamera() {
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        var file: File? = null
        try {
            file = createVideoFile()
        } catch (ex: IOException) {

        }
        if (file != null) {

            cameraUri = FileProvider.getUriForFile(
                requireActivity(), "${requireActivity().packageName}.provider", file
            )
            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30)
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri)
            videoCaptureLauncher.launch(takeVideoIntent)
        }
    }

    private var videoCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.tvReplace.visibility = View.VISIBLE
                try {
                    cameraUri?.let {
                        tempVideoUri = it
                        TrimVideo.activity(cameraUri.toString())
                            .setTrimType(TrimType.MIN_MAX_DURATION).setMinToMax(1, 30)
                            .setHideSeekBar(true).start(requireActivity(), videoTrimmerLauncher)
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                handleNoMediaSelection()
            }
        }

    @SuppressLint("SimpleDateFormat")
    private fun createVideoFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = timeStamp + "_"
        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            Constants.MP4,  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        val actualImage = image.absolutePath
        Log.d("DEBUG", "Path: $actualImage")
        return image
    }

    private fun getImageFile(bitmap: Bitmap, name: String): File {
        val cachePath = File(requireActivity().externalCacheDir, "shared_images/")
        cachePath.mkdirs()

        //create png file
        val file = File(cachePath, name)
        val fileOutputStream: FileOutputStream?
        try {
            fileOutputStream = FileOutputStream(file)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 50, fileOutputStream)
            } else {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 50, fileOutputStream)
            }
            fileOutputStream.flush()
            fileOutputStream.close()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        /* return FileProvider.getUriForFile(
             requireActivity(),
             requireActivity().applicationContext.packageName + ".provider",
             file
         )*/

        return file
    }

    fun getRandomString(length: Int): String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length).map { charset.random() }.joinToString("")
    }

    private fun uploadImageVideoTos3() {
        if (type == Constants.FROM_VENUE_PHOTO) {
            lifecycleScope.launch {
                val response = uploadVenueImageWithTransferUtility(File(compressedPath), "")
                val urlFromS3 = response.first
                if (urlFromS3.isNotEmpty()) {
                    val updateVenueImages = UpdateVenueImages()
                    if (clickedPosition >= 0 && clickedPosition < venueImages.size) {
//                        venueImages[clickedPosition].image = urlFromS3
                        updateVenueImages.id = venueImages[clickedPosition]._id
                        updateVenueImages.name = urlFromS3
                        venueImages[clickedPosition].updateVenue(updateVenueImages)
                    } else {
                        val venueImage = VenueImage(
                            "",
                            0.0,
                            urlFromS3,
                            ArrayList(),
                            "",
                            venueUpdateViewModel.updatingVenueData.value?._id ?: ""
                        )
                        venueImages.add(venueImage)
                        updateVenueImages.name = urlFromS3
                        venueImage.updateVenue(updateVenueImages)
                    }
                } else {
                    requireActivity().showToast("Error in uploading image.")
                    binding.progressBar1.visibility = View.GONE
                }
            }
        } else lifecycleScope.launch {
            val response = uploadWithTransferUtility(File(compressedPath), "")
            val urlFromS3 = response.first
            if (urlFromS3.isNotEmpty()) {
                if (urlFromS3.contains(Constants.MP4)) {
                    videoBitmap?.let {
                        /* val file = getImageFile(
                             it,
                             name = File(compressedPath).name.split(".")[0] + "_thumb.webp"
                         )*/
                        val file = Compressor.compress(
                            requireContext(), getImageFile(
                                it, name = File(compressedPath).name.split(".")[0] + "_thumb.webp"
                            )
                        ) {
                            quality(50)
//                                        this.format(Bitmap.CompressFormat.WEBP)
                            size(897_152)
                            /*this.resolution(720,720)*/
                        }
                        uploadWithTransferUtility(file, "")
                    }
                }
                try {
                    if (getProfileViewModel.profileData.value == null) {
                        val list = ArrayList<String>()
                        list.add(urlFromS3)
                        editProfile(list)
                    } else getProfileViewModel.profileData.value?.let { data ->
                        data.images?.let {
                            if (data.images.size > clickedPosition) {
                                data.images.removeAt(clickedPosition)
                                data.images.add(
                                    clickedPosition, urlFromS3
                                )
                            } else {
                                data.images.add(urlFromS3)
                            }
                            editProfile(data.images)
                        } ?: kotlin.run {
                            val list = ArrayList<String>()
                            list.add(urlFromS3)
                            editProfile(list)
                        }

                    }
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                } catch (e: InstanceCreationException) {
                    e.printStackTrace()
                }

            } else {
                requireActivity().showToast("Error in uploading image.")
                binding.progressBar1.visibility = View.GONE
            }
        }
        /*try {
            s3uploaderObj!!.initUpload(compressedPath)
            s3uploaderObj!!.setOns3UploadDone(object : S3Uploader.S3UploadInterface {
                override fun onUploadSuccess(status: String, response: Pair<String, String>) {
                    try {
                        val urlFromS3 = response.first
                        if (urlFromS3.contains(Constants.MP4)) {
                            val image = getImageFile(
                                videoBitmap!!,
                                File(compressedPath).name.split(".")[0] + "_thumb.webp"
                            ).absolutePath
                            uploadImageVideoTos3(image)
                        }
                        if (getProfileViewModel().profileData.value == null) {
                            val list = ArrayList<String>()
                            list.add(urlFromS3)
                            editProfile(list)
                        } else
                            getProfileViewModel().profileData.value?.let { data ->
                                data.images?.let {
                                    if (data.images.size > clickedPosition) {
                                        data.images.removeAt(clickedPosition)
                                        data.images.add(
                                            clickedPosition,
                                            urlFromS3
                                        )
                                    } else {
                                        data.images.add(urlFromS3)
                                    }
                                    editProfile(data.images)
                                } ?: kotlin.run {
                                    val list = ArrayList<String>()
                                    list.add(urlFromS3)
                                    editProfile(list)
                                }

                            }

                    } catch (e: Exception) {

                    }
                }

                override fun onUploadSuccess(response: String?) {

                }

                override fun onUploadError(response: String?) {
                    response?.let {
                        Utility.showToast(requireActivity(), response)
                    }
                    binding.progressBar1.visibility = View.GONE

                }
            })
        } catch (e: Exception) {
            Log.e("Message", e.message.toString())
        }*/
    }

    private fun uploadImageVideoTos3(path: String) {
        try {
            s3uploaderObj!!.initUpload(path)
            s3uploaderObj!!.setOns3UploadDone(object : S3Uploader.S3UploadInterface {
                override fun onUploadSuccess(status: String, response: Pair<String, String>) {

                }

                override fun onUploadSuccess(response: String?) {
                }

                override fun onUploadError(response: String?) {
                    binding.progressBar1.visibility = View.GONE

                }
            })
        } catch (e: Exception) {
            Log.e("Message", e.message.toString())
        }
    }

    private fun editProfile(list: ArrayList<String>? = ArrayList()) {
        if (view != null) lifecycleScope.launch {
            getProfileViewModel.editProfile(
                if (type == Constants.FROM_REGISTER) EditProfileApiRequest(
                    images = list, StepProgress = 2
                )
                else EditProfileApiRequest(
                    images = list
                )
            ).observe(viewLifecycleOwner) {
                when (it) {
                    is DataResult.Loading -> {

                    }

                    is DataResult.Success -> {
                        isDoneClicked = false
                        Log.d("IMAGE_LIST", "$list")
                        if (type == Constants.FROM_REGISTER) runBlocking {
                            preferencesHelper.setValue(
                                key = PreferencesHelper.PreferencesKeys.STEP_PROGRESS, value = 2
                            )
                        }
                        it.data.data.let {
                            runBlocking(Dispatchers.IO) {
                                preferencesHelper.saveDataEditProfile(
                                    it
                                )
                            }
                        }

                        it.data.data.images?.apply {
                            runBlocking {
                                preferencesHelper.saveImage(
                                    BaseUtils.getImageUrl(
                                        requireContext(), it.data.data.images[0]
                                    ), it.data.data.images[0]
                                )
                            }
                        }

                        binding.ivBack.isEnabled = true

                        back_press = "1"

                        binding.progressBar1.visibility = View.GONE
                        /*it.data.data.replaceProfileImagesWithUrl(requireContext()) { data ->
                            getProfileViewModel.profileData.value = data
                        }*/
                        getProfileViewModel.profileData.value = it.data.data
                        if (type == Constants.FROM_REGISTER) {
                            view?.findNavController()
                                ?.navigate(R.id.action_imageUpload_to_addMorePhotoFragment)
                        } else {
                            if (fromAddMorePictureScreen) {
                                view?.findNavController()?.popBackStack()
                            } else {
                                if (clickedPosition >= 3) {
                                    reset()

                                    list?.get(list.size - 1)?.let { it1 ->
                                        clickedPosition++
                                        threeImagesPairList.add(it1)
                                        setBottomImagesAdapter()
                                    }
                                }
                                if (threeImagesPairList.size >= 3 || clickedPosition <= 2) findNavController().popBackStack()
                            }
                        }
                    }

                    is DataResult.Failure -> {
                        isDoneClicked = false
                        binding.ivBack.isEnabled = true
                        binding.progressBar1.visibility = View.GONE
                        reportApiError(
                            Exception().stackTrace[0].lineNumber,
                            it.statusCode ?: 0,
                            "user/edit-profile",
                            requireActivity().componentName.className,
                            it.message ?: ""
                        )

                        FirebaseCrashlytics.getInstance()
                            .recordException(Exception("user/edit-profile Api Error"))
                    }

                    DataResult.Empty -> {}
                }
            }

        }
    }

    private fun reset() {
        binding.isVideo = false
        binding.isUrlEmpty = true
        isMuted = false
        player?.volume = 0f
        binding.ivVolume.setImageResource(R.drawable.ic_baseline_volume_mute_24)
        stopPlayer()
        binding.instacropper.setImageUri(null)
        binding.ivAdd1.visible(isVisible = true)
        binding.ivVolume.visible(isVisible = false)
        binding.tvReplace.visible(isVisible = false)
        selectedUri = null
        cameraUri = null
        tempVideoUri = null
        compressedPath = ""
        videoOutputFile = null
    }

    private fun clearPhotoFromCropper() {
        Glide.with(binding.root).load(R.color.image_back).into(binding.instacropper)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPlayer()
    }

    private fun stopPlayer() {
        if (player != null) {
            player?.playWhenReady = false
            player?.release()
        }
    }

    private fun VenueImage.updateVenue(updateVenueImages: UpdateVenueImages) {
        if (view != null && isAdded) {
            venueUpdateViewModel.updateVenue(
                UpdateVenueRequest(
                    venue_id = venueUpdateViewModel.updatingVenueData.value?._id,
                    image = updateVenueImages
                )
            ).observe(viewLifecycleOwner) {
                when (it) {
                    DataResult.Empty -> {}
                    is DataResult.Failure -> {
                        binding.progressBar1.visible(isVisible = false)
                        venueImages.remove(this)
                        it.message?.let { it1 -> requireActivity().showToast(it1) }
                    }

                    DataResult.Loading -> {}
                    is DataResult.Success -> {
                        venueUpdateViewModel.updatingVenueData.value = it.data.data
                        binding.progressBar1.visible(isVisible = false)
                        this@UploadFragment.view?.findNavController()?.popBackStack()
                    }
                }
            }

        }
    }

    private fun requestStorageAndCameraPermission() {
        showProgress(isVisible = false)
        requestPermissions.launch(
            if (Build.VERSION.SDK_INT >= 33) arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
            else arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }

    private val requestPermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { requestPermissions ->
            val granted = requestPermissions.entries.all {
                it.value == true
            }

            if (granted) {
                openBottomSheet()
            } else {
                showStoragePermissionRequiredDialog()
            }
        }

    private fun showStoragePermissionRequiredDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Storage and Camera permissions are required to access files and camera.")
            .setPositiveButton("Ok") { _, _ ->
                requireContext().openApplicationDetailsSettings()
            }.show()
    }

    private fun checkStorageAndCameraPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 33) {
            return ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_MEDIA_VIDEO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            return ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

}