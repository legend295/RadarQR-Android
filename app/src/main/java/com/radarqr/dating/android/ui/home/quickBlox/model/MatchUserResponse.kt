package com.radarqr.dating.android.ui.home.quickBlox.model

import com.radarqr.dating.android.ui.home.settings.prodileModel.HobbiesData
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData

data class MatchUserResponse(
    val `data`: List<ProfileData>,
    val message: String,
    val status: Int
)

data class Data(
    val StepProgress: Int? = 0,
    val __v: Int? = 0,
    val _id: String? = "",
    val about_me: String? = "",
    val account_settings: AccountSettings? = null,
    val age: Int? = 0,
    val birthday: String? = "",
    val children: String? = "",
    val country_code: String? = "",
    val createdAt: String? = "",
    val drinking: String? = "",
    val education_level: String? = "",
    val email: String? = "",
    val ethnicity: String? = "",
    val gender: String? = "",
    val height: Double? = 0.0,
    val hobbies_interest: List<HobbiesData>? = null,
    val images: List<String>? = null,
    val job: String? = "",
    val job_title: String? = "",
    val location: Location,
    val name: String? = "",
    val phone_number: String? = "",
    val profile_pic: String? = "",
    val quickblox_external_id: Int? = 0,
    val quickblox_user_id: String? = "",
    val school: String? = "",
    val smoking: String? = "",
    val token: Token,
    val updatedAt: String? = "",
    val weight: String? = "",
    val zodiac: String? = ""
)

data class Location(
    val city: String? = "",
    val country: String? = "",
    val latlon: Latlon,
    val locality: String? = ""
)

data class Token(
    val login: String? = ""
)

data class Latlon(
    val coordinates: List<Double>,
    val type: String? = ""
)

data class AccountSettings(
    val email_notification: Boolean,
    val pause_profile: Boolean,
    val push_notification: Boolean
)