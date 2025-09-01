package com.radarqr.dating.android.ui.home.main

import android.app.Application
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.Location
import android.media.AudioAttributes
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.NotificationCompat
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.quickblox.users.model.QBUser
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseActivity
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.base.HomeBaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.constant.Constants.DIALOG_ID
import com.radarqr.dating.android.constant.Constants.VENUE_ID
import com.radarqr.dating.android.databinding.ActivityHomeBinding
import com.radarqr.dating.android.databinding.LayoutIntroductionClickableAreaBinding
import com.radarqr.dating.android.databinding.LayoutIntroductionFingerBinding
import com.radarqr.dating.android.databinding.LayoutIntroductionTextBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.hotspots.createvenue.VenueUpdateFragment
import com.radarqr.dating.android.hotspots.helpers.addThreeImageDialog
import com.radarqr.dating.android.hotspots.map.HotspotsMapFragment
import com.radarqr.dating.android.hotspots.roamingtimer.RoamingTimerFragment
import com.radarqr.dating.android.hotspots.tag.SearchCloseFriendFragment
import com.radarqr.dating.android.hotspots.venue.MyVenuesFragment
import com.radarqr.dating.android.hotspots.venuealbum.VenueAlbumFragment
import com.radarqr.dating.android.hotspots.venuedetail.VenueDetailsFragment
import com.radarqr.dating.android.hotspots.venuedetailsingles.VenueDetailSinglesFragment
import com.radarqr.dating.android.subscription.SubscriptionWrapper.loginRCAppUserId
import com.radarqr.dating.android.subscription.SubscriptionWrapper.setPushToken
import com.radarqr.dating.android.ui.home.chat.ChatUserFragment
import com.radarqr.dating.android.ui.home.likenew.LikeFragment
import com.radarqr.dating.android.ui.home.likes.model.LikesViewModel
import com.radarqr.dating.android.ui.home.main.model.GetRecommendationViewModel
import com.radarqr.dating.android.ui.home.main.recommended.RecommendedFragment
import com.radarqr.dating.android.ui.home.qrCode.ScanQRFragment
import com.radarqr.dating.android.ui.home.qrCode.ShowQRFragment
import com.radarqr.dating.android.ui.home.quickBlox.ChatFragment
import com.radarqr.dating.android.ui.home.quickBlox.ChatFragment.Companion.EXTRA_DIALOG_ID
import com.radarqr.dating.android.ui.home.quickBlox.ChatViewModel
import com.radarqr.dating.android.ui.home.request.RequestAlreadyExistFragment
import com.radarqr.dating.android.ui.home.settings.AccountDetailsFragment
import com.radarqr.dating.android.ui.home.settings.CommonFragment
import com.radarqr.dating.android.ui.home.settings.EditProfileFragment
import com.radarqr.dating.android.ui.home.settings.LocationFragment
import com.radarqr.dating.android.ui.home.settings.UpdateUsernameFragment
import com.radarqr.dating.android.ui.home.settings.profile.ProfileFragment
import com.radarqr.dating.android.ui.home.settings.web.WebViewFragment
import com.radarqr.dating.android.ui.location.EnableLocationPermissionFragment
import com.radarqr.dating.android.ui.subscription.SubscriptionFragment
import com.radarqr.dating.android.ui.welcome.mobileLogin.AccountSettingsRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.getProfileRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.updateTokenRequest
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.BaseUtils.checkTokenTimeStampExpiredOrNot
import com.radarqr.dating.android.utility.BaseUtils.getLatLngFromProfile
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.PreferencesHelper.PreferencesKeys.KEY_IMAGE_URL
import com.radarqr.dating.android.utility.QuickBloxManager
import com.radarqr.dating.android.utility.SharedPrefsHelper
import com.radarqr.dating.android.utility.Utility.getImageUrl
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.Utility.preventMultipleClicks
import com.radarqr.dating.android.utility.Utility.showItsAMatchDialog
import com.radarqr.dating.android.utility.Utility.showToast
import com.radarqr.dating.android.utility.Utility.toPx
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.Utility.welcomeDialog
import com.radarqr.dating.android.utility.handler.IntroductionHandler
import com.radarqr.dating.android.utility.handler.LocationPermissionHandler
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.introduction.IntroductionScreenType
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeActivity : BaseActivity(), ViewClickHandler {

    var user_id = ""
    var navController: NavController? = null

    private val getUserRecommendationViewModel: GetRecommendationViewModel by viewModel()
    private val likesViewModel: LikesViewModel by viewModel()
    private val chatViewModel: ChatViewModel by viewModel()
    private val mixPanelWrapper: MixPanelWrapper by inject()
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    //    var bottomNavigationView: BottomNavigationView? = null
    private val preferencesHelper: PreferencesHelper by inject()
    var introductionHandler: IntroductionHandler? = null
    private var bottomNavType: BottomNavType = BottomNavType.RECOMMENDATION

    var listener: Helper? = null

    //    private val quickBloxManager: QuickBloxManager by inject()
    var quickBloxId = "0"
    var yourMoveCount = 0
    var mobile = ""
    var my_user_id = ""
    private var doWeNeedToShowImagePopup = false
    private var isFirstTimeDialogShowing = false

    lateinit var binding: ActivityHomeBinding

    // Views variable to show Introduction UI
    // remove theses views when any fragment changes
    private var view: LayoutIntroductionFingerBinding? = null
    private var viewText: LayoutIntroductionTextBinding? = null
    private var viewClickable: LayoutIntroductionClickableAreaBinding? = null
    private var isIntroViewVisible = false


//    fun callHomeChatViewModel(): ChatViewModel = chatViewModel

    companion object {
        var currentDestination: NavDestination? = null
        var userLocation: LatLng? = null
        var userCurrentLocation: LatLng? = null
        var location: Location? = null
        var userProfileLocation: LatLng? = null
        var qbUser: QBUser? = null
        var userMobileNumber = ""
        var userImageUrl = ""
        var loggedInUserId = ""
        var rcSubscriptionUserId: String? = ""
        var likeCount = 0
        var chatCount = 0
        var isPromo = false

        val activeFragment by lazy { MutableLiveData<Fragment>() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        binding.bottomNavType = bottomNavType
        firebaseAnalytics = Firebase.analytics
        binding.clickHandler = this
        doWeNeedToShowImagePopup = intent.getBooleanExtra("show_image_popup", false)
        getDataFromPreferences()
        handleFirstTimeLaunchCondition()

        lifecycleScope.launch {
            chatViewModel.getMatchesDialogsOnly { }
        }

        setIntroductionHandler()
        qbSignIn {}
        getLocation {}

        getAccountDetails()
        getProfile()

//        chatViewModel.mobile = mobile
//        chatViewModel.quickBloxId = quickBloxId
//        chatViewModel.yourMoveCount = yourMoveCount


        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment?)!!
        navController = navHostFragment.navController
//        bottomNavigationView = findViewById(R.id.bottom_nav)
//        NavigationUI.setupWithNavController(
//            (bottomNavigationView!!), navController!!
//        )


        activeFragment.observe(this) {
            hideShowFireIcon(it is HomeFragment)
//            removeHotspotMargin()
            when (it) {
                is VenueBaseFragment<*> -> {
                    if (it is HotspotsMapFragment) {
                        binding.bottomNav.bottomNav.visible(false)
                    } else
                        binding.bottomNav.bottomNav.visible(false)
                    showBackIcon(value = true)
                    binding.homeToolbar.ivInfo.visible(it is MyVenuesFragment || it is VenueUpdateFragment)
                    if (it is VenueDetailsFragment || it is VenueAlbumFragment || it is VenueDetailSinglesFragment || it is HotspotsMapFragment || it is RoamingTimerFragment || it is SearchCloseFriendFragment) {
                        binding.homeToolbar.clHomeToolbar.visible(isVisible = false)
                        removePadding()
                    } else {
                        addPadding()
                        binding.homeToolbar.clHomeToolbar.visible(isVisible = true)
                    }

                }

                is HomeBaseFragment<*> -> {
                    addPadding()
                    showBackIcon(value = false)
                    binding.homeToolbar.ivInfo.visible(isVisible = false)
                    binding.homeToolbar.clHomeToolbar.visible(isVisible = true)
                    binding.bottomNav.bottomNav.visible(true)
                    if (it is HomeFragment || it is LikeFragment || it is ChatUserFragment) {
                        hideShowUserIcon(isVisible = true)
                    } else {
                        hideShowUserIcon(isVisible = false)
                    }
                }

                !is HomeBaseFragment<*> -> {
                    showBackIcon(value = false)
                    hideShowUserIcon(isVisible = false)
                    binding.bottomNav.bottomNav.visible(isVisible = false)
                    when (it) {
                        is ProfileFragment, is ShowQRFragment, is SubscriptionFragment -> {
                            removePadding()
                            binding.homeToolbar.clHomeToolbar.visible(isVisible = false)
                        }

                        is ScanQRFragment -> {
                            addPadding()
                            hideShowUserIcon(isVisible = true)
                            binding.homeToolbar.clHomeToolbar.visible(isVisible = true)
                        }

                        is UpdateUsernameFragment -> {
                            showBackIcon(value = true)
                            binding.homeToolbar.clHomeToolbar.visible(isVisible = true)
                        }

                        /*is EnableLocationPermissionFragment->{
                            binding.homeToolbar.clHomeToolbar.visible(isVisible = false)
                        }*/

                        is ChatFragment, is EnableLocationPermissionFragment -> {
                            addPadding()
                            binding.homeToolbar.clHomeToolbar.visible(isVisible = false)
                        }

                        is WebViewFragment -> {
                            binding.homeToolbar.clHomeToolbar.visible(isVisible = false)
                        }


                        else -> {
                            addPadding()
                            binding.homeToolbar.clHomeToolbar.visible(isVisible = true)
                        }
                    }

                }
            }
        }

        navController?.addOnDestinationChangedListener { t, destination, v ->
            currentDestination = destination
            progressBarVisible(isVisible = false)
            destination.setBottomNavType()
            destination.checkForPauseProfile()
            if (isIntroViewVisible) removeIntroViews()
        }

        intent?.let {
            handleNotificationClick(intent)
        }

        binding.homeToolbar.activityToolbarBackWhite.setOnClickListener {
            navHostFragment.navController.popBackStack()
        }

        getReportOptions()

//        setMessageCount(yourMoveCount)
        Handler(Looper.getMainLooper()).postDelayed({
            getLikes()
        }, 100)

        if (checkTokenTimeStampExpiredOrNot()) {
            // if expired then get new token and update over server
            getFcmToken()
        }

//        chatLogin()

        val intent = intent
        try {
            if (SharedPrefsHelper[Constants.IS_DEEP_LINK, false]) {
                SharedPrefsHelper.save(Constants.IS_DEEP_LINK, false)
//                user_id = intent.getStringExtra("user_id")!!.toString()
                user_id = SharedPrefsHelper[Constants.SHARED_USER_ID, ""]
                if (user_id.isEmpty()) return
                val data = Bundle()
//                data.putString("user_id", user_id)
//                data.putString("tag", "")
//                data.putString("category", Constants.IN_PERSON)
                data.putString(Constants.USER_ID, user_id)
                data.putInt(Constants.FROM, ProfileFragment.FROM_SCAN)
                data.putBoolean(Constants.TYPE, true)
                (navController ?: findNavController(R.id.fragment_container)).navigate(
                    R.id.profileFragment, data
                )
            }

        } catch (e: Exception) {

        }

        /*tv_pause_unpause_profile.setOnClickListener {
            updateAccountSettings(AccountSettingsRequest(pause_profile = false)) { data ->
                data?.let {
                    binding.includePauseProfile.llPauseProfile.visibility = View.GONE
                    binding.fragmentContainer.visibility = View.VISIBLE
                    getProfileViewModel.data.value?.data?.pause_profile =
                        data.pause_profile ?: false
                }
            }
        }*/

        binding.homeToolbar.ivUser.setOnClickListener {
            navController?.navigate(R.id.editProfileFragment)
//            sendNotification()
        }

        binding.homeToolbar.ivFire.setOnClickListener {
            isLocationPermissionGranted(object : LocationPermissionHandler {
                override fun onPermissionGranted(isPermissionGranted: Boolean) {
                    if (isPermissionGranted) navController?.navigate(R.id.hotspotsFragment)
                    else {
                        navController?.navigate(R.id.enableLocationPermissionFragment)
                    }
                }

            })
        }

        requestNotificationPermission()


    }

    fun showNavigation(isVisible: Boolean) {
        binding.bottomNav.bottomNav.visible(isVisible)
    }

    private fun setIntroductionHandler() {
        introductionHandler = object : IntroductionHandler {
            override fun showIntroductoryUI(
                type: IntroductionScreenType,
                hasShown: Boolean,
                co_ordinates: Pair<Float, Float>,
                callBack: (isRedirecting: Boolean) -> Unit
            ) {
                if (hasShown) return

                // Finger view
                view = LayoutIntroductionFingerBinding.inflate(
                    LayoutInflater.from(this@HomeActivity), binding.clParent, false
                )
                // Text area view
                viewText = LayoutIntroductionTextBinding.inflate(
                    LayoutInflater.from(this@HomeActivity), binding.clParent, false
                )
                // Clickable area view
                viewClickable = LayoutIntroductionClickableAreaBinding.inflate(
                    LayoutInflater.from(this@HomeActivity), binding.clParent, false
                )


                view?.ivFinger?.rotation = type.rotation().toFloat()


                viewText?.tvTitle?.text = type.title()
                viewText?.tvDescription?.text = type.description()

                // set view x and y for the actual position of the object
                view?.root?.x = co_ordinates.first
                view?.root?.y = co_ordinates.second
                viewText?.root?.x = 0.0F
                viewClickable?.root?.x = (view?.root?.x?.minus((25).toPx) ?: 0) as Float
                viewClickable?.root?.y = (view?.root?.y?.minus((25).toPx) ?: 0) as Float

                when (type) {
                    IntroductionScreenType.HOTSPOT, IntroductionScreenType.PROFILE, IntroductionScreenType.VENUE_DETAIL_ADD_PHOTO -> {
                        viewText?.root?.y = co_ordinates.second + (150).toPx
                    }

                    IntroductionScreenType.SETTINGS -> {
                        viewText?.root?.y = co_ordinates.second - (350).toPx
                    }

                    IntroductionScreenType.VENUE_DETAIL_SINGLES -> {
                        viewText?.root?.y = co_ordinates.second - (200).toPx
                    }

                    IntroductionScreenType.ROAMING_TIMER -> viewText?.root?.y =
                        co_ordinates.second + (210).toPx
                }
                if (binding.clParent.childCount <= 2) {
                    isIntroViewVisible = true
                    binding.clParent.addView(view?.root, 2)
                    binding.clParent.addView(viewText?.root, 3)
                    binding.clParent.addView(viewClickable?.root, 4)
                }
                binding.viewBlurBackground.visible(isVisible = true)

                viewClickable?.clickableView?.setOnClickListener {
                    removeIntroViews()
                    callBack(true)
                }
                binding.viewBlurBackground.setOnClickListener {
                    removeIntroViews()
                    callBack(false)
                }
            }
        }
    }

    private fun removeIntroViews() {
        try {
            binding.clParent.removeView(view?.root)
            binding.clParent.removeView(viewText?.root)
            binding.clParent.removeView(viewClickable?.root)
            binding.viewBlurBackground.visible(isVisible = false)
            isIntroViewVisible = false
        } catch (ignore: Exception) {

        }
    }


    fun showMiddleRadarLogo(value: Boolean) {
        binding.homeToolbar.ivMiddleRadarLogo.visible(isVisible = value)
        binding.homeToolbar.ivIcon.visible(isVisible = !value)
    }

    fun showBackIcon(value: Boolean) {
        binding.homeToolbar.ivMiddleRadarLogo.visible(isVisible = value)
        binding.homeToolbar.ivIcon.visible(isVisible = !value)
        binding.homeToolbar.ivUser.visible(isVisible = !value)
        binding.homeToolbar.activityToolbarBackWhite.visible(isVisible = value)
    }

    fun hideShowToolbarAndBottomNavigation(value: Boolean) {
        binding.homeToolbar.clHomeToolbar.visible(!value)
        binding.bottomNav.bottomNav.visible(!value)
    }

    private fun NavDestination.checkForPauseProfile() {
        if (id == R.id.home_fragment) {
            if (BaseUtils.isInternetAvailable()) {
                if (SharedPrefsHelper[Constants.IS_PROFILE_PAUSED, false]) {
                    binding.includePauseProfile.llPauseProfile.visibility = View.VISIBLE
                } else {
                    binding.includePauseProfile.llPauseProfile.visibility = View.GONE
                }
            }
        } else binding.includePauseProfile.llPauseProfile.visibility = View.GONE
    }

    fun setFragmentContainer() {
        binding.fragmentContainer.visible(isVisible = true)
    }

    private fun addPadding() {
        binding.fragmentContainer.setPadding(10.toPx.toInt(), 0, 10.toPx.toInt(), 0)
        binding.homeToolbar.clHomeToolbar.setPadding(7.toPx.toInt(), 0, 7.toPx.toInt(), 0)
    }

    private fun setHotspotMargin() {
        val params = ViewGroup.MarginLayoutParams(binding.fragmentContainer.layoutParams)
        params.setMargins(
            binding.fragmentContainer.left,
            binding.fragmentContainer.top,
            binding.fragmentContainer.right,
            -65.toPx.toInt()
        )
        binding.fragmentContainer.layoutParams = params
    }

    private fun removeHotspotMargin() {
        val params = ViewGroup.MarginLayoutParams(binding.fragmentContainer.layoutParams)
        params.setMargins(
            binding.fragmentContainer.left,
            binding.fragmentContainer.top,
            binding.fragmentContainer.right, 0
        )
        binding.fragmentContainer.layoutParams = params
    }


    private fun addBottomNavMargin() {
//        val params = ViewGroup.MarginLayoutParams(binding.bottomNav.layoutParams)
//        params.setMargins(10.toPx.toInt(), 0, 10.toPx.toInt(), binding.bottomNav.marginBottom)
//        binding.bottomNav.layoutParams = params
    }

    private fun removeBottomNavMargin() {
//        val params = ViewGroup.MarginLayoutParams(binding.bottomNav.layoutParams)
//        params.setMargins(2.toPx.toInt(), 0, 2.toPx.toInt(), binding.bottomNav.marginBottom)
//        binding.bottomNav.layoutParams = params
    }

    fun removePadding() {
        binding.fragmentContainer.setPadding(0, 0, 0, 0)
        binding.homeToolbar.clHomeToolbar.setPadding(0, 0, 0, 0)
    }

    fun setHomeToolbarVisibility(isVisible: Boolean) {
        binding.homeToolbar.clHomeToolbar.visible(isVisible)
    }

    fun hideShowFireIcon(isVisible: Boolean) {
        binding.homeToolbar.ivFire.visible(isVisible = false)
    }

    fun hideShowIvAiChatIcon(isVisible: Boolean): View {
        binding.homeToolbar.ivAiChat.visible(isVisible = isVisible)
        return binding.homeToolbar.ivAiChat
    }

    fun hideShowUserIcon(isVisible: Boolean) {
        binding.homeToolbar.ivUser.visible(isVisible)
    }

    fun progressBarVisible(isVisible: Boolean) {
        if (this::binding.isInitialized) {
            if (currentDestination?.id == R.id.dialogs_fragment) binding.homeToolbar.progressBarToolbar.visible(
                isVisible
            )
            else binding.homeToolbar.progressBarToolbar.visible(false)
        }
    }

    /* override fun onPause() {
         super.onPause()
         val activityManager = applicationContext
             .getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
         activityManager.moveTaskToFront(taskId, 0)
     }

     override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
         return true
     }*/

    private fun qbSignIn(isError: Boolean = false, isSuccess: (Boolean) -> Unit) {
        if (userMobileNumber.isEmpty()) return
        if (QuickBloxManager.qbSessionManager.activeSession == null || QuickBloxManager.qbSessionManager.sessionParameters?.userLogin != userMobileNumber || isError) {
            QuickBloxManager.signIn(userMobileNumber) { user, exception ->
                user?.let {
                    connectToChat(isSuccess)
                } ?: kotlin.run {
                    isSuccess(false)
                    if (exception != null && exception.httpStatusCode == 401 && exception.localizedMessage == "Unauthorized") return@signIn
                    QuickBloxManager.signOut {
                        qbSignIn(isError = true, isSuccess)
                    }

                }
            }
        } else connectToChat(isSuccess)
    }

    private fun connectToChat(isSuccess: (Boolean) -> Unit) {
        QuickBloxManager.connectToChat {
            if (it) {
                setUnReadMessageCount()
                isSuccess(true)
            } else {
                qbSignIn(true, isSuccess)
            }
        }
    }

    fun setUnReadMessageCount() {
        QuickBloxManager.getUnReadMessageCount(chatViewModel.dialogsIdsOverServer) { count, _ ->
            setMessageCount(count ?: 0)
        }
    }

    fun setUserImage(image: String) {
        if (this::binding.isInitialized) binding.homeToolbar.ivUser.loadImage(image, true)

    }

    private val pushBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val message = intent.getStringExtra("message")
            val from = intent.getStringExtra("from")
            Log.d(HomeActivity::class.simpleName, "Chat notification received in receiver $message")
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(Constants.NOTIFICATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(broadCastManager, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(broadCastManager, filter)
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(pushBroadcastReceiver, IntentFilter("new-push-event"))

        /*  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              if (Build.VERSION.SDK_INT >= 33) {
  //                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                  when {
                      ContextCompat.checkSelfPermission(
                          this,
                          Manifest.permission.POST_NOTIFICATIONS
                      ) ==
                              PackageManager.PERMISSION_GRANTED -> {
  //                        Log.e(TAG, "User accepted the notifications!")
  //                        sendNotification(this)
                      }
                      shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                          Snackbar.make(
                              binding.root,
                              "The user denied the notifications ):",
                              Snackbar.LENGTH_LONG
                          )
                              .setAction("Settings") {
                                  val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                  intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                  val uri: Uri =
                                      Uri.fromParts(
                                          "com.radarqr.dating",
                                          packageName,
                                          null
                                      )
                                  intent.data = uri
                                  startActivity(intent)
                              }
                              .show()
                      }
                      else -> {
                          requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                      }
                  }
              }
          }*/
        /* LocalBroadcastManager.getInstance(this)
             .registerReceiver(pushBroadcastReceiver, IntentFilter(Constants.CHAT_NOTIFICATION))*/
    }


    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(broadCastManager)
            LocalBroadcastManager.getInstance(this).unregisterReceiver(pushBroadcastReceiver)
        } catch (e: Exception) {

        }
    }

//    private fun checkNotificationPermission(){
//        pushNotificationPermissionLauncher.launch(Manifest.permission.NO)
//    }
//
//    private val pushNotificationPermissionLauncher = registerForActivityResult(
//        ActivityResultContracts.RequestPermission()
//    ) { granted ->
//        viewModel.inputs.onTurnOnNotificationsClicked(granted)
//    }

    private var broadCastManager = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.getStringExtra(Constants.TYPE) ?: "") {
                Constants.LIKE_REQUEST -> {
                    getLikes()
                }

                Constants.CHAT_MESSAGE -> {
                    qbSignIn {}
                }

                Constants.MATCH_REQUEST -> {
                    val name = (intent ?: Intent()).getStringExtra(Constants.NAME) ?: ""
                    val profilePic = (intent ?: Intent()).getStringExtra(Constants.PROFILE_PIC)
                    val dialogId: String? = (intent ?: Intent()).getStringExtra(DIALOG_ID)
                    dialogId?.let {
                        showItsAMatchDialog(
                            profilePic, QuickBloxManager, it
                        ) { _, _, value ->
                            if (value == 0) return@showItsAMatchDialog
                            val bundle = Bundle().apply {
                                putString(DIALOG_ID, dialogId)
                                putString(Constants.FROM, "")
                                putString(Constants.NAME, name)
                                putString(
                                    Constants.PROFILE_PIC, profilePic ?: ""
                                )
                                putString(Constants.TYPE, Constants.FROM_HOME)
                            }
                            navController?.navigate(R.id.chat_fragment, bundle)
                        }
                    }
                }
            }
        }
    }

    private fun getDataFromPreferences() {
        runBlocking {
            quickBloxId =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_QUICK_BLOX_ID)
                    .first() ?: "0"
            yourMoveCount =
                (preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.YOUR_MOVE_COUNT)
                    .first() ?: "0").toInt()

            mobile =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_MOBILE).first()
                    ?: ""

            my_user_id =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_USERID).first()
                    ?: "0"

            loggedInUserId =
                preferencesHelper.getValue(PreferencesHelper.PreferencesKeys.KEY_USERID).first()
                    ?: "0"

            userImageUrl = preferencesHelper.getValue(KEY_IMAGE_URL).first() ?: ""
        }

        userMobileNumber = mobile
    }


    /*private fun login() {
        Log.e("CHECK", "Login")
        chatViewModel.qbLogin().observe(this) {
            it?.let {
                if (it) {
                    reLogin()
                } else setMessageCount(0)
            }
        }
    }*/

    /* private fun reLogin() {
         Log.e("CHECK", "Re-Login")
         chatViewModel.reLoginToChat().observe(this) {
             if (it) {
                 chatViewModel.isUserLoggedIn = true
                 getUserChatList()
             } else {
                 login()
             }
         }
     }
 */
    /* private fun getUserChatList() {
         Log.e("CHECK", "getUserChatList")
         runOnUiThread {
             chatViewModel.getChatUsersList()
                 .observe(this) {
                     when (it) {
                         200 -> {
                             chatViewModel.isDataAvailable = true
                             setMessageCount(chatViewModel.yourMoveCount)
                             getUserMatches(MatchDataRequest(chatViewModel.idsList, 0))
                         }

                         422 -> {
                             login()
                         }
                     }
                 }
         }
     }*/

    /*private fun getUserMatches(request: MatchDataRequest) {
        Log.e("CHECK", "getUserMatches")
        lifecycleScope.launch {
            chatViewModel.getUserMatches(request)
                .observe(this@HomeActivity) {
                    when (it) {
                        is DataResult.Loading -> {

                        }
                        is DataResult.Success -> {
                            chatViewModel.usersList = it.data.data

                            BaseUtils.hideProgressbar()
                            when (chatViewModel.storeImages(this@HomeActivity)) {
                                0 -> {
                                }

                                1, 2, 3 -> {
                                    runBlocking {
                                        preferencesHelper.saveChatUserImage(
                                            chatViewModel.imageUrlHasMap
                                        )
                                    }
                                }
                            }

                            runBlocking {
                                preferencesHelper.saveYourMoveCount(chatViewModel.yourMoveCount)
                            }
                            setMessageCount(chatViewModel.yourMoveCount)

                        }
                        is DataResult.Failure -> {
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/match-data",
                                componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/match-data Api Error"))
                        }
                        is DataResult.Empty -> {

                        }
                    }
                }

        }
    }*/

    private fun getFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("TOKEN", "ERROR - Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
//            SubscribeService.subscribeToPushes(this, true)
//            SubscribeService.subscribeToPushes(this, true)
            Log.e("TOKEN", "Home activity - \n$token")
            token.setPushToken()
//            if (SharedPrefsHelper[Constants.DEVICE_TOKEN, ""] != token) updateToken(token = token)
            updateToken(token = token)
        })
    }

    private fun updateToken(token: String) {
        lifecycleScope.launch {
            getProfileViewModel.updateToken(updateTokenRequest(token, "android"))
                .observe(this@HomeActivity) {
                    when (it) {
                        is DataResult.Loading -> {

                        }

                        is DataResult.Success -> {
                            SharedPrefsHelper.save(Constants.DEVICE_TOKEN, token)
                        }

                        is DataResult.Failure -> {
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/device-token",
                                componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/device-token Api Error"))
                        }

                        is DataResult.Empty -> {

                        }
                    }
                }

        }
    }

    override fun setActionBar(title: String) {
//        binding.homeToolbar.layout_toolbar_title?.text = title
    }

    override fun onBackPressed() {
        showBackButton(false)
//        layout_toolbar_title.text = ""
        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)

        when (findNavController(R.id.fragment_container).currentDestination?.id) {
            R.id.accountDetailsFragment -> {
                if (fragment?.childFragmentManager?.fragments?.get(0) is AccountDetailsFragment) (fragment.childFragmentManager.fragments[0] as AccountDetailsFragment).handleBack()
            }

            R.id.likes_main_fragment, R.id.setting_fragment, R.id.dialogs_fragment, R.id.hotspotsFragment -> {
//                (fragment?.childFragmentManager?.fragments?.get(0) as LikeFragment).handleBack()
//                navController?.popBackStack()
                moveToHome()
            }

            R.id.common_fragment -> {
                if (fragment?.childFragmentManager?.fragments?.get(0) is CommonFragment) (fragment.childFragmentManager.fragments[0] as CommonFragment).handleBack()
            }

            R.id.action_request_alreadyExist -> {
                if (fragment?.childFragmentManager?.fragments?.get(0) is RequestAlreadyExistFragment) (fragment.childFragmentManager.fragments[0] as RequestAlreadyExistFragment).handleBack()
            }

            /*R.id.location_Fragment -> {
                if (fragment?.childFragmentManager?.fragments?.get(0) is LocationFragment) (fragment.childFragmentManager.fragments[0] as LocationFragment).save()
            }*/

            R.id.home_fragment -> {
                finish()
            }

            R.id.editProfileFragment -> {
                if (fragment?.childFragmentManager?.fragments?.get(0) is EditProfileFragment) (fragment.childFragmentManager.fragments.get(
                    0
                ) as EditProfileFragment).handleBack()
            }

            else -> {
                if (binding.viewBlurBackground.isVisible) {
//                    binding.viewBlurBackground.visible(isVisible = false)
                } else
                    super.onBackPressed()
            }
        }

        /* if (findNavController(R.id.fragment_container).currentDestination?.id == R.id.accountDetailsFragment) {

         } else {
             super.onBackPressed()
         }*/

    }

    private fun moveToHome() {
        if (bottomNavType == BottomNavType.RECOMMENDATION) return
        preventMultipleClicks {
            bottomNavType = BottomNavType.RECOMMENDATION
            navController?.navigate(
                R.id.home_fragment,
                null,
                NavOptions.Builder().setLaunchSingleTop(true).build()
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.fragment_container)
        fragment?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        /*Handling notifications clicks*/
        handleNotificationClick(intent)
    }

    private fun handleNotificationClick(intent: Intent?) {
        val type = (intent ?: Intent()).getStringExtra(Constants.TYPE)
        val category = (intent ?: Intent()).getStringExtra(Constants.CATEGORY)
        val userId = (intent ?: Intent()).getStringExtra(Constants.USER_ID)/*?.replace("\"", "")*/
        val name = (intent ?: Intent()).getStringExtra(Constants.NAME) ?: ""
        val profilePic = (intent ?: Intent()).getStringExtra(Constants.PROFILE_PIC)
        val dialogId: String? = (intent ?: Intent()).getStringExtra(DIALOG_ID)
        val venueId: String? = (intent ?: Intent()).getStringExtra(VENUE_ID)

        when (type) {
            Constants.LIKE_REQUEST -> {
                bottomNavType = BottomNavType.LIKE
                binding.bottomNavType = bottomNavType
//                binding.bottomNav.selectedItemId = R.id.likes_main_fragment
                val bundle = Bundle()
                bundle.putString(Constants.USER_ID, userId)
                bundle.putString(Constants.CATEGORY, category)
                bundle.putBoolean(Constants.IS_NOTIFICATION, true)
                navController?.navigate(R.id.likes_main_fragment, bundle)
            }

            Constants.MATCH_REQUEST -> {
                if (dialogId.isNullOrEmpty()) {
                    bottomNavType = BottomNavType.CHAT
                    binding.bottomNavType = bottomNavType
//                    binding.bottomNav.selectedItemId = R.id.dialogs_fragment
                    navController?.navigate(R.id.dialogs_fragment)
                } else {
                    showItsAMatchDialog(
                        profilePic, QuickBloxManager, dialogId
                    ) { _, _, value ->
                        if (value == 0) return@showItsAMatchDialog
                        val bundle = Bundle().apply {
                            putString(DIALOG_ID, dialogId)
                            putString(Constants.FROM, "")
                            putString(Constants.NAME, name)
                            putString(
                                Constants.PROFILE_PIC, profilePic ?: ""
                            )
                            putString(Constants.TYPE, Constants.FROM_HOME)
                        }
                        navController?.navigate(R.id.chat_fragment, bundle)
                    }
                }
            }

            Constants.CHAT_MESSAGE -> {
//                bottom_nav.selectedItemId = R.id.dialogs_fragment
//                navController?.navigate(R.id.dialogs_fragment)
                dialogId?.let {
                    if (chatViewModel.allUserChatDialogsMapForNotification.containsKey(dialogId)) {
                        chatViewModel.allUserChatDialogsMapForNotification[dialogId]?.let { qbData ->
                            chatViewModel.qbObject = qbData
                            val bundle = Bundle().apply {
                                putSerializable(EXTRA_DIALOG_ID, qbData)
                                putString(Constants.FROM, "")
                            }
                            navController?.navigate(R.id.chat_fragment, bundle)
                        } ?: getDialog(dialogId, name, profilePic)

                    } else {
                        getDialog(dialogId, name, profilePic)
                    }
                } ?: showToast("Id error")
            }

            Constants.ROAMING_TIMER_NOTIFICATION -> {
                val bundle = Bundle().apply {
                    putString(VENUE_ID, venueId)
                    putInt(Constants.FROM, RoamingTimerFragment.FROM_HOME)
                }
                navController?.navigate(R.id.fragmentRoamingTimer, bundle)
            }

            Constants.FRIEND_INVITE, Constants.FRIEND_INVITE_ACCEPTED -> {
                val bundle = Bundle().apply {
                    putBoolean(
                        Constants.EXTRA, type == Constants.FRIEND_INVITE_ACCEPTED
                    ) // send false if received friend request else send true if friend request if accepted
                }
                navController?.navigate(R.id.closeFriendAndRequestFragment, bundle)
            }
        }
    }

    private fun getDialog(dialogId: String, name: String?, profilePic: String?) {
        val bundle = Bundle().apply {
            putString(DIALOG_ID, dialogId)
            putString(Constants.FROM, "")
            putString(Constants.NAME, name ?: "")
            putString(Constants.PROFILE_PIC, profilePic ?: "")
            putString(Constants.TYPE, Constants.FROM_HOME)
        }

        navController?.navigate(R.id.chat_fragment, bundle)
        /* QuickBloxManager.getDialogById(dialogId) { qbData ->
             qbData?.let {
                 getUserMatches(
                     MatchDataRequest(ArrayList<String>().apply {
                         add(QuickBloxManager.getOtherUserId(qbData))
                     }),
                     qbData
                 )
             }

         }*/
    }

    fun setLikeCount(count: Int) {
        likeCount = count
        if (count != 0) {
            binding.bottomNav.tvLikeBadge.visible(isVisible = true)
            binding.bottomNav.tvLikeBadge.text = "$count"
//            val badge = bottomNavigationView?.getOrCreateBadge(R.id.likes_main_fragment)
//            badge?.number = count
//            badge?.verticalOffset = 4.toPx.toInt()
//            badge?.horizontalOffset = 4.toPx.toInt()
        } else binding.bottomNav.tvLikeBadge.visible(isVisible = false)
    }

    fun minusLikeCountByOne() {
//        val badge = bottomNavigationView?.getOrCreateBadge(R.id.likes_main_fragment)
        val text = binding.bottomNav.tvLikeBadge.text
        val value = if (text.isNullOrEmpty()) 1 else text.toString().toInt()
        var count = value
        count -= 1
        likeCount = count
        if (count != 0) {
            binding.bottomNav.tvLikeBadge.visible(isVisible = true)
//            badge?.verticalOffset = 4.toPx.toInt()
//            badge?.horizontalOffset = 4.toPx.toInt()
        } else binding.bottomNav.tvLikeBadge.visible(isVisible = false)

    }

    private fun setMessageCount(count: Int) {
        chatCount = count
        runBlocking {
            preferencesHelper.saveYourMoveCount(count)
        }
        if (count != 0) {
            binding.bottomNav.tvChatBadge.visible(isVisible = true)
//            val badge = bottomNavigationView?.getOrCreateBadge(R.id.dialogs_fragment)
//                badge?.number = count //
//            badge?.verticalOffset = (3).toPx.toInt()
//            badge?.horizontalOffset = 3.toPx.toInt()
        } else binding.bottomNav.tvChatBadge.visible(isVisible = false)
    }

    /* private fun handleBackPress() {
         val backHandler = object : OnBackPressedCallback(true) {
             override fun handleOnBackPressed() {
                 val currentDestination = navController?.currentDestination
                 if (currentDestination?.id == R.id.home_fragment) {
                     finish()
                 } else {
                     navController?.popBackStack()
                 }
             }
         }

         onBackPressedDispatcher.addCallback(backHandler)
     }

     override fun onBackPress() {
         super.onBackPress()
         val currentDestination = navController?.currentDestination
         if (currentDestination?.id == R.id.home_fragment) {
             finish()
         } else {
             navController?.popBackStack()
         }
     }*/


    fun getLikes() {
        try {
            lifecycleScope.launch {
                getProfileViewModel.getLikes(1, 1, LikeFragment.LikeType.ALL_LIKES.value())
                    .observe(this@HomeActivity) {
                        when (it) {
                            is DataResult.Loading -> {

                            }

                            is DataResult.Success -> {
                                setLikeCount(it.data.data.total_records)
                                likesViewModel.totalCount = it.data.data.total_records
                                likesViewModel.onlineCount = it.data.data.online_count
                                likesViewModel.inPersonCount = it.data.data.inperson_count

                            }

                            is DataResult.Failure -> {
                                reportApiError(
                                    Exception().stackTrace[0].lineNumber,
                                    it.statusCode ?: 0,
                                    "user/get-likes",
                                    componentName.className,
                                    it.message ?: ""
                                )

                                FirebaseCrashlytics.getInstance()
                                    .recordException(Exception("user/get-likes Api Error"))
                            }

                            DataResult.Empty -> {
                            }
                        }
                    }

            }
        } catch (e: Exception) {

        }
    }

    private fun getReportOptions() {
        if (getProfileViewModel.reportData == null || getProfileViewModel.reportData?.data?.size == 0) {
            lifecycleScope.launch {
                getProfileViewModel.getReportOptions().observe(this@HomeActivity) {
                    when (it) {
                        DataResult.Empty -> {
                        }

                        is DataResult.Failure -> {
                        }

                        DataResult.Loading -> {
                        }

                        is DataResult.Success -> {
                            runBlocking {
                                getProfileViewModel.reportData = it.data
                                preferencesHelper.saveReportOptions(it.data)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getProfile() {
        lifecycleScope.launch {
            getProfileViewModel.getProfile(getProfileRequest()).observe(this@HomeActivity) {
                when (it) {
                    is DataResult.Loading -> {

                    }

                    is DataResult.Success -> {
                        loggedInUserId = it.data.data._id ?: ""
                        rcSubscriptionUserId = it.data.data.subscription_user_id ?: loggedInUserId
                        mixPanelWrapper.identifyUserForMixpanel(loggedInUserId, JSONObject().apply {
                            put("\$name", it.data.data.name)
                            put("username", it.data.data.username)
                            if (!it.data.data.email?.trim().isNullOrEmpty())
                                put("\$email", it.data.data.email)
                            put("\$phone", it.data.data.phone_number)
                        })
                        mixPanelWrapper.setSuperProperties(it.data.data)
                        getUserRecommendationViewModel.adsShowMax =
                            it.data.data.ads_interval ?: Constants.ADS_COUNT
                        it.data.data.let { profileData ->
                            rcSubscriptionUserId.loginRCAppUserId {
                                mixPanelWrapper.setUserSubscriptionStatus()
                            }
                            runBlocking(Dispatchers.IO) {
                                preferencesHelper.setValue(
                                    PreferencesHelper.PreferencesKeys.SUBSCRIPTION_USER_ID,
                                    rcSubscriptionUserId
                                )
                                preferencesHelper.saveUserData(profileData)
                            }
                        }



                        userProfileLocation = it.data.data.getLatLngFromProfile()

                        try {
                            it.data.data.images?.apply {
                                // Check is images are less then 3
                                // Show addThreeImageDialog so that user can add pending images
                                // doWeNeedToShowImagePopup is added to show popup only from Login, don't show this popup if user has just signed up
                                if (doWeNeedToShowImagePopup && !isFirstTimeDialogShowing)
                                    if (this.size < 3) {
                                        if (!RaddarApp.isAddImageDialogShown) {
                                            // Set this boolean to true so that we don't show addThreeImageDialog again
                                            RaddarApp.isAddImageDialogShown = true
                                            addThreeImageDialog(imageSize = this.size) {
                                                navController?.navigate(R.id.editProfileFragment)
                                            }
                                        }
                                    }
                                val image = this@HomeActivity.getImageUrl(this[0])
                                binding.homeToolbar.ivUser.loadImage(this[0], true)
                                /*binding.includePauseProfile.ivUserImage.loadImage(
                                    image,
                                    binding.includePauseProfile.progressBar
                                )*/

                                userImageUrl = image
                                runBlocking {
                                    preferencesHelper.saveImage(
                                        image, it.data.data.images[0]
                                    )
                                }
                                /*getProfileViewModel.storeImages(
                                    this,
                                    this@HomeActivity
                                )*/
                            }

                            /* it.data.data.replaceProfileImagesWithUrl(this@HomeActivity) { data ->
                                 getProfileViewModel.profileData.value = data
                             }*/
                            getProfileViewModel.profileData.value = it.data.data

                        } catch (e: Exception) {

                        }
                        userProfileLocation = it.data.data.getLatLngFromProfile()
                    }

                    is DataResult.Failure -> {
                        reportApiError(
                            Exception().stackTrace[0].lineNumber,
                            it.statusCode ?: 0,
                            "user/get-profile",
                            componentName.className,
                            it.message ?: ""
                        )

                        FirebaseCrashlytics.getInstance()
                            .recordException(Exception("user/get-profile Api Error"))
                    }

                    DataResult.Empty -> {}
                }
            }

        }
    }

    private fun handleFirstTimeLaunchCondition() {
        val preferences = getSharedPreferences(
            packageName,
            Application.MODE_PRIVATE
        )
        Log.d("isFirstTime", "${preferences.getBoolean("firstTime", false)}")
        if (!preferences.getBoolean("firstTime", false)) {
            isFirstTimeDialogShowing = true
            welcomeDialog {
                isFirstTimeDialogShowing = false
                val editor = preferences.edit()
                editor.putBoolean("firstTime", true)
                editor.apply()
                getProfileViewModel.profileData.value?.let {
                    it.images?.apply {
                        // Check is images are less then 3
                        // Show addThreeImageDialog so that user can add pending images
                        // doWeNeedToShowImagePopup is added to show popup only from Login, don't show this popup if user has just signed up
                        if (doWeNeedToShowImagePopup)
                            if (this.size < 3) {
                                if (!RaddarApp.isAddImageDialogShown) {
                                    // Set this boolean to true so that we don't show addThreeImageDialog again
                                    RaddarApp.isAddImageDialogShown = true
                                    addThreeImageDialog(imageSize = this.size) {
                                        navController?.navigate(R.id.editProfileFragment)
                                    }
                                }
                            }
                    }
                }
            }
        }
    }

    private fun getAccountDetails() {
        try {
            lifecycleScope.launch {
                getUserRecommendationViewModel.getAccountSettings().observe(this@HomeActivity) {
                    when (it) {
                        is DataResult.Success -> {
                            SharedPrefsHelper.save(
                                Constants.IS_PROFILE_PAUSED, it.data.data.pause_profile
                            )

                            binding.includePauseProfile.llPauseProfile.visibility =
                                if (it.data.data.pause_profile) View.VISIBLE else View.GONE

                            getProfileViewModel.data.value = it.data
                            getProfileViewModel.pauseProfileObserver.value =
                                it.data.data.pause_profile
                        }

                        DataResult.Empty -> {
                        }

                        is DataResult.Failure -> {
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/get-account-settings",
                                componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/get-account-settings Api Error"))
                        }

                        DataResult.Loading -> {
                        }
                    }
                }
            }
        } catch (e: Exception) {

        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_pause_unpause_profile -> {
                updateAccountSettings(AccountSettingsRequest(pause_profile = false)) { data ->
                    data?.let {
                        getProfileViewModel.data.value?.data?.pause_profile =
                            data.pause_profile ?: false
                        binding.includePauseProfile.llPauseProfile.visibility = View.GONE
                        SharedPrefsHelper.save(
                            Constants.IS_PROFILE_PAUSED, data.pause_profile ?: false
                        )
                        listener?.unPauseClick()
//                        if (navController?.currentDestination?.id == R.id.home_fragment)
//                            HomeFragment().hideShowContainer()
                    }
                }
            }
            /* val options = NavOptions.Builder()
                        .setLaunchSingleTop(true)
                        .setEnterAnim(R.anim.fadein)
                        .setExitAnim(R.anim.fadeout)
            //            .setPopUpTo(navController!!.graph.startDestination, false)
                        .build()
                        */
            R.id.ivSettings, R.id.viewSettings -> {
                if (bottomNavType == BottomNavType.SETTING) return
                preventMultipleClicks {
                    bottomNavType = BottomNavType.SETTING
                    navController?.navigate(
                        R.id.setting_fragment,
                        null,
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                }

            }

            R.id.ivLike, R.id.viewLike -> {
                if (bottomNavType == BottomNavType.LIKE) return
                preventMultipleClicks {
                    bottomNavType = BottomNavType.LIKE
                    navController?.navigate(
                        R.id.likes_main_fragment,
                        null,
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                }
            }

            R.id.ivHotspot, R.id.viewHotspot -> {
                if (bottomNavType == BottomNavType.HOTSPOT) return
                bottomNavType = BottomNavType.HOTSPOT
                preventMultipleClicks {
                    try {
                        navController?.getBackStackEntry(R.id.hotspotsFragment)
                        navController?.popBackStack(R.id.hotspotsFragment, false)
                    } catch (e: Exception) {
                        navController?.navigate(
                            R.id.hotspotsFragment,
                            null,
                            NavOptions.Builder().setLaunchSingleTop(true).build()
                        )
                    }
                }
            }

            R.id.ivRecommendation, R.id.viewRecommendation -> {
                if (bottomNavType == BottomNavType.RECOMMENDATION) return
                preventMultipleClicks {
                    bottomNavType = BottomNavType.RECOMMENDATION
                    navController?.navigate(
                        R.id.home_fragment,
                        null,
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                }
            }

            R.id.ivChat, R.id.viewChat -> {
                if (bottomNavType == BottomNavType.CHAT) return
                preventMultipleClicks {
                    bottomNavType = BottomNavType.CHAT
                    navController?.navigate(
                        R.id.dialogs_fragment,
                        null,
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    )
                }
            }
        }
        /*bottomNavigationView?.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home_fragment -> {
                    /*try {
                        val backstack = navController?.getBackStackEntry(R.id.home_fragment)
                        navController?.popBackStack(R.id.home_fragment, false)
                    } catch (e: Exception) {
                    }*/
                    navController?.navigate(R.id.home_fragment, null, options)

                }

                R.id.setting_fragment -> {
                    /*try {
                        val backstack = navController?.getBackStackEntry(R.id.setting_fragment)
                        navController?.popBackStack(R.id.setting_fragment, false)
                    } catch (e: Exception) {
                    }*/
                    navController?.navigate(
                        R.id.setting_fragment,
                        null,
                        options
                    )

                }

                R.id.likes_main_fragment -> {
                    /*try {
                        val backstack = navController?.getBackStackEntry(R.id.likes_main_fragment)
                        navController?.popBackStack(R.id.likes_main_fragment, false)
                    } catch (e: Exception) {
                    }*/
                    navController?.navigate(
                        R.id.likes_main_fragment,
                        null,
                        options
                    )
                }

                R.id.hotspotsFragment -> {
                    try {
                        val backstack = navController?.getBackStackEntry(R.id.hotspotsFragment)
                        navController?.popBackStack(R.id.hotspotsFragment, false)
                    } catch (e: Exception) {
                        navController?.navigate(R.id.hotspotsFragment, null, options)
                    }
                }

                R.id.dialogs_fragment -> {
                    /*try {
                        val backstack = navController?.getBackStackEntry(R.id.dialogs_fragment)
                        navController?.popBackStack(R.id.dialogs_fragment, false)
                    } catch (e: Exception) {

                    }*/
                    navController?.navigate(
                        R.id.dialogs_fragment,
                        null,
                        options
                    )

                }
            }
            true
        }
        bottomNavigationView?.setOnNavigationItemReselectedListener {
            return@setOnNavigationItemReselectedListener
        }*/
        binding.bottomNavType = bottomNavType
    }


    interface Helper {
        fun unPauseClick()
    }

    val NOTIFICATION_CHANNEL_ID = "10001"
    private val default_notification_channel_id = "default"
    private fun sendNotification() {
        val channelId = getString(R.string.default_notification_channel_id)

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) R.drawable.ic_logo_push else R.mipmap.ic_adaptive_logo_round)
                .setContentTitle("Title")
                .setContentText("Test")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH or NotificationCompat.PRIORITY_MAX)
                .setSound(getUriForSoundName())
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setTimeoutAfter(300000)
                .setWhen(System.currentTimeMillis())
//                .setContentIntent(pendingIntent).setFullScreenIntent(pendingIntent, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationBuilder.setColor(getColor(R.color.mobile_back))
            notificationBuilder.setColorized(true)
        }


        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.radar_notification),
                NotificationManager.IMPORTANCE_HIGH
            )
            val soundAttributes: AudioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            channel.importance = NotificationManager.IMPORTANCE_HIGH
            channel.lightColor = Color.RED
            channel.setShowBadge(true)
            channel.enableVibration(true)
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            channel.enableLights(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            channel.setSound(getUriForSoundName(), soundAttributes)
            notificationBuilder.setChannelId(channelId)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
        /*val sound =
            Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.radar_sound_1)
        val mBuilder = NotificationCompat.Builder(
            this,
            default_notification_channel_id
        )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Test")
            .setSound(getUriForSoundName())
            .setContentText(
                "Hello! This is my first push notification "
            );
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build();
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "NOTIFICATION_CHANNEL_NAME",
                importance
            );
            notificationChannel.enableLights(true);
            notificationChannel.lightColor = Color.RED;
            notificationChannel.enableVibration(true);
            notificationChannel.vibrationPattern =
                longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            notificationChannel.setSound(getUriForSoundName(), audioAttributes)
            mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID)
            mNotificationManager.createNotificationChannel(notificationChannel)
        }
        mNotificationManager.notify(
            System.currentTimeMillis().toInt(),
            mBuilder.build()
        );*/
    }

    private fun getUriForSoundName(): Uri? {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.radar_sound_1)
    }

    private fun NavDestination.setBottomNavType() {
        /*val isVisible =
            id == R.id.homeFragment || id == R.id.profileFragment *//*|| id == R.id.addNewVehicleFragment*//*
        binding.bottomNavigation.bottomNavParent.visible(isVisible)*/
        when (this.id) {
            R.id.home_fragment -> bottomNavType = BottomNavType.RECOMMENDATION
            R.id.setting_fragment -> bottomNavType = BottomNavType.SETTING
            R.id.hotspotsFragment -> bottomNavType = BottomNavType.HOTSPOT
            R.id.likes_main_fragment -> bottomNavType = BottomNavType.LIKE
            R.id.dialogs_fragment -> bottomNavType = BottomNavType.CHAT
//            R.id.searchVehicleDetailsFragment -> bottomNavItemType = BottomNavItemType.ADD
        }

        binding.bottomNavType = bottomNavType
    }

    enum class BottomNavType() {
        RECOMMENDATION, CHAT, LIKE, SETTING, HOTSPOT
    }
}