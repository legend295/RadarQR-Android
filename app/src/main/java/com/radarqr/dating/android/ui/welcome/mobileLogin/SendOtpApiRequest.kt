package com.radarqr.dating.android.ui.welcome.mobileLogin

data class SendOtpApiRequest(
    val phone_number: String,
    val country_code: String
)

data class VerifyOtpApiRequest(
    val phone_number: String,
    val country_code: String,
    val otp: String,
//    val platform: String = "android"
)

data class getProfileRequest(
    val user_id: ArrayList<String>? = null
)

data class getMatchData(
    val quickblox_id: ArrayList<String>? = null,
    val full_profile: String? = null
)


data class updateTokenRequest(
    val device_token: String? = null,
    val device_type: String? = null,

    )

data class LikeDislikeRequest(
    val receiver_id: String? = null,
    val sender_option_choosed: Boolean? = null,
    val category: String? = null,
    val like_id: String? = null,
    val sender_message: String? = null
)

data class AcceptRequest(
    val sender_id: String? = null,
    val receiver_option_choosed: Boolean? = null,
    val receiver_response_message: String? = null,
    val category: String? = null

)

data class UserRecommendationRequest(
    val page: Int = 1,
    val limit: Int = 10,

    )

data class DeleteImagesRequest(
    val images: ArrayList<String>? = null

)

data class EditProfileApiRequest(
    val StepProgress: Int? = null,
    val name: String? = null,
    val age: String? = null,
    var show_age: Boolean? = null,
    val birthday: String? = null,
    val gender: String? = null,
    val whoAreYouInterestedIn: String? = null,
    val email: String? = null,
    val images: ArrayList<String>? = null,
    val about_me: String? = null,
    val height: String? = null,
    val location: LocationEdit? = null,
    val zodiac: String? = null,
    val drinking: String? = null,
    val smoking: String? = null,
    val children: String? = null,
    val marijuana: String? = null,
    val ethnicity: String? = null,
    val job: String? = null,
    val job_title: String? = null,
    val school: String? = null,
    val education_level: String? = null,
    val hobbies_interest: ArrayList<String>? = null,
    val username: String? = null

)

data class SavePreferenceApiRequest(
    val want_to_meet: String? = null,
    val min_age: Int? = null,
    val max_age: Int? = null,
    val min_height: Int? = null,
    val max_height: Int? = null,
    val min_distance: Int? = null,
    val max_distance: Int? = null,
    val zodiac_sign: ArrayList<String>? = null,
    val ethnicity: ArrayList<String>? = null,
    val drinking: String? = null,
    val children: String? = null,
    val smoking: String? = null,
    val marijuana: String? = null,
    val education_level: ArrayList<String>? = null,
    var want_to_meet_req: Boolean? = null,
    var marijuana_req: Boolean? = null,
    var min_age_req: Boolean? = null,
    var max_age_req: Boolean? = null,
    var min_height_req: Boolean? = null,
    var max_distance_req: Boolean? = null,
    var max_height_req: Boolean? = null,
    var zodiac_sign_req: Boolean? = null,
    var drinking_req: Boolean? = null,
    var smoking_req: Boolean? = null,
    var children_req: Boolean? = null,
    var education_level_req: Boolean? = null,

    )

data class LocationEdit(
    val city: String? = "",
    val state: String = "",
    val country: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val locality: String = ""

)

data class AccountSettingsRequest(
    val pause_profile: Boolean? = null,
    val push_notification: Boolean? = null,
    val email_notification: Boolean? = null,
)

data class MatchDataRequest(
    val quickblox_id: ArrayList<String> = ArrayList(),
    val full_profile: Int = 0
)

data class UnMatchRequest(
    val receiver_id: String
)

data class UpdatePhoneNumberRequest(val phone_number: String, val country_code: String)

data class ConfirmOtpRequest(
    val phone_number: String,
    val country_code: String,
    val otp: String
)

data class ReportRequest(
    val user_id: String = "",
    val option_id: String = "",
    val suboption_id: String? = null,
    val sub_suboption_id: String? = null,
    val other_info: String = ""
)

data class DeleteAccount(
    val account_deleted: Boolean = true,
    val device_token: String = ""
)

data class NearYouRequest(
    var lat: String = "",
    var lng: String = "",
    var page: Int = 1,
    var limit: Int = 10
)

data class LikeRequest(
    var page: Int = 1,
    var limit: Int = 10, var category: String
)

data class CurrentLocationUpdateRequest(val lat: String, val lng: String)