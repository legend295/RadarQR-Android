package com.radarqr.dating.android.hotspots.roamingtimer

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import com.radarqr.dating.android.R
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.utility.Utility.showToast
import org.json.JSONObject

class RoamingTimerNotificationManager(private val context: Context) {

    lateinit var data: JSONObject

    companion object {
        const val VENUE_NAME = "venueName"
        const val VENUE_DESCRIPTION = "venueDescription"
        const val VENUE_ID = "venueId"
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun scheduleNotification(delay: Long, data: JSONObject) {
        this.data = data
        if (!data.has(VENUE_ID)) return
        val notificationIntent =
            Intent(context, RoamingTimerNotificationReceiver::class.java).apply {
                putExtra(RoamingTimerNotificationReceiver.NOTIFICATION_ID, 1)
                putExtra(
                    RoamingTimerNotificationReceiver.NOTIFICATION,
                    getNotification(
                        "Roaming Timer",
                        if (data.has(VENUE_NAME)) "Your roaming timer for '${data[VENUE_NAME]}' is about to expire."
                        else ""
                    )
                )
            }
        cancelRoamingTimerNotification()
        val pendingIntent: PendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                context,
                Constants.ROAMING_TIMER_NOTIFICATION_REQUEST_CODE,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                context,
                Constants.ROAMING_TIMER_NOTIFICATION_REQUEST_CODE,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        val futureInMillis: Long = SystemClock.elapsedRealtime() + delay
//        val futureInMillis: Long = System.currentTimeMillis() + delay
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun cancelRoamingTimerNotification() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager?
        val intent = Intent(context, RoamingTimerNotificationReceiver::class.java)
        val pendingIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getBroadcast(
                context,
                Constants.ROAMING_TIMER_NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                context,
                Constants.ROAMING_TIMER_NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE
            )
        }
        pendingIntent?.let { intent2 ->
            alarmManager?.cancel(intent2)
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    fun cancelAlarm() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager?
        val intent = Intent(context, RoamingTimerNotificationReceiver::class.java)
        val pendingIntent: PendingIntent? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getService(
                context,
                Constants.ROAMING_TIMER_NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                context,
                Constants.ROAMING_TIMER_NOTIFICATION_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE
            )
        }
        pendingIntent?.let { _pendingIntent ->
            alarmManager?.cancel(_pendingIntent)
            context.showToast("Notification canceled")
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getNotification(title: String, content: String): Notification {
        val intent = Intent(context, HomeActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            action = Intent.ACTION_MAIN
            putExtra(Constants.TYPE, Constants.ROAMING_TIMER_NOTIFICATION)
            putExtra(
                Constants.VENUE_ID,
                this@RoamingTimerNotificationManager.data.getString(VENUE_ID)
            )
        }
        val pendingIntent =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.getActivity(
                    context,
                    Constants.ROAMING_TIMER_NOTIFICATION_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            } else {
                PendingIntent.getActivity(
                    context,
                    Constants.ROAMING_TIMER_NOTIFICATION_REQUEST_CODE,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }


        val builder: NotificationCompat.Builder =
            NotificationCompat.Builder(
                context,
                context.getString(R.string.default_notification_channel_id)
            )
                .setSmallIcon(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) R.drawable.ic_logo_push else R.drawable.ic_new_logo)
                .setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH or NotificationCompat.PRIORITY_MAX)
                .setChannelId(context.getString(R.string.default_notification_channel_id))
                .setDefaults(Notification.DEFAULT_ALL)
                .setTicker(title)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(pendingIntent).setFullScreenIntent(pendingIntent, true)
        return builder.build()
    }
}