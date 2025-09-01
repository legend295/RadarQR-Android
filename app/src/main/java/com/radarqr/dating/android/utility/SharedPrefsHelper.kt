package com.radarqr.dating.android.utility

import android.content.Context
import android.content.SharedPreferences
import android.text.TextUtils
import com.google.android.gms.maps.model.LatLng
import com.quickblox.core.helper.StringifyArrayList
import com.quickblox.users.model.QBUser
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.constant.Constants
import java.util.Calendar

private const val SHARED_PREFS_NAME = "qb"
private const val QB_USER_ID = "qb_user_id"
private const val QB_USER_LOGIN = "qb_user_login"
private const val QB_USER_PASSWORD = "qb_user_password"
private const val QB_USER_FULL_NAME = "qb_user_full_name"
private const val QB_USER_TAGS = "qb_user_tags"
private const val TOKEN_TIME_STAMP = "token_time_stamp"

object SharedPrefsHelper {
    private var sharedPreferences: SharedPreferences =
        RaddarApp.getInstance().getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    fun save(key: String, value: Any?) {
        val editor = sharedPreferences.edit()
        when {
            value is Boolean -> editor.putBoolean(key, (value as Boolean?)!!)
            value is Int -> editor.putInt(key, (value as Int?)!!)
            value is Float -> editor.putFloat(key, (value as Float?)!!)
            value is Long -> editor.putLong(key, (value as Long?)!!)
            value is String -> editor.putString(key, value as String?)
            value is Enum<*> -> editor.putString(key, value.toString())
            value != null -> throw RuntimeException("Attempting to save non-supported preference")
        }
        editor.apply()
    }

    fun saveLastLocation(latLng: LatLng) {
        save(Constants.LAT, latLng.latitude.toString())
        save(Constants.LNG, latLng.longitude.toString())
    }

    fun saveLastEditProfileLocation(latLng: LatLng) {
        save(Constants.EDIT_LAT, latLng.latitude.toString())
        save(Constants.EDIT_LNG, latLng.longitude.toString())
    }

    fun getLastEditProfileLocation(): LatLng? {
        val lat: String = get(Constants.EDIT_LAT, "")
        val lng: String = get(Constants.EDIT_LNG, "")
        if (lat.isNotEmpty() && lng.isNotEmpty())
            return LatLng(lat.toDouble(), lng.toDouble())
        return null
    }

    fun getLastLocation(): LatLng? {
        val lat: String = get(Constants.LAT, "")
        val lng: String = get(Constants.LNG, "")
        if (lat.isNotEmpty() && lng.isNotEmpty())
            return LatLng(lat.toDouble(), lng.toDouble())
        return null
    }

    fun delete(key: String) {
        if (sharedPreferences.contains(key)) {
            getEditor().remove(key).commit()
        }
    }

    private fun getEditor(): SharedPreferences.Editor {
        return sharedPreferences.edit()
    }

    fun saveQbUser(qbUser: QBUser) {
        save(QB_USER_ID, qbUser.id)
        save(QB_USER_LOGIN, qbUser.login)
        save(QB_USER_PASSWORD, qbUser.login)
        save(QB_USER_FULL_NAME, qbUser.fullName)
        save(QB_USER_TAGS, qbUser.tags.itemsAsString)
    }

    fun removeQbUser() {
        delete(QB_USER_ID)
        delete(QB_USER_LOGIN)
        delete(QB_USER_PASSWORD)
        delete(QB_USER_FULL_NAME)
        delete(QB_USER_TAGS)
    }

    fun hasQbUser(): Boolean {
        return has(QB_USER_LOGIN) && has(QB_USER_PASSWORD)
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String): T? {
        return sharedPreferences.all[key] as T
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <T> get(key: String, defValue: T): T {
        val returnValue = sharedPreferences.all[key] as T
        return returnValue ?: defValue
    }

    private fun has(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    fun getQbUser(): QBUser? {
        if (hasQbUser()) {
            val id = get<Int>(QB_USER_ID)
            val login = get<String>(QB_USER_LOGIN)
            val password = get<String>(QB_USER_PASSWORD)
            val fullName = get<String>(QB_USER_FULL_NAME)
            val tagsInString = get<String>(QB_USER_TAGS)

            var tags: StringifyArrayList<String>? = null

            if (tagsInString != null && !TextUtils.isEmpty(tagsInString)) {
                tags = StringifyArrayList()
                tags.add(*tagsInString.split(",".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray())
            }

            val user = QBUser(login, password)
            user.id = id
            user.fullName = fullName
            user.tags = tags
            return user
        } else {
            return null
        }
    }

    fun clearSession() {
        getEditor().clear().commit()
    }

    fun getTokenTimeStamp(): Long {
        return get(TOKEN_TIME_STAMP, 0)
    }

    fun saveTokenTimeStamp() {
//        val currentTime = System.currentTimeMillis()
//        val oneWeekAgo = currentTime - (7 * 24 * 60 * 60 * 1000)
        save(TOKEN_TIME_STAMP, System.currentTimeMillis())
    }
}