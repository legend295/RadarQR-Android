package com.radarqr.dating.android.hotspots.upload

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.radarqr.dating.android.R
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentUploadVenuePhotoBinding
import com.radarqr.dating.android.databinding.LayoutBottomSheetBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.FileUtil
import com.radarqr.dating.android.utility.Utility
import com.radarqr.dating.android.utility.Utility.createImageFile
import com.radarqr.dating.android.utility.Utility.isVideo
import com.radarqr.dating.android.utility.cropper.InstaCropperView
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

class UploadVenuePhotoFragment : VenueBaseFragment<FragmentUploadVenuePhotoBinding>(),
    ViewClickHandler {

    private var cameraUri: Uri? = null
    private var selectedUri: Uri? = null
    private var actualImage: File? = null
    private var url = "" // Store the url for existing image
    private var compressedPath = ""
    private var venueId: String = ""

    override fun getLayoutRes(): Int = R.layout.fragment_upload_venue_photo

    override fun init(view: View, savedInstanceState: Bundle?) {
        binding.viewHandler = this
        binding.isUrlEmpty = selectedUri == null

        // when user back-press from the forwarded screen then this method will handle the selected media
        handleNoMediaSelection()

        // get data from the previous screen
        venueId = arguments?.getString(Constants.VENUE_ID, "") ?: ""

        binding.includeUploadVenuePhoto.instaCropper.touchEvent = InstaCropperView.OnTouch {
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


    override fun onClick(view: View) {
        when (view.id) {
            R.id.viewInstaCropper, R.id.tv_replace -> {
                if (checkStorageAndCameraPermission()) {
                    openBottomSheet()
                } else {
                    requestStorageAndCameraPermission()
                }
            }

            R.id.tvDone -> {
                if (selectedUri == null) return
                binding.includeUploadVenuePhoto.instaCropper.crop(
                    View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.AT_MOST),
                    View.MeasureSpec.makeMeasureSpec(1024, View.MeasureSpec.AT_MOST)
                ) { bitmap ->
                    runBlocking {
                        if (this@UploadVenuePhotoFragment.view != null && isAdded) {
                            val file = Compressor.compress(
                                requireContext(),
                                getImageFile(
                                    bitmap,
                                    "${venueId}_${System.currentTimeMillis()}.webp"
                                )
                            ) {
                                quality(50)
                                size(897_152)
                            }
                            compressedPath = file.absolutePath
                            val bundle = Bundle().apply {
                                putString(Constants.VENUE_ID, venueId)
                                putString(Constants.EXTRA, compressedPath)
                            }
                            this@UploadVenuePhotoFragment.view?.findNavController()
                                ?.navigate(
                                    R.id.action_from_uploadVenuePhotoFragment_to_imageTagFragment,
                                    bundle
                                )

                        }
                    }
                }

            }
        }
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


    fun openBottomSheet() {
        val dialog = BottomSheetDialog(requireActivity())
        val view = DataBindingUtil.inflate<LayoutBottomSheetBinding>(
            LayoutInflater.from(requireContext()),
            R.layout.layout_bottom_sheet,
            null,
            false
        )
        view.tvGallery.setOnClickListener {
            openGallery()
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
                    requireActivity(),
                    "${requireActivity().packageName}.provider",
                    photoFile
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

    private fun clearPhotoFromCropper() {
        Glide.with(binding.root).load(R.color.image_back)
            .into(binding.includeUploadVenuePhoto.instaCropper)
    }

    private fun handleNoMediaSelection() {
        binding.isUrlEmpty = (selectedUri == null && url.trim().isEmpty())
        selectedUri?.let {
            binding.includeUploadVenuePhoto.instaCropper.setImageUri(selectedUri)
        }
    }

    private fun openGallery() {
        val mime = arrayOf("image/*")
        val intent = Intent().apply {
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, mime)
            action = Intent.ACTION_GET_CONTENT
        }
        galleryImageLauncher.launch(intent)
    }

    private var cameraImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    clearPhotoFromCropper()
                    selectedUri = cameraUri
                    binding.isUrlEmpty = false
//                    binding.isVideo = false
                    binding.includeUploadVenuePhoto.instaCropper.setImageUri(selectedUri)

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

    private var galleryImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.includeUploadVenuePhoto.tvReplace.visibility = View.VISIBLE
                val data: Intent? = result.data
                try {
                    val url = data?.data
                    url?.let { uri ->
                        binding.isUrlEmpty = false
                        if (!requireActivity().isVideo(uri)) {
                            clearPhotoFromCropper()
                            selectedUri = uri
                            binding.includeUploadVenuePhoto.instaCropper.setImageUri(uri)
                            try {
                                actualImage = FileUtil.from(requireActivity(), data.data!!)
                            } catch (e: IOException) {
                                BaseUtils.showMessage(
                                    requireContext(),
                                    message = "Failed to read picture data!"
                                )
                                e.printStackTrace()
                            }
                        } else {
                            /*tempVideoUri = uri
                            TrimVideo.activity(uri.toString())
                                .setTrimType(TrimType.MIN_MAX_DURATION)
                                .setMinToMax(1, 30)
                                .setHideSeekBar(true)
                                .start(this, videoTrimmerLauncher)*/
                        }
                    }


                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                handleNoMediaSelection()
            }
        }


    private fun requestStorageAndCameraPermission() {
        requestPermissions.launch(
            if (Build.VERSION.SDK_INT >= 33) arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            )
            else
                arrayOf(
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
            }
        }


    private fun checkStorageAndCameraPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 33) {
            return ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_MEDIA_VIDEO
                    ) == PackageManager.PERMISSION_GRANTED
        } else {
            return ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
        }
    }


}