package com.radarqr.dating.android.ui.welcome.otpVerify

import androidx.lifecycle.*
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.ui.welcome.mobileLogin.VerifyOtpApiRequest


class VerifyOtpViewModel constructor(private val dataRepository: DataRepository) :
    ViewModel() {

     fun verifyOtp(verifyOtpApiRequest: VerifyOtpApiRequest) =
        dataRepository.verifyOtp(verifyOtpApiRequest).asLiveData()




}





