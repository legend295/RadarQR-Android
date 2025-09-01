package com.radarqr.dating.android.ui.welcome.registerScreens


import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.databinding.FragmentAgeSelectorBinding
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.PreferencesHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class AgeSelectorFragment : BaseFragment<FragmentAgeSelectorBinding>() {
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    var age = ""
    private val preferencesHelper: PreferencesHelper by inject()
    var birthDate = ""
    var alertDialog: AlertDialog? = null
    var tagg = ""
    var selectedDate: Date? = null
    lateinit var progressBar: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.ageSelectoreFragment = this
        val data: Bundle? = arguments

        tagg = data?.getString("tag")!!

        showBackButton(true)
        showToolbar(true)
        showToolbarWhite(false)
        showBackButtonWhite(false)
        showSkip(false)
        binding.tvContinue.setOnClickListener {
            openBottomSheet()
        }

        selectedDate?.let {
            binding.picker.setDefaultDate(it)
        } ?: kotlin.run {
            val c = Calendar.getInstance()
            c.add(Calendar.YEAR, -18) // subtract 2 years from now

            val date = Date()
            date.time =
                (c.time.time - (1000 * 60 * 60 * 24)) /*now - (1000 * 60 * 60 * 24) * 365L * 18L*/
            selectedDate = date
            binding.picker.setDefaultDate(date)
        }


        setDate(date = selectedDate!!)

        binding.picker.addOnDateChangedListener { _, date ->
            selectedDate = date
            setDate(date = date)
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            (it as InitialActivity).hideShowWholeToolbar(isVisible = true)
            it.hideShowSkip(isVisible = false)
        }
    }

    private fun setDate(date: Date) {
        birthDate = DateFormat.format("MMM dd yyyy", date) as String
        var date_birth = DateFormat.format("dd/MM/yyyy", date) as String
        Log.e("birthdate", birthDate)
        val monthString =
            DateFormat.format("MM", date) as String // Jun

        val monthNumber =
            DateFormat.format("dd", date) as String // 06

        val year = DateFormat.format("yyyy", date) as String

        var agee = getAgee(year.toInt(), monthString.toInt(), monthNumber.toInt())!!
        age = BaseUtils.convertAge(birthDate)
        if (age!!.contains("-") || age.toInt() < 18) {
            binding.tvContinue.isEnabled = false
            binding.tvContinue.setBackgroundResource(R.drawable.lightgreen_rect)
        } else {
            binding.tvContinue.isEnabled = true
            binding.tvContinue.setBackgroundResource(R.drawable.green_fill_rect)
            binding.tvCalculate.text = "Age " + age
        }
    }


    override fun getLayoutRes(): Int = R.layout.fragment_age_selector


    fun openBottomSheet() {
        val dialog = AlertDialog.Builder(requireActivity())

        val inflater = this.layoutInflater
        val view: View = inflater.inflate(R.layout.dialog_age_confirm, null)
        dialog.setView(view)
        val tvAge = view.findViewById<TextView>(R.id.tv_age)
        val btnGallery = view.findViewById<TextView>(R.id.tv_continue)
        val cameraBtn = view.findViewById<TextView>(R.id.tv_cancel)
        progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
        tvAge.text = "$age years"
        btnGallery.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            editProfile()
        }
        cameraBtn.setOnClickListener {
            alertDialog?.dismiss()
        }
        alertDialog = dialog.create()
        alertDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.setCanceledOnTouchOutside(false)
        alertDialog?.show()

    }

    private fun getAge(dobString: String): Int {
        var date: Date? = null
        val sdf = SimpleDateFormat("dd/MM/yyyy")
        try {
            date = sdf.parse(dobString)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        if (date == null) return 0
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()
        dob.time = date
        val year = dob[Calendar.YEAR]
        val month = dob[Calendar.MONTH]
        val day = dob[Calendar.DAY_OF_MONTH]
        dob[year, month + 1] = day
        var age = today[Calendar.YEAR] - dob[Calendar.YEAR]
        if (today[Calendar.DAY_OF_YEAR] <= dob[Calendar.DAY_OF_YEAR]) {
            age--
        }
        return age
    }

    private fun getAgee(year: Int, month: Int, day: Int): String? {
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()
        dob[year, month] = day
        var age = today[Calendar.YEAR] - dob[Calendar.YEAR]
//        if (today[Calendar.DAY_OF_YEAR] < dob[Calendar.DAY_OF_YEAR]) {
//            age--
//        }
        if (today.get(Calendar.DAY_OF_YEAR) <= dob.get(Calendar.DAY_OF_YEAR)) {
            age++
        }
        val ageInt = age
        return ageInt.toString()
    }


    @ExperimentalCoroutinesApi
    private fun editProfile() {
        if (view != null)
            lifecycleScope.launch {
                getProfileViewModel.editProfile(
                    EditProfileApiRequest(
                        age = age,
                        birthday = birthDate,
                        StepProgress = 3,
                        show_age = binding.scExcludeAge.isChecked
                    )
                )
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {
                                progressBar.visibility = View.VISIBLE
                            }
                            is DataResult.Success -> {
                                progressBar.visibility = View.GONE
                                it.data.data.let {
                                    runBlocking(Dispatchers.IO) {
                                        preferencesHelper.saveDataEditProfile(
                                            it
                                        )
                                    }
                                }

                                getProfileViewModel.profileData.value = it.data.data
                                if (tagg.equals("1")) {
                                    runBlocking {

                                        preferencesHelper.setValue(
                                            key = PreferencesHelper.PreferencesKeys.STEP_PROGRESS,
                                            value = 3
                                        )
                                    }
                                    findNavController()
                                        .navigate(R.id.action_ageFragment_to_identityFragment)
                                } else {
                                    findNavController().popBackStack()
                                }
                                alertDialog?.dismiss()
                            }
                            is DataResult.Failure -> {
                                progressBar.visibility = View.GONE
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

}
