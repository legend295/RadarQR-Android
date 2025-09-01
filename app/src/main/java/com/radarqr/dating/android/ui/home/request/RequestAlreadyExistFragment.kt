package com.radarqr.dating.android.ui.home.request

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import com.radarqr.dating.android.R
import com.radarqr.dating.android.base.BaseFragment
import com.radarqr.dating.android.databinding.FragmentRequestAlreadyExistBinding

class RequestAlreadyExistFragment : BaseFragment<FragmentRequestAlreadyExistBinding>() {


    override fun getLayoutRes(): Int = R.layout.fragment_request_already_exist

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun handleBack(){
        findNavController().popBackStack()
    }

}