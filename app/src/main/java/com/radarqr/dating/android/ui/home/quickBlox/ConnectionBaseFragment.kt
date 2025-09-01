package com.radarqr.dating.android.ui.home.quickBlox

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import com.radarqr.dating.android.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class ConnectionBaseFragment<Bind : ViewDataBinding> : BaseFragment<Bind>() {

//    val chatViewModel: ChatViewModel by viewModel()

    private fun setViewModel() {
//        chatViewModel = (getBaseActivity() as HomeActivity).callHomeChatViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViewModel()
    }
}