package com.radarqr.dating.android.ui.location

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.navigation.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.databinding.FragmentEnableLocationBinding
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.utility.BaseUtils.isGpsEnabled
import com.radarqr.dating.android.utility.Utility.visible
import com.radarqr.dating.android.utility.handler.LocationPermissionHandler


class EnableLocationFragment : BaseFragment<FragmentEnableLocationBinding>() {

    private var fromHome = false
    private var isEnableClicked = false

    override fun getLayoutRes(): Int = R.layout.fragment_enable_location

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fromHome = (activity is HomeActivity)
        binding.tvSkip.visible(!fromHome)
        binding.tvEnable.setOnClickListener {
            isEnableClicked = true
            handleGps()
        }

        binding.tvSkip.setOnClickListener {
            openHomeActivity()
        }
    }

    private fun openHomeActivity() {
        val intent = Intent(requireActivity(), HomeActivity::class.java)
        intent.putExtra("show_image_popup",true)
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            if (it is InitialActivity) {
                it.hideShowWholeToolbar(isVisible = true)
                it.hideShowSkip(isVisible = false)
                it.hideShowBack(isVisible = false)
            }
        }
        if (requireContext().isGpsEnabled()) {
            handleSuccess()

        }
    }

    private fun handleSuccess() {
        if (fromHome)
            this.view?.findNavController()?.popBackStack()
        else
            openHomeActivity()
    }

    private fun handleGps() {
        if (requireContext().isGpsEnabled()) {
            handleSuccess()
        } else {
            val callGPSSettingIntent = Intent(
                Settings.ACTION_LOCATION_SOURCE_SETTINGS
            )
            startActivity(callGPSSettingIntent)
        }
    }
}