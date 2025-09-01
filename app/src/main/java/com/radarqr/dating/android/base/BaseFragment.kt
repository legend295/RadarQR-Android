package com.radarqr.dating.android.base


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.LayoutRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.radarqr.dating.android.R
import com.radarqr.dating.android.data.model.report.Data
import com.radarqr.dating.android.data.model.report.SubOption
import com.radarqr.dating.android.data.model.report.SubSuboption
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.handler.DialogClickHandler
import com.radarqr.dating.android.utility.handler.LocationPermissionHandler
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import org.json.JSONObject
import org.koin.android.ext.android.inject


abstract class BaseFragment<DB : ViewDataBinding> : Fragment() {

    open lateinit var binding: DB
    var builder: AlertDialog.Builder? = null
    var builderTemp: AlertDialog? = null
    var builderProcessing: AlertDialog.Builder? = null
    var builderTempProcessing: AlertDialog? = null

    //    val getProfileViewModel: GetProfileViewModel by viewModel()
    var window: Window? = null
    private val preferencesHelper: PreferencesHelper by inject()
    private val mixPanelWrapper: MixPanelWrapper by inject()

    // private lateinit var activityRef: WeakReference<InitialActivity>
//    private lateinit var activityRefHome: WeakReference<HomeActivity>

    @LayoutRes
    abstract fun getLayoutRes(): Int

    private fun init(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) {
        binding = DataBindingUtil.inflate(inflater, getLayoutRes(), container, false)
        window = getBaseActivity()?.window
    }

    override fun onResume() {
        super.onResume()
        mixPanelWrapper.logEvent("screen visit", JSONObject().apply {
            val name = if (this@BaseFragment::class.java.simpleName.contains(
                    "Fragment",
                    ignoreCase = true
                )
            ) this@BaseFragment::class.java.simpleName.replace(
                "Fragment",
                ""
            ) else this@BaseFragment::class.java.simpleName
            put(MixPanelWrapper.PropertiesKey.FROM_SCREEN, name)
        })
    }

    open fun getBaseActivity(): BaseActivity? {
        val activity = activity
        if (activity is BaseActivity) {
            return activity
        }
        return null
    }

//    fun getProfileViewModel(): GetProfileViewModel = getProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        init(inflater, container)
        super.onCreateView(inflater, container, savedInstanceState)
        builder = AlertDialog.Builder(activity)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    fun showAlertWithClose(
        message: String,
        title: String,
        okDismissClick: ((DialogInterface, Int) -> Unit?)?
    ) {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(
            ContextThemeWrapper(
                activity,
                R.style.myDialog
            )
        )
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)

        okDismissClick.let {
            alertDialog.setNeutralButton(
                "Ok",
                DialogInterface.OnClickListener(okDismissClick as (DialogInterface, Int) -> Unit)
            )
        }
        alertDialog.show()
    }

    fun showToolbar(isVisible: Boolean) {
        when (activity) {
            is InitialActivity -> {
                (activity as InitialActivity).showToolbar(isVisible)
            }

            is HomeActivity -> {

                (activity as HomeActivity?)?.showToolbar(isVisible)
            }
        }
    }

    fun setTitleBackground(isVisible: Boolean) {
        when (activity) {
            is InitialActivity -> {
                (activity as InitialActivity).setTitleBackground(isVisible)
            }

            is HomeActivity -> {

                (activity as HomeActivity?)?.setTitleBackground(isVisible)
            }
        }
    }

    fun setTitle(text: String) {
        when (activity) {
            is InitialActivity -> {
                (activity as InitialActivity).setTitle(text)
            }

            is HomeActivity -> {
                (activity as HomeActivity?)?.setTitle(text)
            }
        }
    }

    fun showToolbarLayout(isVisible: Boolean) {
        when (activity) {
            is InitialActivity -> {
                (activity as InitialActivity).showToolbarLayout(isVisible)
            }

            is HomeActivity -> {
                (activity as HomeActivity).showToolbarLayout(isVisible)
            }
        }
    }

    fun showProgress(isVisible: Boolean) {
        when (activity) {
            is InitialActivity -> {
                (activity as InitialActivity).showProgress(isVisible)
            }

            is HomeActivity -> {
                (activity as HomeActivity).showProgress(isVisible)
            }
        }
    }

    fun setProgress(number: Int) {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).setProgressbar(number)
            }

            is InitialActivity -> {
                (activity as InitialActivity).setProgressbar(number)
            }
        }
    }

    fun showToolbarWhite(isVisible: Boolean) {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).showToolbarWhite(isVisible)
            }

            is InitialActivity -> {
                (activity as InitialActivity).showToolbarWhite(isVisible)
            }
        }
    }


    fun showAlertConfirmationDialog(
        title: String,
        yesDismissClick: ((DialogInterface, Int) -> Unit?)?
    ) {
        dismissAlert()
        builder
            ?.setTitle(title)
            ?.setPositiveButton(
                "Cancel"
            ) { dialog, _ -> dialog.cancel() }

            ?.setNegativeButton(
                "YES",
                DialogInterface.OnClickListener(yesDismissClick as (DialogInterface, Int) -> Unit)
            )

        builderTemp = (builder as AlertDialog.Builder).create()
        builderTemp?.setCanceledOnTouchOutside(false)
        builderTemp?.setCancelable(false)
        builderTemp?.show()
    }

    fun showErrorAlert(title: String, message: String) {
        dismissAlert()
        builder?.setMessage(message)
            ?.setTitle(title)
            ?.setNeutralButton("OK",
                { dialog, which -> dialog.cancel() })
        builderTemp = (builder as AlertDialog.Builder).create()
        builderTemp?.setCanceledOnTouchOutside(false)
        builderTemp?.setCancelable(false)
        builderTemp?.show()
    }


    fun dismissAlert() {
        builderTemp?.dismiss()
    }

    fun enableUserInteraction(enable: Boolean) {
        val activity: Activity? = activity
        if (enable) {
            if (activity != null) {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }
        } else
            if (activity != null)
                requireActivity().window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
    }

    fun setActionBar(title: String) {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).setActionBar(title)
            }

            is InitialActivity -> {
                (activity as InitialActivity).setActionBar(title)
            }
        }
    }

    fun showBackButton(visibility: Boolean) {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).showBackButton(visibility)
            }

            is InitialActivity -> {
                (activity as InitialActivity).showBackButton(visibility)
            }

        }
    }

    fun showSkip(visibility: Boolean) {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).showSkip(visibility)
            }

            is InitialActivity -> {
                (activity as InitialActivity).showSkip(visibility)
            }

        }
    }

    fun showBackButtonWhite(visibility: Boolean) {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).showBackButton(visibility)
            }

            is InitialActivity -> {
                (activity as InitialActivity).showBackButtonWhite(visibility)
            }

        }
    }

    fun showTitleWhite(visibility: Boolean) {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).showTitle(visibility)
            }

            is InitialActivity -> {
                (activity as InitialActivity).showTitleWhite(visibility)
            }

        }
    }

    fun showTitle(visibility: Boolean) {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).showTitle(visibility)
            }

            is InitialActivity -> {
                (activity as InitialActivity).showTitle(visibility)
            }

        }
    }

    fun changeBackButtonColor(boolean: Boolean) {
        when (activity) {
//            is InitialActivity -> {
//                if (boolean) {
////                    (activity as InitialActivity).layout_toolbar_title.setTextColor(
////                        ContextCompat.getColor(e.radardatingApp.appContext!!, R.color.pink)
////                    )
//                }
//                (activity as InitialActivity).activity_toolbar_back.setColorFilter(
//                    ContextCompat.getColor(e.radardatingApp.appContext!!, R.color.pink)
//                )
//            }
        }
    }


    fun setTitleBackground(codeCode: Int) {
        when (activity) {
//            is HomeActivity -> {
//                (activity as HomeActivity).setTitleBackground(codeCode)
//            }
//            is InitialActivity -> {
//                (activity as InitialActivity).setTitleBackground(codeCode)
//            }

        }
    }


    fun showActionBar(visibility: Boolean) {
        when (activity) {
//            is InitialActivity -> {
//                (activity as InitialActivity).showActionBar(visibility)
//            }
//            is HomeActivity -> {
//                (activity as HomeActivity).showActionBar(visibility)
//            }
        }
    }

    fun showProgressBar(visibility: Boolean) {
        when (activity) {
            is InitialActivity -> {
                hideKeyboard(requireView())
                (activity as InitialActivity).showProgressBar(visibility)

            }

            is HomeActivity -> {
                hideKeyboard(requireView())
                (activity as HomeActivity).showProgressBar(visibility)

            }
        }
    }

    fun showNavigation(visibility: Boolean) {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).showNavigation(visibility)

            }
        }
    }

    fun showNavigationPos(pos: Int) {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).showNavigationPos(pos)

            }
        }
    }


    fun showEditIcon(visibility: Boolean) {

        when (activity) {
//
//            is InitialActivity -> {
//                (activity as InitialActivity).showEditIcon(false)
//            }
//            is HomeActivity -> {
//
//                (activity as HomeActivity).showEditIcon(visibility)
//            }
        }
    }


    fun hideKeyboard(view: View) {
        when (activity) {
            is HomeActivity -> {
                val imm =
                    (activity as HomeActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

            is InitialActivity -> {

                val imm =
                    (activity as InitialActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    fun showKeyboard() {
        when (activity) {
//            is InitialActivity -> {
//                val imm =
//                    (activity as InitialActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//            }
//            is HomeActivity -> {
//
//                val imm =
//                    (activity as HomeActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
//            }
        }
    }

    fun onBackPress() {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).onBackPress()
            }

            is InitialActivity -> {
                (activity as InitialActivity).onBackPress()
            }
        }
    }

    fun reportApiError(
        lineNumber: Int,
        apiStatusCode: Int,
        apiName: String,
        className: String,
        errorMessage: String
    ) {
        /*   var userToken: String?
           runBlocking {
               userToken =
                   preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_AUTH).first()
           }*/
        FirebaseCrashlytics.getInstance().setCustomKeys {
            key("Line Number", lineNumber)
            key("Api Error Code", apiStatusCode)
            key("Api Name", apiName)
            key("Class Name", className)
            key("Error Message", errorMessage)
//            key("User Token", userToken ?: "")
        }
    }

    fun showCustomAlert(
        title: String,
        buttonText: String,
        clickListener: DialogClickHandler<Any>
    ) {
        getBaseActivity()?.showCustomAlert(title, buttonText, clickListener)
    }

    fun showReportDialog() {
        try {
            getBaseActivity()?.showReportDialog()
        } catch (e: Exception) {

        }
    }

    fun openReportDialog(clickListener: (Data, SubOption, SubSuboption, String) -> Unit) {
        try {
            getBaseActivity()?.openReportDialog(clickListener)
        } catch (e: Exception) {

        }
    }

    fun transparentStatusBar() {
        window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }
    }

    open fun showSystemUI() {
//        window?.apply {
//            clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR)
////            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
////            statusBarColor = Color.BLACK
//            statusBarColor = Color.WHITE
//        }
    }

    fun getLocation(isSuccess: (Boolean) -> Unit) {
        getBaseActivity()?.getLocation(isSuccess)
    }

    fun getUserCurrentLocation(isSuccess: (Boolean, LatLng?) -> Unit) {
        getBaseActivity()?.getUserCurrentLocation(isSuccess)
    }

    fun requestNotificationPermission() {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).requestNotificationPermission()
            }

            is InitialActivity -> {
                (activity as InitialActivity).requestNotificationPermission()
            }
        }
    }

    fun isLocationPermissionGranted(handler: LocationPermissionHandler) {
        when (activity) {
            is HomeActivity -> {
                (activity as HomeActivity).isLocationPermissionGranted(handler)
            }

            is InitialActivity -> {
                (activity as InitialActivity).isLocationPermissionGranted(handler)
            }
        }
    }

//    fun requestStorageAndCameraPermission(): Boolean {
//        return when (activity) {
//            is HomeActivity -> {
//                (activity as HomeActivity).requestStorageAndCameraPermission()
//            }
//            is InitialActivity -> {
//                (activity as InitialActivity).requestStorageAndCameraPermission()
//            }
//            else -> false
//        }
//    }


}
