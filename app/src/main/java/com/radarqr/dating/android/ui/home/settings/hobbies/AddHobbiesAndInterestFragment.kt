package com.radarqr.dating.android.ui.home.settings.hobbies

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.radarqr.dating.android.utility.chipslayoutmanager.ChipsLayoutManager
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.hobbies.HobbiesAndInterestData
import com.radarqr.dating.android.databinding.FragmentAddHobbiesAndInterestBinding
import com.radarqr.dating.android.ui.home.settings.hobbies.adapter.AddHobbiesAndInterestAdapter
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.ui.welcome.mobileLogin.EditProfileApiRequest
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddHobbiesAndInterestFragment : BaseFragment<FragmentAddHobbiesAndInterestBinding>(),
    ViewClickHandler {

    private val getProfileViewModel: GetProfileViewModel by viewModel()
    var hobbiesAdapter: AddHobbiesAndInterestAdapter? = null
    var profileData: ProfileData? = null
    private val list: ArrayList<HobbiesAndInterestData> = ArrayList()

    override fun getLayoutRes(): Int = R.layout.fragment_add_hobbies_and_interest

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewHandler = this
        arguments?.let {
            profileData = it.getSerializable(Constants.EXTRA_DATA) as ProfileData?
        }

       binding.tvNoInternet.setOnClickListener {
           getHobbies()
       }

        init()
    }

    private fun init() {
        showToolbarLayout(false)
        showNavigation(false)

        hobbiesAdapter = AddHobbiesAndInterestAdapter(list)
        binding.rvHobby.apply {
            layoutManager = ChipsLayoutManager.newBuilder(requireActivity())
                .setChildGravity(Gravity.TOP)
                .setMaxViewsInRow(3)
                .setGravityResolver { Gravity.CENTER }
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .build()

            adapter = hobbiesAdapter
        }


    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            getHobbies()
        }, 200)
    }

    private fun getHobbies() {
        activity?.let { fragmentActivity ->
            fragmentActivity.lifecycleScope.launch {
                getProfileViewModel.getAllHobbies().observe(viewLifecycleOwner) {
                    binding.progressBar.visibility = View.GONE
                    when (it) {
                        DataResult.Empty -> {
                        }
                        is DataResult.Failure -> {
                        }
                        DataResult.Loading -> {
                        }
                        is DataResult.Success -> {
                            profileData?.let { data ->
                                data.hobbies_interest?.apply {
                                for (i in indices) {
                                    for (j in it.data.data.indices) {
                                        if (this[i]._id == it.data.data[j]._id) {
                                            it.data.data[j].isSelected = true
                                        }
                                    }
                                }
                                }
                            }
                            val list = ArrayList<String>()
                            for (value in it.data.data) {
                                list.add(value.name)
                            }
                            this@AddHobbiesAndInterestFragment.list.clear()
                            this@AddHobbiesAndInterestFragment.list.addAll(it.data.data)
                            hobbiesAdapter?.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    private fun editProfile() {
        activity?.let { fragmentActivity ->
            fragmentActivity.lifecycleScope.launch {
                getProfileViewModel.editProfile(
                    EditProfileApiRequest(
                        hobbies_interest = ArrayList(
                            hobbiesAdapter?.selectedList?.values!!
                        )
                    )
                ).observe(viewLifecycleOwner) {
                    when (it) {
                        DataResult.Empty -> {
                        }
                        is DataResult.Failure -> {
                        }
                        DataResult.Loading -> {
                        }
                        is DataResult.Success -> {
                            getProfileViewModel.profileData.value = it.data.data
                            findNavController().navigateUp()
                        }
                    }
                }
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.activity_toolbar_back -> {
                findNavController().navigateUp()
            }

            R.id.tv_save -> {
               /* if (hobbiesAdapter?.selectedList?.values?.isNotEmpty() == true) {
                }*/
                editProfile()
            }
        }
    }
}