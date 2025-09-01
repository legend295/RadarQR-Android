package com.radarqr.dating.android.ui.welcome.registerScreens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.*
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.mobileLogin.SendOtpViewModel
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.SharedPrefsHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.radarqr.dating.android.utility.BaseUtils.openApplicationDetailsSettings
import com.radarqr.dating.android.utility.Utility.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AddPictureFragment : BaseFragment<FragmentAddPhotoBinding>(), View.OnClickListener {

    var tag = 0
    val PERMISSION_ID = 42
    var imageUri: Uri? = null
    val SELECT_PHOTO = 1002
    val REQUEST_IMAGE_CAPTURE = 1
    lateinit var photoURI: Uri
    private val imageViewModel: ImageUploadViewModel by viewModel()
    private val preferencesHelper: PreferencesHelper by inject()
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val sendOtpViewModel: SendOtpViewModel by viewModel()
    var user_id = ""
    private var userfile: File? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        binding.addPhotoFragment = this
        runBlocking(Dispatchers.IO) {
            user_id =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_USERID).first()
                    ?: ""

        }
        binding.clFacebook.visibility =
            if (SharedPrefsHelper[Constants.LOGIN_TYPE, Constants.PHONE] == Constants.FACEBOOK) View.VISIBLE else View.GONE
//        showToolbarLayout(true)
//        showBackButton(true)
//        showToolbar(true)
//        showToolbarWhite(false)
//        showBackButtonWhite(false)
//        showProgress(true)
//        setProgress(28)
//        showSkip(false)
//        binding.tvContinue.visibility = View.GONE
        binding.ivPhoto.setOnClickListener(this)
        binding.clFacebook.setOnClickListener(this)
        binding.tvContinue.setOnClickListener(this)
    }

    override fun getLayoutRes(): Int = R.layout.fragment_add_photo

    override fun onResume() {
        super.onResume()
        activity?.let {
            (it as InitialActivity).hideShowWholeToolbar(isVisible = true)
            it.hideShowSkip(isVisible = false)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tv_continue -> {
//                this.findNavController()
//                    .navigate(R.id.action_addPhoto_to_addMorePhotoFragment)
                requireContext().showToast(getString(R.string.please_choose_a_profile_image))
            }

            R.id.iv_photo -> {
                if (checkStorageAndCameraPermission()) {
                    openBottomSheet()
                } else {
                    requestStorageAndCameraPermission()
                }
            }

            R.id.cl_facebook -> {
                if (sendOtpViewModel.facebookImage.isEmpty()) {
                    sendOtpViewModel.facebookImage =
                        "https://graph.facebook.com/${SharedPrefsHelper[Constants.SOCIAL_ID_FACEBOOK, ""]}/picture?type=large"
                }
                if (sendOtpViewModel.facebookImage.isNotEmpty()) {
                    handleFacebookClick(isVisible = true)
                    Glide.with(binding.root).asBitmap().load(sendOtpViewModel.facebookImage)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onResourceReady(
                                resource: Bitmap,
                                transition: Transition<in Bitmap>?
                            ) {
                                val bytes = ByteArrayOutputStream()
                                resource.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                                val path =
                                    MediaStore.Images.Media.insertImage(
                                        requireContext().contentResolver,
                                        resource,
                                        "Title",
                                        null
                                    )
                                Glide.with(binding.root).load(Uri.parse(path))
                                    .into(binding.ivPhoto)
                                Log.d("FILE", "$resource")
                                val data = Bundle()
                                data.putString(
                                    Constants.EXTRA, Uri.parse(path).toString().trim()
                                )
                                data.putInt(Constants.TYPE, Constants.FROM_REGISTER)
                                data.putStringArrayList("imageList_signup", image_list_signup)
                                data.putInt(Constants.POSITION, 0)
                                handleFacebookClick(isVisible = false)
                                this@AddPictureFragment.findNavController()
                                    .navigate(R.id.action_addPhoto_to_addMorePhotoFragment, data)

                            }

                            override fun onLoadCleared(placeholder: Drawable?) {
                                Log.d("FILE", "$placeholder")
                                binding.progressBar.visibility = View.GONE
                            }
                        })
                } else CommonCode.setToast(requireContext(), "Something wrong while loading image")
            }
        }
    }

    private fun handleFacebookClick(isVisible: Boolean) {
        binding.progressBar.visibility = if (isVisible) View.VISIBLE else View.GONE
        binding.clFacebook.isEnabled = !isVisible
        binding.ivPhoto.isEnabled = !isVisible
    }

    /*fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }*/

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
        val dataa = Bundle()
        dataa.putInt(Constants.TYPE, Constants.FROM_REGISTER)
        dataa.putStringArrayList("imageList_signup", image_list_signup)
        dataa.putInt(Constants.POSITION, 0)
        if (requestCode == SELECT_PHOTO) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                try {
                    image_list_signup.clear()
                    imageUri = data!!.data
                    /*  var dataa = Bundle()
                      dataa.putInt("tag", 0)
                      dataa.putStringArrayList("imageList_signup", image_list_signup)
                      dataa.putInt("position", 0)
                      dataa.putString(
                          "position_uri", imageUri.toString().trim()
                      )*/


                    dataa.putString(
                        Constants.EXTRA, imageUri.toString().trim()
                    )

                    this.findNavController()
                        .navigate(R.id.action_addPhoto_to_addMorePhotoFragment, dataa)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            try {
                image_list_signup.clear()
                /*var dataa = Bundle()

                dataa.putInt("tag", 0)
                dataa.putStringArrayList("imageList_signup", image_list_signup)
                dataa.putInt("position", 0)
                dataa.putString(
                    "position_uri", photoURI.toString().trim()
                )*/

                dataa.putString(
                    Constants.EXTRA, photoURI.toString().trim()
                )

                this.findNavController()
                    .navigate(R.id.action_addPhoto_to_addMorePhotoFragment, dataa)
            } catch (e: Exception) {

            }
        }

    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri? {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(
                inContext.getContentResolver(),
                inImage,
                user_id + "_" + System.currentTimeMillis() / 1000,
                null
            )
        return Uri.parse(path)
    }

    fun openBottomSheet() {
        val dialog = BottomSheetDialog(requireActivity())

        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet, null)

        val btngallery = view.findViewById<TextView>(R.id.tv_gallery)
        val camerabtn = view.findViewById<TextView>(R.id.tv_camera)
        newimageUri1 = null
        btngallery.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Tack Image"), SELECT_PHOTO)
            dialog.dismiss()
        }

        camerabtn.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.d("mylog", "Exception while creating file: $ex")
            }
            // Continue only if the File was successfully created
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d("mylog", "Photofile not null")
                photoURI = FileProvider.getUriForFile(
                    requireActivity(),
                    "${requireContext().packageName}.provider",
                    photoFile
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
            dialog.dismiss()
        }
        dialog.setCancelable(false)

        dialog.setContentView(view)

        dialog.show()

    }

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    private fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = context?.externalCacheDir
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        val actualImage = image.absolutePath
        Log.d("mylog", "Path: $actualImage")
        return image
    }

}
