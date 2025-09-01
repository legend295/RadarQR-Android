package com.radarqr.dating.android.hotspots

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.ui.home.main.HomeActivity

abstract class VenueBaseFragment<DB : ViewDataBinding> : BaseFragment<DB>() {

    abstract fun init(view: View, savedInstanceState: Bundle?)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HomeActivity.activeFragment.value = this
        if (this.view != null)
            init(view, savedInstanceState)
    }


}