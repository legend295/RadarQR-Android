package com.radarqr.dating.android.ui.common

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.databinding.LayoutScreenDialogCommonBinding

class CommonDialogScreen : BaseFragment<LayoutScreenDialogCommonBinding>() {

    override fun getLayoutRes(): Int = R.layout.layout_screen_dialog_common

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showNavigation(visibility = false)
        binding.tvGotIt.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}