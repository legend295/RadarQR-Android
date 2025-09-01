package com.radarqr.dating.android.utility.singleton

import android.content.Context
import com.mixpanel.android.mpmetrics.MixpanelAPI
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.ui.home.likes.model.UserLikes
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.utility.singleton.MixPanelWrapper.PropertiesKey.SUBSCRIBED
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

class MixPanelWrapper(context: Context) {
    private var mixpanel: MixpanelAPI? = null

    init {
        mixpanel = MixpanelAPI.getInstance(
            context,
            "21c924a9dc2ed4fa4ae12d7578860da1",
            TRACK_AUTOMATIC_EVENT
        )
    }

    fun getMixPanel(): MixpanelAPI? {
        return mixpanel
    }

    fun identifyUserForMixpanel(userId: String, data: JSONObject) {
        mixpanel?.identify(userId, true)
        updateUserPropertyOverMixpanel(data)
    }

    fun setSuperProperties(data: ProfileData) {
        mixpanel?.registerSuperProperties(JSONObject().apply {
            put(PropertiesKey.NAME, data.name ?: Constants.MixPanelFrom.NA)
            put(PropertiesKey.USERNAME, data.username ?: Constants.MixPanelFrom.NA)
            put(PropertiesKey.AGE, data.age ?: Constants.MixPanelFrom.NA)
            put(PropertiesKey.GENDER, data.gender ?: Constants.MixPanelFrom.NA)
            put(PropertiesKey.DOB, parseDob(dob = data.birthday))
            put(PropertiesKey.LOCATION_CITY, data.location.city ?: Constants.MixPanelFrom.NA)
            put(PropertiesKey.LOCATION_STATE, data.location.state ?: Constants.MixPanelFrom.NA)
            put(
                PropertiesKey.USER_LOCATION,
                data.location.latlon?.coordinates ?: Constants.MixPanelFrom.NA
            )
        })
    }

    private fun parseDob(dob: String?): String {
        dob ?: return Constants.MixPanelFrom.NA
        try {
            val simpleDateFormat = SimpleDateFormat("MM-dd-yyyy", Locale.getDefault())
            val date = simpleDateFormat.parse(dob)
            date?.let {
                val formatedDate = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault()).format(it)
                return formatedDate
            } ?: return dob
        } catch (e: Exception) {
            return dob
        }
    }

    fun updateUserPropertyOverMixpanel(data: JSONObject) {
        mixpanel?.people?.set(data)
    }

    fun logLoginEvent(userId: String?) {
        mixpanel?.track(LOGIN, JSONObject().apply { put("\$Userid", userId) })
    }

    fun logEvent(eventName: String, jsonObject: JSONObject?) {
        mixpanel?.track(eventName, jsonObject)
    }

    fun logSendLikeEvent(data: ProfileData, jsonObject: JSONObject) {
        mixpanel?.track(SEND_LIKE, jsonObject.apply {
            data.let {
                put(PropertiesKey.PROFILE_AGE, it.age)
                put(PropertiesKey.PROFILE_GENDER, it.gender)
                put(PropertiesKey.PROFILE_USERNAME, it.username)
                put(PropertiesKey.PROFILE_NAME, it.name)
                put(
                    PropertiesKey.PROFILE_USER_LOCATION,
                    it.location.latlon?.coordinates ?: Constants.MixPanelFrom.NA
                )
                put(
                    PropertiesKey.PROFILE_LOCATION_CITY,
                    it.location.city ?: Constants.MixPanelFrom.NA
                )
                put(
                    PropertiesKey.PROFILE_LOCATION_STATE,
                    it.location.state ?: Constants.MixPanelFrom.NA
                )
                put(PropertiesKey.PROFILE_DOB, it.birthday)
            }
        })
    }

    fun logAcceptLikeEvent(data: Any, jsonObject: JSONObject) {
        /* mixpanel?.track(LIKE_ACCEPTED, jsonObject.apply {
             if (data is ProfileData)
                 data.let {
                     put(PropertiesKey.PROFILE_AGE, it.age)
                     put(PropertiesKey.PROFILE_GENDER, it.gender)
                     put(PropertiesKey.PROFILE_USERNAME, it.username)
                     put(PropertiesKey.PROFILE_NAME, it.name)
                     put(PropertiesKey.PROFILE_USER_LOCATION, it.location.latlon?.coordinates)
                     put(PropertiesKey.PROFILE_LOCATION_CITY, it.location.city?:Constants.MixPanelFrom.NA)
                     put(PropertiesKey.PROFILE_LOCATION_STATE, it.location.state?:Constants.MixPanelFrom.NA)
                     put(PropertiesKey.PROFILE_DOB, it.birthday)
                 }
             else if (data is UserLikes) {
                 data.user_detail?.let {
                     put(PropertiesKey.PROFILE_AGE, it.age)
                     put(PropertiesKey.PROFILE_GENDER, it.gender)
                     put(PropertiesKey.PROFILE_NAME, it.name)
                     put(PropertiesKey.PROFILE_USERNAME, Constants.MixPanelFrom.NA)
                     put(PropertiesKey.PROFILE_USER_LOCATION, it.location.latlon?.coordinates?:Constants.MixPanelFrom.NA)
                     put(PropertiesKey.PROFILE_LOCATION_CITY, it.location.city?:Constants.MixPanelFrom.NA)
                     put(PropertiesKey.PROFILE_LOCATION_STATE, it.location.state?:Constants.MixPanelFrom.NA)
                     put(PropertiesKey.PROFILE_DOB, it.birthday)
                 }
             }
         })*/
    }

    fun logSignUpEvent(userId: String?) {
        mixpanel?.track(SIGN_UP, JSONObject().apply { put("\$Userid", userId) })
    }

    fun logSubscriptionScreenVisitEvent(jsonObject: JSONObject?) {
        mixpanel?.track(SUBSCRIPTION_SCREEN_VISIT, jsonObject)
    }

    fun logSubscriptionPurchasedEvent(
        data: JSONObject? = null
    ) {
        mixpanel?.track(SUBSCRIPTION_PURCHASED, data)
        setUserSubscriptionStatus()
    }

    fun setUserSubscriptionStatus() {
        val type = RaddarApp.getSubscriptionStatus() == SubscriptionStatus.PLUS
        mixpanel?.people?.set(SUBSCRIBED, type)
    }

    fun logSendCloseFriendRequest(data: JSONObject) {
        mixpanel?.track(SEND_CLOSE_FRIEND_REQUEST, data)
    }

    fun logRemoveFriendRequest(data: JSONObject) {
        mixpanel?.track(REMOVE_CLOSE_FRIEND, data)
    }

    fun resetProperties() {
        mixpanel?.reset()
    }

    companion object {
        const val TRACK_AUTOMATIC_EVENT = true

        // MixPanel event name
        const val SIGN_UP = "sign Up"
        const val LOGIN = "login"
        const val SUBSCRIPTION_SCREEN_VISIT = "subscription visit"
        const val SUBSCRIPTION_PURCHASE_INITIATED = "subscription purchase initiated"
        const val SUBSCRIPTION_PURCHASED = "subscription purchase completed"
        const val SUBSCRIPTION_PURCHASE_FAILED = "subscription purchase failed"
        const val VENUE_VISIT = "venue visit"
        const val VENUE_VISIT_ROAMING = "venue visit roaming"
        const val VENUE_CHECK_IN = "venue checkin"
        const val VENUE_CHECKOUT = "venue checkout"
        const val VENUE_EXPIRED = "venue expired"
        const val VENUE_EXTEND = "venue extend"
        const val SEND_LIKE = "send like"
        const val LIKE_ACCEPTED = "like accepted"
        const val PROFILE_VISIT = "profile visit"
        const val SUBSCRIPTION_RESTORE = "subscription restore"
        const val SEND_CLOSE_FRIEND_REQUEST = "send close friend request"
        const val REMOVE_CLOSE_FRIEND = "remove close friend"
    }

    object PropertiesKey {
        const val FROM_SCREEN = "visit from"
        const val IS_SUBSCRIPTION_BOUGHT = "is subscription bought"
        const val SUBSCRIPTION_ID = "subscription id"
        const val CHECKED_IN_USER_COUNT = "checked in user count"
        const val IS_ROAMING_TIMER_ENABLED = "is roaming timer enabled"
        const val VENUE_VISIT_TIME = "venue visit time"
        const val VENUE_NAME = "venue name"
        const val VENUE_LOCATION = "venue location"
        const val VENUE_ID = "venue id"
        const val AGE = "age" // Number - eg 24
        const val PROFILE_AGE = "profile age" // Number - eg 24
        const val GENDER = "gender" // Male/Female/Non Binary
        const val PROFILE_GENDER = "profile gender" // Male/Female/Non Binary
        const val USERNAME = "username"
        const val PROFILE_USERNAME = "profile username"
        const val NAME = "name"
        const val PROFILE_NAME = "profile name"
        const val USER_LOCATION = "user location"
        const val PROFILE_USER_LOCATION = "profile user location"
        const val LOCATION_CITY =
            "location city" // String, eg California  // location saved for a user
        const val VENUE_LOCATION_CITY =
            "venue location city" // String, eg California  // location saved for a user
        const val PROFILE_LOCATION_CITY =
            "profile location city" // String, eg California  // location saved for a user
        const val LOCATION_STATE = "location state" // String, eg CA
        const val VENUE_LOCATION_STATE = "venue location state" // String, eg CA
        const val PROFILE_LOCATION_STATE = "profile location state" // String, eg CA
        const val DOB = "dob"
        const val PROFILE_DOB = "profile dob"
        const val ACTION = "action"
        const val CHECK_IN_DURATION_HRS = "checkin duration hrs" //60, 120, 180, 240
        const val CHECK_OUT_DURATION = "checkout duration" //60, 120, 180, 240
        const val EXTEND_TIME = "extend time" //60, 120, 180, 240
        const val LIKE_TYPE = "type" //recommendation/qrcode/nearyou/hotspots/like
        const val SUBSCRIPTION_INITIATED_FOR =
            "subscription initiated for" //recommendation/qrcode/nearyou/hotspots
        const val AFTER_NO_OF_ADS_COUNT =
            "after no of ads count" //recommendation/qrcode/nearyou/hotspots
        const val VISIT_FROM_AD_TYPE = "visit from ad type"
        const val USER_DISTANCE = "user distance"
        const val CATEGORY = "category"
        const val SUBSCRIBED = "subscribed"
        const val STOP = "stop"
    }
}