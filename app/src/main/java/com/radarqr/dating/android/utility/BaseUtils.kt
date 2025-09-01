package com.radarqr.dating.android.utility

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.gms.maps.model.LatLng
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.constant.Constants.EducationLevel.DO_NOT_WANT_TO_SAY_FRONT_END_VALUE
import com.radarqr.dating.android.constant.Constants.FEMALE
import com.radarqr.dating.android.constant.Constants.MALE
import com.radarqr.dating.android.data.model.profile.BasicInfoData
import com.radarqr.dating.android.databinding.LayoutImageVideoViewBinding
import com.radarqr.dating.android.databinding.ProgressBarBinding
import com.radarqr.dating.android.hotspots.model.ContactInfo
import com.radarqr.dating.android.ui.home.settings.prodileModel.LocationDetail
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.home.settings.prodileModel.VenueSubscription
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.utility.Utility.showToast
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


object BaseUtils {


    var pDialog: Dialog? = null

    fun showProgressbar(context: Context) {
        if (pDialog == null) {
            pDialog = Dialog(context)
            val binding = DataBindingUtil.inflate<ProgressBarBinding>(
                LayoutInflater.from(context),
                R.layout.progress_bar,
                null,
                false
            )
            pDialog = Dialog(context)
            pDialog!!.setContentView(binding.root)
            pDialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            pDialog!!.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            pDialog!!.window?.setDimAmount(0.5f)
            pDialog!!.setCancelable(false)
            pDialog!!.show()


        }


    }

    fun hideProgressbar() {
        if (pDialog != null) {
            pDialog!!.apply {
                dismiss()
            }
            pDialog = null

        }

    }

    fun showMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun getCrashlyticsMessage(lineNumber: String, apiName: String, className: String): String =
        "$lineNumber , $apiName , $className"

    fun Context.isGpsEnabled(): Boolean {
        val lm = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun startInitialActivity() {
        val intent = Intent(RaddarApp.getInstance(), InitialActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(
            Constants.TYPE,
            Constants.REPORT
        )
        RaddarApp.getInstance().startActivity(intent)
    }

    fun getModifiedString(value: String, type: Int): String {
        return when (value.lowercase()) {
            "men", "man", MALE.lowercase() -> {
//                if (type == 0) Constants.CAPITAL_MAN else Constants.CAPITAL_MEN
                MALE
            }

            "women", "woman", FEMALE.lowercase() -> {
//                if (type == 0) Constants.CAPITAL_WOMAN else Constants.CAPITAL_WOMEN
                FEMALE
            }

            else -> {
                if (type == 0) "Non Binary" else Constants.EVERYONE
            }
        }
    }


    fun convertToFeetInches(str: String?): String {
        str?.let {
            return try {
                val value = str.toDouble()
//                val feet = Math.floor(value / 30.48).toInt()
//                val inches = Math.round(value / 2.54 - feet * 12).toInt()
                val feet = /*floor(value / 30.48).toInt()*/ (0.0328 * value).toInt()
                val inches = /*(value / 2.54 - feet * 12).roundToInt()*/
                    (0.3937 * value - feet * 12).toInt()
                "$feet' $inches\""
            } catch (e: Exception) {
                ""
            }
        } ?: kotlin.run {
            return ""
        }
    }

    fun getLocationString(data: ProfileData?): String {
        data?.let {
            return when {
                !it.location.city.isNullOrEmpty() && !it.location.state.isNullOrEmpty() -> {
                    it.location.city.makeEveryFirstWordCapital() + ", " + it.location.state.uppercase()
                }

                it.location.city != null -> {
                    it.location.city.makeEveryFirstWordCapital()
                }

                it.location.state != null -> {
                    it.location.state.makeEveryFirstWordCapital()
                }

                it.location.country != null -> {
                    it.location.country.makeEveryFirstWordCapital()
                }

                else -> {
                    ""
                }
            }
        }

        return ""
    }

    fun getLocationString(data: ContactInfo?): String {
        data?.let {
            return when {
                !it.locality.isNullOrEmpty() && !it.state.isNullOrEmpty() -> {
                    it.locality + ", " + it.state!!.makeEveryFirstWordCapital()
                }

                !it.locality.isNullOrEmpty() -> {
                    it.locality!!.makeEveryFirstWordCapital()
                }

                !it.city.isNullOrEmpty() && !it.state.isNullOrEmpty() -> {
                    it.city!!.makeEveryFirstWordCapital() + ", " + it.state!!.uppercase()
                }

                it.city != null -> {
                    it.city!!.makeEveryFirstWordCapital()
                }

                it.state != null -> {
                    it.state!!.makeEveryFirstWordCapital()
                }

                it.country != null -> {
                    it.country!!.makeEveryFirstWordCapital()
                }

                else -> {
                    ""
                }
            }
        }

        return ""
    }

    fun String.makeEveryFirstWordCapital(): String {
        val words = split(" ").toMutableList()
        var output = ""

        for (word in words) {
            output += word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } + " "
        }
        output = output.trim()
        return output
    }

    fun getLocationString(data: LocationDetail?): String {
        data?.let {
            return when {
                !it.city.isNullOrEmpty() && !it.state.isNullOrEmpty() -> {
                    it.city.makeEveryFirstWordCapital() + ", " + it.state.uppercase()
                }

                it.city != null -> {
                    it.city.makeEveryFirstWordCapital()
                }

                it.state != null -> {
                    it.state.makeEveryFirstWordCapital()
                }

                it.country != null -> {
                    it.country.makeEveryFirstWordCapital()
                }

                else -> {
                    ""
                }
            }
            /*return when {
               *//* it.city != null && it.city.toString()
                    .isNotEmpty() && it.country != null && it.country.toString()
                    .isNotEmpty() -> {
                    it.city.toString() *//**//*+ ", " + it.location.country.toString()*//**//*
                }
                it.city != null -> {
                    it.city.toString()
                }

                it.country != null -> {
                    it.country.toString()
                }

                else -> {
                    ""
                }*//*

            }*/
        }

        return ""
    }


    fun getImageUrl(context: Context, imageName: String): String {
        val urlFromS3 = S3Utils.generatesShareUrl(
            context, imageName
        )

        return urlFromS3.replace(" ", "%20")
    }


    fun handleClick(
        list: ArrayList<LayoutImageVideoViewBinding>,
        playerList: TreeMap<Int, SimpleExoPlayer>
    ) {
        try {

            list[0].ivVolume.setOnClickListener {
                if (playerList[0]!!.volume == 0f) {
                    playerList[0]!!.volume = 100f
                    list[0].ivVolume.setImageResource(R.drawable.ic_unmute)
                } else {
                    playerList[0]!!.volume = 0f
                    list[0].ivVolume.setImageResource(R.drawable.ic_baseline_volume_mute_24)
                }
            }
            list[1].ivVolume.setOnClickListener {
                if (playerList[1]?.volume == 0f) {
                    playerList[1]?.volume = 100f
                    list[1].ivVolume.setImageResource(R.drawable.ic_unmute)
                } else {
                    playerList[1]?.volume = 0f
                    list[1].ivVolume.setImageResource(R.drawable.ic_baseline_volume_mute_24)
                }
            }
            list[2].ivVolume.setOnClickListener {
                if (playerList[2]!!.volume == 0f) {
                    playerList[2]!!.volume = 100f
                    list[2].ivVolume.setImageResource(R.drawable.ic_unmute)
                } else {
                    playerList[2]!!.volume = 0f
                    list[2].ivVolume.setImageResource(R.drawable.ic_baseline_volume_mute_24)
                }
            }
            list[3].ivVolume.setOnClickListener {
                if (playerList[3]!!.volume == 0f) {
                    playerList[3]!!.volume = 100f
                    list[3].ivVolume.setImageResource(R.drawable.ic_unmute)
                } else {
                    playerList[3]!!.volume = 0f
                    list[3].ivVolume.setImageResource(R.drawable.ic_baseline_volume_mute_24)
                }
            }
            list[4].ivVolume.setOnClickListener {
                if (playerList[4]!!.volume == 0f) {
                    playerList[4]!!.volume = 100f
                    list[4].ivVolume.setImageResource(R.drawable.ic_unmute)
                } else {
                    playerList[4]!!.volume = 0f
                    list[4].ivVolume.setImageResource(R.drawable.ic_baseline_volume_mute_24)
                }
            }
            list[5].ivVolume.setOnClickListener {
                if (playerList[5]!!.volume == 0f) {
                    playerList[5]!!.volume = 100f
                    list[5].ivVolume.setImageResource(R.drawable.ic_unmute)
                } else {
                    playerList[5]!!.volume = 0f
                    list[5].ivVolume.setImageResource(R.drawable.ic_baseline_volume_mute_24)
                }
            }
        } catch (e: IndexOutOfBoundsException) {

        }

    }

    fun scrollListener(
        scrollView: NestedScrollView,
        list: ArrayList<LayoutImageVideoViewBinding>,
        recyclerTopView: View,
        recyclerBottomView: View,
        position: (Int) -> Unit
    ) =
        View.OnScrollChangeListener { _, _, _, _, _ ->
            val scrollBounds = Rect()
            scrollView.getHitRect(scrollBounds)

            if (list[0].viewTop.getLocalVisibleRect(scrollBounds) && list[0].viewBottom.getLocalVisibleRect(
                    scrollBounds
                )
            ) {
                position(0)
            }

            if (list[1].viewTop.getLocalVisibleRect(scrollBounds) && list[1].viewBottom.getLocalVisibleRect(
                    scrollBounds
                )
            ) {
                position(1)
            }

            if (list[2].viewTop.getLocalVisibleRect(scrollBounds) && list[2].viewBottom.getLocalVisibleRect(
                    scrollBounds
                )
            ) {
                position(2)
            }

            if (list[3].viewTop.getLocalVisibleRect(scrollBounds) && list[3].viewBottom.getLocalVisibleRect(
                    scrollBounds
                )
            ) {
                position(3)
            }

            if (list[4].viewTop.getLocalVisibleRect(scrollBounds) && list[4].viewBottom.getLocalVisibleRect(
                    scrollBounds
                )
            ) {
                position(4)
            }

            if (list[5].viewTop.getLocalVisibleRect(scrollBounds) && list[5].viewBottom.getLocalVisibleRect(
                    scrollBounds
                )
            ) {
                position(5)
            }

            if (recyclerTopView.getLocalVisibleRect(scrollBounds) && recyclerBottomView.getLocalVisibleRect(
                    scrollBounds
                )
            ) {
                position(6)
            }
        }


    fun getListItem(it: ProfileData, list: (ArrayList<BasicInfoData>) -> Unit) {
        val listItem = ArrayList<BasicInfoData>()
        /*if (it.gender != null) {
            when (it.gender) {
                "man", "men", MALE.lowercase() -> {
                    listItem.add(
                        BasicInfoData(
//                            "gender,${Constants.CAPITAL_MAN}",
                            "gender#${MALE}", 0
                            *//*R.drawable.ic_male_sign*//*
                        )
                    )

                }
                "woman", "women", FEMALE.lowercase() -> {
                    listItem.add(
                        BasicInfoData(
//                            "gender,${Constants.CAPITAL_WOMAN}",
                            "gender#${FEMALE}", 0
                            *//*R.drawable.ic_female_sign*//*
                        )
                    )

                }
                else -> {
                    listItem.add(
                        BasicInfoData(
                            "gender#Non Binary", 0
                            *//*R.drawable.ic_non_binary*//*
                        )
                    )
                }
            }
        }*/

        /* if (!it.show_age) {
             listItem.add(
                 BasicInfoData(
                     "age,${it.age}",
                     R.drawable.ic_age
                 )
             )
         }*/

        try {
            if (it.height != null && it.height!!.isNotEmpty()) {
                val value = it.height!!.toDouble()
                val feet = Math.floor(value / 30.48).toInt()
                val inches = Math.round(value / 2.54 - feet * 12).toInt()
                val ouput = "$feet' $inches\""
                listItem.add(
                    BasicInfoData(
                        "height#$ouput", 0
                        /*R.drawable.ic_scale*/
                    )
                )
            }
        } catch (e: Exception) {

        }

        it.drinking?.let {
            /*if yes then Prefer drinking, if no then Prefer not to drinking, if sometimes then Prefer drinking sometimes*/
            val drinkingText = when {
                it.lowercase() == "yes" -> "Drinks"
                it.lowercase() == "no" -> "Doesn't Drink"
                it.lowercase() == "sometimes" -> "Drink Sometimes"
                else -> null
            }
            drinkingText?.apply {
                listItem.add(
                    BasicInfoData(
                        "drinking#${drinkingText}", 0
//                    "drinking,${it.getStringWithFirstWordCapital()}", 0
                        /*R.drawable.ic_drinking*/
                    )
                )
            }
        }
        it.smoking?.let {
            val smokingText = when {
                it.lowercase() == "yes" -> "Smokes"
                it.lowercase() == "no" -> "Doesn't Smoke"
                it.lowercase() == "sometimes" -> "Smoke Sometimes"
                else -> null
            }
            smokingText?.apply {
                listItem.add(
                    BasicInfoData(
                        "smoking#${smokingText}", 0
//                        "smoking,${it.getStringWithFirstWordCapital()}", 0
                        /*R.drawable.ic_smoking*/
                    )
                )
            }
        }

        it.marijuana?.let {
            val marijuanaText = when {
                it.lowercase() == "yes" -> "420 Friendly"
                it.lowercase() == "no" -> "Not 420 Friendly"
                it.lowercase() == "sometimes" -> "420 Friendly"
                else -> null
            }
            marijuanaText?.apply {
                listItem.add(
                    BasicInfoData(
                        "marijuana#${marijuanaText}", 0
//                        "smoking,${it.getStringWithFirstWordCapital()}", 0
                        /*R.drawable.ic_smoking*/
                    )
                )
            }
        }

        it.children?.let {
            if (it.lowercase() != "prefer not to say")
                listItem.add(
                    BasicInfoData(
                        "children#${it.getChildrenViewValue()}", 0
                        /*R.drawable.ic_person*/
                    )
                )
        }
        it.zodiac?.let {
            listItem.add(
                BasicInfoData(
                    "zodiac#${it.getStringWithFirstWordCapital()}", 0
                    /*it.getImageAccordingToZodiacSign()*/
                )
            )
        }

        /* it.location.country?.let {
             if (it.trim().isNotEmpty())
                 listItem.add("location,$it")
         }*/

        /*if (it.location.locality != null && it.location.locality.isNotEmpty()) {
            listItem.add(
                BasicInfoData(
                    "location," + it.location.locality,
                    R.drawable.ic_map_pin
                )
            )
        } else {
            if (it.location.city != null && it.location.city.isNotEmpty()) {
                listItem.add(
                    BasicInfoData(
                        "location," + it.location.city,
                        R.drawable.ic_map_pin
                    )
                )
            }
        }*/

        list(listItem)
    }

    fun String.getChildrenValue(): String {
        return when (this.lowercase()) {
            Constants.Children.HAVE_AND_OPEN_TO_MORE_BACKEND_VALUE -> Constants.Children.HAVE_AND_OPEN_TO_MORE_FRONTEND_VALUE
            Constants.Children.HAVE_AND_DO_NOT_WANT_MORE_BACKEND_VALUE -> Constants.Children.HAVE_AND_DO_NOT_WANT_MORE_FRONTEND_VALUE
            Constants.Children.DO_NOT_HAVE_BUT_WANT_BACKEND_VALUE -> Constants.Children.DO_NOT_HAVE_BUT_WANT_FRONTEND_VALUE
            Constants.Children.DO_NOT_WANT_BACKEND_VALUE -> Constants.Children.DO_NOT_WANT_FRONTEND_VALUE
            Constants.Children.OPEN_MINDED_BACKEND_VALUE -> Constants.Children.OPEN_MINDED_FRONTEND_VALUE
            Constants.Children.UNDECIDED_BACKEND_VALUE -> Constants.Children.UNDECIDED_FRONTEND_VALUE
            Constants.Children.OPEN_TO_ALL_BACKEND_VALUE -> Constants.Children.OPEN_TO_ALL_FRONTEND_VALUE
            else -> ""
        }
    }

    fun String.getChildrenViewValue(): String {
        return when (this.lowercase()) {/*Have children, open to more
Have children, don’t want more
Want children
Don’t want children
Open to children
*/
            Constants.Children.HAVE_AND_OPEN_TO_MORE_BACKEND_VALUE -> "Have children, open to more"
            Constants.Children.HAVE_AND_DO_NOT_WANT_MORE_BACKEND_VALUE -> "Have children, don’t want more"
            Constants.Children.DO_NOT_HAVE_BUT_WANT_BACKEND_VALUE -> "Want children"
            Constants.Children.DO_NOT_WANT_BACKEND_VALUE -> "Don’t want children"
            Constants.Children.OPEN_MINDED_BACKEND_VALUE -> "Open to children"
            Constants.Children.OPEN_TO_ALL_BACKEND_VALUE -> Constants.Children.OPEN_TO_ALL_FRONTEND_VALUE
            else -> ""
        }
    }

    private fun String.getImageAccordingToZodiacSign(): Int {
        return when (this.lowercase()) {
            "Aries", "aries" -> {
                R.drawable.ic_aries_zodiac
            }

            "Taurus", "taurus" -> {
                (R.drawable.ic_taurus_zodiac)
            }

            "Gemini", "gemini" -> {
                (R.drawable.ic_gemini_zodiac)
            }

            "Cancer", "cancer" -> {
                (R.drawable.ic_cancer_zodiac)
            }

            "Leo", "leo" -> {
                (R.drawable.ic_leo_zodiac)
            }

            "Virgo", "virgo" -> {
                (R.drawable.ic_virgo_zodiac)
            }

            "Libra", "libra" -> {
                (R.drawable.ic_libra)
            }

            "Scorpio", "scorpio" -> {
                (R.drawable.ic_scorpio_zodiac)
            }

            "Sagittarius", "sagittarius" -> {
                (R.drawable.ic_sagittarius_zodiac)
            }

            "Capricorn", "capricorn" -> {
                (R.drawable.ic_capricorn)
            }

            "Aquarius", "aquarius" -> {
                (R.drawable.ic_acquarius)
            }

            "Pisces", "pisces" -> {
                (R.drawable.ic_pisces)
            }

            else -> {
                R.drawable.ic_aries_zodiac
            }
        }
    }

    fun getWorkItem(it: ProfileData, list: (ArrayList<BasicInfoData>) -> Unit) {
        val listItem = ArrayList<BasicInfoData>()
        val job = it.job ?: ""
        val jobTitle = it.job_title ?: ""
        val school = it.school ?: ""
        val education = it.education_level ?: ""
        var finalJob = ""
        var finalEducation = ""
        when {
            job.trim().isNotEmpty() && jobTitle.trim().isNotEmpty() -> {
                finalJob =
                    "${Constants.JOB}#${jobTitle.getStringWithFirstWordCapital()} at ${job.getStringWithFirstWordCapital()}"
            }

            else -> {
                if (job.isNotEmpty())
                    finalJob = "${Constants.JOB}#${job.getStringWithFirstWordCapital()}"
                if (jobTitle.isNotEmpty())
                    finalJob = "${Constants.JOB}#${jobTitle.getStringWithFirstWordCapital()}"
            }
        }
        if (finalJob.isNotEmpty())
            listItem.add(BasicInfoData(value = finalJob, R.drawable.ic_work))

        when {
            school.trim().isNotEmpty() && education.trim().isNotEmpty() -> {
                finalEducation =
                    "${Constants.EDUCATION}#${education.getStringWithFirstWordCapital()} at ${school.getStringWithFirstWordCapital()}"
            }

            else -> {
                if (school.isNotEmpty())
                    finalEducation =
                        "${Constants.EDUCATION}#${school.getStringWithFirstWordCapital()}"
                if (education.isNotEmpty())
                    finalEducation =
                        "${Constants.EDUCATION}#${education.getStringWithFirstWordCapital()}"
            }
        }
        if (finalEducation.isNotEmpty())
            listItem.add(BasicInfoData(value = finalEducation, R.drawable.ic_education))

        list(listItem)
    }

    fun RecyclerView?.getCurrentPosition(): Int {
        return (this?.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
    }

    fun stopAllPlayers(playerList: TreeMap<Int, SimpleExoPlayer>) {
        for (player in playerList.values) {
            player.playWhenReady = false
//            player.release()
        }

    }

    fun convertAge(it: ProfileData?): String {
        it?.let {
            val format = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
            val formatSecond = SimpleDateFormat("MM dd yyyy", Locale.getDefault())
            var date: Date? = null
            it.birthday?.let { birthday ->
                date = try {
                    format.parse(birthday)
                } catch (e: Exception) {
                    formatSecond.parse(birthday)
                }
            }
            val cal = Calendar.getInstance()
            val today = Calendar.getInstance()
            return if (date != null) {
                cal.time = date!!
                var age = today.get(Calendar.YEAR) - cal.get(Calendar.YEAR)
                if (today.get(Calendar.DAY_OF_YEAR) < cal.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                "$age"
            } else it.age ?: ""
        } ?: kotlin.run {
            return ""
        }
    }

    fun RecyclerView.addOnPageChangedListener(listener: (pos: Int) -> Unit) {

        val layoutManager = this.layoutManager as LinearLayoutManager
        var lastPos = -1

        this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // RecyclerView's onScrollStateChanged() is unstable and doesn't work as expected at least in 27.0.2.
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val pos = layoutManager.findFirstCompletelyVisibleItemPosition()
                if (pos != -1 && pos != lastPos) {
                    lastPos = pos
                    listener.invoke(pos)
                }
            }

        })

    }

    @SuppressLint("SimpleDateFormat")
    fun convertAge(birthday: String?): String {
        val format = SimpleDateFormat("MMM dd yyyy")
        val formatSecond = SimpleDateFormat("MM dd yyyy")
        var date: Date? = null
        birthday?.let { day ->
            date = try {
                format.parse(day)
            } catch (e: Exception) {
                try {
                    formatSecond.parse(day)
                } catch (e: Exception) {
                    return ""
                }
            }
        }
        val cal = Calendar.getInstance()
        val today = Calendar.getInstance()
        return if (date != null) {
            cal.time = date!!
            var age = today.get(Calendar.YEAR) - cal.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < cal.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            "$age"
        } else ""
    }

    fun isInternetAvailable(): Boolean {
        val connectivityManager =
            RaddarApp.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetworkInfo.also { return it != null && it.isConnected }
    }

    fun Context.isInternetAvailable(msg: String = ""): Boolean {
        return if (this@BaseUtils.isInternetAvailable()) {
            true
        } else {
            showToast(msg.ifEmpty { getString(R.string.no_internet_msg) })
            false
        }
    }

    fun View.showKeyboard() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, 0)
    }

//    fun Context.hideKeyboard(){
//        val imm =
//            (activity as InitialActivity).getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        imm.hideSoftInputFromWindow(view.windowToken, 0)
//    }

    fun getImage(value: String): String {
        return if (value.contains(Constants.MP4)) value.split(".")[0] + "_thumb.webp" else value
    }

    fun String.getStringWithFirstWordCapital(): String {
        return when (this) {
            "opentoall" -> "Open to all"
            "highschool" -> "High school"
            "dontwanttosay" -> DO_NOT_WANT_TO_SAY_FRONT_END_VALUE
            "" -> ""
            else -> substring(0, 1).uppercase(Locale.getDefault()) + substring(1).lowercase()
        }
    }

    fun getStringWithFirstWordCapital(value: String) {
        value.substring(0, 1).uppercase(Locale.getDefault()) + value.substring(1).lowercase()
    }

    fun playerListener(position: Int, view: View, errorException: (PlaybackException?) -> Unit) =
        object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateString: String = when (playbackState) {
                    ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
                    ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
                    ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
                    ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
                    else -> "UNKNOWN_STATE             -"
                }
                Log.d("VIDEO_PLAY_LISTENER", "changed state to $stateString $position")

                when (playbackState) {
                    ExoPlayer.STATE_BUFFERING -> view.visibility =
                        View.VISIBLE

                    ExoPlayer.STATE_READY -> view.visibility =
                        View.GONE

                    else -> view.visibility = View.GONE
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                super.onPlayerError(error)
                Log.d("VIDEO_PLAY_ERROR", " onPlayerError $error")
                errorException(error)
            }

            override fun onPlayerErrorChanged(error: PlaybackException?) {
                super.onPlayerErrorChanged(error)
                Log.d("VIDEO_PLAY_ERROR", " onPlayerErrorChanged $error")
            }
        }

    fun ProfileData.getLatLngFromProfile(): LatLng? {
        if (location.latlon?.coordinates == null) return null
        val lat = location.latlon.coordinates[1]
        val lng = location.latlon.coordinates[0]
        SharedPrefsHelper.saveLastEditProfileLocation(LatLng(lat, lng))
        return LatLng(lat, lng)
    }

    fun VenueSubscription.isPackageValid(): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return try {
            val purchaseDate = dateFormat.parse(purchaseDate) ?: return false
            val todayDate = Date()
            val expirationDate = dateFormat.parse(expirationDate) ?: return false
            todayDate <= expirationDate
        } catch (e: ParseException) {
            e.printStackTrace()
            false
        }
    }

    fun Context.openApplicationDetailsSettings() {
        val intent = Intent()
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.setData(uri)
        startActivity(intent)
    }

    /**
     * Check if the stored timestamp is more than 1 week old or not
     * if more than 1 week old then return true else false
     * and if old then 1 week then store new current timestamp in shared preferences
     * */
    fun checkTokenTimeStampExpiredOrNot(): Boolean {
        // get Stored timestamp from local cache
        val storedTime = SharedPrefsHelper.getTokenTimeStamp()
        // if stored time is 0 which means new user is trying to login for first time/ or user logged out and logging in again
        if (storedTime == 0L) {
            SharedPrefsHelper.saveTokenTimeStamp()
            // return true so that new token will get updated over server
            return true
        }
        // get current time and store in variable
        val currentTimeStamp = Calendar.getInstance()
        // get current time and store in variable
        val storedTimeStamp = Calendar.getInstance()
        // update storedTimeStamp variable with stored time in cache
        storedTimeStamp.timeInMillis = storedTime
        // calculate difference between current time and stored time in cache
        val daysDifference =
            (currentTimeStamp.timeInMillis - storedTimeStamp.timeInMillis) / (1000 * 60 * 60 * 24)
        // if difference is more than 1 week then return true else false
        if (daysDifference >= 7) {
            SharedPrefsHelper.saveTokenTimeStamp()
            return true
        } else return false
    }
}