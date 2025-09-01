package com.radarqr.dating.android.ui.home.settings.prodileModel

import com.google.gson.annotations.SerializedName
import com.radarqr.dating.android.data.model.FriendList
import com.radarqr.dating.android.data.model.SubscriptionStatus
import java.io.Serializable

data class getProfileApiResponse(
    val `data`: ProfileData,
    val message: String,
    val status: Int
)

data class ProfileData(
    val _id: String? = "",
    val account_settings: AccountSettings? = null,
    var about_me: String? = "",
    var birthday: String? = "",
    val children: String? = "",
    val country_code: String? = "",
    val createdAt: String? = "",
    val drinking: String? = "",
    val education_level: String? = "",
    val email: String? = "",
    val ethnicity: String? = "",
    var gender: String? = "",
    var height: String? = "",
    var profile_pic: String? = "",
    var hobbies_interest: ArrayList<HobbiesData>? = ArrayList(),
    val images: ArrayList<String>? = ArrayList(),
    var imageData: ImageData = ImageData("", ""),
    var imageDataMap: LinkedHashMap<String, ImageData> = LinkedHashMap(),
    var interested_in: String? = "",
    val job: String? = "",
    val isActive: Boolean = true,
    val job_title: String? = "",
    val location: LocationDetail = LocationDetail(),
    val name: String? = "",
    var age: String? = "",
    val school: String? = "",
    val smoking: String? = "",
    val weight: String? = "",
    val zodiac: String? = "",
    val marijuana: String? = "",
    val phone_number: String? = "",
    val profileCompletness: Int? = 0,
    val quickblox_external_id: Int? = 0,
    val quickblox_user_id: String? = "",
    val token: Token = Token(),
    val updatedAt: String? = "",
    var like_status: LikeStatus = LikeStatus(),
    var show_age: Boolean = false,
    var username: String? = "",
    var friendlist: FriendList? = null,
    var venue_subscription: SubscriptionStatus? = null,
    var subscription_user_id: String? = null,
    var ads_interval: Int? = null
) : Serializable

data class ImageData(val key: String = "", val url: String = "") : Serializable

data class LikeStatus(
    val request_sent: Boolean = false,
    val request_receive: Boolean = false,
    val is_match: Boolean = false,
    val is_unmatch: Boolean = false,
    val is_decline: Boolean = false,
) : Serializable

data class HobbiesData(
    val name: String = "",
    val value: String = "",
    val image: String = "",
    val _id: String = "",
    val category: String = ""
) : Serializable

data class LocationDetail(
    val city: String? = "",
    val country: String? = "",
    val state: String? = "",
    val locality: String? = "",
    val latlon: Latlon? = Latlon()
) : Serializable

data class Latlon(
    val coordinates: List<Double>? = ArrayList(),
    val type: String = ""
) : Serializable

data class Token(
    val login: String = ""
) : Serializable

data class AccountSettings(
    val email: Boolean,
    val pause_profile: Boolean,
    val push_notification: Boolean,
    val is_promo: Boolean,
) : Serializable

data class VenueSubscription(
    @SerializedName("purchase_date")
    val purchaseDate: String,
    @SerializedName("fullfillment_date")
    val fulfillmentDate: String,
    @SerializedName("expiration_date")
    val expirationDate: String,
    @SerializedName("package")
    val packageId: String,
    @SerializedName("venues_count")
    val venueCount: Int,
    val status: Int
) : Serializable