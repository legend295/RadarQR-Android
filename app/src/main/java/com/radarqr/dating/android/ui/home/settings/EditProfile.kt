package com.radarqr.dating.android.ui.home.settings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.format.DateFormat
import android.util.Log
import android.view.*
import android.widget.SeekBar
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.radarqr.dating.android.utility.chipslayoutmanager.ChipsLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.constant.Constants.CAPITAL_MAN
import com.radarqr.dating.android.constant.Constants.CAPITAL_WOMAN
import com.radarqr.dating.android.constant.Constants.Children.DO_NOT_HAVE_BUT_WANT_BACKEND_VALUE
import com.radarqr.dating.android.constant.Constants.Children.DO_NOT_HAVE_BUT_WANT_FRONTEND_VALUE
import com.radarqr.dating.android.constant.Constants.Children.DO_NOT_WANT_BACKEND_VALUE
import com.radarqr.dating.android.constant.Constants.Children.DO_NOT_WANT_FRONTEND_VALUE
import com.radarqr.dating.android.constant.Constants.Children.HAVE_AND_DO_NOT_WANT_MORE_BACKEND_VALUE
import com.radarqr.dating.android.constant.Constants.Children.HAVE_AND_DO_NOT_WANT_MORE_FRONTEND_VALUE
import com.radarqr.dating.android.constant.Constants.Children.HAVE_AND_OPEN_TO_MORE_BACKEND_VALUE
import com.radarqr.dating.android.constant.Constants.Children.HAVE_AND_OPEN_TO_MORE_FRONTEND_VALUE
import com.radarqr.dating.android.constant.Constants.Children.OPEN_MINDED_BACKEND_VALUE
import com.radarqr.dating.android.constant.Constants.Children.OPEN_MINDED_FRONTEND_VALUE
import com.radarqr.dating.android.constant.Constants.FEMALE
import com.radarqr.dating.android.constant.Constants.MALE
import com.radarqr.dating.android.data.model.hobbies.HobbiesAndInterestData
import com.radarqr.dating.android.databinding.LayoutBottomSheetUpdateProfileBinding
import com.radarqr.dating.android.databinding.LayoutEditAdapterItemBinding
import com.radarqr.dating.android.databinding.LayoutHobbiesInterestBottomSheetBinding
import com.radarqr.dating.android.hotspots.helpers.showSubscriptionSheet
import com.radarqr.dating.android.subscription.SubscriptionStatus
import com.radarqr.dating.android.ui.home.settings.hobbies.adapter.AddHobbiesAndInterestAdapter
import com.radarqr.dating.android.ui.home.settings.model.EditProfileGeneralContentData
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.home.settings.prodileModel.SaveData
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.ui.welcome.mobileLogin.SavePreferenceApiRequest
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.EditProfileGeneralContentTypes
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.enums.SubscriptionPopUpType
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

object EditProfile {

    /*-------------------------------------------------------------------------------------------------*/
    /*-------------------------------- #region Profile field and Preferences update--------------------*/
    /*-------------------------------------------------------------------------------------------------*/

    /**
     * if updating preferences then set value to true else stays false
     * used to check that we are updating profile or updating preferences
     * */
    var fromPreferences = false

    /** if any value change then set true else stays false
     * if true then we are updating profile
     * */
    var anyChangeMade = false

    var contentType: EditProfileGeneralContentTypes = EditProfileGeneralContentTypes.NAME
    var saveData: SaveData? = null

    fun EditProfileGeneralContentData.openBottomSheetToUpdateProfile(
        context: Context,
        fromPreferences: Boolean = false,
        profileData: ProfileData,
        saveData: SaveData?,
        fragment: Fragment,
        request: (EditProfileApiRequest, SavePreferenceApiRequest) -> Unit
    ) {
        anyChangeMade = false
        this@EditProfile.saveData = saveData
        this@EditProfile.contentType = contentType
        this@EditProfile.fromPreferences = fromPreferences
        var editProfileApiRequest = EditProfileApiRequest()
        var savePreferenceApiRequest = SavePreferenceApiRequest()
        val sheet = BottomSheetDialog(context, R.style.DialogStyle)

        val binding = LayoutBottomSheetUpdateProfileBinding.inflate(
            LayoutInflater.from(sheet.context),
            null,
            false
        )
        binding.subscriptionStatus = RaddarApp.getSubscriptionStatus()
        binding.buttonText = "Done"
        binding.title =
            if (this.contentType == EditProfileGeneralContentTypes.NAME) "First Name" else this.title

        binding.etInput.setText(this.value)

        if (this.contentType == EditProfileGeneralContentTypes.NAME ||
            this.contentType == EditProfileGeneralContentTypes.JOB_TITLE ||
            this.contentType == EditProfileGeneralContentTypes.JOB ||
            this.contentType == EditProfileGeneralContentTypes.SCHOOL
        ) {
            binding.etInput.requestFocus()
            binding.etInput.post {
                binding.etInput.setSelection(this.value.length)
            }
        }




        binding.type = this.contentType
        binding.fromPreferences = fromPreferences
        binding.nonNegotiableSwitch.isChecked = nonNegotiableKey
        sheet.behavior.skipCollapsed = true
        sheet.behavior.state = BottomSheetBehavior.STATE_EXPANDED

        if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.PLUS) {
            binding.nonNegotiableSwitch.isEnabled = true
            binding.viewNonNegotiableSwitch.visible(isVisible = false)
        } else {
            binding.nonNegotiableSwitch.isEnabled = binding.nonNegotiableSwitch.isChecked
            binding.viewNonNegotiableSwitch.visible(isVisible = !binding.nonNegotiableSwitch.isChecked)
        }

        binding.nonNegotiableSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked && RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS) {
                binding.nonNegotiableSwitch.isEnabled = false
                binding.viewNonNegotiableSwitch.visible(isVisible = true)
            }
        }



        binding.viewNonNegotiableSwitch.setOnClickListener {
            if (RaddarApp.getSubscriptionStatus() == SubscriptionStatus.NON_PLUS) {
                fragment.showSubscriptionSheet(
                    SubscriptionPopUpType.PREFERENCES,
                    popBackStack = false
                ) {
                    sheet.dismiss()
                }
            }
        }

        when (contentType) {
            EditProfileGeneralContentTypes.HEIGHT -> {
                binding.handleHeight(this, context) {
                    anyChangeMade = true
                    editProfileApiRequest = EditProfileApiRequest(height = it)
                }
            }

            EditProfileGeneralContentTypes.AGE -> {
                binding.handleAge(data = profileData, contentData = this) { age, birthday ->
                    anyChangeMade = true
                    editProfileApiRequest = EditProfileApiRequest(age = age, birthday = birthday)
                }

                binding.scExcludeAge.isChecked = profileData.show_age
            }

            EditProfileGeneralContentTypes.AGE_RANGE -> {
                binding.handleAgeRange()
            }

            EditProfileGeneralContentTypes.HEIGHT_PREF -> {
                binding.handleHeightPref()
            }

            EditProfileGeneralContentTypes.MAX_DISTANCE -> {
                binding.handleMaxDistance(context) {
                    anyChangeMade = true
                    savePreferenceApiRequest = SavePreferenceApiRequest(
                        max_distance = it,
                        max_distance_req = binding.nonNegotiableSwitch.isChecked
                    )
                }
            }

            else -> {
                /** update values in lower case*/
                binding.setAdapter(this) { editProfileModal, list ->
                    anyChangeMade = true
                    val sentValue =
                        editProfileModal.value.ifEmpty { editProfileModal.title.lowercase(Locale.getDefault()) }
                    if (fromPreferences) {
                        savePreferenceApiRequest = when (contentType) {
                            EditProfileGeneralContentTypes.I_WANT_TO_MEET -> {
                                SavePreferenceApiRequest(want_to_meet = sentValue)
                            }

                            EditProfileGeneralContentTypes.DRINKING -> {
                                SavePreferenceApiRequest(
                                    drinking = sentValue,
                                    drinking_req = binding.nonNegotiableSwitch.isChecked
                                )
                            }

                            EditProfileGeneralContentTypes.SMOKING -> {
                                SavePreferenceApiRequest(
                                    smoking = sentValue,
                                    smoking_req = binding.nonNegotiableSwitch.isChecked
                                )
                            }

                            EditProfileGeneralContentTypes.FRIENDLY_420 -> {
                                SavePreferenceApiRequest(marijuana = sentValue)
                            }

                            EditProfileGeneralContentTypes.CHILDREN -> {
                                SavePreferenceApiRequest(
                                    children = sentValue,
                                    children_req = binding.nonNegotiableSwitch.isChecked
                                )
                            }

                            EditProfileGeneralContentTypes.ZODIAC_SIGN -> {
                                val zodiacList = ArrayList<String>()
                                list.forEach {
                                    if (it.isSelected) zodiacList.add(it.value.ifEmpty {
                                        it.title.lowercase(
                                            Locale.getDefault()
                                        )
                                    })
                                }
                                SavePreferenceApiRequest(
                                    zodiac_sign = zodiacList,
                                    zodiac_sign_req = binding.nonNegotiableSwitch.isChecked
                                )
                            }

                            EditProfileGeneralContentTypes.EDUCATION_LEVEL -> {
                                val educationList = ArrayList<String>()
                                list.forEach {
                                    if (it.isSelected) educationList.add(it.value.ifEmpty {
                                        it.title.lowercase(
                                            Locale.getDefault()
                                        )
                                    })
                                }
                                SavePreferenceApiRequest(
                                    education_level = educationList,
                                    education_level_req = binding.nonNegotiableSwitch.isChecked
                                )
                            }

                            else -> {
                                anyChangeMade = false
                                SavePreferenceApiRequest()
                            }
                        }
                    } else
                        editProfileApiRequest = when (contentType) {
                            EditProfileGeneralContentTypes.GENDER -> {
                                EditProfileApiRequest(gender = sentValue)
                            }

                            EditProfileGeneralContentTypes.ZODIAC_SIGN -> {
                                EditProfileApiRequest(zodiac = sentValue)
                            }

                            EditProfileGeneralContentTypes.DRINKING -> {
                                EditProfileApiRequest(drinking = sentValue)
                            }

                            EditProfileGeneralContentTypes.SMOKING -> {
                                EditProfileApiRequest(smoking = sentValue)
                            }

                            EditProfileGeneralContentTypes.LOOKING_FOR -> {
                                EditProfileApiRequest(whoAreYouInterestedIn = sentValue)
                            }

                            EditProfileGeneralContentTypes.FRIENDLY_420 -> {
                                EditProfileApiRequest(marijuana = sentValue)
                            }

                            EditProfileGeneralContentTypes.CHILDREN -> {
                                EditProfileApiRequest(children = sentValue)
                            }

                            EditProfileGeneralContentTypes.EDUCATION_LEVEL -> {
                                EditProfileApiRequest(education_level = sentValue)
                            }

                            else -> {
                                anyChangeMade = false
                                EditProfileApiRequest()
                            }
                        }
                }
            }
        }

        binding.tvContinue.setOnClickListener {
            when (contentType) {
                EditProfileGeneralContentTypes.NAME -> {
                    if (binding.etInput.text != null && binding.etInput.text?.isNotEmpty() == true) {
                        editProfileApiRequest =
                            EditProfileApiRequest(name = binding.etInput.text.toString())
                        request(editProfileApiRequest, savePreferenceApiRequest)
                    }
                }

                EditProfileGeneralContentTypes.JOB -> {
                    if (binding.etInput.text != null && binding.etInput.text?.isNotEmpty() == true) {
                        editProfileApiRequest =
                            EditProfileApiRequest(job = binding.etInput.text.toString())
                        request(editProfileApiRequest, savePreferenceApiRequest)
                    }
                }

                EditProfileGeneralContentTypes.SCHOOL -> {
                    if (binding.etInput.text != null && binding.etInput.text?.isNotEmpty() == true) {
                        editProfileApiRequest =
                            EditProfileApiRequest(school = binding.etInput.text.toString())
                        request(editProfileApiRequest, savePreferenceApiRequest)
                    }
                }

                EditProfileGeneralContentTypes.JOB_TITLE -> {
                    if (binding.etInput.text != null && binding.etInput.text?.isNotEmpty() == true) {
                        editProfileApiRequest =
                            EditProfileApiRequest(job_title = binding.etInput.text.toString())
                        request(editProfileApiRequest, savePreferenceApiRequest)
                    }
                }

                EditProfileGeneralContentTypes.AGE_RANGE -> {
                    savePreferenceApiRequest = SavePreferenceApiRequest(
                        min_age = binding.rangeSeekBar.getCurrentValues().leftValue.roundToInt(),
                        max_age = binding.rangeSeekBar.getCurrentValues().rightValue.roundToInt(),
                        max_age_req = binding.nonNegotiableSwitch.isChecked
                    )
                    request(editProfileApiRequest, savePreferenceApiRequest)
                }

                EditProfileGeneralContentTypes.HEIGHT_PREF -> {
                    savePreferenceApiRequest = SavePreferenceApiRequest(
                        min_height = binding.rangeSeekBar.getCurrentValues().leftValue.toInt(),
                        max_height = binding.rangeSeekBar.getCurrentValues().rightValue.toInt(),
                        max_height_req = binding.nonNegotiableSwitch.isChecked
                    )
                    request(editProfileApiRequest, savePreferenceApiRequest)
                }

                EditProfileGeneralContentTypes.AGE -> {
                    editProfileApiRequest.show_age = binding.scExcludeAge.isChecked
                    request(editProfileApiRequest, savePreferenceApiRequest)
                }

                else -> {
                    if (anyChangeMade)
                        request(editProfileApiRequest, savePreferenceApiRequest)
                }
            }

            /** Updating the non-negotiable value in save preferences request on click because if user changed the
             * switch value after value update or want to change on non-negotiable value hence value changed here
             * */
            if (fromPreferences) {
                when (contentType) {
                    EditProfileGeneralContentTypes.DRINKING -> {
                        savePreferenceApiRequest.drinking_req =
                            binding.nonNegotiableSwitch.isChecked
                    }

                    EditProfileGeneralContentTypes.SMOKING -> {
                        savePreferenceApiRequest.smoking_req = binding.nonNegotiableSwitch.isChecked
                    }

                    EditProfileGeneralContentTypes.FRIENDLY_420 -> {
                        savePreferenceApiRequest.marijuana_req =
                            binding.nonNegotiableSwitch.isChecked
                    }

                    EditProfileGeneralContentTypes.CHILDREN -> {
                        savePreferenceApiRequest.children_req =
                            binding.nonNegotiableSwitch.isChecked
                    }

                    EditProfileGeneralContentTypes.ZODIAC_SIGN -> {
                        savePreferenceApiRequest.zodiac_sign_req =
                            binding.nonNegotiableSwitch.isChecked
                    }

                    EditProfileGeneralContentTypes.EDUCATION_LEVEL -> {
                        savePreferenceApiRequest.education_level_req =
                            binding.nonNegotiableSwitch.isChecked
                    }

                    EditProfileGeneralContentTypes.MAX_DISTANCE -> {
                        savePreferenceApiRequest.max_distance_req =
                            binding.nonNegotiableSwitch.isChecked
                    }

                    EditProfileGeneralContentTypes.AGE_RANGE -> {
                        savePreferenceApiRequest.min_age_req =
                            binding.nonNegotiableSwitch.isChecked
                        savePreferenceApiRequest.max_age_req =
                            binding.nonNegotiableSwitch.isChecked
                    }

                    EditProfileGeneralContentTypes.HEIGHT_PREF -> {
                        savePreferenceApiRequest.min_height_req =
                            binding.nonNegotiableSwitch.isChecked
                        savePreferenceApiRequest.max_height_req =
                            binding.nonNegotiableSwitch.isChecked
                    }

                    else -> {

                    }
                }
                request(editProfileApiRequest, savePreferenceApiRequest)
            }

            sheet.cancel()
        }
        sheet.dismissWithAnimation = true
        sheet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        sheet.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        sheet.window?.setDimAmount(0.4f)
        sheet.window?.statusBarColor = Color.TRANSPARENT
        sheet.setContentView(binding.root)
        sheet.setCanceledOnTouchOutside(true)
        sheet.show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun LayoutBottomSheetUpdateProfileBinding.setAdapter(
        data: EditProfileGeneralContentData,
        callBack: (EditProfileModel, ArrayList<EditProfileModel>) -> Unit
    ) {
        val adapter = EditAdapter(
            data.getList(layoutBinding = this),
            layoutBinding = this
        ) { editProfileModal, list ->
            callBack(
                editProfileModal,
                list
            )
        }
        adapter.setHasStableIds(true)
        rvOptions.adapter = adapter
    }


    private fun LayoutBottomSheetUpdateProfileBinding.handleAge(
        data: ProfileData,
        contentData: EditProfileGeneralContentData,
        callBack: (String, String) -> Unit
    ) {
        data.birthday?.let {
            var age = contentData.value
            tvCalculate.text = "Age $age"
            val format = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
            val format1 = SimpleDateFormat("MM dd yyyy", Locale.getDefault())
            if (it.isNotEmpty()) {
                val newDate: Date? = try {
                    format.parse(it)
                } catch (e: ParseException) {
                    format1.parse(it)
                }
                picker.setDefaultDate(newDate)
            }


            picker.addOnDateChangedListener { displayed, date ->
                val birthDate = DateFormat.format("MMM dd yyyy", date) as String
                Log.e("birthdate", birthDate)
                age = BaseUtils.convertAge(birthDate)
                if (age.contains("-") || age.toInt() < 18) {
                } else {
                    callBack(age, birthDate)
                    tvCalculate.text = "Age $age"
                }
            }
        }
    }

    private fun LayoutBottomSheetUpdateProfileBinding.handleAgeRange() {
        var minAge = 18
        var maxAge = 80
        rangeSeekBar.setDifference(0.15F, 0.15F)
        rangeSeekBar.setType(0)
        tvMinRange.text = minAge.toString()
        tvMaxRange.text = maxAge.toString()
        saveData?.apply {
            if (min_age != 0) {
                minAge = min_age ?: 18
            }
            if (max_age != 0) {
                maxAge = max_age ?: 80
            }
            rangeSeekBar.setCurrentValues(minAge, maxAge)
        }
    }

    private fun LayoutBottomSheetUpdateProfileBinding.handleHeightPref() {
        var minHeight = 92
        var maxHeight = 214
        rangeSeekBar.setDifference(0.09F, 0.09F)
        rangeSeekBar.setType(1)
        rangeSeekBar.setRightTextDifference(28F)
        rangeSeekBar.setRange(minHeight, maxHeight, 1)
        tvMinRange.text = "3'0\""
        tvMaxRange.text = "7'0\""
        saveData?.apply {
            if (min_height != 0) {
                minHeight = min_height ?: 92
            }
            if (max_height != 0) {
                maxHeight = max_height ?: 214
            }
            rangeSeekBar.setCurrentValues(minHeight, maxHeight)
        }
    }

    private fun LayoutBottomSheetUpdateProfileBinding.handleHeight(
        data: EditProfileGeneralContentData,
        context: Context,
        height: (String) -> Unit
    ) {
        val minSeekbar = 92
        val maxSeekbar = 214
        val step = 1
        llSeek.visibility = View.VISIBLE
        if (data.originalValue == "") {
            data.originalValue = minSeekbar.toString()
        }
        val maxValue = BaseUtils.convertToFeetInches(data.originalValue)

        seekBar.requestFocus()
        seekBar.max = ((maxSeekbar - minSeekbar) / step)
        seekBar.progress = (data.originalValue.toInt() - minSeekbar)
        tvSeekNo.text = maxValue
        val width =
            context.resources.displayMetrics.widthPixels - (seekBar.paddingLeft + seekBar.paddingRight + seekBar.marginLeft + seekBar.marginRight + clParent.paddingRight + clParent.paddingLeft)
        val pos =
            width * (seekBar.progress / seekBar.max.toFloat()) + seekBar.x
        tvSeekNo.x = pos

        seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar,
                progress: Int,
                b: Boolean
            ) {
                val calculatedProgress = (minSeekbar + (progress * step))
                val feet =
                    BaseUtils.convertToFeetInches(calculatedProgress.toString())
                tvSeekNo.text = feet
                tvSeekNo.x = getThumbPosition(seekBar)
                height(calculatedProgress.toString())

            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        tvMinRange1.text = "3'0\""
        tvMaxRange1.text = "7'0\""
    }

    private fun LayoutBottomSheetUpdateProfileBinding.handleMaxDistance(
        context: Context,
        maxDistance: (Int) -> Unit
    ) {
        var distance = 100
        saveData?.apply {
            if (max_distance != 0) {
                distance = max_distance ?: 100
            }
            maxDistance(distance)
            seekBar.requestFocus()
            seekBar.progress = distance
            tvSeekNo.text = "${seekBar.progress} mi"
            val width =
                context.resources.displayMetrics.widthPixels - (seekBar.paddingLeft + seekBar.paddingRight + seekBar.marginLeft + seekBar.marginRight + clParent.paddingRight + clParent.paddingLeft) - (2 * seekBar.thumbOffset)
            val pos =
                width * (seekBar.progress / seekBar.max.toFloat()) + seekBar.x
            tvSeekNo.x = pos

            seekBar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar,
                    progress: Int,
                    b: Boolean
                ) {
                    val widthChanging =
                        context.resources.displayMetrics.widthPixels - (seekBar.paddingLeft + seekBar.paddingRight + seekBar.marginLeft + seekBar.marginRight + clParent.paddingRight + clParent.paddingLeft) - (2 * seekBar.thumbOffset)
                    val posChanging =
                        widthChanging * (seekBar.progress / seekBar.max.toFloat()) + seekBar.x
                    tvSeekNo.text = "$progress mi"
                    tvSeekNo.x = posChanging
                    distance = progress
                    maxDistance(distance)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {

                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {

                }
            })
        }
    }

    private fun getThumbPosition(seekBar: SeekBar): Float {
        val width = seekBar.width - seekBar.paddingLeft - seekBar.paddingRight
        return width * (seekBar.progress / seekBar.max.toFloat()) + seekBar.x
    }


    data class EditProfileModel(var title: String, var value: String, var isSelected: Boolean)

    class EditAdapter(
        val list: ArrayList<EditProfileModel>,
        val layoutBinding: LayoutBottomSheetUpdateProfileBinding,
        val callBack: (EditProfileModel, ArrayList<EditProfileModel>) -> Unit
    ) :
        RecyclerView.Adapter<EditAdapter.ViewHolder>() {
        var selectedPosition = 0

        inner class ViewHolder(val binding: LayoutEditAdapterItemBinding) :
            RecyclerView.ViewHolder(binding.root) {
            @SuppressLint("NotifyDataSetChanged")
            fun bind() {
                val data = list[absoluteAdapterPosition]
                binding.data = data
                if (selectedPosition == 0)
                    selectedPosition =
                        if (data.isSelected) absoluteAdapterPosition else 0
                binding.tvTitle.setOnClickListener {
                    if (fromPreferences && (contentType == EditProfileGeneralContentTypes.ZODIAC_SIGN || contentType == EditProfileGeneralContentTypes.EDUCATION_LEVEL)) {
                        /** if selected item is last position then mark other item unselected
                         *  because if open to all selected then it means user is available to all other items
                         * */
                        if (absoluteAdapterPosition == list.size - 1) {
                            list.forEach {
                                it.isSelected = false
                            }
                            data.isSelected = true
                            notifyDataSetChanged()
                            callBack(data, list)
                        } else {
                            /** Check if open to all is selected then mark unselected because other values are getting selected*/

                            if (list[list.size - 1].isSelected) {
                                list[list.size - 1].isSelected = false
                                notifyItemChanged(list.size - 1)
                            }
                            data.isSelected =
                                !data.isSelected
                            notifyItemChanged(absoluteAdapterPosition)
                            callBack(data, list)
                        }
                    } else {
                        if (!list[absoluteAdapterPosition].isSelected) {
                            list[selectedPosition].isSelected = false
                            notifyItemChanged(selectedPosition)
                            selectedPosition = absoluteAdapterPosition
                            list[absoluteAdapterPosition].isSelected = true
                            notifyItemChanged(absoluteAdapterPosition)
                            callBack(list[absoluteAdapterPosition], ArrayList())
                        }
                    }

                    if (list.isNotEmpty()) {
                        layoutBinding.isOpenToAll = list[list.size - 1].isSelected
                        if (list[list.size - 1].isSelected)
                            layoutBinding.nonNegotiableSwitch.isChecked = false
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = LayoutEditAdapterItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind()
        }

        override fun getItemCount(): Int = list.size

        override fun getItemId(position: Int): Long = position.toLong()
    }

    private fun EditProfileGeneralContentData.getList(layoutBinding: LayoutBottomSheetUpdateProfileBinding): ArrayList<EditProfileModel> {
        val list = ArrayList<EditProfileModel>()
        when (contentType) {
            EditProfileGeneralContentTypes.GENDER -> {
                list.clear()
                list.add(
                    EditProfileModel(
                        MALE,
                        MALE.lowercase(),
                        value.lowercase() == MALE.lowercase()
                    )
                )
                list.add(
                    EditProfileModel(
                        FEMALE,
                        FEMALE.lowercase(),
                        value.lowercase() == FEMALE.lowercase()
                    )
                )
                list.add(EditProfileModel("Non Binary", "non-binary", value == "Non Binary"))
            }

            EditProfileGeneralContentTypes.ZODIAC_SIGN -> {
                list.clear()

                list.add(EditProfileModel("Aries", "aries", value == "Aries"))
                list.add(EditProfileModel("Taurus", "taurus", value == "Taurus"))
                list.add(EditProfileModel("Gemini", "gemini", value == "Gemini"))
                list.add(EditProfileModel("Cancer", "cancer", value == "Cancer"))
                list.add(EditProfileModel("Leo", "leo", value == "Leo"))
                list.add(EditProfileModel("Virgo", "virgo", value == "Virgo"))
                list.add(EditProfileModel("Libra", "libra", value == "Libra"))
                list.add(EditProfileModel("Sagittarius", "sagittarius", value == "Sagittarius"))
                list.add(EditProfileModel("Capricorn", "capricorn", value == "Capricorn"))
                list.add(EditProfileModel("Aquarius", "aquarius", value == "Aquarius"))
                list.add(EditProfileModel("Pisces", "pisces", value == "Pisces"))
                if (fromPreferences) {
                    list.add(
                        EditProfileModel(
                            "Open to all",
                            "opentoall",
                            originalValue.replace(" ", "").lowercase() == "opentoall"
                        )
                    )
                    layoutBinding.isOpenToAll = list[list.size - 1].isSelected
                }

                if (fromPreferences) {
                    val values = value.replace(" ", "").split(",")
                    if (values.size > 1) {
                        list.forEachIndexed { index, editProfileModel ->
                            if (values.contains(editProfileModel.title)) {
                                editProfileModel.isSelected = true
                                list[index] = editProfileModel
                                (values as ArrayList).remove(editProfileModel.title)
                            }
                        }
                    }
                }
            }

            EditProfileGeneralContentTypes.DRINKING -> {
                list.clear()
                list.add(EditProfileModel("Yes", "", value == "Yes"))
                list.add(EditProfileModel("No", "", value == "No"))
                list.add(EditProfileModel("Sometimes", "", value == "Sometimes"))
                if (fromPreferences) {
                    list.add(
                        EditProfileModel(
                            "Open to all",
                            "opentoall",
                            originalValue == "opentoall"
                        )
                    )
                    layoutBinding.isOpenToAll = list[list.size - 1].isSelected
                }
            }

            EditProfileGeneralContentTypes.SMOKING, EditProfileGeneralContentTypes.FRIENDLY_420 -> {
                list.clear()
                list.add(EditProfileModel("Yes", "", value == "Yes"))
                list.add(EditProfileModel("No", "", value == "No"))
                list.add(EditProfileModel("Sometimes", "", value == "Sometimes"))
                if (fromPreferences) {
                    list.add(
                        EditProfileModel(
                            "Open to all",
                            "opentoall",
                            originalValue == "opentoall"
                        )
                    )
                    layoutBinding.isOpenToAll = list[list.size - 1].isSelected
                }
            }

            EditProfileGeneralContentTypes.LOOKING_FOR, EditProfileGeneralContentTypes.I_WANT_TO_MEET -> {
                list.clear()
                list.add(
                    EditProfileModel(
                        MALE,
                        "",
                        value.lowercase() == (MALE).lowercase() || value == (CAPITAL_MAN)
                    )
                )
                list.add(
                    EditProfileModel(
                        FEMALE,
                        "",
                        value.lowercase() == FEMALE.lowercase() || value == (CAPITAL_WOMAN)
                    )
                )
                list.add(EditProfileModel("Non Binary", "non-binary", value == "Non-binary"))
                list.add(EditProfileModel(Constants.EVERYONE, "", value == Constants.EVERYONE))
            }

            EditProfileGeneralContentTypes.CHILDREN -> {
                list.clear()
                list.add(
                    EditProfileModel(
                        HAVE_AND_OPEN_TO_MORE_FRONTEND_VALUE,
                        HAVE_AND_OPEN_TO_MORE_BACKEND_VALUE,
                        originalValue.lowercase() == HAVE_AND_OPEN_TO_MORE_BACKEND_VALUE
                    )
                )
                list.add(
                    EditProfileModel(
                        HAVE_AND_DO_NOT_WANT_MORE_FRONTEND_VALUE,
                        HAVE_AND_DO_NOT_WANT_MORE_BACKEND_VALUE,
                        originalValue.lowercase() == HAVE_AND_DO_NOT_WANT_MORE_BACKEND_VALUE
                    )
                )
                list.add(
                    EditProfileModel(
                        DO_NOT_HAVE_BUT_WANT_FRONTEND_VALUE,
                        DO_NOT_HAVE_BUT_WANT_BACKEND_VALUE,
                        originalValue.lowercase() == DO_NOT_HAVE_BUT_WANT_BACKEND_VALUE
                    )
                )
                list.add(
                    EditProfileModel(
                        DO_NOT_WANT_FRONTEND_VALUE,
                        DO_NOT_WANT_BACKEND_VALUE,
                        originalValue.lowercase() == DO_NOT_WANT_BACKEND_VALUE
                    )
                )
                list.add(
                    EditProfileModel(
                        OPEN_MINDED_FRONTEND_VALUE,
                        OPEN_MINDED_BACKEND_VALUE,
                        originalValue.lowercase() == OPEN_MINDED_BACKEND_VALUE
                    )
                )
                /*list.add(
                    EditProfileModel(
                        UNDECIDED_FRONTEND_VALUE,
                        UNDECIDED_BACKEND_VALUE,
                        originalValue.lowercase() == UNDECIDED_BACKEND_VALUE
                    )
                )*/
//                list.add(EditProfileModel("Don't have children", "", value == "Don't have children"))
//                list.add(EditProfileModel("Have children", "", value == "Have children"))
//                list.add(EditProfileModel("Prefer not to say", "", value == "Prefer not to say"))
                if (fromPreferences) {
                    list.add(
                        EditProfileModel(
                            "Open to all",
                            "opentoall",
                            originalValue == "opentoall"
                        )
                    )
                    layoutBinding.isOpenToAll = list[list.size - 1].isSelected
                }
            }

            EditProfileGeneralContentTypes.EDUCATION_LEVEL -> {
                list.clear()
                list.add(
                    EditProfileModel(
                        Constants.EducationLevel.HIGH_SCHOOL_FRONT_END_VALUE,
                        "highschool",
                        value.lowercase() == "High school".lowercase()
                    )
                )
                list.add(
                    EditProfileModel(
                        Constants.EducationLevel.UNDER_GRAD_FRONT_END_VALUE,
                        "",
                        value == "Undergrad"
                    )
                )
                list.add(
                    EditProfileModel(
                        Constants.EducationLevel.POST_GRAD_FRONT_END_VALUE,
                        "",
                        value == "Postgrad"
                    )
                )
                if (fromPreferences) {
                    list.add(
                        EditProfileModel(
                            "Open to all",
                            "opentoall",
                            originalValue.replace(" ", "").lowercase() == "opentoall"
                        )
                    )
                    layoutBinding.isOpenToAll = list[list.size - 1].isSelected
                } else list.add(
                    EditProfileModel(
                        Constants.EducationLevel.DO_NOT_WANT_TO_SAY_FRONT_END_VALUE,
                        "dontwanttosay",
                        originalValue == "dontwanttosay"
                    )
                )

                if (fromPreferences) {
                    val values = value.replace(" ", "").split(",")
                    if (values.size > 1) {
                        list.forEachIndexed { index, editProfileModel ->
                            if (values.contains(editProfileModel.title.replace(" ", ""))) {
                                editProfileModel.isSelected = true
                                list[index] = editProfileModel
                                (values as ArrayList).remove(editProfileModel.title)
                            }
                        }
                    }
                }
            }

            else -> list.clear()
        }
        return list
    }

    /*-------------------------------------------------------------------------------------------------*/
    /*-------------------------- #endregion Profile field and Preferences update-----------------------*/
    /*-------------------------------------------------------------------------------------------------*/


    @SuppressLint("ClickableViewAccessibility")
    fun ArrayList<HobbiesAndInterestData>.updateHobbiesAndInterestBottomSheet(
        context: Context,
        request: (EditProfileApiRequest) -> Unit
    ) {
        val sheet = BottomSheetDialog(context, R.style.DialogStyle)

        val binding = LayoutHobbiesInterestBottomSheetBinding.inflate(
            LayoutInflater.from(sheet.context),
            null,
            false
        )

        val hobbiesAdapter = AddHobbiesAndInterestAdapter(this)
        hobbiesAdapter.setHasStableIds(true)
        binding.rvHobbiesAndInterests.apply {
            layoutManager = ChipsLayoutManager.newBuilder(context)
                .setChildGravity(Gravity.TOP)
                .setMaxViewsInRow(3)
                .setGravityResolver { Gravity.CENTER }
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .build()

            setHasFixedSize(true)
            adapter = hobbiesAdapter

        }

        binding.tvContinue.setOnClickListener {
            request(
                EditProfileApiRequest(
                    hobbies_interest = ArrayList(
                        hobbiesAdapter.selectedList.values
                    )
                )
            )
            sheet.cancel()
        }
        sheet.dismissWithAnimation = true
        sheet.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        sheet.window?.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        sheet.window?.setDimAmount(0.4f)
        sheet.window?.statusBarColor = Color.TRANSPARENT
        sheet.setContentView(binding.root)
        sheet.setCanceledOnTouchOutside(true)
        sheet.show()
    }
}