package com.radarqr.dating.android.ui.home.main

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.HomeBaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.HomeFragmentBinding
import com.radarqr.dating.android.ui.home.main.adapter.HomeAdapter
import com.radarqr.dating.android.ui.home.main.nearyou.NearYouFragment
import com.radarqr.dating.android.ui.home.main.recommended.RecommendedFragment
import com.radarqr.dating.android.ui.home.quickBlox.ChatViewModel
import com.radarqr.dating.android.utility.BaseUtils.isGpsEnabled
import com.radarqr.dating.android.utility.SharedPrefsHelper
import com.radarqr.dating.android.utility.Utility.color
import com.radarqr.dating.android.utility.Utility.drawable
import com.radarqr.dating.android.utility.Utility.toPx
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.Utility.welcomeDialog
import com.radarqr.dating.android.utility.handler.ViewClickHandler
import com.radarqr.dating.android.utility.introduction.IntroductionScreenType
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : HomeBaseFragment<HomeFragmentBinding>(), ViewClickHandler {
    lateinit var homeAdapter: HomeAdapter

    var type: Boolean? = null

    private var isRecommended = true
    private var isNearYouClicked = false
    private var fragment: Fragment? = null
    private var recommendedFragment: RecommendedFragment? = null
    private val chatViewModel: ChatViewModel by viewModel()
//    private var newYouFragment: NearYouFragment? = null
//    private val quickBloxManager: QuickBloxManager by inject()


    companion object {
//        var nestedScrollView: NestedScrollView? = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.homeFragment = this
        binding.clickHandler = this
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
//        newYouFragment = NearYouFragment()
        handleView(isRecommended)

        hideShowContainer()

        (activity as HomeActivity?)?.setFragmentContainer()

        init()
//        handleFirstTimeLaunchCondition()
    }

    // This function is now added in Home Activity
    // This is added because at the very first launch of application
    // after login user see this popup
    // so We need to show three image popup after this popup
   /* private fun handleFirstTimeLaunchCondition() {
        if (view != null && isAdded) {
            val preferences = requireActivity().getSharedPreferences(
                requireContext().packageName,
                Application.MODE_PRIVATE
            )
            Log.d("isFirstTime", "${preferences.getBoolean("firstTime", false)}")
            if (!preferences.getBoolean("firstTime", false)) {
                requireActivity().welcomeDialog {
                    val editor = preferences.edit()
                    editor.putBoolean("firstTime", true)
                    editor.apply()
                    handleIntroductoryUI()
                }
            } else handleIntroductoryUI()
        }
    }*/

    private fun handleIntroductoryUI() {
        /*Handler(Looper.getMainLooper()).postDelayed({
            val isShown = SharedPrefsHelper.get(
                Constants.IntroductionConstants.HOTSPOT,
                defValue = false
            )
            (activity as HomeActivity)
            (activity as HomeActivity).introductionHandler?.showIntroductoryUI(
                IntroductionScreenType.HOTSPOT,
                hasShown = isShown,
                Pair(
                    (activity as HomeActivity).binding.homeToolbar.ivFire.x - (35).toPx,
                    (activity as HomeActivity).binding.homeToolbar.ivFire.y
                )
            ) {
                SharedPrefsHelper.save(Constants.IntroductionConstants.HOTSPOT, true)
                if (it) {
                    view?.findNavController()?.navigate(R.id.hotspotsFragment)
                }
            }
        }, 50)*/

    }

    private fun init() {
        (activity as HomeActivity?)?.listener = object : HomeActivity.Helper {
            override fun unPauseClick() {
                hideShowContainer()
            }

        }

        /* if (view != null && isAdded)
             lifecycleScope.launchWhenStarted {
                 chatViewModel.getMatchesDialogsOnly {

                 }
             }*/
    }

    fun hideShowContainer() {
        binding.container.visible(!SharedPrefsHelper[Constants.IS_PROFILE_PAUSED, false])
    }


    private fun loadBitmapFromView(v: View): Bitmap? {
        val b = Bitmap.createBitmap(
            v.measuredWidth,
            v.measuredHeight,
            Bitmap.Config.ARGB_8888
        )
        val c = Canvas(b)
        v.layout(0, 0, v.measuredWidth, v.measuredHeight)
        v.draw(c)
        return b
    }


    override fun getLayoutRes(): Int = R.layout.home_fragment

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::homeAdapter.isInitialized)
            homeAdapter.releasePlayer()
    }


    override fun onResume() {
        super.onResume()
        if (isNearYouClicked && isRecommended) {
            isNearYouClicked = false
            if (requireContext().isGpsEnabled()) {
                getLocation {}
                handleView(isRecommended = false)
            }
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.tv_pause_unpause_profile -> {
                /*getBaseActivity()?.let {
                    it.updateAccountSettings(AccountSettingsRequest(pause_profile = false)) { data ->
                        data?.let {
                            getProfileViewModel().data.value?.data?.pause_profile =
                                data.pause_profile ?: false
                            getUserRecommendation()
                            binding.includePauseProfile.llPauseProfile.visibility = View.GONE
                        }
                    }
                }*/
            }

            R.id.tvRecommended -> {
                isNearYouClicked = false
                if (!isRecommended)
                    handleView(isRecommended = true)
            }

            R.id.tvNearYou -> {
                isNearYouClicked = true
                if (isRecommended)
                    if (requireContext().isGpsEnabled()) {
                        isNearYouClicked = false
                        getLocation {}
                        handleView(isRecommended = false)
                    } else this.view?.findNavController()?.navigate(R.id.enableLocationFragment)

            }

            R.id.tvMap -> {
                this.view?.findNavController()?.navigate(R.id.hotspotsFragment)
            }
        }
    }

    private fun handleView(isRecommended: Boolean = true, fromInterface: Boolean = false) {
        this.isRecommended = isRecommended
        binding.tvRecommended.background =
            requireContext().drawable(if (isRecommended) R.drawable.bg_round_teal_fill else R.drawable.round_cetacean_blue_stroke_round)
        binding.tvRecommended.setTextColor(requireContext().color(if (isRecommended) R.color.white else R.color.iconsColorDark))

        binding.tvNearYou.background =
            requireContext().drawable(if (!isRecommended) R.drawable.bg_round_teal_fill else R.drawable.round_cetacean_blue_stroke_round)
        binding.tvNearYou.setTextColor(requireContext().color(if (!isRecommended) R.color.white else R.color.iconsColorDark))

        if (!fromInterface)
            loadFragment()
    }

    private fun loadFragment() {
        fragment = if (isRecommended) RecommendedFragment() else NearYouFragment()
        fragment?.let {
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.container, it)
            transaction.commit()
        }
    }

}
