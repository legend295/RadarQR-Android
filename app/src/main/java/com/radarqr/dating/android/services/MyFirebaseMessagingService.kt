package com.radarqr.dating.android.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.radarqr.dating.android.R
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.ui.home.main.HomeActivity


class MyFirebaseMessagingService : FirebaseMessagingService() {
    private var mNotificationManager: NotificationManager? = null

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)

        Log.e("TOKEN", "onNewToken - $p0")

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.e("TAG", "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.e("TAG", "Message data payload: ${remoteMessage.data}")
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("TAG", "Message Notification Body: ${it.body}")
            sendNotification(it.title ?: "Radar", it.body ?: "", remoteMessage)
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()

    }

    private fun sendNotification(title: String, messageBody: String, remoteMessage: RemoteMessage) {

        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra(Constants.TYPE, remoteMessage.data[Constants.TYPE])
        intent.putExtra(Constants.USER_ID, remoteMessage.data[Constants.USER_ID])

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
            }else{
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }

        val channelId = getString(R.string.default_notification_channel_id)

        val defaultSoundUri = Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE
                    + "://" + packageName + "/raw/kalimba"
        )
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) R.drawable.ic_logo_push else R.drawable.ic_new_logo)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH or NotificationCompat.PRIORITY_MAX)
                .setChannelId(channelId)
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_ALL)
                .setTicker(title)
                .setTimeoutAfter(300000)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent).setFullScreenIntent(pendingIntent, true)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                getString(R.string.default_notification_channel_id),
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.importance = NotificationManager.IMPORTANCE_HIGH
            channel.lightColor = Color.RED
            channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
            channel.enableLights(true)
            channel.enableVibration(true)
            channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationBuilder.setChannelId(channelId)
            notificationManager.createNotificationChannel(channel)
        }
        mNotificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val id = 0
        mNotificationManager?.notify(id, notificationBuilder.build())
    }
}