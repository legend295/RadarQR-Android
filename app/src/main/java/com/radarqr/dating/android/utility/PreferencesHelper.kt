package com.radarqr.dating.android.utility

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.quickblox.users.model.QBUser
import com.radarqr.dating.android.R
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.ImageModel
import com.radarqr.dating.android.data.model.like.LikeImageModel
import com.radarqr.dating.android.data.model.report.ReportData
import com.radarqr.dating.android.ui.home.settings.prodileModel.Latlon
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.welcome.otpVerify.VerifyOtpApiResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val QB_USER_ID = "qb_user_id"
private const val QB_USER_LOGIN = "qb_user_login"
private const val QB_USER_PASSWORD = "qb_user_password"
private const val QB_USER_FULL_NAME = "qb_user_full_name"
private const val QB_USER_TAGS = "qb_user_tags"

class PreferencesHelper(val context: Context) {

    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = context.getString(
            R.string.app_name
        )
    )

    suspend inline fun <reified T> setValue(key: Preferences.Key<T>, value: Any?) {
        value ?: return
        context.dataStore.edit {
            it[key] = value as T
        }
    }

    suspend inline fun <reified T> getValue(key: Preferences.Key<T>): Flow<T?> {
        return context.dataStore.data.catch {
            if (it is IOException) {
                emit(emptyPreferences())
            } else {
                throw it
            }
        }.map {
            it[key]
        }
    }

    suspend fun clearAllPreferences() {
        context.dataStore.edit {
            it.clear()
        }
    }

    suspend fun saveQbUser(qbUser: QBUser) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.QB_USER_ID] = qbUser.id
            prefs[PreferencesKeys.QB_USER_LOGIN] = qbUser.login
            prefs[PreferencesKeys.QB_USER_PASSWORD] = qbUser.password
            prefs[PreferencesKeys.QB_USER_FULL_NAME] = qbUser.fullName
            prefs[PreferencesKeys.QB_USER_TAGS] = qbUser.tags.itemsAsString
        }
    }

    suspend fun saveUserImages(userImageUrlMap: LinkedHashMap<String, Pair<Int, String>>) {
        context.dataStore.edit {
            it[PreferencesKeys.KEY_USER_IMAGE_URL] = Gson().toJson(userImageUrlMap)
        }
    }

    suspend fun saveReportOptions(options: ReportData) {
        context.dataStore.edit {
            it[PreferencesKeys.REPORT_OPTIONS] = Gson().toJson(options)
        }
    }

    /* suspend fun saveUserImagesPair(pair: ArrayList<Pair<String, String>>) {
         context.dataStore.edit {
             it[PreferencesKeys.KEY_USER_IMAGE_URL] = Gson().toJson(pair)
         }
     }
 */
    suspend fun saveYourMoveCount(count: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.YOUR_MOVE_COUNT] = count.toString()
        }
    }

    suspend fun saveChatUserImage(
//        userImageMap: HashMap<String, ImageModel>,
        userImageUrlMap: HashMap<String, ImageModel>
    ) {
        context.dataStore.edit {
//            it[PreferencesKeys.KEY_CHAT_USER_IMAGE] = Gson().toJson(userImageMap)
            it[PreferencesKeys.KEY_CHAT_USER_IMAGE_URL] = Gson().toJson(userImageUrlMap)
            it[PreferencesKeys.KEY_ARE_IMAGES_STORED] = Constants.TRUE

        }
    }

    suspend fun saveLikeUserImage(
        userImageMap: HashMap<String, LikeImageModel>,
        userImageUrlMap: HashMap<String, LikeImageModel>
    ) {
        context.dataStore.edit {
            it[PreferencesKeys.KEY_LIKE_USER_IMAGE] = Gson().toJson(userImageMap)
            it[PreferencesKeys.KEY_LIKE_USER_IMAGE_URL] = Gson().toJson(userImageUrlMap)
        }
    }


    suspend fun removeChatUserImage() {
        context.dataStore.edit {
            it.remove(PreferencesKeys.KEY_CHAT_USER_IMAGE)
            it.remove(PreferencesKeys.KEY_CHAT_USER_IMAGE_URL)
            it.remove(PreferencesKeys.KEY_ARE_IMAGES_STORED)

        }
    }

    suspend fun saveUserData(data: ProfileData) {
        context.dataStore.edit { prefs ->

            data.images?.apply {
                if (size != 0) {
                    val image_filter = StringBuilder()

                    for (i in 0 until size) {
                        image_filter.append(this[i] + ",")
                        val image = method(image_filter.toString())
                        prefs[PreferencesKeys.KEY_IMAGE_ARRAY] = image ?: ""
                    }
                }
                try {
                    this[0].let {
                        prefs[PreferencesKeys.KEY_IMAGE] = it
                    }
                } catch (e: Exception) {

                }
            }
            prefs[PreferencesKeys.KEY_QUICK_BLOX_ID] = data.quickblox_user_id ?: ""

            prefs[PreferencesKeys.KEY_FIRSTNAME] = data.name ?: ""
            prefs[PreferencesKeys.KEY_USER_NAME] = data.username ?: ""
            prefs[PreferencesKeys.KEY_AGE] = data.age ?: ""
            prefs[PreferencesKeys.KEY_GENDER] = data.gender ?: ""
            prefs[PreferencesKeys.KEY_CITY] = data.location.city ?: ""
            prefs[PreferencesKeys.KEY_COUNTRY] = data.location.country ?: ""
            prefs[PreferencesKeys.KEY_INTERESTED_IN] = data.interested_in ?: ""
            prefs[PreferencesKeys.KEY_LONG] =
                if ((data.location.latlon ?: Latlon(ArrayList(), "")).coordinates.isNullOrEmpty()) {
                    0.0
                } else (data.location.latlon ?: Latlon(ArrayList(), "")).coordinates?.get(1) ?: 0.0
            prefs[PreferencesKeys.KEY_LAT] = if ((data.location.latlon ?: Latlon(
                    ArrayList(),
                    ""
                )).coordinates.isNullOrEmpty()
            ) 0.0 else
                (data.location.latlon ?: Latlon(ArrayList(), "")).coordinates?.get(0) ?: 0.0
            prefs[PreferencesKeys.KEY_CHILDREN] = data.children ?: ""
            prefs[PreferencesKeys.KEY_ZODIAC] = data.zodiac ?: ""
            prefs[PreferencesKeys.KEY_SMOKING] = data.smoking ?: ""
            prefs[PreferencesKeys.KEY_DRINKING] = data.drinking ?: ""
            prefs[PreferencesKeys.KEY_HEIGHT] = data.height ?: ""
            prefs[PreferencesKeys.KEY_ABOUT] = data.about_me ?: ""
            prefs[PreferencesKeys.KEY_PERCENTAGE] = data.profileCompletness ?: 0
            prefs[PreferencesKeys.KEY_USERID] = data._id!!
        }
    }

    suspend fun saveNormaldata(age: String? = "", lat: Double? = 0.0, longi: Double? = 0.0) {
        context.dataStore.edit { prefs ->
            try {
                prefs[PreferencesKeys.KEY_AGE] = age!!
                prefs[PreferencesKeys.KEY_LAT] = lat!!
                prefs[PreferencesKeys.KEY_LONG] = longi!!


            } catch (e: Exception) {

            }

        }
    }

    suspend fun saveDataStep(data: Int) {
        context.dataStore.edit { prefs ->

            prefs[PreferencesKeys.KEY_STEP] = data
        }
    }

    suspend fun saveData(about_me: String) {
        context.dataStore.edit { prefs ->

            prefs[PreferencesKeys.KEY_ABOUT_ME] = about_me

        }
    }

    suspend fun saveImage(url: String, image_key: String) {
        context.dataStore.edit { prefs ->

            prefs[PreferencesKeys.KEY_IMAGE_URL] = url
            prefs[PreferencesKeys.KEY_Image] = image_key
        }
    }


    suspend fun saveData(data: VerifyOtpApiResponse.UserData) {
        context.dataStore.edit { prefs ->

            prefs[PreferencesKeys.KEY_USERID] = data.user_id
        }
    }

    suspend fun saveDataEditProfile(data: ProfileData) {
        context.dataStore.edit { prefs ->
            if (data.images != null)
                if (data.images.size != 0) {
                    var image_filter = StringBuilder()

                    for (i in 0 until data.images.size) {
                        image_filter.append(data.images.get(i) + ",")
                        var image = method(image_filter.toString())
                        prefs[PreferencesKeys.KEY_IMAGE_ARRAY] = image ?: ""
                    }
                }
            try {
                data.images?.let { images ->
                    images[0].let {
                        prefs[PreferencesKeys.KEY_IMAGE] = it
                    }
                }
            } catch (e: Exception) {

            }
            prefs[PreferencesKeys.KEY_FIRSTNAME] = data.name ?: ""

            prefs[PreferencesKeys.KEY_GENDER] = data.gender ?: ""
            prefs[PreferencesKeys.KEY_CITY] = data.location.city ?: ""
            prefs[PreferencesKeys.KEY_COUNTRY] = data.location.country ?: ""
            prefs[PreferencesKeys.KEY_INTERESTED_IN] = data.interested_in ?: ""

            prefs[PreferencesKeys.KEY_CHILDREN] = data.children ?: ""
            prefs[PreferencesKeys.KEY_ZODIAC] = data.zodiac ?: ""
            prefs[PreferencesKeys.KEY_SMOKING] = data.smoking ?: ""
            prefs[PreferencesKeys.KEY_DRINKING] = data.drinking ?: ""
            prefs[PreferencesKeys.KEY_HEIGHT] = data.height ?: ""
            prefs[PreferencesKeys.KEY_ABOUT] = data.about_me ?: ""
        }
    }

    fun method(str: String?): String? {
        var str = str
        if (str != null && str.length > 0 && str[str.length - 1] == ',') {
            str = str.substring(0, str.length - 1)
        }
        return str
    }

    object PreferencesKeys {
        val KEY_AUTH = stringPreferencesKey("key_auth_token")
        val STEP_PROGRESS = intPreferencesKey("key_step_progress")
        val KEY_FIRSTNAME = stringPreferencesKey("key_firstname")
        val KEY_USERID = stringPreferencesKey("key_user_id")
        val KEY_STEP = intPreferencesKey("key_step")
        val KEY_AGE = stringPreferencesKey("key_age")
        val KEY_GENDER = stringPreferencesKey("key_gender")
        val KEY_HEIGHT = stringPreferencesKey("key_height")
        val KEY_ABOUT = stringPreferencesKey("key_about")
        val KEY_PERCENTAGE = intPreferencesKey("key_percentage")
        val KEY_ZODIAC = stringPreferencesKey("key_zodic")
        val KEY_COUNTRY = stringPreferencesKey("key_country")
        val KEY_ETHENTICITY = stringPreferencesKey("key_ethenticity")
        val KEY_CHILDREN = stringPreferencesKey("key_children")
        val KEY_SMOKING = stringPreferencesKey("key_smoking")
        val KEY_LAT = doublePreferencesKey("key_lat")
        val KEY_LONG = doublePreferencesKey("key_long")
        val KEY_CITY = stringPreferencesKey("key_city")
        val QB_USER_ID = intPreferencesKey("qb_user_id")
        val QB_USER_LOGIN = stringPreferencesKey("qb_user_login")
        val QB_USER_PASSWORD = stringPreferencesKey("qb_user_password")
        val QB_USER_FULL_NAME = stringPreferencesKey("qb_user_full_name")
        val QB_USER_TAGS = stringPreferencesKey("qb_user_tags")
        val KEY_DRINKING = stringPreferencesKey("key_drinking")
        val KEY_MOBILE = stringPreferencesKey("key_mobile")
        var KEY_QUICK_BLOX_ID = stringPreferencesKey("key_quick_id")
        val KEY_IMAGE = stringPreferencesKey("key_image")
        var KEY_IMAGE_ARRAY = stringPreferencesKey("key_image_array")
        val KEY_INTERESTED_IN = stringPreferencesKey("key_interested")
        val KEY_PROFILE_DISTANCE_PREF = stringPreferencesKey("key_distance_pref")
        val KEY_PROFILE_AGE_PREF = stringPreferencesKey("key_age_pref")
        val KEY_ABOUT_ME = stringPreferencesKey("key_about_me")
        val KEY_CHAT_USER_IMAGE = stringPreferencesKey("key_chat_user_image")
        val KEY_CHAT_USER_IMAGE_URL = stringPreferencesKey("key_chat_user_image_url")
        val KEY_ARE_IMAGES_STORED = stringPreferencesKey("key_are_images_stored")
        val KEY_Image = stringPreferencesKey("key_Image")
        val KEY_IMAGE_URL = stringPreferencesKey("key_url")

        val KEY_LIKE_USER_IMAGE = stringPreferencesKey("key_like_user_image")
        val KEY_LIKE_USER_IMAGE_URL = stringPreferencesKey("key_like_user_image_url")
        val KEY_USER_IMAGE_URL = stringPreferencesKey("key_user_image_url")
        val REPORT_OPTIONS = stringPreferencesKey("report_options")
        val YOUR_MOVE_COUNT = stringPreferencesKey("your_move_count")
        val SUBSCRIPTION_USER_ID = stringPreferencesKey("subscription_user_id")
        val KEY_USER_NAME = stringPreferencesKey("key_user_name")
        val ADS_COUNT = intPreferencesKey("ads_count")
    }


}