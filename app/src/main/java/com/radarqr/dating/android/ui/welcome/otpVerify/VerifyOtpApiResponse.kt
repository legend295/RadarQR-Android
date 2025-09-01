package com.radarqr.dating.android.ui.welcome.otpVerify


data class VerifyOtpApiResponse(
    val message: String,
    val status: String,
    val data: UserData
) {
    data class UserData(
        val token: String, val NewUser: String, val profileCompletness: String,
        val user_id: String, val StepProgress: Int, val user: User_Data
    ) {

        data class User_Data(
            val quickblox_user_id: String,
            val name: String,
            val phone_number: String,
            val social_id: SocialIds,
            val profile_pic: String,
            val country_code: String
        )

        data class SocialIds(var facebook: String, var google: String, var apple: String)
    }
}
