package com.radarqr.dating.android.ui.home.qrCode

import android.Manifest
import android.content.ContentValues
import android.content.Context.WINDOW_SERVICE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.format.DateFormat
import android.view.View
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.LayoutShowQrcodeBinding
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.PreferencesHelper.PreferencesKeys.KEY_FIRSTNAME
import com.radarqr.dating.android.utility.PreferencesHelper.PreferencesKeys.KEY_IMAGE_URL
import com.radarqr.dating.android.utility.PreferencesHelper.PreferencesKeys.KEY_USERID
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.openLink
import com.radarqr.dating.android.utility.Utility.share
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.qrgenearator.QRGContents
import com.radarqr.dating.android.utility.qrgenearator.QRGEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*


class ShowQRFragment : BaseFragment<LayoutShowQrcodeBinding>(), ViewClickHandler {
    override fun getLayoutRes() = R.layout.layout_show_qrcode
    private val preferencesHelper: PreferencesHelper by inject()
    var qr_code = ""
    var user_id = ""
    var name = ""
    var image = ""
    var first = 0
    private val savePath = Environment.getExternalStorageDirectory().path + "/QRCode/"
    private var bitmap: Bitmap? = null
    private var qrgEncoder: QRGEncoder? = null
    private val getProfileViewModel: GetProfileViewModel by viewModel()

    //    var url = "http://radar.trantorglobal.com/"
    var url = RaddarApp.getEnvironment().getShareUrl()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HomeActivity.activeFragment.value = this
        /*val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())*/
        binding.viewHandler = this
        setActionBar("QR Code")
        binding.showQRFragment = this
        runBlocking(Dispatchers.IO) {
            user_id = preferencesHelper.getValue(KEY_USERID).first() ?: ""
            name = preferencesHelper.getValue(KEY_FIRSTNAME).first() ?: ""
            image = preferencesHelper.getValue(KEY_IMAGE_URL).first() ?: ""
        }
        qr_code = url + user_id
//        binding.tvName.text = name
//        binding.tvNameN.text = name

        initializeObserver()
        if (this.view != null && isAdded) getBaseActivity()?.apply {
            getProfile(getProfileRequest()) { data, _ ->
                data?.let {
                    if (context != null)
                    /*it.replaceProfileImagesWithUrl(context?:RaddarApp.getInstance().applicationContext) { data ->
                        getProfileViewModel.profileData.value = data
                    }*/ getProfileViewModel.profileData.value = it
                }
            }
        }
        /* binding.reload.setOnClickListener {
             if (BaseUtils.isInternetAvailable()) {
                 binding.reload.visibility = View.GONE
                 binding.progressBar.visibility = View.VISIBLE
                 getBaseActivity()?.apply {
                     getProfile(getProfileRequest()) {
                         it?.let {
                             getProfileViewModel.profileData.value = it
                         }
                     }
                 }
             } else CommonCode.setToast(
                 requireContext(),
                 resources.getString(R.string.no_internet_msg)
             )
         }*/

        loadImage()


        val manager = requireActivity().getSystemService(WINDOW_SERVICE) as WindowManager?
        val display = manager!!.defaultDisplay
        val point = Point()
        display.getSize(point)
        val width: Int = point.x
        val height: Int = point.y
        val smallerDimension = if (width < height) width else height
//        smallerDimension = smallerDimension * 3 / 4

//        qrgEncoder = QRGEncoder(
//            qr_code, null, QRGContents.Type.TEXT, smallerDimension
//        )
//        qrgEncoder?.colorBlack = ContextCompat.getColor(requireContext(), R.color.teal_200)
//        qrgEncoder?.colorBlack = ContextCompat.getColor(requireContext(),R.color.red)
//        qrgEncoder?.colorWhite = Color.BLUE

        try {
//            bitmap = qrgEncoder!!.bitmap
            bitmap = getQRCode()
//            bitmap?.setColorSpace(Colo)

//            val overlay = BitmapFactory.decodeResource(resources, R.drawable.qr_logo)
//            binding.idIVQrcode.setImageBitmap(mergeBitmaps(overlay, bitmap!!))
            binding.idIVQrcodeSave.setImageBitmap(bitmap)
            binding.idIVQrcode.setImageBitmap(bitmap)
//            binding.idIVQrcodeNew.setImageBitmap(mergeBitmaps(overlay, bitmap!!))
        } catch (e: Exception) {
            e.printStackTrace()
        }


        /*binding.clSave.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireActivity().applicationContext.getApplicationContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                takeScreenshot()

            } else {
                ActivityCompat.requestPermissions(
                    requireActivity()!!,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    0
                )
            }
        }
        binding.clShare.setOnClickListener {

            val share = Intent(Intent.ACTION_SEND)
            share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            share.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            share.type = "image/jpeg"
            val v1: View =
                binding.rlQrCode//requireActivity()?.getWindow().getDecorView().getRootView()!!
            v1.setDrawingCacheEnabled(true)
            val bitmap: Bitmap = Bitmap.createBitmap(v1.getDrawingCache())
            v1.setDrawingCacheEnabled(false)
            val bytes = ByteArrayOutputStream()

            bitmap.compress(Bitmap.CompressFormat.PNG, 50, bytes)
            val path =
                MediaStore.Images.Media.insertImage(
                    requireActivity().getContentResolver(),
                    bitmap,
                    "IMG" + "_" + user_id,
                    null
                )
            val f = File(
                path
            )
            try {
                f.createNewFile()
                val fo = FileOutputStream(f)
                fo.write(bytes.toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val uri = Uri.parse(path.toString())

            share.putExtra(Intent.EXTRA_STREAM, uri)
            binding.clShare.isEnabled = false
            val handler = Handler()
            handler.postDelayed({
                binding.clShare.isEnabled = true
            }, 5000)
            startActivity(Intent.createChooser(share, "Share Image"))

        }
*/

        binding.tvScanCode.setOnClickListener {
            this.view?.findNavController()?.navigate(R.id.action_showQRFragment_to_scanFragment)
        }
    }

    private fun initializeObserver() {
        getProfileViewModel.profileData.observe(viewLifecycleOwner) {
            it.images?.apply {
                /* getProfileViewModel.storeImages(
                     it.images,
                     requireContext()
                 )*/

                if (it.images.isNotEmpty()) {
                    image = it.images[0]
//                    image = getProfileViewModel.userImages[it.images[0]] ?: ""
                    loadImage()
                }
            }

//            binding.tvName.text = it.name


            runBlocking(Dispatchers.IO) { preferencesHelper.saveUserData(it) }
//            binding.tvName.text = it.name ?: ""
        }
    }

    private fun loadImage() {
        binding.ivUser.loadImage(image, isThumb = true)
    }


    private fun mergeBitmaps(overlay: Bitmap, bitmap: Bitmap): Bitmap? {
        val height = bitmap.height
        val width = bitmap.width
        val combined = Bitmap.createBitmap(width, height, bitmap.config)
        val canvas = Canvas(combined)
        val canvasWidth: Int = canvas.width
        val canvasHeight: Int = canvas.height
        canvas.drawBitmap(bitmap, Matrix(), null)
        val centreX = (canvasWidth - overlay.width) / 2f
        val centreY = (canvasHeight - overlay.height) / 2f
        canvas.drawBitmap(overlay, centreX, centreY, null)
        return combined
    }

    private fun takeScreenshot() {
        val now = Date()
        DateFormat.format("yyyy-MM-dd_hh:mm:ss", now)
        try {
            val bitmap = getBitmapFromView(binding.clSave)
            saveMediaToStorage(bitmap)
            /*val v1: View =
//                binding.idIVQrcode//requireActivity()?.getWindow().getDecorView().getRootView()!!
                binding.clSave//requireActivity()?.getWindow().getDecorView().getRootView()!!
            v1.isDrawingCacheEnabled = true
            val bitmap: Bitmap = Bitmap.createBitmap(v1.drawingCache)
            v1.isDrawingCacheEnabled = false

            val projection = arrayOf(
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.MediaColumns.RELATIVE_PATH
            )

            val pathName = "Pictures"
            val name = "IMG_$user_id"

            val selection =
                MediaStore.Files.FileColumns.RELATIVE_PATH + " like ? and " + MediaStore.Files.FileColumns.DISPLAY_NAME + " like ?"

            val selectionArgs = arrayOf("%$pathName%", "%$name%")
            val cursor = context?.contentResolver?.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
            )


            val indexDisplayName = cursor?.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)

            if (cursor!!.count > 0) {
                // file is exist
                if (first == 0) {
                    CommonCode.setToast(
                        requireActivity(),
                        "Your image have been saved to your photos"
                    )
                    first = 1
                }
            } else {
                val bytes = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes)
                val path =
                    MediaStore.Images.Media.insertImage(
                        requireActivity().contentResolver,
                        bitmap,
                        "IMG_$user_id",
                        null
                    )
                CommonCode.setToast(requireActivity(), "Your image have been saved to your photos")

            }

            cursor.close()*/
        } catch (e: Throwable) {
            // Several error may come out with file handling or DOM
            e.printStackTrace()
        }
    }

    private fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    fun getBitmapFromView(view: View, defaultColor: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(
            view.width, view.height, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        canvas.drawColor(defaultColor)
        view.draw(canvas)
        return bitmap
    }

    private fun saveMediaToStorage(bitmap: Bitmap) {
        //Generating a file name
        val appName = requireContext().getString(R.string.app_name)
        val middle = user_id.substring(IntRange(0, user_id.length / 2))
        val filename = "${appName}_$middle.jpg"
        var doesFileExists = false
        //Output stream
        var fos: OutputStream? = null

        //For devices running android >= Q
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //getting the contentResolver
            context?.contentResolver?.also { resolver ->

                //Content resolver will process the contentValues
                val contentValues = ContentValues().apply {

                    //putting file information in content values
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }

                //Inserting the contentValues to contentResolver and getting the Uri
                val imageUri: Uri? =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                imageUri?.let {
//                    doesFileExists =
//                        DocumentFile.fromSingleUri(requireContext(), it)?.exists() ?: false
//                    if (!doesFileExists) {
//                    }
                    fos = resolver.openOutputStream(it)
                }
                //Opening an outputStream with the Uri that we got

            }
        } else {
            //These for devices running on android < Q
            //So I don't think an explanation is needed here
            val imagesDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
//            doesFileExists = image.exists()
//            if (!doesFileExists) {
//            }
            fos = FileOutputStream(image)

        }

        fos?.use {
            //Finally writing the bitmap to the output stream that we opened
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            requireContext().showToast("Image saved to Photos")
        } ?: kotlin.run {
//            if (doesFileExists){
//                requireContext().showToast("Image already exists")
//            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivShare -> {
                requireActivity().share(
                    "Let's connect on RadarQR!\n" + "The below message will navigate you to my profile. Check me out! ${
                        RaddarApp.getEnvironment().getShareUrl()
                    }" + user_id
                )
                /*val share = Intent(Intent.ACTION_SEND)
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                share.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                share.type = "image/jpeg"
                val v1: View =
                    binding.clSave
//                    binding.idIVQrcode//requireActivity()?.getWindow().getDecorView().getRootView()!!
                v1.setDrawingCacheEnabled(true)
                val bitmap: Bitmap = Bitmap.createBitmap(v1.getDrawingCache())
                v1.setDrawingCacheEnabled(false)
                val bytes = ByteArrayOutputStream()

                bitmap.compress(Bitmap.CompressFormat.PNG, 50, bytes)
                val path =
                    MediaStore.Images.Media.insertImage(
                        requireActivity().getContentResolver(),
                        bitmap,
                        "IMG_$user_id",
                        null
                    )
                val f = File(
                    path
                )
                try {
                    f.createNewFile()
                    val fo = FileOutputStream(f)
                    fo.write(bytes.toByteArray())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                val uri = Uri.parse(path.toString())

                share.putExtra(Intent.EXTRA_STREAM, uri)
                binding.ivShare.isEnabled = false
                val handler = Handler(Looper.getMainLooper())
                handler.postDelayed({
                    binding.ivShare.isEnabled = true
                }, 5000)
                startActivity(Intent.createChooser(share, "Share Image"))
*/
            }

            R.id.ivPrint -> {
                if (checkStorageAndCameraPermission()) {
                    takeScreenshot()
                } else {
                    requestStorageAndCameraPermission()
                }
            }

            R.id.clBottom -> {
                requireActivity().openLink(Constants.ORDER_PROFILE_QR)
            }
        }
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


    private fun requestStorageAndCameraPermission() {
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
                takeScreenshot()
            }
        }

    private fun getQRCode(): Bitmap? {
        val multiFormatWriter = MultiFormatWriter()
        try {
            val bitMatrix = multiFormatWriter.encode(qr_code, BarcodeFormat.QR_CODE, 1000, 1000)
            val barcodeEncoder = BarcodeEncoder()
            return barcodeEncoder.createBitmap(bitMatrix)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return null
        }
    }

}