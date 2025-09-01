package com.radarqr.dating.android.data.model.accountdetails

data class AccountDetailsResponse(
    var `data`: Data = Data(),
    val message: String = "",
    val status: Int = 0
)