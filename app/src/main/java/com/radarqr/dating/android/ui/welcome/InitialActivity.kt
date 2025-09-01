package com.radarqr.dating.android.ui.welcome

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseActivity
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.base.cameraInterface
import com.radarqr.dating.android.databinding.ActivityMainBinding
import com.radarqr.dating.android.ui.bottomSheet.OptionsBottomSheetFragment
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.ui.welcome.registerScreens.FirstNameFragment
import com.radarqr.dating.android.ui.welcome.registerScreens.image_list_signup
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.Utility.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject


val sharedPrefFile = "foundsharedpreference"
var action = 0

class InitialActivity : BaseActivity(), OptionsBottomSheetFragment.ItemClickListener,
    cameraInterface {
    lateinit var mListener: cameraInterface
    var step = 0
    var binding: ActivityMainBinding? = null
    private val preferencesHelper: PreferencesHelper by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mListener = this

        // Reset to false so that After login or new registration user will see add image dialog
        RaddarApp.isAddImageDialogShown = false
        RaddarApp.isBackConfirmationImageDialogShown = false

        binding?.tvSkip?.setOnClickListener {
            editProfile()
        }

        binding?.ivBack?.setOnClickListener {
            onBackPressed()
        }

        /*binding.ivBack.setOnClickListener {
            showBackButton(false)
            layout_toolbar_title.text = ""
            onBackPressed()

        }

        activity_toolbar_back.setOnClickListener {
            showBackButton(false)
            layout_toolbar_title.text = ""
            onBackPressed()
//            runBlocking(Dispatchers.IO) {
//                step = preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_STEP).first()!!
//            }
//            if(step>=1)
//            {
//                finishAffinity()
//            }
        }*/
//        clear()
    }

    fun hideShowWholeToolbar(isVisible: Boolean) {
        binding?.ivBack?.visible(isVisible)
        binding?.ivLogo?.visible(isVisible)
        binding?.tvSkip?.visible(isVisible)
    }

    fun hideShowSkip(isVisible: Boolean) {
        binding?.tvSkip?.visible(isVisible)
    }

    fun hideShowBack(isVisible: Boolean) {
        binding?.ivBack?.visible(isVisible)
    }

    private fun editProfile() {
        lifecycleScope.launch {
            getProfileViewModel.editProfile(EditProfileApiRequest(StepProgress = 6))
                .observe(this@InitialActivity) {
                    when (it) {
                        is DataResult.Loading -> {

                        }

                        is DataResult.Success -> {
                            findNavController(R.id.fragment_container).navigate(R.id.welcome_Fragment)
                        }

                        is DataResult.Failure -> {

                            CommonCode.setToast(
                                this@InitialActivity,
                                it.message ?: "Email Already Exist"
                            )

                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/edit-profile",
                                componentName.className,
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


    override fun setActionBar(title: String) {
//        layout_toolbar_title?.text = title
    }

    override fun onBackPressed() {
        super.onBackPressed()
//        showBackButton(false)
//        layout_toolbar_title.text = ""
        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)
        when {
            fragment?.childFragmentManager?.fragments?.get(0) is FirstNameFragment -> {
                startActivity(Intent(this, InitialActivity::class.java))
                finishAffinity()
            }
        }
        try {
            runBlocking(Dispatchers.IO) {
                step =
                    preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_STEP).first()
                        ?: 0
            }
            if (step >= 5) {
                finishAffinity()
            }
        } catch (e: Exception) {

        }

        try {
            image_list_signup.clear()
        } catch (e: Exception) {
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onItemClick(item: Int) {
        action = item
        mListener.cameraListener(action)
    }

    override fun cameraListener(value: Int) {

    }


}