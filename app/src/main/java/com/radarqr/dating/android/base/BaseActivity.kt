package com.radarqr.dating.android.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.AndroidRuntimeException
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.gowtham.library.utils.TrimVideo.activity
import com.quickblox.core.QBEntityCallback
import com.quickblox.core.exception.QBResponseException
import com.quickblox.sample.chat.kotlin.utils.showSnackbar
import com.radarqr.dating.android.R
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.report.Data
import com.radarqr.dating.android.data.model.report.SubOption
import com.radarqr.dating.android.data.model.report.SubSuboption
import com.radarqr.dating.android.databinding.DialogNoUserfoundBinding
import com.radarqr.dating.android.databinding.LayoutReportBinding
import com.radarqr.dating.android.databinding.LayoutReportDialogBinding
import com.radarqr.dating.android.ui.adapter.ReportPagerAdapter
import com.radarqr.dating.android.ui.adapter.ReportType
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.main.HomeActivity.Companion.userCurrentLocation
import com.radarqr.dating.android.ui.home.main.HomeViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.report.ReportFragment
import com.radarqr.dating.android.ui.welcome.mobileLogin.CurrentLocationUpdateRequest
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.SharedPrefsHelper
import com.radarqr.dating.android.utility.chat.ChatHelper
import com.radarqr.dating.android.utility.handler.DialogClickHandler
import com.radarqr.dating.android.utility.handler.LocationPermissionHandler
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.coroutines.resumeWithException


abstract class BaseActivity : BaseApiActivity(), BaseView {

    private val preferencesHelper: PreferencesHelper by inject()
    private var reportDialog: BottomSheetDialog? = null
    private var bindingReport: LayoutReportBinding? = null
    val getProfileViewModel: GetProfileViewModel by viewModel()
    private val homeViewModel: HomeViewModel by viewModel()
    private val mixPanelWrapper: MixPanelWrapper by inject()
    private var locationPermissionHandler: LocationPermissionHandler? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingReport = reportBinding()
//        transparentStatusBar()
    }

    fun transparentStatusBar() {
//        val w: Window = window
//        w.setFlags(
//            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
//            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
//        )

        window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }

//        w.decorView.apply {
//            // Hide both the navigation bar and the status bar.
//            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
//            // a general rule, you should design your app to hide the status bar whenever you
//            // hide the navigation bar.
//            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//        }
    }

//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        return if (keyCode == KeyEvent.KEYCODE_HOME) {
//            Log.i("TAG", "Press Home")
//            System.exit(0)
//            true
//        } else {
//            super.onKeyDown(keyCode, event);
//        }
//    }
//
//    override fun onAttachedToWindow() {
//        this.window.setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG)
//        super.onAttachedToWindow()
//    }

    private fun Activity.hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let {
                // Default behavior is that if navigation bar is hidden, the system will "steal" touches
                // and show it again upon user's touch. We just want the user to be able to show the
                // navigation bar by swipe, touches are handled by custom code -> change system bar behavior.
                // Alternative to deprecated SYSTEM_UI_FLAG_IMMERSIVE.
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                // make navigation bar translucent (alternative to deprecated
                // WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
                // - do this already in hideSystemUI() so that the bar
                // is translucent if user swipes it up
//                window.navigationBarColor = getColor(R.color.internal_black_semitransparent_light)
                // Finally, hide the system bars, alternative to View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                // and SYSTEM_UI_FLAG_FULLSCREEN.
                it.hide(WindowInsets.Type.navigationBars())
            }
        } else {
            // Enables regular immersive mode.
            // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
            // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    // Do not let system steal touches for showing the navigation bar
                    View.SYSTEM_UI_FLAG_IMMERSIVE
                            // Hide the nav bar and status bar
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            // Keep the app content behind the bars even if user swipes them up
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
            // make navbar translucent - do this already in hideSystemUI() so that the bar
            // is translucent if user swipes it up
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        }
    }

    open fun showSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
    }

    override fun setActionBar(title: String) {

    }

    override fun setTitleBackground(isSet: Boolean) {
        if (isSet) {
//            activity_main_top_bar_layout?.rl_toolbar?.setBackgroundColor(
//                ContextCompat.getColor(
//                    this,
//                    R.color.white
//                )
//            )
        }
    }

    override fun showBackButton(isVisible: Boolean) {
//        if (isVisible) {
//            activity_main_top_bar_layout?.activity_toolbar_back?.visibility = View.VISIBLE
//            activity_main_top_bar_layout?.layout_toolbar_title?.visibility = View.VISIBLE
//        } else {
//            activity_main_top_bar_layout?.activity_toolbar_back?.visibility = View.GONE
//            activity_main_top_bar_layout?.layout_toolbar_title?.visibility = View.VISIBLE
//        }
    }

    override fun showBackButtonWhite(isVisible: Boolean) {
//        if (isVisible) {
//            activity_main_top_bar_layout?.activity_toolbar_back_white?.visibility = View.VISIBLE
//            activity_main_top_bar_layout?.layout_toolbar_title_white?.visibility = View.VISIBLE
//        } else {
//            activity_main_top_bar_layout?.activity_toolbar_back_white?.visibility = View.GONE
//            activity_main_top_bar_layout?.layout_toolbar_title_white?.visibility = View.VISIBLE
//        }
    }

    override fun showTitle(isVisible: Boolean) {
//        if (isVisible) {
//            activity_main_top_bar_layout?.layout_toolbar_title?.visibility = View.VISIBLE
//        } else {
//
//            activity_main_top_bar_layout?.layout_toolbar_title?.visibility = View.GONE
//        }
    }

    override fun setTitle(text_title: String) {

//        activity_main_top_bar_layout?.layout_toolbar_title?.text = text_title

    }

    override fun showProgress(isVisible: Boolean) {
//        if (isVisible) {
//            activity_main_top_bar_layout?.progress_bar?.visibility = View.VISIBLE
//        } else {
//
//            activity_main_top_bar_layout?.progress_bar?.visibility = View.GONE
//        }
    }

    override fun showNavigationPos(pos: Int) {
//        bottom_nav.setSelectedItemId(R.id.scan_main_fragment)
    }

    override fun showImageDialog(action: Int) {

    }

    override fun showSkip(isVisible: Boolean) {
//        if (isVisible) {
//            activity_main_top_bar_layout?.layout_skip?.visibility = View.VISIBLE
//        } else {
//
//            activity_main_top_bar_layout?.layout_skip?.visibility = View.GONE
//        }
    }

    override fun setProgressbar(number: Int) {
//        activity_main_top_bar_layout?.progress_bar?.setProgress(number)
    }

    override fun showTitleWhite(isVisible: Boolean) {
//        if (isVisible) {
//            activity_main_top_bar_layout?.layout_toolbar_title_white?.visibility = View.VISIBLE
//        } else {
//
//            activity_main_top_bar_layout?.layout_toolbar_title_white?.visibility = View.GONE
//        }
    }

    override fun showActionBar(isVisible: Boolean) {
        if (isVisible)
            supportActionBar?.show()
        else
            supportActionBar?.hide()
    }

    override fun showToolbar(isVisible: Boolean) {
//        if (isVisible) {
//            activity_main_top_bar_layout?.rl_toolbar?.visibility = View.VISIBLE
//        } else {
//            activity_main_top_bar_layout?.rl_toolbar?.visibility = View.GONE
//        }
    }

    override fun showToolbarLayout(isVisible: Boolean) {
//        if (isVisible) {
//            activity_main_top_bar_layout?.visibility = View.VISIBLE
//        } else {
//
//            activity_main_top_bar_layout?.visibility = View.GONE
//        }
    }

    override fun showToolbarWhite(isVisible: Boolean) {
//        if (isVisible) {
//            activity_main_top_bar_layout?.rl_toolbar_white?.visibility = View.VISIBLE
//        } else {
//
//            activity_main_top_bar_layout?.rl_toolbar_white?.visibility = View.GONE
//        }
    }

    override fun showAlert(
        message: String,
        title: String,
        okDismissClick: ((DialogInterface, Int) -> Unit?)?
    ) {

    }

    override fun showAlertWithView(view: View?): AlertDialog? {
        val alertDialogBuilder = AlertDialog.Builder(this)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.setView(view)
        alertDialog.show()
        return alertDialog
    }


    fun showProgressBar(isVisible: Boolean) {
        if (isVisible) {
//            progress_bar_frame!!.visibility = View.VISIBLE
            enableUserInteraction(false)
        } else {
//            progress_bar_frame!!.visibility = View.GONE
            enableUserInteraction(true)
        }

    }

    fun enableUserInteraction(enable: Boolean) {
        if (enable) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        } else {

            window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        }
    }

    override fun showConfirmatonAlert(
        message: String,
        title: String,
        yesDismissClick: ((DialogInterface, Int) -> Unit?)?,
        noDismissClick: ((DialogInterface, Int) -> Unit?)?
    ) {

    }

    private val TAG = BaseActivity::class.java.simpleName
    private var progressDialog: ProgressDialog? = null

    override fun onPause() {
        super.onPause()
        hideProgressDialog()
        dismissDialog()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    protected fun showErrorSnackbar(
        @StringRes resId: Int,
        e: Exception?,
        clickListener: View.OnClickListener?
    ) {
        val rootView = window.decorView.findViewById<View>(android.R.id.content)
        rootView?.let {
            showSnackbar(it, resId, e, R.string.dlg_retry, clickListener)
        }
    }

    protected fun showProgressDialog(@StringRes messageId: Int) {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(this)
            progressDialog!!.isIndeterminate = true
            progressDialog!!.setCancelable(false)
            progressDialog!!.setCanceledOnTouchOutside(false)

            // Disable the back button
            val keyListener = DialogInterface.OnKeyListener { dialog,
                                                              keyCode,
                                                              event ->
                keyCode == KeyEvent.KEYCODE_BACK
            }
            progressDialog!!.setOnKeyListener(keyListener)
        }
        progressDialog!!.setMessage(getString(messageId))
        try {
            progressDialog!!.show()
        } catch (e: Exception) {
            e.message?.let { Log.d(TAG, it) }
        }
    }

    protected fun hideProgressDialog() {
        progressDialog?.let {
            if (it.isShowing) {
                it.dismiss()
            }
        }
    }

    protected fun isProgresDialogShowing(): Boolean {
        if (progressDialog != null && progressDialog?.isShowing != null) {
            return progressDialog!!.isShowing
        } else {
            return false
        }
    }

    override fun onResume() {
        super.onResume()
        val currentUser = ChatHelper.getCurrentUser()
        hideNotifications()
        /* if (currentUser != null && !QBChatService.getInstance().isLoggedIn) {
             Log.d(TAG, "Resuming with Relogin")
             ChatHelper.login(SharedPrefsHelper.getQbUser()!!, object : QBEntityCallback<QBUser> {
                 override fun onSuccess(qbUser: QBUser?, b: Bundle?) {
                     Log.d(TAG, "Relogin Successful")
                     reloginToChat()
                 }

                 override fun onError(e: QBResponseException?) {
                     e?.message?.let { Log.d(TAG, it) }
                 }
             })

         } else {
             Log.d(TAG, "Resuming without Relogin to Chat")
             onResumeFinished()
         }*/
    }

    private fun reloginToChat() {
        ChatHelper.loginToChat(SharedPrefsHelper.getQbUser()!!, object : QBEntityCallback<Void> {
            override fun onSuccess(aVoid: Void?, bundle: Bundle?) {
                Log.d(TAG, "Relogin to Chat Successful")
                onResumeFinished()
            }

            override fun onError(e: QBResponseException?) {
                Log.d(TAG, "Relogin to Chat Error: " + e?.message)
                onResumeFinished()
            }
        })
    }

    private fun hideNotifications() {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    open fun onResumeFinished() {
        // Need to Override onResumeFinished() method in nested classes if we need to handle returning from background in Activity
    }

    override fun onBackPress() {
        onBackPressed()
    }

    override fun reportApiError(
        lineNumber: Int,
        apiStatusCode: Int,
        apiName: String,
        className: String,
        errorMessage: String
    ) {
//        var userToken: String?
//        runBlocking {
//            userToken =
//                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_AUTH).first()
//        }
        FirebaseCrashlytics.getInstance().setCustomKeys {
            key("Line Number", lineNumber)
            key("Api Error Code", apiStatusCode)
            key("Api Name", apiName)
            key("Class Name", className)
            key("Error Message", errorMessage)
//            key("User Token", userToken ?: "")
        }
    }

    override fun showCustomAlert(
        title: String,
        buttonText: String,
        clickListener: DialogClickHandler<Any>
    ) {
        val dialog = Dialog(this, R.style.AlertStyle)
        val dialogBinding = DataBindingUtil.inflate<DialogNoUserfoundBinding>(
            LayoutInflater.from(this),
            R.layout.dialog_no_userfound,
            null,
            false
        )
        dialogBinding.header = title
        dialogBinding.buttonMessage = buttonText
        dialogBinding.clContinue.setOnClickListener {
            dialog.dismiss()
            clickListener.onClick(value = true)
        }
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun showReportDialog() {
        val dialog = Dialog(this, R.style.AlertStyle)
        val dialogBinding = DataBindingUtil.inflate<LayoutReportDialogBinding>(
            LayoutInflater.from(this),
            R.layout.layout_report_dialog,
            null,
            false
        )
        dialogBinding.tvOkay.setOnClickListener {
            dialog.dismiss()
        }
        dialogBinding.tvEmail.setOnClickListener {
            dialog.dismiss()
            openEmail()
        }
        dialog.setContentView(dialogBinding.root)
        dialog.setCancelable(false)
        dialog.show()
//        finishAffinity()
    }

    private fun openEmail() {

        if (BaseUtils.isPackageInstalled("com.google.android.gm", packageManager)) {
            try {
                val intent = Intent(Intent.ACTION_SEND)
                val recipients = arrayOf(Constants.SUPPORT_EMAIL)
                intent.putExtra(Intent.EXTRA_EMAIL, recipients)
//                intent.putExtra(Intent.EXTRA_SUBJECT, "Query")
//                intent.putExtra(Intent.EXTRA_TEXT, "Enter Your Query...")
                intent.type = "text/html"
                intent.setPackage("com.google.android.gm")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(Intent.createChooser(intent, "Send mail"))
            } catch (e: AndroidRuntimeException) {
                e.printStackTrace()
            }
        } else CommonCode.setToast(
            this,
            "You don't have any application to use this feature."
        )
    }

    override fun openReportDialog(clickListener: (Data, SubOption, SubSuboption, String) -> Unit) {
        if (reportDialog != null) {
            dismissDialog()
        }
        bindingReport = reportBinding()
        reportDialog = BottomSheetDialog(this, R.style.DialogStyle)
        reportDialog?.setContentView(bindingReport?.root!!)
        reportDialog?.behavior?.skipCollapsed = true
        reportDialog?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
        reportDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        reportDialog?.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        reportDialog?.window?.setDimAmount(0.4f)
        reportDialog?.window?.statusBarColor = Color.TRANSPARENT
        reportDialog?.show()

        getProfileViewModel.reportData?.let { reportData ->
            val reportAdapter = ReportPagerAdapter(this)

            val menuFragment = ReportFragment<Data>(ReportType.MENU)
            val subMenu = ReportFragment<SubOption>(ReportType.SUB_MENU)
            val child = ReportFragment<SubSuboption>(ReportType.CHILD)
            val edit = ReportFragment<Any>(ReportType.EDIT)

            reportAdapter.addFragment(menuFragment)
            reportAdapter.addFragment(subMenu)
            reportAdapter.addFragment(child)
            reportAdapter.addFragment(edit)

            var menuData = Data()
            var subMenuData = SubOption()
            var childData = SubSuboption()
            var header = ""
            var screenObserver = 0

            val pageChangeListener = object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    bindingReport?.apply {
                        ivBack.visibility = if (position == 0) View.GONE else View.VISIBLE
                        tvReport.visibility = if (position == 0) View.VISIBLE else View.GONE
                        tvPrivate.visibility = if (position == 0) View.VISIBLE else View.GONE

                        val fragment = reportAdapter.getFragments()[position]
                        fragment.view?.let {
                            updatePagerHeightForChild(it, vpReport)
                        }
                    }
                }


                fun updatePagerHeightForChild(view: View, pager: ViewPager2) {
                    view.post {
                        val widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                            view.width, View.MeasureSpec.EXACTLY
                        )
                        val heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(
                            0, View.MeasureSpec.UNSPECIFIED
                        )
                        view.measure(widthMeasureSpec, heightMeasureSpec)
                        if (pager.layoutParams.height != view.measuredHeight) {
                            pager.layoutParams = (pager.layoutParams).also {
                                it.height = view.measuredHeight
                            }
                        }
                    }
                }
            }



            bindingReport?.apply {
                vpReport.adapter = reportAdapter
                vpReport.offscreenPageLimit = reportAdapter.getFragments().size
                vpReport.isUserInputEnabled = false
                vpReport.registerOnPageChangeCallback(pageChangeListener)

                viewHandler = object : ViewClickHandler {
                    override fun onClick(v: View) {
                        when (v.id) {
                            R.id.iv_back -> {
                                val imm =
                                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(v.windowToken, 0)
                                if (screenObserver >= 0) {
                                    screenObserver -= 1
                                } else screenObserver = 0

                                bindingReport?.vpReport?.setCurrentItem(screenObserver, true)
                            }

                            R.id.iv_close -> {
                                dismissDialog()
                            }
                        }
                    }
                }

            }

            menuFragment.clickHandler = { position, reportType, data ->
                if (reportType == ReportType.MENU) {
                    val value = data[position]
                    if (value.type == Constants.LIST && value.sub_options.isNotEmpty()) {
                        header = value.option
                        menuData = value
                        screenObserver += 1
                        bindingReport?.vpReport?.setCurrentItem(1, true)
                        getProfileViewModel.reportAdapterObserver.value =
                            Pair(true, ReportType.SUB_MENU)
                    } else {
                        dismissDialog()
                        clickListener(value, subMenuData, childData, "")
                    }
                }
            }


            subMenu.clickHandler = { position, reportType, data ->
                if (reportType == ReportType.SUB_MENU) {
                    val subValue = data[position]
                    subMenuData = subValue
                    header = subValue.value
                    if (subValue.type == Constants.LIST && subValue.sub_suboptions.isNotEmpty()) {
                        screenObserver += 1
                        bindingReport?.vpReport?.setCurrentItem(2, true)
                        getProfileViewModel.reportAdapterObserver.value =
                            Pair(true, ReportType.CHILD)
                    } else {
                        screenObserver += 1
                        bindingReport?.vpReport?.setCurrentItem(3, true)
                        getProfileViewModel.reportAdapterObserver.value =
                            Pair(true, ReportType.EDIT)
                    }
                }
            }


            child.clickHandler = { position, reportType, data ->
                if (reportType == ReportType.CHILD) {
                    val childVale = data[position]
                    childData = childVale
                    header = childVale.value
                    if (childVale.type == Constants.EDIT_TEXT) {
                        screenObserver += 1
                        bindingReport?.vpReport?.setCurrentItem(3, true)
                        getProfileViewModel.reportAdapterObserver.value =
                            Pair(true, ReportType.EDIT)
                    } else {
                        dismissDialog()
                        clickListener(menuData, subMenuData, childData, "")
                    }
                }
            }

            edit.submitClickHandler = object : DialogClickHandler<Pair<String, View>> {
                override fun onClick(value: Pair<String, View>) {
                    dismissDialog()
                    clickListener(menuData, subMenuData, childData, value.first)
                }
            }

            edit.clickHandler = { position, reportType, data ->

            }

            getProfileViewModel.reportAdapterObserver.observe(this) {
                if (it.first) {
                    when (it.second) {
                        ReportType.MENU -> {
                            menuFragment.setList(reportData.data, "")
                        }

                        ReportType.SUB_MENU -> {
                            subMenu.setList(menuData.sub_options, menuData.option)
                        }

                        ReportType.CHILD -> {
                            child.setList(subMenuData.sub_suboptions, subMenuData.value)
                        }

                        ReportType.EDIT -> {
                            edit.setList(ArrayList(), header)
                        }
                    }
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        dismissDialog()
    }


    private fun dismissDialog() {
        reportDialog?.apply {
            dismissWithAnimation = true
            cancel()
        }
        reportDialog = null
    }

    private fun reportBinding(): LayoutReportBinding = DataBindingUtil.inflate(
        LayoutInflater.from(this),
        R.layout.layout_report,
        null,
        false
    )


    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    @SuppressLint("MissingPermission")
    fun getLocation(isSuccess: (Boolean) -> Unit) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation(isSuccess)
        val task = mFusedLocationClient.lastLocation
        task.addOnSuccessListener { location ->
            location ?: return@addOnSuccessListener
            HomeActivity.location = location
            val latLng = LatLng(location.latitude, location.longitude)
            HomeActivity.userLocation = latLng
            SharedPrefsHelper.saveLastLocation(latLng)
            isSuccess(true)
            latLng.updateCurrentLocation()
        }
    }

    @SuppressLint("MissingPermission")
    fun getUserCurrentLocation(isSuccess: (Boolean, LatLng?) -> Unit) {
        val locationClient = LocationServices.getFusedLocationProviderClient(this)
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                val cts = CancellationTokenSource()
                locationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                    .addOnSuccessListener {
                        it?.let {
                            val latLng = LatLng(it.latitude, it.longitude)
                            userCurrentLocation = latLng
                            mixPanelWrapper.updateUserPropertyOverMixpanel(JSONObject().apply {
                                put(MixPanelWrapper.PropertiesKey.USER_LOCATION, userCurrentLocation)
                            })
                            isSuccess(true, latLng)
                        } ?: run {
                            isSuccess(false, null)
                        }
                    }
                    .addOnFailureListener {
                        locationClient.lastLocation.addOnSuccessListener {
                            it?.let {
                                val latLng = LatLng(it.latitude, it.longitude)
                                userCurrentLocation = latLng
                                mixPanelWrapper.updateUserPropertyOverMixpanel(JSONObject().apply {
                                    put(MixPanelWrapper.PropertiesKey.USER_LOCATION, userCurrentLocation)
                                })
                                isSuccess(true, latLng)
                            } ?: run {
                                isSuccess(false, null)
                            }
                        }
                    }

            } else {
                isSuccess(false, null)
//                showToast(getString(R.string.enable_gps_msg))
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun FusedLocationProviderClient.awaitCurrentLocation(priority: Int): Location? {
        return suspendCancellableCoroutine {
            // to use for request cancellation upon coroutine cancellation
            val cts = CancellationTokenSource()
            getCurrentLocation(priority, cts.token)
                .addOnSuccessListener { location ->
                    // remember location is nullable, this happens sometimes
                    // when the request expires before an update is acquired
                    it.resume(location) { e ->
                        it.resumeWithException(e)
                    }
                }.addOnFailureListener { e ->
                    it.resumeWithException(e)
                }

            it.invokeOnCancellation {
                cts.cancel()
            }
        }
    }

    private fun LatLng.updateCurrentLocation() {
        homeViewModel.updateCurrentLocation(
            CurrentLocationUpdateRequest(
                latitude.toString(),
                longitude.toString()
            )
        ).observe(this@BaseActivity) {
            when (it) {
                DataResult.Empty -> {}
                is DataResult.Failure -> {
                    println("error ${it.message}")
                }

                DataResult.Loading -> {}
                is DataResult.Success -> {
                    println("success ${it.data.message}")
                }
            }
        }
    }


    @SuppressLint("MissingPermission")
    private fun getLastLocation(isSuccess: (Boolean) -> Unit) {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient =
                    LocationServices.getFusedLocationProviderClient(this)
                val cts = CancellationTokenSource()
                mFusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    cts.token
                ).addOnSuccessListener { location ->
                    location?.let {
                        val latLng = LatLng(it.latitude, it.longitude)
                        userCurrentLocation = latLng
                        mixPanelWrapper.updateUserPropertyOverMixpanel(JSONObject().apply {
                            put(MixPanelWrapper.PropertiesKey.USER_LOCATION, userCurrentLocation)
                        })
                    }
                }
                mFusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData(isSuccess)
                    } else {
                        HomeActivity.location = location
                        val latLng = LatLng(location.latitude, location.longitude)
                        HomeActivity.userLocation = latLng
                        SharedPrefsHelper.saveLastLocation(latLng)
                        isSuccess(true)
                        latLng.updateCurrentLocation()
                    }
                }
            } else {
                isSuccess(false)
//                showToast(getString(R.string.enable_gps_msg))
            }
        } else {
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }


    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val list = ArrayList<Boolean>()
            permissions.forEach { actionMap ->
                if (actionMap.value) list.add(actionMap.value)
            }

            if (list.size == 2) {
                getLocation {}
                getUserCurrentLocation { _, _ -> }
            }
        }


    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(isSuccess: (Boolean) -> Unit) {
        val locationRequest = LocationRequest.create().apply {
            interval = 0
            fastestInterval = 0
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            numUpdates = 1
        }
        /* val mLocationRequest = LocationRequest()
         mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
         mLocationRequest.interval = 0
         mLocationRequest.fastestInterval = 0
         mLocationRequest.numUpdates = 1*/

        mFusedLocationClient =
            LocationServices.getFusedLocationProviderClient(this)
        Looper.myLooper()?.let {
            mFusedLocationClient.requestLocationUpdates(
                locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        val mLastLocation: Location? = locationResult.lastLocation
                        HomeActivity.location = mLastLocation
                        mLastLocation?.let {
                            val latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
                            HomeActivity.userLocation = latLng
                            SharedPrefsHelper.saveLastLocation(latLng)
                            isSuccess(true)
                            latLng.updateCurrentLocation()
                        }
                    }
                },
                it
            )
        }
    }

    /*private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation
            HomeActivity.location = mLastLocation
            val latLng = LatLng(mLastLocation.latitude, mLastLocation.longitude)
            HomeActivity.userLocation = latLng
            SharedPrefsHelper.saveLastLocation(latLng)
            latLng.updateCurrentLocation()
        }
    }*/

    override fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            hasNotificationPermissionGranted = true
        }
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            hasNotificationPermissionGranted = isGranted
            if (!isGranted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                            showNotificationPermissionRationale()
                        } else {
                            showSettingDialog()
                        }
                    }
                }
            }
        }

    var hasNotificationPermissionGranted = false

    private fun showNotificationPermissionRationale() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Alert")
            .setMessage("Notification permission is required, to show notification")
            .setPositiveButton("Ok") { _, _ ->
                if (Build.VERSION.SDK_INT >= 33) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showSettingDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Notification Permission")
            .setMessage("Notification permission is required, Please allow notification permission from setting")
            .setPositiveButton("Ok") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun isLocationPermissionGranted(handler: LocationPermissionHandler) {
        this.locationPermissionHandler = handler
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionHandler?.onPermissionGranted(isPermissionGranted = true)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val list = ArrayList<Boolean>()
            permissions.forEach { actionMap ->
                if (actionMap.value) list.add(actionMap.value)
            }
            locationPermissionHandler?.onPermissionGranted(isPermissionGranted = list.size == 2)
        }


}