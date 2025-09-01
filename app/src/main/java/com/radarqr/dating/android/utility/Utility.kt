package com.radarqr.dating.android.utility

import android.app.Activity
import android.app.ActivityManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Address
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.bold
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.maps.android.SphericalUtil
import com.quickblox.chat.model.QBChatDialog
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.databinding.*
import com.radarqr.dating.android.ui.home.likes.model.UserLikes
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.quickBlox.model.MatchedData
import com.radarqr.dating.android.ui.home.settings.model.EditProfileGeneralContentData
import com.radarqr.dating.android.ui.home.settings.prodileModel.ImageData
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.home.settings.prodileModel.SaveData
import com.radarqr.dating.android.utility.BaseUtils.convertAge
import com.radarqr.dating.android.utility.BaseUtils.getChildrenValue
import com.radarqr.dating.android.utility.BaseUtils.getStringWithFirstWordCapital
import com.radarqr.dating.android.utility.Utility.loadImage
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.imageZoom.Zoomy
import java.io.File
import java.io.FileNotFoundException
import java.text.DecimalFormat
import java.util.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


object Utility {

    private const val DEBOUNCE_DURATION = 300L

    var lastClickTime = 0L

    /**
     * Show Toast on screen
     * @param activity
     */
    fun showToast(activity: FragmentActivity, msg: String) {
        if (isAppOnForeground(activity)) {
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Show Toast on screen
     * @param context
     */
    /* fun showToast(raddarAppContext: Context, msg: String) {
         if (isAppOnForeground(raddarAppContext)) {
             Toast.makeText(raddarAppContext, msg, Toast.LENGTH_SHORT).show()
         }
     }*/

    /**
     * Show Toast on screen
     * @param raddarApp
     */
    fun showToast(raddarApp: RaddarApp, msg: String) {
        if (isAppOnForeground(raddarApp)) {
            Toast.makeText(raddarApp, msg, Toast.LENGTH_LONG).show()
        }
    }

    fun Context.showToast(msg: String) {
        if (isAppOnForeground(this)) {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Show the keyboard
     */
    fun showKeyboard(
        activity: FragmentActivity?
    ) {
        ((activity)?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .toggleSoftInput(
                InputMethodManager.SHOW_FORCED,
                InputMethodManager.HIDE_IMPLICIT_ONLY
            )
    }

    /**
     * Hide the keyboard
     */
    fun hideKeyboard(
        activity: FragmentActivity?,
        fragment: Fragment?
    ) {
        val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = if (fragment != null) {
            fragment.view?.rootView ?: View(activity).rootView
        } else {
            activity.currentFocus ?: View(activity)
        }
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }


    /**
     * Method to get the key for aws s3 storage
     */
    fun getFileKeyForAWS(filePath: String, imageFolder: String): String {
        val extension: String = File(filePath).extension
        val name: String = File(filePath).nameWithoutExtension

        val fileName = "$name${System.currentTimeMillis()}.$extension"
        return "$imageFolder/$fileName"
    }

    /**
     * Convert meter to miles
     */
    fun metersToMiles(meters: Double): String {
        val decimalFormatter = DecimalFormat("#.##")
        val miles = meters / 1609.3440057765
        return decimalFormatter.format(miles)
    }

    /**
     * Check mobile number is valid or not
     */
    fun isValidMobile(phone: String): Boolean {
        return android.util.Patterns.PHONE.matcher(phone).matches()
    }

    /**
     * Check email is valid or not
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Method to bold some part of text
     */
    fun getTextWithBold(title: String, body: String): SpannableStringBuilder {
        return SpannableStringBuilder()
            .bold { append(title) }
            .append(" ")
            .append(body)
    }


    /**
     * Convert value to decimal format
     */
    fun convertToDecimalFormat(value: Double): Float {
        val decimalFormatter = DecimalFormat("#.##")
        return decimalFormatter.format(value).toFloat()
    }

    /**
     * Check app is running or not
     */
    fun isAppRunning(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE)
                as ActivityManager
        val procInfo = activityManager.runningAppProcesses
        if (procInfo != null) {
            for (processInfo in procInfo) {
                if (processInfo.processName == context.applicationContext.packageName) {
                    return true
                }
            }
        }
        return false
    }

    fun isAppOnForeground(context: Context): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcesses = activityManager.runningAppProcesses ?: return false
        for (appProcess in appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                && appProcess.processName == context.applicationContext.packageName
            ) {
                return true
            }
        }
        return false
    }

    fun clearAllNotification(activity: Activity) {
        val notificationManager = NotificationManagerCompat.from(
            activity
        )
        notificationManager.cancelAll()
    }

    fun openNotificationSetting(activity: FragmentActivity) {
        val intent: Intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, activity.packageName)
        activity.startActivity(intent)
    }

    fun getAge(dob: String): Int {
        val st = dob.split("-")
        val day = st[0].toInt()
        val month = st[1].toInt()
        val year = st[2].toInt()
        val cal = GregorianCalendar()
        var noofyears: Int
        val y: Int = cal[Calendar.YEAR] // current year ,
        val m: Int = cal[Calendar.MONTH] // current month
        val d: Int = cal[Calendar.DAY_OF_MONTH] // current day
        cal[year, month] = day // here ur date
        noofyears = (y - cal[Calendar.YEAR])
        Log.d("Age......", noofyears.toString())
        if (m < cal[Calendar.MONTH] || m == cal[Calendar.MONTH] && d < cal[Calendar.DAY_OF_MONTH]) {
            --noofyears
        }
        Log.d("Age......", noofyears.toString())
//        if (noofyears != 0) {
//            ageCount = noofyears
//        } else {
//            ageCount = 0
//        }
        require(noofyears >= 0) { "age < 0" }
        return noofyears
    }

    fun phoneFormat(phoneNumber: String): String {
//        return phoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d+)".toRegex(), "($1) $2-$3")
        return phoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d+)".toRegex(), "($1) $2-$3")
    }

    fun phoneFormatWithDash(phoneNumber: String): String {
//        return phoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d+)".toRegex(), "($1) $2-$3")
        return phoneNumber.replaceFirst("(\\d{3})(\\d{3})(\\d+)".toRegex(), "$1 $2-$3")
    }

    fun getTextWithFirstCapital(name: String): String {
        return try {
            name.substring(0, 1).uppercase(Locale.getDefault()) + name.substring(1)
                .lowercase(Locale.getDefault())
        } catch (e: Exception) {
            ""
        }
    }

    fun convertToFeetInchesHeight(str: String): String {
        val value = str.toDouble()
        val feet = /*Math.floor(value / 30.48).toInt()*/ (0.0328 * value).toInt()
        val inches = /*Math.round(value / 2.54 - feet * 12).toInt()*/
            (0.3937 * value - feet * 12).toInt()
        val ouput = "$feet' $inches\""
        return ouput
    }

    fun zoomImage(context: Activity, imageView: ImageView) {
        val builder: Zoomy.Builder = Zoomy.Builder(context)
            .target(imageView)
            .interpolator(OvershootInterpolator())
            .tapListener {

            }
            .longPressListener {}
            .doubleTapListener {}

        builder.register()
    }

    fun getDaysDifference(time: Long): Int =
        (((time - Date(System.currentTimeMillis()).time) / (1000L * 60 * 60 * 24)) % 365).toInt()

    fun getHoursDifference(time: Long): Int =
        (((time - Date(System.currentTimeMillis()).time) / (1000L * 60 * 60)) % 24).toInt()

    fun getEarlierDate(): Date {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, +7)
        return cal.time
    }

    fun getMimeType(url: String): String? {
        var type: String? = null
        val extension = MimeTypeMap.getFileExtensionFromUrl(url)
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        }
        return type
    }

    fun File.getMimeType(fallback: String = "image/*"): String {
        return MimeTypeMap.getFileExtensionFromUrl(toString())
            ?.run {
                MimeTypeMap.getSingleton().getMimeTypeFromExtension(lowercase(Locale.getDefault()))
            }
            ?: fallback // You might set it to */*
    }

    fun View.visible(isVisible: Boolean) {
        visibility = if (isVisible) View.VISIBLE else View.GONE
    }

    fun Context.drawable(id: Int): Drawable? {
        return ContextCompat.getDrawable(this, id)
    }

    fun Context.color(id: Int): Int {
        return ContextCompat.getColor(this, id)
    }

    fun Context.font(id: Int): Typeface? {
        return ResourcesCompat.getFont(this, id)
    }

    fun ImageView.loadImage(url: Any, progressBar: View?) {
        Glide.with(this).load(url)
            .placeholder(R.drawable.unpaused_user_profile).apply(
                RequestOptions.circleCropTransform()
            ).listener(listener("")).into(this)
    }

    fun ImageView.loadImage(url: String?, color: Int = R.color.teal_200) {
        if (url.isNullOrEmpty()) {
            Glide.with(this).applyDefaultRequestOptions(RequestOptions().fitCenter())
                .load(ContextCompat.getDrawable(this.context, R.drawable.placeholder)).into(this)
            return
        }
        val imageUrl = this.context.getUrl(url)
        Log.d(Utility::class.simpleName, imageUrl)
        Glide.with(this).load(imageUrl.ifEmpty { url })
            .placeholder(
                this.getCircularDrawable(
                    ContextCompat.getColor(
                        this.context,
                        color
                    )
                )
            ).error({
                ContextCompat.getDrawable(this.context, R.drawable.placeholder)
            }).addListener(this.listener(url ?: ""))
            .into(this)
    }

    fun ImageView.loadVenueImage(url: String?, color: Int = R.color.teal_200) {
        if (url.isNullOrEmpty()) {
            Glide.with(this).applyDefaultRequestOptions(RequestOptions().fitCenter())
                .load(ContextCompat.getDrawable(this.context, R.drawable.placeholder)).into(this)
            return
        }
        val imageUrl = this.context.getVenueUrl(url)

        Glide.with(this).load(imageUrl.ifEmpty { url })
            .placeholder(
                this.getCircularDrawable(
                    ContextCompat.getColor(
                        this.context,
                        color
                    )
                )
            ).error({
                ContextCompat.getDrawable(this.context, R.drawable.placeholder)
            }).addListener(this.listener(url))
            .into(this)
    }

    private fun Context.getUrl(url: String?): String {
        var imageUrl = url ?: ""
        if (!imageUrl.contains("https")) {
            if (RaddarApp.imagesMap.containsKey(imageUrl)) imageUrl = RaddarApp.imagesMap[url] ?: ""
            else {
                imageUrl = S3Utils.generatesShareUrl(this, url).replace(" ", "%20")
                RaddarApp.imagesMap[url ?: ""] = imageUrl
            }
        }
        return imageUrl
    }

    fun Context.getVenueUrl(url: String?): String {
        var imageUrl = url ?: ""
        if (!imageUrl.contains("https")) {
            if (RaddarApp.imagesMap.containsKey(imageUrl)) imageUrl = RaddarApp.imagesMap[url] ?: ""
            else {
                imageUrl = S3Utils.generatesVenueShareUrl(this, url).replace(" ", "%20")
                RaddarApp.imagesMap[url ?: ""] = imageUrl
            }
        }
        return imageUrl
    }

    fun ImageView.listener(url: String): RequestListener<Drawable> {
        return object : RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                e?.let {
                    if (it.rootCauses.isNotEmpty()) {
                        if (e.rootCauses[0].cause is FileNotFoundException) {
                            if (url.isNotEmpty()) {
                                RaddarApp.imagesMap.remove(url)
                                RaddarApp.thumbOrMediumImagesMap.remove(url)
                            }
                            val drawable = ContextCompat.getDrawable(
                                this@listener.context,
                                R.drawable.placeholder
                            )
                            Handler(Looper.getMainLooper()).post {
                                Glide.with(this@listener)
                                    .applyDefaultRequestOptions(RequestOptions().fitCenter())
                                    .load(drawable).into(this@listener)
                            }
                        }
                    }
                }
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
        }
    }

    fun ImageView.loadImage(url: String?, isThumb: Boolean, color: Int = R.color.teal_200) {
        var imageUrl = url ?: ""

//        imageUrl = S3Utils.generatesThumbShareUrl(context, url).replace(" ", "%20")
        if (!imageUrl.contains("https")) {
            if (RaddarApp.thumbOrMediumImagesMap.containsKey(imageUrl)) imageUrl =
                RaddarApp.thumbOrMediumImagesMap[url] ?: ""
            else {
                imageUrl = S3Utils.generatesThumbShareUrl(context, url).replace(" ", "%20")
                RaddarApp.thumbOrMediumImagesMap[url ?: ""] = imageUrl
            }
        }

        Glide.with(this).load(imageUrl.ifEmpty { url })
            .placeholder(
                this.getCircularDrawable(
                    ContextCompat.getColor(
                        this.context,
                        color
                    )
                )
            ).error({
                loadImage(url)
            }).addListener(listener(url ?: ""))
            .into(this)
    }

    fun ImageView.loadImage(url: Any?, color: Int = R.color.teal_200) {
        Glide.with(this).load(url)
            .placeholder(
                this.getCircularDrawable(
                    ContextCompat.getColor(
                        this.context,
                        color
                    )
                )
            )
            .into(this)
    }

    fun ImageView.getCircularDrawable(color: Int): CircularProgressDrawable {
        val circularProgressDrawable =
            CircularProgressDrawable(this.context)
        circularProgressDrawable.strokeWidth = 4f
        circularProgressDrawable.centerRadius = 15f
        circularProgressDrawable.setColorSchemeColors(color)
        circularProgressDrawable.start()
        return circularProgressDrawable
    }

    fun ProfileData.getEditProfileGeneralContentList(): ArrayList<EditProfileGeneralContentData> {
        val list = ArrayList<EditProfileGeneralContentData>()
        list.add(
            EditProfileGeneralContentData(
                0,
                EditProfileGeneralContentTypes.NAME,
                "Name",
                name ?: "",
                name ?: ""
            )
        )
        list.add(
            EditProfileGeneralContentData(
                1,
                EditProfileGeneralContentTypes.USERNAME,
                "Username",
                username ?: "",
                username ?: ""
            )
        )

        list.add(
            EditProfileGeneralContentData(
                2,
                EditProfileGeneralContentTypes.GENDER,
                "Gender",
                gender?.let { BaseUtils.getModifiedString(it, 0) } ?: "",
                gender ?: ""
            )
        )

        list.add(
            EditProfileGeneralContentData(
                3,
                EditProfileGeneralContentTypes.AGE,
                "Age",
                convertAge(this),
                age ?: ""
            )
        )

        list.add(
            EditProfileGeneralContentData(
                4,
                EditProfileGeneralContentTypes.HEIGHT,
                "Height",
                BaseUtils.convertToFeetInches(height),
                height ?: ""
            )
        )

        /*list.add(
            EditProfileGeneralContentData(
                4,
                EditProfileGeneralContentTypes.WEIGHT,
                "Weight",
                weight ?: "",
                weight ?: "",
            )
        )*/
        /*val gcd = Geocoder(context, Locale("en"))
        val addresses = gcd.getFromLocation(this.location.latlon?.coordinates?.get(0)?:0.0, center.longitude, 1)*/
        val modifiedLocation = BaseUtils.getLocationString(this).lowercase()
            .split(' ').joinToString { it.replaceFirstChar(Char::titlecase) }
            .replace(",", "")

        val modifiedState = if (!location.state.isNullOrEmpty()) location.state.lowercase()
            .split(' ').joinToString { it.replaceFirstChar(Char::titlecase) }
            .replace(",", "") else ""
        val location =
            if (location.state.isNullOrEmpty()) modifiedLocation else "$modifiedLocation, $modifiedState"
        list.add(
            EditProfileGeneralContentData(
                5,
                EditProfileGeneralContentTypes.LOCATION,
                "Location",
                BaseUtils.getLocationString(this),
//                location,
                ""
            )
        )
        list.add(
            EditProfileGeneralContentData(
                6,
                EditProfileGeneralContentTypes.ZODIAC_SIGN,
                "Zodiac Sign",
                zodiac?.getStringWithFirstWordCapital() ?: "",
                zodiac ?: ""
            )
        )
        list.add(
            EditProfileGeneralContentData(
                7,
                EditProfileGeneralContentTypes.DRINKING,
                "Drinking",
                drinking?.getStringWithFirstWordCapital() ?: "",
                drinking ?: ""
            )
        )
        list.add(
            EditProfileGeneralContentData(
                8,
                EditProfileGeneralContentTypes.SMOKING,
                "Smoking",
                smoking?.getStringWithFirstWordCapital() ?: "",
                smoking ?: ""
            )
        )
        list.add(
            EditProfileGeneralContentData(
                9,
                EditProfileGeneralContentTypes.FRIENDLY_420,
                "420 Friendly",
                marijuana?.getStringWithFirstWordCapital() ?: "",
                marijuana ?: ""
            )
        )
        list.add(
            EditProfileGeneralContentData(
                10,
                EditProfileGeneralContentTypes.LOOKING_FOR,
                "Looking for",
                interested_in?.let { BaseUtils.getModifiedString(it, 1) } ?: "",
                interested_in ?: ""
            )
        )
        list.add(
            EditProfileGeneralContentData(
                11,
                EditProfileGeneralContentTypes.CHILDREN,
                "Children",
                children?.getChildrenValue() ?: "",
                children ?: ""
            )
        )
        /* list.add(
             EditProfileGeneralContentData(
                 11,
                 EditProfileGeneralContentTypes.ETHNICITY,
                 "Ethnicity",
                 ethnicity?.getStringWithFirstWordCapital() ?: "",
                 ethnicity ?: ""
             )
         )*/
        return list
    }

    fun ProfileData.getEditProfileWorkEducationContentList(): ArrayList<EditProfileGeneralContentData> {
        val list = ArrayList<EditProfileGeneralContentData>()
        list.add(
            EditProfileGeneralContentData(
                0,
                EditProfileGeneralContentTypes.JOB,
                "Company",
                job ?: "",
                job ?: "",

                )
        )
        list.add(
            EditProfileGeneralContentData(
                1,
                EditProfileGeneralContentTypes.JOB_TITLE,
                "Job Title",
                job_title ?: "",
                job_title ?: "",

                )
        )

        list.add(
            EditProfileGeneralContentData(
                2,
                EditProfileGeneralContentTypes.SCHOOL,
                "School",
                school ?: "",
                school ?: "",

                )
        )

        list.add(
            EditProfileGeneralContentData(
                3,
                EditProfileGeneralContentTypes.EDUCATION_LEVEL,
                "Education level",
                education_level?.getStringWithFirstWordCapital() ?: "",
                education_level ?: ""
            )
        )

        return list
    }

    fun SaveData.getPreferencesContentList(): ArrayList<EditProfileGeneralContentData> {
        val list = ArrayList<EditProfileGeneralContentData>()
        list.add(
            EditProfileGeneralContentData(
                0,
                EditProfileGeneralContentTypes.I_WANT_TO_MEET,
                "I want to meet",
                want_to_meet?.getStringWithFirstWordCapital() ?: "",
                want_to_meet ?: "",
            )
        )
        list.add(
            EditProfileGeneralContentData(
                1,
                EditProfileGeneralContentTypes.AGE_RANGE,
                "Age range",
                getMinMaxAge(),
                getMinMaxAge(),
                nonNegotiableKey = max_age_req ?: false
            )
        )
        list.add(
            EditProfileGeneralContentData(
                2,
                EditProfileGeneralContentTypes.HEIGHT_PREF,
                "Height",
                getHeightRange(),
                getHeightRange(),
                nonNegotiableKey = max_height_req ?: false
            )
        )
        list.add(
            EditProfileGeneralContentData(
                3,
                EditProfileGeneralContentTypes.MAX_DISTANCE,
                "Maximum Distance",
                max_distance?.let { "$max_distance mi" } ?: "",
                max_distance?.toString() ?: "",
                nonNegotiableKey = max_distance_req ?: false
            )
        )
        /*list.add(
            EditProfileGeneralContentData(
                4,
                EditProfileGeneralContentTypes.LOCATION,
                "Location",
                BaseUtils.getLocationString(
                    ProfileData(
                        location = LocationDetail(
                            city = location.city,
                            country = location.country
                        )
                    )
                ).lowercase().getStringWithFirstWordCapital(),
                ""
            )
        )*/
        list.add(
            EditProfileGeneralContentData(
                4,
                EditProfileGeneralContentTypes.ZODIAC_SIGN,
                "Zodiac Sign",
                getZodiacSign(),
                getZodiacSign(),
                nonNegotiableKey = zodiac_sign_req ?: false
            )
        )
        list.add(
            EditProfileGeneralContentData(
                5,
                EditProfileGeneralContentTypes.DRINKING,
                "Drinking",
                drinking?.getStringWithFirstWordCapital() ?: "",
                drinking ?: "",
                nonNegotiableKey = drinking_req ?: false
            )
        )
        list.add(
            EditProfileGeneralContentData(
                6,
                EditProfileGeneralContentTypes.SMOKING,
                "Smoking",
                smoking?.getStringWithFirstWordCapital() ?: "",
                smoking ?: "",
                nonNegotiableKey = smoking_req ?: false
            )
        )
        list.add(
            EditProfileGeneralContentData(
                7,
                EditProfileGeneralContentTypes.FRIENDLY_420,
                "420 Friendly",
                marijuana?.getStringWithFirstWordCapital() ?: "",
                marijuana ?: "",
                nonNegotiableKey = marijuana_req ?: false
            )
        )
        list.add(
            EditProfileGeneralContentData(
                8,
                EditProfileGeneralContentTypes.CHILDREN,
                "Children",
                children?.getChildrenValue() ?: "",
                children ?: "",
                nonNegotiableKey = children_req ?: false
            )
        )
        list.add(
            EditProfileGeneralContentData(
                9,
                EditProfileGeneralContentTypes.EDUCATION_LEVEL,
                "Education Level",
                getEducationLevel(),
                getEducationLevel(),
                nonNegotiableKey = education_level_req ?: false
            )
        )
        return list
    }

    private fun SaveData.getMinMaxAge(): String {
        val minAge = min_age?.toString() ?: ""
        val maxAge = max_age?.toString() ?: ""
        return if (minAge.isEmpty() && maxAge.isEmpty()) "" else "$minAge-$maxAge"
    }

    private fun SaveData.getHeightRange(): String {
        val minHeight = min_height?.toString() ?: ""
        val maxHeight = max_height?.toString() ?: ""
        return if (minHeight.isEmpty() && maxHeight.isEmpty()) ""
        else "${convertToFeetInchesHeight(minHeight)}-${convertToFeetInchesHeight(maxHeight)}"
    }

    private fun SaveData.getZodiacSign(): String {
        val stringBuilder = StringBuilder()
        zodiac_sign.forEachIndexed { index, s ->
            stringBuilder.append(s.getStringWithFirstWordCapital() + if (index == zodiac_sign.size - 1 || zodiac_sign.size == 1) "" else ", ")
        }
        return stringBuilder.toString()
    }

    private fun SaveData.getEducationLevel(): String {
        val stringBuilder = StringBuilder()
        education_level.forEachIndexed { index, s ->
            stringBuilder.append((if (s == "highschool") "High school" else s.getStringWithFirstWordCapital()) + if (index == education_level.size - 1 || education_level.size == 1) "" else ", ")
        }
        return stringBuilder.toString()
    }


    /**
     * Common sheet with one input field
     *
     * Used to update Filed with input
     *
     * @param title work like header, top text
     * @param body it is optional showing below title
     * @param hint used to set input field hint
     * @param inputText used to set text using two-way binding
     * @param buttonText can be modified according to UI
     * @param maxMin should be size of 2 else give indexOutOfBoundException used to set min and max lines for input field
     * @param inputType used to change keyboard type and input field
     * @param callBack return the entered input in input field
     *
     *
     * */
    fun Context.openBottomSheetWithEditField(
        title: String,
        body: String = "",
        hint: String,
        inputText: String,
        buttonText: String = "Done",
        maxMin: ArrayList<Int>,
        isCancelVisible: Boolean = false,
        cancelText: String = "Cancel",
        forVenueSingles: Boolean = false,
        callBack: (String) -> Unit
    ) {
        val sheet = BottomSheetDialog(this, R.style.DialogStyle)
        val binding =
            LayoutBottomSheetWithEditFieldBinding.inflate(
                LayoutInflater.from(sheet.context),
                null,
                false
            )
        binding.etInput.requestFocus()
        binding.isCancelVisible = isCancelVisible
        binding.cancelText = cancelText
        binding.title = title
        binding.body = body
        binding.hint = hint
        binding.buttonText = buttonText
        binding.inputText = inputText
        binding.etInput.minLines = if (maxMin.isEmpty()) 1 else maxMin[0]
        binding.etInput.maxLines = if (maxMin.isNotEmpty() && maxMin.size > 1) maxMin[1] else 1

        binding.etInput.addTextChangedListener(MessageTextWatcher(binding.etInput))

        if (forVenueSingles) {
            binding.tvContinue.isEnabled = !binding.etInput.text.isNullOrEmpty()
            binding.tvContinue.alpha = if (binding.etInput.text.isNullOrEmpty()) 0.5F else 1F
            binding.etInput.doAfterTextChanged {
                binding.tvContinue.isEnabled = !it.isNullOrEmpty()
                binding.tvContinue.alpha = if (it.isNullOrEmpty()) 0.5F else 1F
            }
        }

        binding.tvContinue.setOnClickListener {
            callBack(binding.inputText.toString())
            sheet.dismissWithAnimation = true
            sheet.dismiss()
        }

        binding.tvCancel.setOnClickListener {
            sheet.dismissWithAnimation = true
            sheet.dismiss()
        }

        sheet.behavior.skipCollapsed = true
        sheet.behavior.state = STATE_EXPANDED
        sheet.setDimBackground()
        sheet.setContentView(binding.root)
        if (isCancelVisible) sheet.setCancelable(false)
        sheet.show()

    }

    private class MessageTextWatcher(val editText: AppCompatEditText) : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

        }

        override fun afterTextChanged(p0: Editable?) {
            if (p0?.contains("\n") == true) {
                val modifiedText = p0.replace(Regex("\n"), "")
                editText.removeTextChangedListener(this)
                editText.setText(modifiedText)
                handleSelection()
                editText.addTextChangedListener(this)
            } else if (p0?.contains("  ") == true) {
                val modifiedText = p0.replace(Regex(" {2}"), " ")
                editText.removeTextChangedListener(this)
                editText.setText(modifiedText)
                handleSelection()
                editText.addTextChangedListener(this)
            }
        }

        private fun handleSelection() {
            editText.setSelection(editText.text?.length ?: 0)
        }
    }

    fun Context.openBottomSheet(
        data: UserLikes,
        callBack: (String, BottomSheetDialog, LayoutLikesBottomSheetBinding) -> Unit
    ) {
        val sheet = BottomSheetDialog(this, R.style.DialogStyle)
        val binding =
            LayoutLikesBottomSheetBinding.inflate(LayoutInflater.from(sheet.context), null, false)
        binding.data = data
        binding.etInput.requestFocus()
        binding.tvContinue.setOnClickListener {
            callBack(binding.etInput.text?.toString() ?: "", sheet, binding)
        }

        binding.etInput.addTextChangedListener(MessageTextWatcher(binding.etInput))

        binding.tvCancel.setOnClickListener {
            if (!binding.progressBar.isVisible)
                sheet.dismiss()
        }

        sheet.dismissWithAnimation = true
        sheet.behavior.skipCollapsed = true
        sheet.setCancelable(false)
        sheet.behavior.state = STATE_EXPANDED
        sheet.setDimBackground()
        sheet.setContentView(binding.root)
        sheet.show()
    }

    fun Context.getImageUrl(imageName: String): String =
        S3Utils.generatesShareUrl(this, imageName).replace(" ", "%20")

    fun ArrayList<ProfileData>.replaceImageWithUrl(
        context: Context,
        previousData: LinkedHashMap<String, ProfileData?>
    ): LinkedHashMap<String, ProfileData> {
        val linkedHashMap = LinkedHashMap<String, ProfileData>()
        forEachIndexed { _, profileData ->
            Log.d("IMAGE_URL", "${profileData.images}")
            var profilePic: String? = ""
            var imageData = ImageData("", "")
            profileData.profile_pic?.let {
                if (previousData.containsKey(profileData._id)) {
                    if (previousData[profileData._id]?.imageData?.key != it) {
                        var url = it
                        if (!it.contains("https")) {
                            url = context.getImageUrl(it)
                            profilePic = url
                        }
                        imageData = ImageData(it, url)
                    } else {
                        profilePic = previousData[profileData._id]?.profile_pic
                        imageData = previousData[profileData._id]?.imageData ?: ImageData("", "")
                    }
                } else {
                    var url = it
                    if (!it.contains("https")) {
                        url = context.getImageUrl(it)
                        profilePic = url
                    }
                    imageData = ImageData(it, url)
                }
            }
            profileData.profile_pic = profilePic
            profileData.imageData = imageData
            profileData.replaceProfileImagesWithUrl(context) {
                profileData.imageDataMap = it.imageDataMap
            }
            profileData._id?.let { linkedHashMap[it] = profileData }
        }

        return linkedHashMap
    }

    fun ProfileData.replaceProfileImagesWithUrl(context: Context, data: (ProfileData) -> Unit) {
        images?.forEach {
            if (!imageDataMap.containsKey(it)) {
                imageDataMap[it] = ImageData(it, context.getImageUrl(it))
            }
        }

        data(this)
    }

    fun ArrayList<UserLikes>.replaceLikesImageWithUrl(
        context: Context,
        likesMap: LinkedHashMap<String, UserLikes>
    ): LinkedHashMap<String, UserLikes> {
        val linkedHashMap = LinkedHashMap<String, UserLikes>()
        forEachIndexed { _, profileData ->
            Log.d("IMAGE_URL", "${profileData.user_detail?.profile_pic}")
            var profilePic: String? = ""
            var imageData = ImageData("", "")
            profileData.user_detail?.profile_pic?.let {
                if (likesMap.containsKey(profileData._id)) {
                    if (likesMap[profileData._id]?.user_detail?.imageData?.key != it) {
                        var url = it
                        if (!it.contains("https")) {
                            url = context.getImageUrl(it)
                            profilePic = url
                        }
                        imageData = ImageData(it, url)
                    } else {
                        profilePic = likesMap[profileData._id]?.user_detail?.profile_pic
                        imageData = likesMap[profileData._id]?.user_detail?.imageData ?: ImageData()
                    }
                } else {
                    var url = it
                    if (!it.contains("https")) {
                        url = context.getImageUrl(it)
                        profilePic = url
                    }
                    imageData = ImageData(it, url)

                }

            }
            profileData.user_detail?.profile_pic = profilePic ?: ""
            profileData.user_detail?.imageData = imageData
            profileData._id?.let { linkedHashMap[it] = profileData }
        }

        return linkedHashMap
    }

    fun ArrayList<MatchedData>.replaceUserMatchesImageWithUrl(
        context: Context,
        previousData: LinkedHashMap<String, MatchedData>
    ): LinkedHashMap<String, MatchedData> {
        val linkedHashMap = LinkedHashMap<String, MatchedData>()
        forEachIndexed { _, profileData ->
            var profilePic = ""
            var imageData: ImageData
            profileData.profile_pic.let {
                if (previousData.containsKey(profileData.quickblox_user_id)) {
                    if (previousData[profileData.quickblox_user_id]?.imageData?.key != it) {
                        var url = it
                        if (!it.contains("https")) {
                            url = context.getImageUrl(it)
                            profilePic = url
                        }
                        imageData = ImageData(it, url)
                    } else {
                        profilePic = previousData[profileData.quickblox_user_id]?.profile_pic ?: ""
                        imageData =
                            previousData[profileData.quickblox_user_id]?.imageData ?: ImageData(
                                "",
                                ""
                            )
                    }
                } else {
                    var url = it
                    if (!it.contains("https")) {
                        url = context.getImageUrl(it)
                        profilePic = url
                    }
                    imageData = ImageData(it, url)
                }
            }
            profileData.profile_pic = profilePic
            profileData.imageData = imageData

            linkedHashMap[profileData.quickblox_user_id] = profileData
        }

        return linkedHashMap
    }

    val Number.toPx
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
        )

    fun AppCompatDialog.setDimBackground() {
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window?.setDimAmount(0.4f)
        window?.statusBarColor = Color.TRANSPARENT
    }

    fun Context.checkInternet(isTrue: (Boolean) -> Unit) {
        if (!BaseUtils.isInternetAvailable()) {
            CommonCode.setToast(this, resources.getString(R.string.no_internet_msg))
        } else isTrue(true)
    }

    fun Context.showItsAMatchDialog(
        otherUserImage: String?,
        quickBloxManager: QuickBloxManager,
        dialogId: String,
        callBack: (QBChatDialog?, LayoutItsAMatchBinding, Int) -> Unit
    ) {
        val itsAMatchDialog =
            Dialog(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        val layoutBinding = LayoutItsAMatchBinding.inflate(
            LayoutInflater.from(itsAMatchDialog.context),
            null,
            false
        )

        with(layoutBinding) {
            ivUserFirst.loadImage(HomeActivity.userImageUrl)

            viewClickHandler = object : ViewClickHandler {
                override fun onClick(view: View) {
                    when (view.id) {
                        R.id.tvGoBack -> {
                            itsAMatchDialog.dismiss()
                            callBack(null, layoutBinding, 0)
                        }

                        R.id.tvGoToConnection -> {
//                            layoutBinding.progressBar.visible(isVisible = false)
//                            layoutBinding.tvGoToConnection.visible(isVisible = true)
                            itsAMatchDialog.dismiss()
                            callBack(null, layoutBinding, 1)
                            /* quickBloxManager.getDialogById(dialogId) {
                                 itsAMatchDialog.dismiss()
                                 callBack(it, layoutBinding)
                             }*/
                        }
                    }
                }
            }
            Log.d(Utility::class.simpleName, otherUserImage ?: "")
            ivUserSecond.loadImage(otherUserImage, color = R.color.white)
        }


        itsAMatchDialog.apply {
            setCancelable(false)
            setContentView(layoutBinding.root)
            show()
        }
    }
    /*

        fun Context.introductionDialog(callBack: () -> Unit) {
            val dialog =
                Dialog(this, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
            val layoutBinding = LayoutIntroductionBinding.inflate(
                LayoutInflater.from(dialog.context),
                null,
                false
            )

            with(layoutBinding) {
    //            val constraintSet = ConstraintSet()
    //            val imageView = AppCompatImageView(dialog.context)
    //            imageView.layoutParams = ConstraintLayout.LayoutParams(200, 200)
    //            imageView.setImageDrawable(
    //                ContextCompat.getDrawable(
    //                    dialog.context,
    //                    R.drawable.ic_demo_finger
    //                )
    //            )
    //            imageView.id = View.generateViewId()
    //
    //            constraintSet.clone(layout)
    //            constraintSet.connect(view.id, ConstraintSet.START, view.id, ConstraintSet.END)
    //            constraintSet.connect(view.id, ConstraintSet.TOP, view.id, ConstraintSet.BOTTOM)
    //
    //            constraintSet.applyTo(layout)
    //            layout.addView(imageView, 0)

                root.setOnClickListener {
                    callBack()
                    dialog.dismiss()
                }
            }


            dialog.apply {
                setCancelable(false)
                setContentView(layoutBinding.root)
                show()
            }
        }
    */

    fun Context.welcomeDialog(callBack: () -> Unit) {
        val welcomeDialog =
            Dialog(this, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        val layoutBinding = LayoutFirstTimeBinding.inflate(
            LayoutInflater.from(welcomeDialog.context),
            null,
            false
        )

        with(layoutBinding) {
            viewClickHandler = object : ViewClickHandler {
                override fun onClick(view: View) {
                    when (view.id) {
                        R.id.tvThankYou -> {
                            welcomeDialog.dismiss()
                            callBack()
                        }
                    }
                }
            }

        }


        welcomeDialog.apply {
            setCancelable(false)
            setContentView(layoutBinding.root)
            show()
        }
    }

    fun getDistanceFromLatLonInKM(latLngSecond: LatLng, latLngFirst: LatLng): Double {

        val r = 6378.16  // Radius of the earth in km
        val dLat = deg2rad(latLngSecond.latitude - latLngFirst.latitude)  // deg2rad below
        val dLon = deg2rad(latLngSecond.longitude - latLngFirst.latitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(deg2rad(latLngFirst.latitude)) * cos(deg2rad(latLngSecond.latitude)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val d = r * c // Distance in km
        return (d * 0.62137) / 1000
    }

    private fun deg2rad(deg: Double): Double {
        return deg * (Math.PI / 180)
    }

    fun View.openKeyboard() {
        val imm = this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun Activity.share(msg: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        startActivity(
            Intent.createChooser(
                intent,
                "Share Profile"
            )
        )
    }

    fun Activity.openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(Intent.createChooser(intent, "Open link"))
    }

    fun Activity.openEmail(email: String, msg: String = "") {
        if (isMailClientPresent(this)) {
            val intent2 = Intent()
            intent2.action = Intent.ACTION_SEND
            intent2.type = "message/rfc822"
            intent2.`package` = "com.google.android.gm"
            intent2.putExtra(Intent.EXTRA_EMAIL, Array(1) { email })
            intent2.putExtra(Intent.EXTRA_SUBJECT, "Enter your query...")
            intent2.putExtra(
                Intent.EXTRA_TEXT, msg
            )
//            shareBitmap(intent2)
            startActivity(Intent.createChooser(intent2, "Send mail"))
        } else CommonCode.setToast(
            this,
            "You don't have any application to use this feature."
        )
    }

    private fun isMailClientPresent(context: Context): Boolean {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/html"
        val packageManager = context.packageManager
        val list = if (Build.VERSION.SDK_INT >= 33) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
            )
        } else {
            packageManager.queryIntentActivities(intent, 0)
        }
        return list.size != 0
    }

    fun Context.isVideo(uri: Uri): Boolean {
        val cR = contentResolver
        val mime = MimeTypeMap.getSingleton()
        val type = mime.getExtensionFromMimeType(cR.getType(uri))
        Log.d("mimetype", "$type")
        type?.let {
            return (type.contains("mp4", ignoreCase = true)
                    || type.contains("mkv", ignoreCase = true)
                    || type.contains("mov", true)
                    || type.contains("wmv", true)
                    || type.contains("avi", true))
        } ?: return false
    }

    fun Context.isVideo(uri: String): Boolean {
        return (uri.contains(".mp4", ignoreCase = true)
                || uri.contains(".mkv", ignoreCase = true)
                || uri.contains(".mov", true)
                || uri.contains(".wmv", true)
                || uri.contains(".avi", true))
    }

    fun Context.createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = System.currentTimeMillis().toString()
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        val actualImage = image.absolutePath
        Log.d("DEBUG", "Path: $actualImage")
        return image
    }

    fun Address.getCityAndState(context: Context, data: (String, String, String) -> Unit) {
        var city = ""
        var state = ""
        var country = ""
        var locality = ""
        if (subLocality == null) {
            if (this.locality == null) {
                if (subAdminArea == null) {
                    if (adminArea != null) {
                        city = adminArea
                        locality = adminArea
                    }
                    state = ""
                } else {
                    city = subAdminArea
                    state = adminArea
                    locality = subAdminArea
                }

            } else {
                city = this.locality
                state = adminArea
                locality = this.locality
            }
        } else {
            city = subLocality
            state = adminArea
            locality = subLocality
        }
        country = countryName

        data(city, state, country)
    }

    fun GoogleMap.getDisplayPulseRadius(radius: Float): Float {
        val diff = maxZoomLevel - cameraPosition.zoom
        if (diff < 3) return radius
        if (diff < 3.7) return radius * (diff / 2)
        if (diff < 4.5) return radius * diff
        if (diff < 5.5) return radius * diff * 1.5f
        if (diff < 7) return radius * diff * 2f
        if (diff < 7.8) return radius * diff * 3.5f
        if (diff < 8.5) return (radius * diff) * 5
        if (diff < 10) return radius * diff * 10f
        if (diff < 12) return radius * diff * 18f
        if (diff < 13) return radius * diff * 28f
        if (diff < 16) return radius * diff * 40f
        return if (diff < 18) radius * diff * 60 else radius * diff * 80
    }

    fun getDistanceBetweenTwoLatLngInMiles(from: LatLng?, to: LatLng?): Double =
        (SphericalUtil.computeDistanceBetween(from, to) * 0.62137) / 1000

    fun preventMultipleClicks(callback: () -> Unit) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime >= DEBOUNCE_DURATION) {
            lastClickTime = currentTime
            callback()
        }
    }

}