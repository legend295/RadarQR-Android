package com.radarqr.dating.android.data.model.accountdetails


data class Data(
    var country_code: String = "",
    var email: String? = "",
    var email_notification: Boolean = false,
    var pause_profile: Boolean = false,
    var phone_number: String? = "",
    var push_notification: Boolean = false,
    var is_promo: Boolean = false,
    var user_id: String = ""
)