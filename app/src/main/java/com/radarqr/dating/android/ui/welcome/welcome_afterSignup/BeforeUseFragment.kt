package com.radarqr.dating.android.ui.welcome.welcome_afterSignup

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.databinding.FragmentBeforeUseBinding
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.ui.welcome.InitialActivity
import com.radarqr.dating.android.utility.BaseUtils.isGpsEnabled


class BeforeUseFragment : BaseFragment<FragmentBeforeUseBinding>() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.beforeUseFragment = this

//        showTitle(false)
//        showToolbar(true)
//        showToolbarWhite(false)
//        showProgress(false)
//        showBackButton(false)
//        showSkip(false)

        binding.tvGotIt.setOnClickListener {
            if (requireContext().isGpsEnabled()) {
                val intent = Intent(requireActivity(), HomeActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                findNavController().navigate(R.id.enableLocationFragment)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            (it as InitialActivity).hideShowWholeToolbar(isVisible = false)
            it.hideShowSkip(isVisible = false)
        }
    }


    override fun getLayoutRes(): Int = R.layout.fragment_before_use

}
