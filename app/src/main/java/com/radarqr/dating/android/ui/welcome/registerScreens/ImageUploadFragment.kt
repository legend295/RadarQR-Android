package com.radarqr.dating.android.ui.welcome.registerScreens

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.databinding.FragmentImageUploadBinding
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.utility.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.gowtham.library.utils.TrimType
import com.gowtham.library.utils.TrimVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.radarqr.dating.android.utility.S3Uploader
import com.radarqr.dating.android.utility.S3Utils
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

import android.os.Build
import com.radarqr.dating.android.constant.Constants
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import java.io.FileNotFoundException
import java.io.FileOutputStream


var newimageUri1: Uri? = null
var back_press = ""


var image_list_signup: ArrayList<String> = ArrayList()

class ImageUploadFragment : BaseFragment<FragmentImageUploadBinding>(),
    View.OnClickListener {
    var isMute = false
    private var player: SimpleExoPlayer? = null
    private val preferencesHelper: PreferencesHelper by inject()
    private val imageViewModel: ImageUploadViewModel by viewModel()
    var image_list: ArrayList<String> = ArrayList()
    var tag = 0
    var poss = 0
    var start = 0
    var isNew: Boolean = false
    private var actualImage: File? = null
    private var compressedImage: File? = null
    var image_list_edit: ArrayList<String> = ArrayList()
    var videoUri = ""
    lateinit var sign_imageUri: Uri
    val PERMISSION_ID = 42
    var imageUrii: Uri? = null
    val SELECT_PHOTO = 1002
    val REQUEST_IMAGE_CAPTURE = 1
    var url = ""
    private var s3uploaderObj: S3Uploader? = null
    private var urlFromS3: String? = null
    var user_id = ""
    var compressedImagePath = ""
    private var mTranscodeOutputFile: File? = null

    private val getProfileViewModel: GetProfileViewModel by viewModel()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageUploadFragment = this
        s3uploaderObj = S3Uploader(requireActivity())
        runBlocking(Dispatchers.IO) {
            user_id =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_USERID).first()
                    ?: ""
        }
        val data: Bundle? = arguments

        tag = data?.getInt("tag")!!
        showToolbarLayout(false)


        try {
            if (tag == 1) {
                clearPhotoFromCropper()
                poss = data.getInt("position")
                pos = data.getInt("position")
                image_list_edit = data.getStringArrayList("imageList_edit")!!
                url = data.getString("url")!!

                if (url != "") {
                    image_list.addAll(image_list_edit)
                    binding.ivAdd1.visibility = View.GONE
                    binding.ivView1.visibility = View.GONE
                    binding.clView1.visibility = View.VISIBLE
                    binding.tvReplace.visibility = View.VISIBLE
                    if (url.contains(Constants.MP4)) {
                        binding.isVideo = true
                        binding.ffVideo.visibility = View.VISIBLE
                        binding.ivVolume.visibility = View.VISIBLE
                        initializePlayer(Uri.parse(url))
                    } else {
                        binding.isVideo = false
                        binding.ivVolume.visibility = View.GONE
                        binding.ffVideo.visibility = View.GONE
                        Glide.with(requireActivity()).load(url)
                            .into(binding.instacropper)
                    }

                    /* for (i in 0 until image_list_edit.size) {
                         imageViewModel.imageUrlList[i] = image_list_edit[i]
                     }*/
                } else {
                    binding.tvReplace.visibility = View.GONE
                    binding.ivVolume.visibility = View.GONE
                    binding.ffVideo.visibility = View.GONE
                    image_list.addAll(image_list_edit)
                }
            } else {

                poss = data.getInt("position")
                pos = data.getInt("position")
                clearPhotoFromCropper()
                try {
                    try {
                        imageUrii = data.getString("position_uri")!!.toUri()
                    } catch (e: Exception) {
                    }
                    if (imageUrii != null) {

//                        imageViewModel.imageUriList[0] =
//                            imageUrii!!
                        binding.ivAdd1.visibility = View.GONE
                        binding.instacropper.setImageUri(imageUrii)
                    } else {
                        try {
                            if (imageViewModel.imageUriList[imageViewModel.clickedPosition].toString()
                                    .contains("com.android.providers.media.documents") || (Utility.getMimeType(
                                    FileUtil.from(
                                        requireContext(),
                                        imageViewModel.imageUriList[imageViewModel.clickedPosition]!!
                                    ).absolutePath
                                )?.contains("video") == true
                                        )
                            ) {
                                binding.isVideo = true
                                isNew = false
                                binding.instacropper.visibility = View.GONE
                                binding.ffVideo.visibility = View.VISIBLE
                                binding.videoView.scaleX = 2.1f
                                binding.videoView.scaleY = 2f
                                initializePlayer(imageViewModel.imageUriList[imageViewModel.clickedPosition]!!)
                                videoUri =
                                    imageViewModel.imageUriList[imageViewModel.clickedPosition].toString()

                            } else {
                                binding.isVideo = false
                                imageUrii =
                                    imageViewModel.imageUriList[imageViewModel.clickedPosition]

                                binding.instacropper.visibility = View.VISIBLE
                                binding.ffVideo.visibility = View.GONE
                                binding.instacropper.setImageUri(imageViewModel.imageUriList[imageViewModel.clickedPosition])

                            }
                            if (imageViewModel.imageUriList[imageViewModel.clickedPosition] != null) {
                                binding.ivAdd1.visibility = View.GONE
                                binding.tvReplace.visibility = View.VISIBLE
                            } else {
                                binding.ivAdd1.visibility = View.VISIBLE
                                binding.tvReplace.visibility = View.GONE
                            }
                        } catch (e: Exception) {

                        }
                    }
                    binding.ivView1.visibility = View.GONE
                    binding.clView1.visibility = View.VISIBLE


                    if (image_list_signup.size == 0) {
                        start = 1
                    } else {
                        image_list.addAll(image_list_signup)
                    }

                } catch (e: Exception) {
                    showError("Failed to read picture data!")
                    e.printStackTrace()
                }
            }
        } catch (e: Exception) {

        }

        binding.ivBack.setOnClickListener {
            back_press = ""
            findNavController().popBackStack()
        }
        binding.tvContinue.setOnClickListener {
            if (videoUri != null && videoUri != "") {
                if (isNew) {
                    binding.progressBar1.visibility = View.VISIBLE
                    binding.tvContinue.isEnabled = false
                    uploadImageVideoTos3(null, true)
                } else {
                    findNavController().popBackStack()
                }
            } else if (imageUrii == null) {

                if (url == "") {
                    CommonCode.setToast(requireActivity(), "Please choose image")
                } else {
//                    editProfile()
                    findNavController().popBackStack()
                }
            } else {
                binding.progressBar1.visibility = View.VISIBLE
                binding.ivBack.isEnabled = false
                binding.instacropper.crop(
                    View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
                ) {
                    if (image_list_signup.equals("") && poss == 0) {

                    } else {
                        compressedImagePath = getImageFile(it).absolutePath
                        uploadImageVideoTos3(imageUrii!!, false)
//                        actualImage = FileUtil.from(requireActivity(), imageUrii!!)
//                        compressImage()
                    }
                }
            }
        }

        binding.ivView1.setOnClickListener(this)
        binding.ffImage.setOnClickListener(this)
        binding.tvReplace.setOnClickListener(this)
        binding.ivClose1.setOnClickListener(this)
        binding.ivVolume.setOnClickListener(this)

    }


    override fun getLayoutRes(): Int = R.layout.fragment_image_upload
    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.iv_view1, R.id.tv_replace -> {
                if (checkPermissions()) {

                    openBottomSheet()

                } else {
                    requestPermissions()
                }
            }
            R.id.ff_image -> {
                if (checkPermissions()) {

                    openBottomSheet()

                } else {
                    requestPermissions()
                }
            }
            R.id.ivVolume -> {

                if (isMute) {
                    isMute = false
                    player!!.volume = 100f
                    binding.ivVolume.setImageResource(R.drawable.ic_unmute)
                } else {
                    isMute = true
                    player!!.volume = 0f
                    binding.ivVolume.setImageResource(R.drawable.ic_baseline_volume_mute_24)
                }


            }
            R.id.iv_close1 -> {
                if (checkPermissions()) {

                    openBottomSheet()

                } else {
                    requestPermissions()
                }
            }


        }
    }

    private fun checkPermissions(): Boolean {
        if (activity is InitialActivity) {
            if (ActivityCompat.checkSelfPermission(
                    activity as InitialActivity,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    activity as InitialActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    activity as InitialActivity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            )
                return true
        }

        return false
    }

    private fun requestPermissions() {
        showProgressBar(false)
        if (activity is InitialActivity) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSION_ID
            )
        } else {
            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                PERMISSION_ID
            )
        }
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SELECT_PHOTO) {
            if (resultCode == RESULT_OK && data != null) {
                binding.tvReplace.visibility = View.VISIBLE
                try {
                    clearPhotoFromCropper()
                    imageUrii = data.data

                    binding.ivClose1.visibility = View.GONE
                    binding.ivAdd1.visibility = View.GONE
                    binding.ivView1.visibility = View.GONE
                    binding.clView1.visibility = View.VISIBLE
                    binding.instacropper.setImageUri(imageUrii)
                    try {
                        actualImage = FileUtil.from(requireActivity(), data.data!!)

                    } catch (e: IOException) {
                        showError("Failed to read picture data!")
                        e.printStackTrace()
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            try {
                binding.tvReplace.visibility = View.VISIBLE
                clearPhotoFromCropper()
                imageUrii = photoURI
                binding.ivAdd1.visibility = View.GONE
                binding.ivView1.visibility = View.GONE
                binding.clView1.visibility = View.VISIBLE
                binding.instacropper.setImageUri(imageUrii)

                try {
                    actualImage = FileUtil.from(requireActivity(), photoURI)

                } catch (e: IOException) {
                    showError("Failed to read picture data!")
                    e.printStackTrace()
                }


            } catch (e: Exception) {

            }
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (player != null) {
            player?.stop()
        }
    }


    private fun getImageFile(bitmap: Bitmap): File {
        val cachePath = File(requireActivity().externalCacheDir, "shared_images/")
        cachePath.mkdirs()

        //create png file
        val file = File(cachePath, "${user_id}_${System.currentTimeMillis()}.webp")
        val fileOutputStream: FileOutputStream?
        try {
            fileOutputStream = FileOutputStream(file)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSLESS, 80, fileOutputStream)
            } else {
                bitmap.compress(Bitmap.CompressFormat.WEBP, 80, fileOutputStream)
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

    lateinit var photoURI: Uri
    fun openBottomSheet() {
        val dialog = BottomSheetDialog(requireActivity())

        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet, null)

        val btngallery = view.findViewById<TextView>(R.id.tv_gallery)
        val camerabtn = view.findViewById<TextView>(R.id.tv_camera)
        val tvVideo = view.findViewById<TextView>(R.id.tv_video)
        val tv_find = view.findViewById<TextView>(R.id.tv_find)
        if (!url.equals("")) {
            tv_find.text = "Find a replacement"
        } else {
            tv_find.text = "Upload Photo/Video"

        }
        if (imageViewModel.clickedPosition != 0 && imageViewModel.clickedPosition != -1) {
            tvVideo.visibility = View.VISIBLE
        } else {
            tvVideo.visibility = View.GONE
        }
        btngallery.setOnClickListener {
            openGallery()
            dialog.dismiss()
        }
        tvVideo.setOnClickListener {
//            val intent = Intent()
//            intent.type = "image/*"
//            intent.action = Intent.ACTION_GET_CONTENT
//            startActivityForResult(Intent.createChooser(intent, "Tack Image"), SELECT_PHOTO)
//            dialog.dismiss()
            openVideoCamera()
            dialog.dismiss()
        }
        camerabtn.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)


            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                Log.d("mylog", "Exception while creating file: $ex")
            }
            if (photoFile != null) {
                Log.d("mylog", "Photofile not null")
                photoURI = FileProvider.getUriForFile(
                    requireActivity(),
                    "com.e.radardating.dev.provider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }

            dialog.dismiss()
        }
        dialog.setCancelable(true)

        dialog.setContentView(view)

        dialog.show()

    }

    private fun openVideoCamera() {
        val takeVideoIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
        var photoFile: File? = null
        try {
            photoFile = createVideoFile()
        } catch (ex: IOException) {

        }
        if (photoFile != null) {

            photoURI = FileProvider.getUriForFile(
                requireActivity(),
                "${requireActivity().packageName}.provider",
                photoFile
            )
            takeVideoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30)
            takeVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            videoCaptureLauncher.launch(takeVideoIntent)
        }
    }

    @Throws(IOException::class)
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
        Log.d("mylog", "Path: $actualImage")
        return image
    }

    private fun openGallery() {
        val intent = Intent()
        intent.type = "image/* video/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
        intent.action = Intent.ACTION_GET_CONTENT
        galleryImageLauncher.launch(intent)
    }

    private var galleryImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                binding.tvReplace.visibility = View.VISIBLE
                val data: Intent? = result.data
                try {
                    imageUrii = data?.data
                    val mimeType: String? = result.data?.let { returnUri ->
                        requireContext().contentResolver.getType(returnUri.data!!)
                    }
                    if (mimeType?.contains("image") == true) {

                        clearPhotoFromCropper()
                        binding.ffVideo.visibility = View.GONE
                        binding.instacropper.visibility = View.VISIBLE
                        binding.ivClose1.visibility = View.GONE
                        binding.ivAdd1.visibility = View.GONE
                        binding.ivView1.visibility = View.GONE
                        binding.clView1.visibility = View.VISIBLE
                        binding.instacropper.setImageUri(imageUrii)
                        try {
                            actualImage = FileUtil.from(requireActivity(), data?.data!!)
//                            compressImage()
                        } catch (e: IOException) {
                            showError("Failed to read picture data!")
                            e.printStackTrace()
                        }
                    } else {
//                        binding.ffVideo.visibility = View.VISIBLE
//                        binding.instacropper.visibility = View.GONE

                       /* TrimVideo.activity(imageUrii.toString())
                            .setTrimType(TrimType.MIN_MAX_DURATION)
                            .setMinToMax(1, 30)
                            .setHideSeekBar(true)
                            .start(this, videoTrimmerLauncher)*/
                    }


                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

    private var videoCaptureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                binding.tvReplace.visibility = View.VISIBLE
                val data: Intent? = result.data
                try {
//                    clearPhotoFromCropper()
                    imageUrii = photoURI//data?.data
//                    binding.ffVideo.visibility = View.GONE
//                    binding.instacropper.visibility = View.VISIBLE
                    TrimVideo.activity(imageUrii.toString())
                        .setTrimType(TrimType.MIN_MAX_DURATION)
                        .setMinToMax(1, 30)
                        .setHideSeekBar(true)
                        .start(this, videoTrimmerLauncher)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }


    private var videoTrimmerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                isNew = true
                binding.ffVideo.visibility = View.VISIBLE
                binding.instacropper.visibility = View.GONE
                val uri = Uri.parse(TrimVideo.getTrimmedVideoPath(result.data))
                mTranscodeOutputFile =
                    result.data?.extras?.get(TrimVideo.CROPPED_VIDEO_PATH) as File
                Log.d("TAG", "Trimmed path:: " + uri)
                binding.ivAdd1.visibility = View.GONE
                videoUri = result.data?.extras?.get(TrimVideo.CROPPED_VIDEO_PATH).toString()
                initializePlayer(Uri.fromFile(result.data?.extras?.get(TrimVideo.CROPPED_VIDEO_PATH) as File))
            }
        }

    private fun initializePlayer(uri: Uri) {
        player = SimpleExoPlayer.Builder(requireActivity())
            .build()
            .also { exoPlayer ->
                isMute = true
                binding.ivVolume.visibility = View.VISIBLE
                binding.videoView.player = exoPlayer
                val mediaItem = MediaItem.fromUri(uri)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            }
        player!!.volume = 0f

    }

    private fun clearPhotoFromCropper() {
        Glide.with(binding.root).load(R.color.image_back).into(binding.instacropper)
        binding.ivAdd1.visibility = View.VISIBLE
    }

    @ExperimentalCoroutinesApi
    private fun editProfile() {
        if (view != null)
            lifecycleScope.launch {
                getProfileViewModel.editProfile(
                    EditProfileApiRequest(
                        images = ArrayList(
                            imageViewModel.imageUrlList.values
                        )
                    )
                )
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {

                            }
                            is DataResult.Success -> {

                                it.data.data.let {
                                    runBlocking(Dispatchers.IO) {
                                        preferencesHelper.saveDataEditProfile(
                                            it
                                        )
                                    }
                                }

                                try {
                                    val urlFromS3 = S3Utils.generatesShareUrl(
                                        requireActivity(), it.data.data.images?.get(0)
                                    )

                                    val Image = urlFromS3.replace(" ", "%20")

                                    runBlocking {
                                        it.data.data.images?.get(0)?.let { it1 ->
                                            preferencesHelper.saveImage(
                                                Image,
                                                it1
                                            )
                                        }
                                    }
                                } catch (e: Exception) {

                                }
                                if (url.isNotEmpty()) {
                                    /*getProfileViewModel.userImages.remove(
                                        image_list_edit[imageViewModel.clickedPosition]
                                    )*/
                                }
                                binding.ivBack.isEnabled = true
                                if (imageUrii != null) {
                                    newimageUri1 = imageUrii
                                    if (poss == 0 && url != "") {
                                        image_list_edit.add(
                                            imageViewModel.clickedPosition,
                                            it.data.data.images?.get(0) ?: ""
                                        )
                                        for (i in 0 until image_list_edit.size) {
                                            imageViewModel.imageUrlList[i] =
                                                image_list_edit[i]
                                        }
                                    } else {
                                        if (imageViewModel.clickedPosition == -1) {
                                            imageViewModel.clickedPosition = 0
                                        }

                                        imageViewModel.imageUriList[imageViewModel.clickedPosition] =
                                            imageUrii!!
                                    }
                                } else {

                                }
                                back_press = "1"
                                if (tag == 0) {
                                    image_list_signup.clear()

                                    image_list_signup.addAll(image_list)
                                }
                                binding.progressBar1.visibility = View.GONE
                                getProfileViewModel.profileData.value = it.data.data
                                if (start == 1) {
                                    findNavController().navigate(R.id.action_imageUpload_to_addMorePhotoFragment)
                                } else {
                                    findNavController().popBackStack()
                                }
                            }
                            is DataResult.Failure -> {
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

                            else -> {}
                        }
                    }

            }
    }


    private fun uploadImageVideoTos3(imageUri: Uri? = null, isVideoUploading: Boolean) {
        var path = ""
        path = if (isVideoUploading) {
            videoUri
        } else {
            compressedImagePath
        }
//        val path = compressedImagePath
        if (path != null) {
            try {
                s3uploaderObj!!.initUpload(path)
                /*s3uploaderObj!!.setOns3UploadDone(object : S3Uploader.S3UploadInterface {
                    override fun onUploadSuccess(response: String?) {
                        if (response.equals("Success", ignoreCase = true)) {
                            try {
                                urlFromS3 = S3Utils.generates3ShareUrl(requireActivity(), path)
                                imageViewModel.imageUrlList[imageViewModel.clickedPosition] =
                                    urlFromS3 ?: ""
                                editProfile()
                            } catch (e: Exception) {

                            }
                        }

                    }

                    override fun onUploadError(response: String?) {


                    }
                })*/
            } catch (e: Exception) {
                Log.e("Message", e.message.toString())
            }
        }
    }

    private fun showError(errorMessage: String) {
        Toast.makeText(requireActivity(), errorMessage, Toast.LENGTH_SHORT).show()
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context?.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        var actualImage = image.absolutePath
        Log.d("mylog", "Path: $actualImage")
        return image
    }


}
