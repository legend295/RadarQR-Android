package com.radarqr.dating.android.ui.location

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.databinding.FragmentEnableLocationPermissionBinding
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.utility.BaseUtils.openApplicationDetailsSettings
import com.radarqr.dating.android.utility.handler.LocationPermissionHandler

class EnableLocationPermissionFragment : BaseFragment<FragmentEnableLocationPermissionBinding>() {
    override fun getLayoutRes(): Int = R.layout.fragment_enable_location_permission

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HomeActivity.activeFragment.value = this
        binding.tvEnable.setOnClickListener {
            isLocationPermissionGranted(object : LocationPermissionHandler {
                override fun onPermissionGranted(isPermissionGranted: Boolean) {
                    if (isPermissionGranted) {
                        this@EnableLocationPermissionFragment.view?.findNavController()
                            ?.popBackStack()
                    } else {
                        requireContext().openApplicationDetailsSettings()
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) this@EnableLocationPermissionFragment.view?.findNavController()
            ?.popBackStack()
    }


}