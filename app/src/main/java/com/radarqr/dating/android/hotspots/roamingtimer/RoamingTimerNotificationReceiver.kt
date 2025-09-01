package com.radarqr.dating.android.hotspots.roamingtimer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.radarqr.dating.android.R

class RoamingTimerNotificationReceiver : BroadcastReceiver() {
    companion object {
        var NOTIFICATION_ID = "notification-id"
        var NOTIFICATION = "notification"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager: NotificationManager =
            context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = intent?.getParcelableExtra<Notification>(NOTIFICATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance: Int = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(
                context.getString(R.string.default_notification_channel_id),
                "ROAMING_TIMER",
                importance
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val id = intent?.getIntExtra(NOTIFICATION_ID, 0) ?: 0
        notificationManager.notify(id, notification)
    }

}