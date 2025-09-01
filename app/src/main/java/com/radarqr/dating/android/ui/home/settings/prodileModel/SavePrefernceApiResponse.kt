package com.radarqr.dating.android.ui.home.settings.prodileModel

data class SavePrefernceApiResponse(
    val `data`: SaveData,
    val message: String,
    val status: Int
)
data class SaveData(
    val __v: Int,
    val _id: String,
    val children: String?,
    val createdAt: String,
    val drinking: String?,
    val education_level: List<String>,
    val ethnicity: List<String>,
    val max_age: Int?,
    val max_distance: Int?,

    val location: LocationDetailP,
    val max_height: Int?,
    val min_age: Int?,
    val min_distance: Int?,
    val min_height: Int?,
    val smoking: String?,
    val marijuana: String?,
    val updatedAt: String,
    val user_id: String,
    val want_to_meet: String?,
    val zodiac_sign: List<String>,
    val want_to_meet_req: Boolean? = false,
    val marijuana_req: Boolean? = false,
    val min_age_req: Boolean? = false,
    val max_age_req: Boolean? = false,
    val min_height_req: Boolean? = false,
    val max_distance_req: Boolean? = false,
    val max_height_req: Boolean? = false,
    val zodiac_sign_req: Boolean? = false,
    val drinking_req: Boolean? = false,
    val smoking_req: Boolean? = false,
    val children_req: Boolean? = false,
    val education_level_req: Boolean? = false,
)

data class LocationDetailP(
    val city: String?,
    val country: String?,
    val locality: String?,
    val latlon: LatlonP
)
data class LatlonP(
    val coordinates: List<Double>,
    val type: String
)