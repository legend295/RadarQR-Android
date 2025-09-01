package com.radarqr.dating.android.ui.home.settings

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants.CAPITAL_MAN
import com.radarqr.dating.android.constant.Constants.CAPITAL_MEN
import com.radarqr.dating.android.constant.Constants.CAPITAL_WOMAN
import com.radarqr.dating.android.constant.Constants.CAPITAL_WOMEN
import com.radarqr.dating.android.constant.Constants.MAN
import com.radarqr.dating.android.constant.Constants.WOMAN
import com.radarqr.dating.android.databinding.FragmentCommonBinding
import com.radarqr.dating.android.ui.home.settings.adapter.CommonAdapter
import com.radarqr.dating.android.ui.home.settings.model.CommonModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.mobileLogin.SavePreferenceApiRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.seekbar.RangeSeekBar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hearsilent.discreteslider.DiscreteSlider.OnValueChangedListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class CommonFragment : BaseFragment<FragmentCommonBinding>() {
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    var specialChars = "/*!@#$%^&*()\"{}_[]|\\?/<>,.:-'';§£¥..."

    private val preferencesHelper: PreferencesHelper by inject()
    var ListItem: ArrayList<CommonModel> = ArrayList()

    var zodiac_value: ArrayList<String> = ArrayList()
    var tag = 1
    var name = ""
    var name_val = ""
    var type = ""
    var hight = ""
    var multi_value = 0
    var value = ""
    var value_distance = 0
    var screen_tag = ""
    var min_age = 0
    var max_age = 0
    var min_distance = 0
    var max_distance = 0
    var min_height = 0
    var max_height = 0
    var date_of_birth = ""
    private val image_filter = StringBuilder()
    lateinit var progressBar: ProgressBar
    var alertDialog: AlertDialog? = null
    var age = ""
    var birthDate = ""
    lateinit var commonAdapter: CommonAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.commonFragment = this
        binding.viewModel = getProfileViewModel

        showToolbarLayout(false)

        showNavigation(false)

        val data: Bundle? = arguments

        tag = data?.getInt("tag")!!
        if (data.containsKey("date_of_birth")) {
            date_of_birth = data.getString("date_of_birth")!!
        }
        multi_value = data.getInt("multi")
        if (data.containsKey("value")) {
            value = data.getString("value")!!
        }
        if (data.containsKey("value_distance")) {
            value_distance = data.getInt("value_distance")!!
        }
        screen_tag = data.getString("screen_tag")!!
        try {
            if (data.containsKey("min_hight")) {
                min_height = data.getInt("min_hight")
            }
            if (data.containsKey("max_hight")) {
                max_height = data.getInt("max_hight")
            }
            if (data.containsKey("min_age")) {
                min_age = data.getInt("min_age")
            }
            if (data.containsKey("max_age")) {
                max_age = data.getInt("max_age")
            }
            if (data.containsKey("zodiac_value")) {
                zodiac_value = data.getStringArrayList("zodiac_value")!!
            }
            if (data.containsKey("education_value")) {
                zodiac_value = data.getStringArrayList("education_value")!!
            }
            if (data.containsKey("min_distance")) {
                min_distance = data.getInt("min_distance")
            }
            if (data.containsKey("max_distance")) {
                max_distance = data.getInt("max_distance")
            }

        } catch (e: Exception) {

        }




        when (tag) {
            1 -> {
                gender()
                binding.layoutToolbarTitle.text = "Gender"
                binding.rvItems.visibility = View.VISIBLE
                binding.clHight.visibility = View.GONE
                binding.clCalender.visibility = View.GONE
                binding.clName.visibility = View.GONE
                binding.clRange.visibility = View.GONE
            }
            2 -> {
                ZodicSign()
                for (j in 0 until ListItem.size) {
                    for (i in 0 until zodiac_value.size) {
                        if (zodiac_value[i].equals(ListItem[j].name, ignoreCase = true)) {
                            ListItem[j].isSelected = true
                        }
                    }
                }
                binding.layoutToolbarTitle.text = requireActivity().getString(R.string.zodic_sign)
                binding.rvItems.visibility = View.VISIBLE
                binding.clHight.visibility = View.GONE
                binding.clCalender.visibility = View.GONE
                binding.clName.visibility = View.GONE
                binding.clRange.visibility = View.GONE
            }
            3 -> {
                drinking()
                binding.layoutToolbarTitle.text = "Drinking"
                binding.rvItems.visibility = View.VISIBLE
                binding.clHight.visibility = View.GONE
                binding.clCalender.visibility = View.GONE
                binding.clName.visibility = View.GONE
                binding.clRange.visibility = View.GONE
            }
            4 -> {
                lookingFor()
                binding.layoutToolbarTitle.text = "Looking For"
                binding.rvItems.visibility = View.VISIBLE
                binding.clHight.visibility = View.GONE
                binding.clCalender.visibility = View.GONE
                binding.clName.visibility = View.GONE
                binding.clRange.visibility = View.GONE
            }
            5 -> {
                children()
                binding.layoutToolbarTitle.text = "Children"
                binding.rvItems.visibility = View.VISIBLE
                binding.clHight.visibility = View.GONE
                binding.clCalender.visibility = View.GONE
                binding.clName.visibility = View.GONE
                binding.clRange.visibility = View.GONE
            }
            15 -> {
                Education_level()
                for (j in 0 until ListItem.size) {
                    for (i in 0 until zodiac_value.size) {
                        if (zodiac_value[i].equals(ListItem[j].name, ignoreCase = true)) {
                            ListItem[j].isSelected = true
                        }
                    }
                }
                binding.layoutToolbarTitle.text = "Education Level"
                binding.rvItems.visibility = View.VISIBLE
                binding.clHight.visibility = View.GONE
                binding.clCalender.visibility = View.GONE
                binding.clName.visibility = View.GONE
                binding.clRange.visibility = View.GONE
            }
            7 -> {
                smoking()
                binding.layoutToolbarTitle.text = "Smoking"
                binding.clRange.visibility = View.GONE
                binding.rvItems.visibility = View.VISIBLE
                binding.clHight.visibility = View.GONE
                binding.clCalender.visibility = View.GONE
                binding.clName.visibility = View.GONE
            }
            8 -> {

                binding.layoutToolbarTitle.text = "Height"
                binding.rvItems.visibility = View.GONE
                binding.rangeSeekBar.setType(1)
                binding.rangeSeekBar.setDifference(0.09F, 0.09F)
                binding.rangeSeekBar.setRightTextDifference(28F)
                if (screen_tag == "1") {
                    binding.clHight.visibility = View.GONE
                    binding.clRange.visibility = View.VISIBLE
                    if (min_height == 0) {
                        min_height = 92
                    }
                    if (max_height == 0) {
                        max_height = 214
                    }
                    var min_h = convertTofeetInches(min_height.toString())
                    var max_h = convertTofeetInches(max_height.toString())

                    binding.rangeSeekBar.setRange(92, 214, 1)

                    binding.rangeSeekBar.setCurrentValues(min_h.toInt(), max_h.toInt())
                    binding.tvMinRange.text = "3'00\""
                    binding.tvMaxRange.text = "7'00\""

                    binding.rangeSeekBar.listenerRealTime =
                        object : RangeSeekBar.OnRangeSeekBarRealTimeListener {
                            override fun onValuesChanging(minValue: Float, maxValue: Float) {

                            }

                            override fun onValuesChanging(minValue: Int, maxValue: Int) {

                            }
                        }

                } else {
                    val minSeekbar = 92
                    val maxSeekbar = 214
                    val step = 1
                    binding.llSeek.visibility = View.VISIBLE
                    binding.clHight.visibility = View.GONE
                    binding.clRange.visibility = View.GONE
                    if (value == "") {
                        value = minSeekbar.toString()
                    }
                    var max_value = convertTofeetInchesHeight(value)

                    binding.seekBarLuminosite.requestFocus()
                    binding.seekBarLuminosite.max = ((maxSeekbar - minSeekbar) / step)
//                    binding.seekBarLuminosite.min = 92
                    binding.seekBarLuminosite.progress = (value.toInt() - minSeekbar)
//                        (value.toInt() - if (value.toInt() < 122) 92 else 114)
                    val valuee =
                        binding.seekBarLuminosite.progress * ((resources.displayMetrics.widthPixels - (binding.seekBarLuminosite.paddingLeft + binding.seekBarLuminosite.paddingLeft + binding.seekBarLuminosite.marginLeft)) - 2 * binding.seekBarLuminosite.thumbOffset) / 100
                    binding.tvSeekNo.text = max_value
                    val width =
                        resources.displayMetrics.widthPixels - (binding.seekBarLuminosite.paddingLeft + binding.seekBarLuminosite.paddingRight + binding.seekBarLuminosite.marginLeft + binding.seekBarLuminosite.marginRight)
                    val pos =
                        width * (binding.seekBarLuminosite.progress / binding.seekBarLuminosite.max.toFloat()) + binding.seekBarLuminosite.x
                    binding.tvSeekNo.x = pos/*getThumbPosition(binding.seekBarLuminosite)*/
//                        binding.seekBarLuminosite.x + valuee + binding.seekBarLuminosite.thumbOffset / 2

                    binding.seekBarLuminosite.setOnSeekBarChangeListener(object :
                        OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar,
                            progress: Int,
                            b: Boolean
                        ) {
                            val calculatedProgress = (minSeekbar + (progress * step))
                            val `val` =
                                progress * ((resources.displayMetrics.widthPixels - (binding.seekBarLuminosite.paddingLeft + binding.seekBarLuminosite.paddingLeft + binding.seekBarLuminosite.marginLeft)) - 2 * seekBar.thumbOffset) / 100
                            var max_value =
                                convertTofeetInchesHeight(calculatedProgress.toString() /*+ if (progress < 50) 92 else 114*/)
                            binding.tvSeekNo.text = max_value
                            val width = seekBar.width - seekBar.paddingLeft - seekBar.paddingRight
                            val pos = width * (seekBar.progress / seekBar.max.toFloat())
//                            binding.tvSeekNo.x = seekBar.x + `val` - seekBar.thumbOffset / 2
                            binding.tvSeekNo.x = getThumbPosition(seekBar)
                            hight = /*(progress + if (progress < 50) 92 else 114).toString()*/
                                calculatedProgress.toString()
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) {

                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar) {

                        }
                    })
                    binding.tvMinRange1.text = "3'00\""
                    binding.tvMaxRange1.text = "7'00\""


                    //                    value = value.replace("cm", "")
                    //                    binding.rangeSeekbar2.progress = value.toInt()
                }


                binding.clCalender.visibility = View.GONE
                binding.clName.visibility = View.GONE
            }
            14 -> {


                binding.layoutToolbarTitle.text = "Maximum Distance"
                binding.rvItems.visibility = View.GONE
                if (screen_tag.equals("1")) {
                    binding.clHight.visibility = View.GONE
                    binding.clRange.visibility = View.GONE
                    binding.llSeek.visibility = View.VISIBLE
                    //                if (min_distance == 0) {
                    //                    min_distance = 0
                    //                }
                    if (value_distance == 0) {
                        max_distance = 100
                    }
                    binding.seekBarLuminosite.requestFocus()
                    binding.seekBarLuminosite.progress = max_distance
                    val valuee =
                        max_distance * ((resources.displayMetrics.widthPixels - (binding.seekBarLuminosite.paddingLeft + binding.seekBarLuminosite.paddingLeft + binding.seekBarLuminosite.marginLeft)) - 2 * binding.seekBarLuminosite.thumbOffset) / binding.seekBarLuminosite.max
                    binding.tvSeekNo.text = "" + binding.seekBarLuminosite.progress + " mi"
                    binding.tvSeekNo.x =
                        binding.seekBarLuminosite.x + valuee + binding.seekBarLuminosite.thumbOffset / 2

                    binding.seekBarLuminosite.setOnSeekBarChangeListener(object :
                        OnSeekBarChangeListener {
                        override fun onProgressChanged(
                            seekBar: SeekBar,
                            progress: Int,
                            b: Boolean
                        ) {
                            val `val` =
                                progress * ((resources.displayMetrics.widthPixels - (binding.seekBarLuminosite.paddingLeft + binding.seekBarLuminosite.paddingLeft + binding.seekBarLuminosite.marginLeft)) - 2 * seekBar.thumbOffset) / seekBar.max
                            binding.tvSeekNo.text = "" + progress + " mi"
                            binding.tvSeekNo.x = seekBar.x + `val` + seekBar.thumbOffset / 2
                            max_distance = progress
                        }

                        override fun onStartTrackingTouch(seekBar: SeekBar) {

                        }

                        override fun onStopTrackingTouch(seekBar: SeekBar) {

                        }
                    })
                    binding.rangeSeekBar.visibility = View.GONE
                    //                binding.rangeSeekBar.setRange(0, 100, 1)
                    //                binding.rangeSeekBar.setCurrentValues(min_distance, max_distance)
                    //                binding.tvMinRange.text = "0 mi"
                    //                binding.tvMaxRange.text = "100 mi"
                } else {
                    binding.clHight.visibility = View.VISIBLE
                    binding.clRange.visibility = View.GONE
                }

                binding.clCalender.visibility = View.GONE
                binding.clName.visibility = View.GONE
            }
            9 -> {
                if (!value.equals("")) {
                    name = value
                    //                binding.etName.setText(value)
                    getProfileViewModel.firstName.value = name
                }
                binding.layoutToolbarTitle.text = "First Name"
                binding.rvItems.visibility = View.GONE
                binding.clHight.visibility = View.GONE
                binding.clCalender.visibility = View.GONE
                binding.clName.visibility = View.VISIBLE
                binding.etName.addTextChangedListener {
                    getProfileViewModel.firstName.value =
                        (it?.toString() ?: "").replace("[^A-Za-z0-9 ]".toRegex(), "")
                    binding.etName.setSelection(it?.length ?: 0)
                }

            }
            10 -> {

                binding.rvItems.visibility = View.GONE
                binding.clHight.visibility = View.GONE

                if (screen_tag.equals("1")) {
                    binding.layoutToolbarTitle.text = "Age Range"

                    binding.clCalender.visibility = View.GONE
                    binding.clRange.visibility = View.VISIBLE
                    binding.rangeSeekBar.setDifference(0.15F, 0.15F)
                    binding.rangeSeekBar.setType(0)
                    if (min_age == 0) {
                        min_age = 18
                    }
                    if (max_age == 0) {
                        max_age = 80
                    }
                    binding.rangeSeekBar.setCurrentValues(min_age, max_age)
                    binding.tvMinRange.text = "18"
                    binding.tvMaxRange.text = "80"
                    binding.rangeSeekBar.listenerRealTime =
                        object : RangeSeekBar.OnRangeSeekBarRealTimeListener {
                            override fun onValuesChanging(minValue: Float, maxValue: Float) {

                            }

                            override fun onValuesChanging(minValue: Int, maxValue: Int) {
                                /* if (maxValue - minValue < 10) {
                                     binding.rangeSeekBar.isEnabled = false
                                 } else {

                                 }*/
                            }
                        }
                } else {
                    binding.layoutToolbarTitle.text = "Choose Age"
                    if (value != "") {
                        age = value
                        binding.tvCalculate.text = "Age $value"
                    }
                    binding.clCalender.visibility = View.VISIBLE
                    binding.clRange.visibility = View.GONE
                }
                binding.clName.visibility = View.GONE
            }
            16 -> {
                binding.etCommon.hint = requireActivity().getString(R.string.job_title)

                if (!value.equals("")) {
                    name = value
                    binding.etCommon.setText(value)

                }
                binding.layoutToolbarTitle.text = requireActivity().getString(R.string.job_title)
                binding.etCommon.visibility = View.VISIBLE


            }
            17 -> {
                binding.etCommon.hint = requireActivity().getString(R.string.school)

                if (value != "") {
                    name = value
                    binding.etCommon.setText(value)

                }
                binding.layoutToolbarTitle.text = requireActivity().getString(R.string.school)
                binding.etCommon.visibility = View.VISIBLE


            }
            18 -> {
                binding.etCommon.hint = requireActivity().getString(R.string.job)

                if (value != "") {
                    name = value
                    binding.etCommon.setText(value)

                }
                binding.layoutToolbarTitle.text = requireActivity().getString(R.string.job)
                binding.etCommon.visibility = View.VISIBLE
            }
        }

//age selector

        val format = SimpleDateFormat("MMM dd yyyy")
        if (date_of_birth.isNotEmpty()) {
            val newDate: Date = format.parse(date_of_birth)
            binding.picker.setDefaultDate(newDate)
        }


        binding.picker.addOnDateChangedListener { displayed, date ->
            birthDate = DateFormat.format("MMM dd yyyy", date) as String
            Log.e("birthdate", birthDate)
            val monthString =
                DateFormat.format("MM", date) as String // Jun

            val monthNumber =
                DateFormat.format("dd", date) as String // 06

            val year = DateFormat.format("yyyy", date) as String

//            age = getAge(year.toInt(), monthString.toInt(), monthNumber.toInt())!!
            age = convertAge(birthDate ?: "")
            if (age.contains("-") || age.toInt() < 18) {
            } else {
                binding.tvCalculate.text = "Age " + age
            }
        }

        //adapter for items list
        commonAdapter = CommonAdapter(
            ListItem,
            multi_value,
            value,
            requireActivity(),
            object : CommonAdapter.ItemCallback {
                override fun onItemClick(item_name: String, item_type: String, sent_value: String) {
                    if (sent_value == "") {
                        name = item_name.lowercase()
                        type = item_type
                        if (multi_value == 1) {
                            zodiac_value = ArrayList()
                            image_filter.setLength(0)
                            for (i in 0 until ListItem.size) {
                                if (ListItem[i].isSelected) {
                                    zodiac_value.add(ListItem[i].name.lowercase())
                                }
                            }

                        }
                    } else {
                        name = sent_value.lowercase()
                        type = item_type
                    }

                }

            }
        )
        binding.rvItems.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.VERTICAL, false
            )
            setItemViewCacheSize(ListItem.size)
            setHasFixedSize(true)
            commonAdapter.setHasStableIds(true)
            itemAnimator = DefaultItemAnimator()
            adapter = commonAdapter
            adapter?.notifyDataSetChanged()
        }

        binding.rangeSeekbar2.setOnValueChangedListener(object : OnValueChangedListener() {
            override fun onValueChanged(progress: Int, fromUser: Boolean) {
                super.onValueChanged(progress, fromUser)
                Log.i("DiscreteSlider", "Progress: $progress, fromUser: $fromUser")
                convertTofeetInches(progress.toString())
            }

            override fun onValueChanged(minProgress: Int, maxProgress: Int, fromUser: Boolean) {
                super.onValueChanged(minProgress, maxProgress, fromUser)
                Log.i(
                    "DiscreteSlider",
                    "MinProgress: " + minProgress + ", MaxProgress: " + maxProgress +
                            ", fromUser: " + fromUser
                )
            }
        })

        binding.activityToolbarBack.setOnClickListener {
            handleBack()
        }

    }

    private fun getThumbPosition(seekBar: SeekBar): Float {
        val width = seekBar.width - seekBar.paddingLeft - seekBar.paddingRight
        return width * (seekBar.progress / seekBar.max.toFloat()) + seekBar.x
    }

    fun convertAge(it: String): String {
        val format = SimpleDateFormat("MMM dd yyyy")
        val formatSecond = SimpleDateFormat("MM dd yyyy")
        var date: Date? = null
        it.let { birthday ->
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
            }/*else age --*/
            "$age"
        } else ""
    }

    fun handleBack() {
        if (!BaseUtils.isInternetAvailable()) {
            findNavController().popBackStack()
            return
        }
        binding.progressBar.visibility = View.VISIBLE
        if (screen_tag == "1") {
            when (tag) {
                2 -> {
                    savePreferencs(SavePreferenceApiRequest(zodiac_sign = zodiac_value))
                }
                15 -> {

                    savePreferencs(SavePreferenceApiRequest(education_level = zodiac_value))
                }
                3 -> {
                    name = checkValue()
                    if (name.isNotEmpty())
                        savePreferencs(SavePreferenceApiRequest(drinking = name.lowercase(Locale.getDefault())))
                }
                4 -> {
                    name = checkValue()
                    savePreferencs(SavePreferenceApiRequest(want_to_meet = name.lowercase(Locale.getDefault())))
                }
                5 -> {
                    name = checkValue()
                    savePreferencs(SavePreferenceApiRequest(children = name.lowercase(Locale.getDefault())))
                }
                7 -> {
                    name = checkValue()
                    if (name.isNotEmpty())
                        savePreferencs(SavePreferenceApiRequest(smoking = name.lowercase(Locale.getDefault())))
                }
                8 -> {

                    savePreferencs(
                        SavePreferenceApiRequest(
                            min_height = binding.rangeSeekBar.getCurrentValues().leftValue.toInt(),
                            max_height = binding.rangeSeekBar.getCurrentValues().rightValue.toInt()
                        )
                    )

                }
                10 -> {
                    savePreferencs(
                        SavePreferenceApiRequest(
                            min_age = binding.rangeSeekBar.getCurrentValues().leftValue.toInt(),
                            max_age = binding.rangeSeekBar.getCurrentValues().rightValue.toInt()
                        )
                    )

                }
                14 -> {
                    savePreferencs(
                        SavePreferenceApiRequest(
                            min_distance = 0,
                            max_distance = binding.seekBarLuminosite.progress
                        )
                    )

                }
            }
        } else {
            if (tag == 1) {
                name = checkValue()
                editProfile(EditProfileApiRequest(gender = name.lowercase()))
            } else if (tag == 2) {
                name = checkValue()
                editProfile(EditProfileApiRequest(zodiac = name.lowercase()))
            } else if (tag == 3) {
                name = checkValue()
                editProfile(EditProfileApiRequest(drinking = name.lowercase()))
            } else if (tag == 4) {
                name = checkValue()
                editProfile(EditProfileApiRequest(whoAreYouInterestedIn = name.lowercase()))
            } else if (tag == 5) {
                name = checkValue()
                editProfile(EditProfileApiRequest(children = name.lowercase()))
            } else if (tag == 6) {
                name = checkValue()
                editProfile(EditProfileApiRequest(ethnicity = name.lowercase()))
            } else if (tag == 7) {
                name = checkValue()
                editProfile(EditProfileApiRequest(smoking = name.lowercase()))
            } else if (tag == 8) {
                if (value == "") {
                    if (hight == "")

                        hight = value
                }
                editProfile(EditProfileApiRequest(height = hight))
            } else if (tag == 9) {

                if (binding.etName.text.isBlank()) {
                    binding.progressBar.visibility = View.GONE
                    CommonCode.setToast(requireActivity(), "Please enter first name")
                } else {

                    val p: Pattern = Pattern.compile("[^A-Za-z0-9 ]")
                    val m: Matcher = p.matcher(binding.etName.text.toString())
                    if (m.find()) {
                        binding.progressBar.visibility = View.GONE
                        CommonCode.setToast(requireActivity(), "No Special Character Allowed")
                    } else {
                        val name = binding.etName.text.toString()

                        val editName = name.split(" ")[0]
                        val lastName = name.split(" ")

                        var finalLastName = ""

                        for (pos in lastName.indices) {
                            if (pos != 0)
                                finalLastName += "${lastName[pos]} "
                        }


                        val newName = editName.toLowerCase(Locale.getDefault())
                            .capitalize() + " $finalLastName"
                        editProfile(EditProfileApiRequest(name = newName.trim()))
                    }

                }
            } else if (tag == 10) {

                if (age.toInt() < 18) {
                    binding.progressBar.visibility = View.GONE
                    CommonCode.setToast(requireActivity(), "Age can not be less than 18")

                } else {
                    if (birthDate != "") {
                        editProfile(EditProfileApiRequest(age = age, birthday = birthDate))
                    } else {
                        editProfile(EditProfileApiRequest(age = age))

                    }

                }
            } else if (tag == 16) {
                val jobTitle = binding.etCommon.text.toString()
                if (jobTitle.trim().isEmpty()) {
                    findNavController().navigateUp()
                    return
                }
                editProfile(EditProfileApiRequest(job_title = jobTitle))
            } else if (tag == 17) {
                val school = binding.etCommon.text.toString()
                if (school.trim().isEmpty()) {
                    findNavController().navigateUp()
                    return
                }
                editProfile(EditProfileApiRequest(school = school))
            } else if (tag == 18) {
                val job = binding.etCommon.text.toString()
                if (job.trim().isEmpty()) {
                    findNavController().navigateUp()
                    return
                }
                editProfile(EditProfileApiRequest(job = job))
            } else if (tag == 15) {
                name = checkValue()
                editProfile(EditProfileApiRequest(education_level = name.lowercase()))
            }
        }
    }


    fun removeNonAlphanumeric(stri: String): String {
        var str = stri
        str = str.replace(
            "[^a-zA-Z0-9]".toRegex(), ""
        )
        return str
    }

    private fun checkValue(): String {
        var name_new = ""
        name_new = if (type == "") {
            if (value == "") {
                ListItem[0].name
            } else {
                value
            }
        } else {
            if (name_val == "") {
                name
            } else {
                name_val
            }
        }
        return name_new
    }

    fun gender() {
        var commonModel = CommonModel(CAPITAL_MAN, "1", "man")
        ListItem.add(commonModel)
        commonModel = CommonModel(CAPITAL_WOMAN, "1", "woman")
        ListItem.add(commonModel)
        commonModel = CommonModel("Non Binary", "1", "non-binary")
        ListItem.add(commonModel)
    }

    fun ZodicSign() {
        var commonModel: CommonModel = CommonModel("Aries", "2")
        ListItem.add(commonModel)
        commonModel = CommonModel("Taurus", "2")
        ListItem.add(commonModel)
        commonModel = CommonModel("Gemini", "2")
        ListItem.add(commonModel)
        commonModel = CommonModel("Cancer", "2")
        ListItem.add(commonModel)
        commonModel = CommonModel("Leo", "2")
        ListItem.add(commonModel)
        commonModel = CommonModel("Virgo", "2")
        ListItem.add(commonModel)
        commonModel = CommonModel("Libra", "2")
        ListItem.add(commonModel)
        commonModel = CommonModel("Scorpio", "2")
        ListItem.add(commonModel)
        commonModel = CommonModel("Sagittarius", "2")
        ListItem.add(commonModel)
        commonModel = CommonModel("Capricorn", "2")
        ListItem.add(commonModel)
        commonModel = CommonModel("Aquarius", "2")
        ListItem.add(commonModel)
        commonModel = CommonModel("Pisces", "2")
        ListItem.add(commonModel)
        if (screen_tag.equals("1")) {
            commonModel = CommonModel("Open to all", "7")
            ListItem.add(commonModel)
        }

    }

    fun drinking() {
        var commonModel: CommonModel = CommonModel("Yes", "3")
        ListItem.add(commonModel)
        commonModel = CommonModel("No", "3")
        ListItem.add(commonModel)
        commonModel = CommonModel("Sometimes", "3")
        ListItem.add(commonModel)
        if (screen_tag.equals("1")) {
            commonModel = CommonModel("Open to all", "7")
            ListItem.add(commonModel)
        }

    }

    fun lookingFor() {
        var commonModel: CommonModel = CommonModel(CAPITAL_MEN, "4", MAN)
        ListItem.add(commonModel)
        commonModel = CommonModel(CAPITAL_WOMEN, "4", WOMAN)
        ListItem.add(commonModel)
        commonModel = CommonModel("Everyone", "4", "everyone")
        ListItem.add(commonModel)

    }

    fun children() {
        var commonModel: CommonModel = CommonModel("Don't have children", "5")
        ListItem.add(commonModel)
        commonModel = CommonModel("Have children", "5")
        ListItem.add(commonModel)
        commonModel = CommonModel("Prefer Not to Say", "5")
        ListItem.add(commonModel)
        if (screen_tag.equals("1")) {
            commonModel = CommonModel("Open to all", "7")
            ListItem.add(commonModel)
        }

    }

    fun Education_level() {
        var commonModel = CommonModel("High School", "9")
        ListItem.add(commonModel)
        commonModel = CommonModel("Undergrad", "9")
        ListItem.add(commonModel)
        commonModel = CommonModel(resources.getString(R.string.post_grad), "9")
        ListItem.add(commonModel)
        if (screen_tag != "1") {
            commonModel = CommonModel(resources.getString(R.string.dont_want_to_say), "9")
            ListItem.add(commonModel)
        }
        if (screen_tag == "1") {
            commonModel = CommonModel("Open to all", "9")
            ListItem.add(commonModel)
        }

    }

    fun ethnicity() {

        var commonModel: CommonModel = CommonModel("American Indian", "6")
        ListItem.add(commonModel)
        commonModel = CommonModel("Black/African Descent", "6")
        ListItem.add(commonModel)
        commonModel = CommonModel("East Asian", "6")
        ListItem.add(commonModel)
        commonModel = CommonModel("Hispanic/Latino", "6")
        ListItem.add(commonModel)
        commonModel = CommonModel("Middle Eastern", "6")
        ListItem.add(commonModel)
        commonModel = CommonModel("Pacificc Islander", "6")
        ListItem.add(commonModel)
        commonModel = CommonModel("South Asianibra", "6")
        ListItem.add(commonModel)
        commonModel = CommonModel("White/Caucasian", "6")
        ListItem.add(commonModel)
        commonModel = CommonModel("Other", "6")
        ListItem.add(commonModel)
        commonModel = CommonModel("Prefer Not to Say", "6")
        ListItem.add(commonModel)

    }

    fun smoking() {
        var commonModel: CommonModel = CommonModel("Yes", "7")
        ListItem.add(commonModel)
        commonModel = CommonModel("No", "7")
        ListItem.add(commonModel)
        commonModel = CommonModel("Sometimes", "7")
        ListItem.add(commonModel)
        if (screen_tag.equals("1")) {
            commonModel = CommonModel("Open to all", "7")
            ListItem.add(commonModel)
        }

    }

    override fun getLayoutRes(): Int = R.layout.fragment_common


    @ExperimentalCoroutinesApi
    private fun editProfile(editprofileApiRequest: EditProfileApiRequest) {
        lifecycleScope.launch {
            getProfileViewModel.editProfile(editprofileApiRequest)
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {

                        }
                        is DataResult.Success -> {
                            it.data.data.let {
                                runBlocking(Dispatchers.IO) {
                                    preferencesHelper.saveDataEditProfile(
                                        it
                                    )
                                }
                            }

                            getProfileViewModel.profileData.value = it.data.data


                            binding.progressBar.visibility = View.GONE
                            getProfileViewModel.stateSaved = true
                            findNavController().navigateUp()
                        }
                        is DataResult.Failure -> {
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/edit-profile",
                                requireActivity().componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/edit-profile Api Error"))
                        }

                        else -> {}
                    }
                }

        }
    }

    @ExperimentalCoroutinesApi
    private fun savePreferencs(savePreferenceApiRequest: SavePreferenceApiRequest) {
        lifecycleScope.launch {
            getProfileViewModel.savePreferences(savePreferenceApiRequest)
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {

                        }
                        is DataResult.Success -> {

                            binding.progressBar.visibility = View.GONE
                            getProfileViewModel.stateSaved = true
                            findNavController()
                                .navigateUp()
                        }
                        is DataResult.Failure -> {
                            reportApiError(
                                Exception().stackTrace[0].lineNumber,
                                it.statusCode ?: 0,
                                "user/save-preferences",
                                requireActivity().componentName.className,
                                it.message ?: ""
                            )

                            FirebaseCrashlytics.getInstance()
                                .recordException(Exception("user/save-preferences Api Error"))
                        }

                        else -> {}
                    }
                }

        }
    }

    @Throws(NumberFormatException::class)
    private fun convertTofeetInchesHeight(str: String): String {
        val value = str.toDouble()
        val feet = /*floor(value / 30.48).toInt()*/ (0.0328 * value).toInt()
        val inches = /*(value / 2.54 - feet * 12).roundToInt()*/(0.3937 * value - feet * 12).toInt()
        val ouput = "$feet' $inches\""
        if (screen_tag == "1") {

        } else {
            binding.tvHightText.text = ouput
        }
        hight = str
        return ouput
    }

    @Throws(NumberFormatException::class)
    private fun convertTofeetInches(str: String): String {
        val value = str.toDouble()
        val feet = /*Math.floor(value / 30.48).toInt()*/ (0.0328 * value).toInt()
        val inches = /*Math.round(value / 2.54 - feet * 12).toInt()*/
            (0.3937 * value - feet * 12).toInt()
        val ouput = "$feet' $inches\""
        if (screen_tag.equals("1")) {

        } else {
            binding.tvHightText.text = ouput
        }
        hight = str
        return hight
    }

    fun openBottomSheet() {
        val dialog = AlertDialog.Builder(requireActivity())

        val inflater = this.layoutInflater
        val view: View = inflater.inflate(R.layout.dialog_age_confirm, null)
        dialog.setView(view)
        val tvAge = view.findViewById<TextView>(R.id.tv_age)
        val btnGallery = view.findViewById<TextView>(R.id.tv_continue)
        val cameraBtn = view.findViewById<TextView>(R.id.tv_cancel)

        progressBar = view.findViewById(R.id.progress_bar)
        tvAge.text = "$age years"

        btnGallery.setOnClickListener {
            binding.progressBar.visibility = View.GONE
            editProfile(EditProfileApiRequest(age = age, birthday = birthDate))
        }

        cameraBtn.setOnClickListener {
            binding.progressBar.visibility = View.GONE
            alertDialog?.dismiss()
        }
        alertDialog = dialog.create()
        alertDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.setCanceledOnTouchOutside(false)
        alertDialog?.show()

    }

    private fun getAge(year: Int, month: Int, day: Int): String? {
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()
        dob[year, month] = day
        var age = today[Calendar.YEAR] - dob[Calendar.YEAR]
//        if (today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
//            age--
//        }
        val ageInt = age
        return ageInt.toString()
    }

    fun method(str: String?): String? {
        var str = str
        if (str != null && str.length > 0 && str[str.length - 1] == ',') {
            str = str.substring(0, str.length - 1)
        }
        return str
    }

}
