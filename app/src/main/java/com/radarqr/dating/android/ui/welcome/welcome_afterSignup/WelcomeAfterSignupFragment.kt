package com.radarqr.dating.android.ui.welcome.welcome_afterSignup

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.databinding.FragmentWelcomeAfterSignupBinding
import com.radarqr.dating.android.ui.welcome.InitialActivity


class WelcomeAfterSignupFragment : BaseFragment<FragmentWelcomeAfterSignupBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.welcomeSignupFragment = this

        showTitle(false)
        showToolbar(true)
        showToolbarWhite(false)
        showProgress(false)
        showBackButton(false)
        showSkip(false)
        binding.tvGotIt.setOnClickListener {
            this.findNavController()
                .navigate(R.id.action_welcomeFragment_to_beforeUseFragment)
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            (it as InitialActivity).hideShowWholeToolbar(isVisible = true)
            it.hideShowSkip(isVisible = false)
            it.hideShowBack(isVisible = false)
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_welcome_after_signup

}
