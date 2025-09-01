package com.radarqr.dating.android.ui.response

data class RegisterNewUserApiRequest(
    val verification_status: String,
    val country_code: String,
    val phone_number: String
)