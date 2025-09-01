package com.radarqr.dating.android.ui.home.main.model

import com.radarqr.dating.android.ui.home.settings.prodileModel.HobbiesData
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData

data class RecommendationResponse(
    val `data`: ArrayList<ProfileData>,
    val message: String,
    val status: Int
)

data class RecommendationData(
    val __v: Int,
    val _id: String,
    val about_me: String,
    val account_settings: AccountSettings,
    val age: Int,
    val birthday: String,
    val children: String?,
    val country_code: String,
    val createdAt: String,
    val drinking: String?,
    val education_level: String?,
    val email: String,
    val ethnicity: String,
    val gender: String,
    val height: String?,
    val images: List<String>,
    val hobbies_interest: List<HobbiesData>?,
    val job: String?,
    val job_title: String?,
    val location: Location?,
    val name: String,
    val phone_number: String,
    val profile_pic: String,
    val quickblox_external_id: Int,
    val quickblox_user_id: String,
    val school: String?,
    val smoking: String?,
    val token: Token,
    val updatedAt: String,
    val weight: String,
    val zodiac: String?,
    val like_id: String?
)

data class AccountSettings(
    val email: Boolean,
    val is_deleted: Boolean,
    val pause_profile: Boolean,
    val push_notification: Boolean
)

data class Latlon(
    val coordinates: List<Double>,
    val type: String
)

data class Location(
    val city: String?,
    val country: String?,
    val locality: String?,
    val latlon: Latlon?
)

data class Token(
    val login: String
)