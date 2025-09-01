package com.radarqr.dating.android.ui.welcome.registerScreens

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.databinding.FragmentFirstNameBinding
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.Utility.showToast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class FirstNameFragment : BaseFragment<FragmentFirstNameBinding>() {
    private val getProfileViewModel: GetProfileViewModel by viewModel()
    private val preferencesHelper: PreferencesHelper by inject()
    var tagg = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.firstNameFragment = this
        binding.viewModel = getProfileViewModel
        binding.etName.setText(getProfileViewModel.firstName.value ?: "")
        val data: Bundle? = arguments
        try {
            tagg = data?.getString("tag")!!
            if (tagg == "1") {
//                showToolbarLayout(true)
//                showBackButton(false)
//                showProgress(true)
//                setProgress(14)
//                binding.tvWhats.visibility = View.VISIBLE
//                binding.tvVerification.visibility = View.VISIBLE
                showTitle(false)

            } else {
//                showToolbarLayout(true)
//                showProgress(false)
//                binding.tvWhats.visibility = View.GONE
//                binding.tvVerification.visibility = View.GONE
                showTitle(true)
                showBackButton(false)
                setTitle("Name")
            }
        } catch (e: Exception) {

        }
        showToolbar(true)
        showToolbarWhite(false)
        showBackButtonWhite(false)

        showSkip(false)
        binding.tvContinue.setOnClickListener {
            if (binding.etName.text?.isBlank() == true) {
                requireContext().showToast("Please enter first name")
            } else if (binding.etUsername.text?.isBlank() == true) {
                requireContext().showToast("Please enter user name")
            } else {
                val p: Pattern = Pattern.compile("[^A-Za-z0-9 ]")
                val m: Matcher = p.matcher(binding.etName.text.toString())
                if (m.find()) {
                    CommonCode.setToast(requireActivity(), "No Special Character Allowed")
                } else {
                    binding.progressBar.visibility = View.VISIBLE
                    editProfile()
                }


            }
        }

        getProfileViewModel.firstName.value = ""

        binding.etName.addTextChangedListener {
            getProfileViewModel.firstName.value =
                (it?.toString() ?: "").replace("[^A-Za-z0-9 ]".toRegex(), "")
            binding.etName.setSelection(it?.length ?: 0)
        }


        /*  binding.etName.filters = arrayOf(
              InputFilter { src, start, end, dst, dstart, dend ->
                  if (src == "") { // for backspace
                      return@InputFilter src
                  }
                  if (src.toString().matches("[a-zA-Z 0-9]+".toRegex())) {
                      src
                  } else ""
              }
          )*/
    }

    override fun getLayoutRes(): Int = R.layout.fragment_first_name

    override fun onResume() {
        super.onResume()
        activity?.let {
            (it as InitialActivity).hideShowWholeToolbar(isVisible = true)
            it.hideShowSkip(isVisible = false)
            it.hideShowBack(isVisible = false)
        }
    }

    private fun editProfile() {
        val name = binding.etName.text.toString()

        val editName = name.split(" ")[0]
        val lastName = name.split(" ")

        var finalLastName = ""

        for (pos in lastName.indices) {
            if (pos != 0)
                finalLastName += "${lastName[pos]} "
        }


        val newName = editName.lowercase(Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() } + " $finalLastName"
        lifecycleScope.launch {
            getProfileViewModel.editProfile(
                EditProfileApiRequest(
                    name = newName,
                    username = binding.etUsername.text.toString(),
                    StepProgress = 1
                )
            )
                .observe(viewLifecycleOwner) {
                    when (it) {
                        is DataResult.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                        }

                        is DataResult.Success -> {
                            binding.progressBar.visibility = View.GONE
                            getProfileViewModel.profileData.value = it.data.data
                            it.data.data.let {
                                runBlocking(Dispatchers.IO) {
                                    preferencesHelper.saveDataEditProfile(
                                        it
                                    )
                                    preferencesHelper.saveDataStep(
                                        1
                                    )
                                }
                            }
                            if (tagg.equals("1")) {
                                runBlocking {

                                    preferencesHelper.setValue(
                                        key = PreferencesHelper.PreferencesKeys.STEP_PROGRESS,
                                        value = 1
                                    )
                                }
                                findNavController()
                                    .navigate(R.id.action_first_to_addPhotoFragment)
                            } else {
                                findNavController().popBackStack()
                            }

                        }

                        is DataResult.Failure -> {
                            binding.progressBar.visibility = View.GONE
                            it.message?.let { it1 -> requireActivity().showToast(it1) }
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
