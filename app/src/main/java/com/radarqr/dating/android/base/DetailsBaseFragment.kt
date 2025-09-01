package com.radarqr.dating.android.base

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.radarqr.dating.android.ui.home.main.HomeActivity

abstract class DetailsBaseFragment<DB : ViewDataBinding> : BaseFragment<DB>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HomeActivity.activeFragment.value = this
    }
}