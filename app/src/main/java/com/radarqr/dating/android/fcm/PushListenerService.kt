package com.radarqr.dating.android.fcm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.ktx.setCustomKeys
import com.google.firebase.messaging.RemoteMessage
import com.quickblox.messages.services.SubscribeService
import com.quickblox.messages.services.fcm.QBFcmPushListenerService
import com.quickblox.sample.chat.kotlin.utils.ActivityLifecycle
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.ui.home.main.HomeActivity

class PushListenerService : QBFcmPushListenerService() {
    private val TAG = PushListenerService::class.java.simpleName
    private var mNotificationManager: NotificationManager? = null
    private var title: String = ""
    private var body: String = ""
//    var dialogId = ""


    override fun sendPushMessage(data: MutableMap<Any?, Any?>?, from: String?, message: String?) {
        super.sendPushMessage(data, from, message)
        Log.d(TAG, "Notification Data : $data")
        Log.d(TAG, "From: $from")
        Log.d(TAG, "Message: $message")

        if (ActivityLifecycle.isBackground()) {
//            showNotification(message ?: " ")
        }
        if (data != null && data.isNotEmpty()) {
            if (data[Constants.TYPE].toString() == Constants.LIKE_REQUEST
                || data[Constants.TYPE].toString() == Constants.FRIEND_INVITE
                || data[Constants.TYPE].toString() == Constants.FRIEND_INVITE_ACCEPTED
            ) {
                sendBroadcast(
                    Intent().setAction(Constants.NOTIFICATION)
                        .putExtra(Constants.TYPE, data[Constants.TYPE].toString())
                )
                sendNotification(title, body, data)
            } else if (data[Constants.TYPE].toString() == Constants.CHAT_MESSAGE) {
                if (data[Constants.DIALOG_ID].toString() == RaddarApp.dialogId) return
                sendBroadcast(
                    Intent().setAction(Constants.NOTIFICATION)
                        .putExtra(Constants.TYPE, data[Constants.TYPE].toString())
                )
                sendNotification(
                    data[Constants.MESSAGE].toString().split("\n")[0],
                    message.toString().split("\n")[1],
                    data
                )
            } else if (data[Constants.TYPE].toString() == Constants.MATCH_REQUEST) {
                sendBroadcast(
                    Intent().setAction(Constants.NOTIFICATION)
                        .putExtra(Constants.TYPE, Constants.MATCH_REQUEST)
                        .putExtra(Constants.NAME, data[Constants.NAME]?.toString() ?: "")
                        .putExtra(
                            Constants.PROFILE_PIC,
                            data[Constants.PROFILE_PIC]?.toString() ?: ""
                        )
                        .putExtra(Constants.DIALOG_ID, data[Constants.DIALOG_ID].toString())
                )
//                sendNotification(title, body, data)
            } else if (data[Constants.TYPE] == null && data[Constants.DIALOG_ID]?.toString()
                    ?.isNotEmpty() == true
            ) {
                sendBroadcast(
                    Intent().setAction(Constants.NOTIFICATION)
                        .putExtra(Constants.TYPE, Constants.CHAT_MESSAGE)
                )
                if (data[Constants.DIALOG_ID].toString() == RaddarApp.dialogId) return
                data[Constants.TYPE] = Constants.CHAT_MESSAGE
                sendNotification(data["message"]?.toString() ?: "", body, data)
            } else {
                sendNotification(title, body, data)
            }
        }
    }

    override fun onNewToken(token: String) {
        val tokenRefreshed = true
        SubscribeService.subscribeToPushes(this, tokenRefreshed)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        p0.notification?.let {
            title = it.title ?: "Radar"
            body = it.body ?: ""
        }
        super.onMessageReceived(p0)
    }

    private fun sendNotification(
        title: String,
        messageBody: String,
        data: MutableMap<Any?, Any?>?
    ) {
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(Constants.TYPE, data?.get(Constants.TYPE).toString())
        intent.putExtra(Constants.USER_ID, data?.get(Constants.USER_ID).toString())
        intent.putExtra(Constants.CATEGORY, data?.get(Constants.CATEGORY).toString())
        intent.putExtra(Constants.DIALOG_ID, data?.get(Constants.DIALOG_ID).toString())
        intent.putExtra(Constants.NAME, data?.get(Constants.NAME)?.toString() ?: "")
        intent.putExtra(Constants.PROFILE_PIC, data?.get(Constants.PROFILE_PIC)?.toString() ?: "")

        FirebaseCrashlytics.getInstance().setCustomKeys {
            key("Line Number", Exception().stackTrace[0].lineNumber)
            key("Error type", "Notification")
            key("Class Name", "PushListenerService")
            key("User Id", "${data?.get(Constants.USER_ID)}")
            key("Category", "${data?.get(Constants.CATEGORY)}")
            key("Notification Type", "${data?.get(Constants.TYPE)}")
            key("Chat DialogId", "${data?.get(Constants.DIALOG_ID)}")
        }

        intent.action = Intent.ACTION_MAIN
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        val channelId = getString(R.string.default_notification_channel_id)

        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) R.drawable.ic_logo_push else R.mipmap.ic_adaptive_logo_round)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH or NotificationCompat.PRIORITY_MAX)
                .setSound(getUriForSoundName())
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setTimeoutAfter(300000)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent).setFullScreenIntent(pendingIntent, true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationBuilder.setColor(getColor(R.color.mobile_back))
            notificationBuilder.setColorized(true)
        }


        mNotificationManager =
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
            mNotificationManager?.createNotificationChannel(channel)
        }

        mNotificationManager?.notify(
            System.currentTimeMillis().toInt(),
            notificationBuilder.build()
        )
    }

    private fun getUriForSoundName(): Uri? {
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + packageName + "/" + R.raw.radar_sound_1)
    }
}