package com.radarqr.dating.android.ui.home.settings

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.radarqr.dating.android.R
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.databinding.FragmentPreferencesBinding
import com.radarqr.dating.android.hotspots.helpers.showSubscriptionSheet
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.home.settings.EditProfile.openBottomSheetToUpdateProfile
import com.radarqr.dating.android.ui.home.settings.adapter.EditProfileGeneralContentAdapter
import com.radarqr.dating.android.ui.home.settings.model.EditProfileGeneralContentData
import com.radarqr.dating.android.ui.home.settings.prodileModel.GetProfileViewModel
import com.radarqr.dating.android.ui.home.settings.prodileModel.SavePrefernceApiResponse
import com.radarqr.dating.android.ui.welcome.mobileLogin.SavePreferenceApiRequest
import com.radarqr.dating.android.utility.BaseUtils
import com.radarqr.dating.android.utility.BaseUtils.isGpsEnabled
import com.radarqr.dating.android.utility.CommonCode
import com.radarqr.dating.android.utility.EditProfileGeneralContentTypes
import com.radarqr.dating.android.utility.Utility.getPreferencesContentList
import com.radarqr.dating.android.utility.enums.SubscriptionPopUpType
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PreferenceFragment : BaseFragment<FragmentPreferencesBinding>(), View.OnClickListener,
        (EditProfileGeneralContentData) -> Unit {

    private val getProfileViewModel: GetProfileViewModel by viewModel()

    private val preferencesAdapter by lazy {
        EditProfileGeneralContentAdapter(
            arrayListOf(),
            false,
            this
        )
    }
    private val preferencesList = ArrayList<EditProfileGeneralContentData>()
    private var isLocationClicked = false
    private var itemClicked = false


    var lat = 0.0
    var longt = 0.0

    private var mRootView: ViewGroup? = null
    private var mIsFirstLoad = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HomeActivity.activeFragment.value = this
        if (mRootView == null) {
            binding.preferencesFragment = this
            mIsFirstLoad = true
        } else {
            mIsFirstLoad = false
        }

        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        setAdapter()
        if (getProfileViewModel.saveData != null) {
            setData(getProfileViewModel.saveData!!)
        }
        getPreferences()

        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun checkSubscription() {
        if (view != null && !RaddarApp.getSubscriptionStatus().canSetPreferencesNonNegotiable()) {
            showSubscriptionSheet(SubscriptionPopUpType.PREFERENCES) {

            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (requireContext().isGpsEnabled() && isLocationClicked) {
            openLocationFragment()
        }
    }


    override fun getLayoutRes(): Int = R.layout.fragment_preferences

    private fun getPreferences() {
        try {
            lifecycleScope.launch {
                getProfileViewModel.getPreferences()
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {

                            }

                            is DataResult.Success -> {
                                getProfileViewModel.saveData = it.data
                                setData(it.data)
                            }

                            is DataResult.Failure -> {
                                reportApiError(
                                    Exception().stackTrace[0].lineNumber,
                                    it.statusCode ?: 0,
                                    "user/preferences",
                                    requireActivity().componentName.className,
                                    it.message ?: ""
                                )

                                FirebaseCrashlytics.getInstance()
                                    .recordException(Exception("user/preferences Api Error"))
                            }

                            DataResult.Empty -> {}
                        }
                    }

            }
        } catch (e: Exception) {

        }
    }

    private fun setAdapter() {
        binding.rvPreferences.adapter = preferencesAdapter
    }

    private fun EditProfileGeneralContentData.handleAdapterClick() {
        if (contentType == EditProfileGeneralContentTypes.LOCATION) {
            itemClicked = false
            if (BaseUtils.isInternetAvailable()) {
                isLocationClicked = true
                if (requireContext().isGpsEnabled()) {
                    openLocationFragment()
                } else {
                    this@PreferenceFragment.view?.findNavController()
                        ?.navigate(R.id.enableLocationFragment)
                }
            } else CommonCode.setToast(
                requireContext(),
                resources.getString(R.string.no_internet_msg)
            )
        } else {
            getProfileViewModel.profileData.value?.apply {
                openBottomSheetToUpdateProfile(
                    requireContext(),
                    fromPreferences = true,
                    profileData = this,
                    saveData = getProfileViewModel.saveData?.data,
                    fragment = this@PreferenceFragment
                ) { _, apiRequest ->
                    savePreferences(apiRequest, this@handleAdapterClick)
                }
            }
            Handler(Looper.getMainLooper()).postDelayed({
                itemClicked = false
            }, 200)
        }
    }

    private fun openLocationFragment() {
        isLocationClicked = false
        val data = Bundle()
        data.putDouble("lati", lat)
        data.putDouble("longt", longt)
        data.putString("screen_tag", "0")
        findNavController()
            .navigate(R.id.action_preference_to_LocationFragment, data)
    }

    private fun setData(it: SavePrefernceApiResponse) {
        preferencesList.clear()
        preferencesList.addAll(it.data.getPreferencesContentList())
        preferencesAdapter.apply {
            val old = list
            val new = it.data.getPreferencesContentList()
            val callback = SpotDiffCallback(old, new)
            val result = DiffUtil.calculateDiff(callback)
            list = new
            result.dispatchUpdatesTo(this)
        }
    }

    override fun onClick(v: View?) {

    }

    private fun savePreferences(
        savePreferenceApiRequest: SavePreferenceApiRequest,
        editProfileGeneralContentData: EditProfileGeneralContentData
    ) {
        if (view != null && isAdded && isVisible)
            lifecycleScope.launch {
                getProfileViewModel.savePreferences(savePreferenceApiRequest)
                    .observe(viewLifecycleOwner) {
                        when (it) {
                            is DataResult.Loading -> {

                            }

                            is DataResult.Success -> {
                                getProfileViewModel.stateSaved = true
                                getProfileViewModel.saveData = it.data
                                preferencesList[editProfileGeneralContentData.id] =
                                    it.data.data.getPreferencesContentList()[editProfileGeneralContentData.id]
                                preferencesAdapter.list[editProfileGeneralContentData.id] =
                                    it.data.data.getPreferencesContentList()[editProfileGeneralContentData.id]
                                preferencesAdapter.notifyItemChanged(
                                    editProfileGeneralContentData.id
                                )
//                            setData(it.data)

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

                            DataResult.Empty -> {}
                        }
                    }

            }
    }

    override fun invoke(p1: EditProfileGeneralContentData) {
        if (itemClicked) return
        itemClicked = true
        p1.handleAdapterClick()
    }

    inner class SpotDiffCallback(
        private val old: ArrayList<EditProfileGeneralContentData>,
        private val new: List<EditProfileGeneralContentData?>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int {
            return old.size
        }

        override fun getNewListSize(): Int {
            return new.size
        }

        override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            return old[oldPosition].id == new[newPosition]?.id
        }

        override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            return old[oldPosition] == new[newPosition]
        }

    }
}
