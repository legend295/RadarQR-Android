package com.radarqr.dating.android.ui.response


data class RegisterApiResponse(
    val message: String,
    val status: String,
    val data: Data
) {

    data class Data(
        val otp: String
    )
}
