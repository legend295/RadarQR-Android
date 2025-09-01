package com.radarqr.dating.android.ui.home.settings.web

import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.navigation.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.databinding.FragmentWebViewBinding
import com.radarqr.dating.android.ui.home.main.HomeActivity
import com.radarqr.dating.android.utility.Utility.visible

class WebViewFragment : BaseFragment<FragmentWebViewBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        HomeActivity.activeFragment.value = this
//        binding.webView.settings.javaScriptEnabled = true

        val webSettings: WebSettings = binding.webView.settings
        webSettings.javaScriptEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.domStorageEnabled = true

        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.progressBar.visible(isVisible = false)
            }

        }

        binding.ivBack.setOnClickListener {
            this.view?.findNavController()?.popBackStack()
        }

        when (arguments?.getString(Constants.TYPE) ?: "") {
            Constants.PRIVACY_POLICY -> {
                binding.webView.loadUrl("https://www.radarqr.com/privacy-policy")
            }

            Constants.RADAR_WEBSITE -> {
                binding.webView.loadUrl("https://www.radarqr.com/")
            }

            Constants.HELP_CENTER -> {
                binding.webView.loadUrl("https://www.radarqr.com/tips")
            }

            Constants.TERMS_OF_SERVICES -> {
                binding.webView.loadUrl("https://www.radarqr.com/terms-and-conditions")
            }

            Constants.RUN_DAY_AI -> {
                binding.webView.loadUrl("https://agent.runday.ai/radarqr")
            }

            else -> {

                binding.webView.loadUrl("https://www.radarqr.com/")
            }
        }
    }

    override fun getLayoutRes(): Int = R.layout.fragment_web_view

    override fun onResume() {
        super.onResume()
        binding.webView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.webView.onPause()
    }

}