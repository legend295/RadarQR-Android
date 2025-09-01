package com.radarqr.dating.android.ui.home.qrCode

import android.Manifest
import android.animation.Animator
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import com.airbnb.lottie.LottieAnimationView
import com.budiyev.android.codescanner.*
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.LayoutScanQrcodeBinding
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.handler.DialogClickHandler
import org.koin.androidx.viewmodel.ext.android.viewModel


class ScanQRFragment : BaseFragment<LayoutScanQrcodeBinding>() {
    override fun getLayoutRes() = R.layout.layout_scan_qrcode
    var Code = ""
    private lateinit var codeScanner: CodeScanner
    var alertDialog: AlertDialog? = null
    private var profileData: ProfileData? = null
    var hasScanned = false
    private val getProfileViewModel: GetProfileViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HomeActivity.activeFragment.value = this
        binding.scanQRFragment = this

//        setActionBar("Scan QR Code")


        codeScanner = CodeScanner(requireActivity(), binding.scannerView)
        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.CONTINUOUS // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not


        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }

        binding.tvCancel.setOnClickListener {
            this.view?.findNavController()?.popBackStack()
        }

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            requireActivity().runOnUiThread {
                Log.e("qr_code", it.text)

                val linkArray = it.text?.split("/")
                val userId: String = try {
                    linkArray?.get(4) ?: ""
                } catch (e: Exception) {
                    ""
                }
                if (userId == "") {
                    showBottomSheet()
                } else {
/*
                    var intent = Intent(requireActivity(), HomeActivity::class.java)
                    intent.putExtra("user_id", user_id)
                    intent.putExtra("tag", "2")
                    startActivity(intent)
//                    findNavController().popBackStack(R.id.otherProfile_Fragment,true)
                    requireActivity().finish()*/
                    /* val data = Bundle()
                     data.putString("user_id", userId)
                     data.putString("tag", "")
                     data.putString("category", Constants.IN_PERSON)
                     this.view?.findNavController()?.navigate(R.id.action_to_other_profile_fragment, data)
 */
                    if (!hasScanned) {
                        hasScanned = true
                        getProfile(userId)
                        radarDialog(userId)
                    }
                }

            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            requireActivity().runOnUiThread {
                Toast.makeText(
                    requireActivity(), "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }


    }


    override fun onResume() {
        super.onResume()
        startPreview()
        /*activity?.let {
            if (it is HomeActivity) {
//                it.removePadding()
                it.setHomeToolbarVisibility(isVisible = true)
                it.hideShowUserIcon(isVisible = true)
                it.binding.bottomNav.visible(isVisible = false)
            }
        }*/

    }

    override fun onStop() {
        super.onStop()
        /*activity?.let {
            if (it is HomeActivity) {
//                it.removePadding()
                it.setHomeToolbarVisibility(isVisible = false)
                it.hideShowUserIcon(isVisible = false)
                it.binding.bottomNav.visible(isVisible = false)
            }
        }*/
    }

    fun startPreview() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (requireActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    requireActivity(), arrayOf(Manifest.permission.CAMERA),
                    1
                )
                return
            } else {
                codeScanner.startPreview()
            }
        } else {
            codeScanner.startPreview()
        }
    }

    override fun onPause() {
        super.onPause()
        codeScanner.releaseResources()
    }

    private fun showBottomSheet() {
        codeScanner.releaseResources()

        showCustomAlert(
            "That's not a code we can scan\nTry scanning a Radar QR code",
            "Ok",
            object : DialogClickHandler<Any> {
                override fun onClick(value: Any) {
                    startPreview()
                }
            })
    }

    private fun getProfile(userId: String) {
        if (view != null && isAdded && userId.isNotEmpty()) {
            if (BaseUtils.isInternetAvailable()) {
                try {
                    getBaseActivity()?.getProfile(getProfileRequest(user_id = arrayListOf(userId))) { data, _ ->
                        data?.let {
                            profileData = it
                        }
                    }
                } catch (e: java.lang.Exception) {
                }
            } else CommonCode.setToast(
                requireContext(),
                resources.getString(R.string.no_internet_msg)
            )
        }
    }

    private fun radarDialog(userId: String) {
        val dialog = AlertDialog.Builder(requireActivity())

        val inflater = this.layoutInflater
        val view: View = inflater.inflate(R.layout.layout_scan_dialog, null)
        dialog.setView(view)
        val animationView: LottieAnimationView = view.findViewById(R.id.lottie_animation_view)

        animationView.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {

            }

            override fun onAnimationEnd(p0: Animator) {
                val bundle = Bundle().apply {
                    putString(Constants.USER_ID, userId)
                    putInt(Constants.FROM, ProfileFragment.FROM_SCAN)
                    putSerializable(Constants.EXTRA_DATA, profileData)
                    putBoolean(
                        Constants.TYPE,
                        userId != getProfileViewModel.profileData.value?._id
                    )
                }
                try {
                    this@ScanQRFragment.view?.findNavController()
                        ?.navigate(R.id.action_scanFragment_to_profileFragment, bundle)
                } catch (_: IllegalArgumentException) {
                }
                Handler(Looper.getMainLooper()).postDelayed({
                    alertDialog?.dismiss()
                }, 100)
            }

            override fun onAnimationCancel(p0: Animator) {

            }

            override fun onAnimationRepeat(p0: Animator) {

            }
        })

        alertDialog = dialog.create()
        alertDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.setCanceledOnTouchOutside(false)
        alertDialog?.show()


        Handler(Looper.getMainLooper()).postDelayed({
            animationView.cancelAnimation()
        }, 5000)


    }

}