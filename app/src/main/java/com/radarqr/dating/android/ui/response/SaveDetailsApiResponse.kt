package com.radarqr.dating.android.ui.response




data class SaveDetailsApiResponse(
    val status: String,
    val message: String,
    val data: UserData
) {
    data class Details(
        val image: ArrayList<Url>?,
        val is_profile_open: Boolean,
        val _id: String,
        val username: String,
        val email: String,
        val password: String,
        val phone_number: String,
        val updatedAt: String,
        val firstname: String,
        val lastname: String,
        val gender: Int,
        val fellows: ArrayList<Fellows>,
        val age_preference: String,
        val bio: String,
        val city: String,
        val distance_preference: String,
        val dob: String?,
        val education: String,
        val work: String,
        val deviceInfo: RegisterApiRequest.DeviceInfo
    )

    data class UserData(val details: Details, val token: String)
    data class Url(val url: String)
    data class Fellows(val user_id: String, val status: Int, val _id: String)


}

