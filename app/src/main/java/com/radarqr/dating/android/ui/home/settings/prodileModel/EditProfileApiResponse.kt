package com.radarqr.dating.android.ui.home.settings.prodileModel

data class EditProfileApiResponse(
    val `data`: EditData,
    val message: String,
    val status: Int
)
data class EditData(
    val about_me: String,
    val birthday: String,
    val children: String,
    val drinking: String,
    val education_level: String,
    val email: String,
    val ethnicity: String,
    val gender: String,
    val height: String,
    val hobbies_interest: List<HobbiesData>,
    val images: List<String>?,
    val interested_in: String,
    val job: String,
    val job_title: String,
    val location: Location,
    val name: String,

    val school: String,
    val smoking: String,
    val weight: String,
    val zodiac: String
)
data class Location(
    val city: String,
    val country: String
)