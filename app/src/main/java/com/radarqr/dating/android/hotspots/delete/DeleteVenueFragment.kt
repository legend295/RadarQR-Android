package com.radarqr.dating.android.hotspots.delete

import android.os.Bundle
import android.view.View
import androidx.databinding.ObservableField
import androidx.navigation.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.databinding.FragmentDeleteVenueBinding
import com.radarqr.dating.android.hotspots.VenueBaseFragment
import com.radarqr.dating.android.utility.handler.ViewClickHandler

class DeleteVenueFragment : VenueBaseFragment<FragmentDeleteVenueBinding>(), ViewClickHandler {

    val isChecked: ObservableField<Boolean> = ObservableField<Boolean>(false)

    override fun getLayoutRes(): Int = R.layout.fragment_delete_venue


    override fun init(view: View, savedInstanceState: Bundle?) {
//        binding.fragment = this
        binding.viewHandler = this
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivBack -> {
                this.view?.findNavController()?.popBackStack()
            }

            R.id.tvDelete -> {
                this.view?.findNavController()?.popBackStack()
            }
        }
    }
}