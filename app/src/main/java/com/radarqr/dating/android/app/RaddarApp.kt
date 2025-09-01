package com.radarqr.dating.android.app

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.quickblox.auth.session.QBSettings
import com.quickblox.chat.QBChatService
import com.quickblox.core.SubscribePushStrategy
import com.quickblox.messages.services.QBPushManager
import com.radarqr.dating.android.BuildConfig
import com.radarqr.dating.android.BuildConfig.ACCOUNT_KEY
import com.radarqr.dating.android.R
import com.radarqr.dating.android.di.appComponent
import com.radarqr.dating.android.di.viewModel
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.subscription.SubscriptionWrapper.initializeRevenueCatSDK
import com.radarqr.dating.android.utility.environment.Environment
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin


const val USER_DEFAULT_PASSWORD = "quickblox"
const val CHAT_PORT = 5223
const val SOCKET_TIMEOUT = 300
const val KEEP_ALIVE: Boolean = true
const val USE_TLS: Boolean = true
const val AUTO_JOIN: Boolean = false
const val AUTO_MARK_DELIVERED: Boolean = true
const val RECONNECTION_ALLOWED: Boolean = true
const val ALLOW_LISTEN_NETWORK: Boolean = true

class RaddarApp : Application() {

    companion object {
        var appContext: Context? = null
        private var subscriptionStatus: SubscriptionStatus = SubscriptionStatus.PLUS

        private lateinit var instance: RaddarApp

        fun getInstance(): RaddarApp = instance

        fun getSubscriptionStatus() = subscriptionStatus

        private lateinit var environment: Environment

        fun getEnvironment(): Environment = environment

        val imagesMap by lazy { HashMap<String, String>() }
        val thumbOrMediumImagesMap by lazy { HashMap<String, String>() }
        var dialogId = "" // to check opened message
        // variable is used to handle one time show of dialog
        // used in HomeActivity
        // Reset to false when user logout from the application so that dialog will be shown again
        var isAddImageDialogShown = false

        // variable is used to handle one time show of confirmation dialog on EditProfile back press
        // used in EditProfileFragment
        // Reset to false when user logout from the application so that dialog will be shown again
        var isBackConfirmationImageDialogShown = false
    }

    @Suppress("KotlinConstantConditions")
    private fun getEnvironmentType() {
        environment = when (BuildConfig.ENVIRONMENT_TYPE) {
            "1" -> Environment.DEVELOPMENT
            "2" -> Environment.STAGING
            "3" -> Environment.PRODUCTION
            "4" -> Environment.RELEASE
            else -> Environment.DEVELOPMENT
        }
    }

    fun setSubscriptionStatus(status: SubscriptionStatus) {
//        subscriptionStatus = status
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        appContext = this.applicationContext
        initializeRevenueCatSDK()

        getEnvironmentType()
        initCredentials()
        startKoin {
            viewModel
            androidLogger(org.koin.core.logger.Level.NONE)
            androidContext(this@RaddarApp)
            loadKoinModules(appComponent)
        }

        if (!Places.isInitialized())
            Places.initialize(applicationContext, resources.getString(R.string.api_key))

        /* if (getEnvironment() != Environment.PRODUCTION) {
             getExternalFilesDir(null)?.let { publicAppDirectory -> // getExternalFilesDir don't need storage permission
                 val logDirectory = File("${publicAppDirectory.absolutePath}/logs")
                 if (!logDirectory.exists()) {
                     logDirectory.mkdir()
                 }

                 val logFile = File(logDirectory, "logcat_" + System.currentTimeMillis() + ".txt")
                 // clear the previous logcat and then write the new one to the file
                 try {
                     Runtime.getRuntime().exec("logcat -c")
                     Runtime.getRuntime().exec("logcat -f $logFile")
                 } catch (e: IOException) {
                     e.printStackTrace()
                 }
             }
         }
 */
/*
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                // Track screen visit for each activity
                mixPanelWrapper.logEvent("Screen Visit", JSONObject().apply {
                    put(MixPanelWrapper.PropertiesKey.FROM_SCREEN, activity.localClassName)
                })
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })*/
    }

    private fun initCredentials() {
        QBSettings.getInstance().accountKey = ACCOUNT_KEY
        QBSettings.getInstance().init(
            applicationContext,
            BuildConfig.APPLICATION_ID,
            BuildConfig.AUTH_KEY,
            BuildConfig.AUTH_SECRET
        )
//
        QBSettings.getInstance().subscribePushStrategy = SubscribePushStrategy.ALWAYS
        QBChatService.getInstance().isReconnectionAllowed = true

        setListener()
    }

    private fun setListener() {
        QBPushManager.getInstance().addListener(object : QBPushManager.QBSubscribeListener {
            override fun onSubscriptionCreated() {
                Log.e(
                    "SUBSCRIPTION",
                    "Subscription created ${QBPushManager.getInstance().isSubscribedToPushes}"
                )
            }

            override fun onSubscriptionError(e: Exception?, resultCode: Int) {
                if (resultCode >= 0) {
                    // Might be Google play service exception
                }
                Log.e("SUBSCRIPTION", "Subscription error  ${e?.localizedMessage}")
            }

            override fun onSubscriptionDeleted(deleted: Boolean) {
                Log.e("SUBSCRIPTION", "Subscription deleted $deleted")
            }
        })
    }

}

