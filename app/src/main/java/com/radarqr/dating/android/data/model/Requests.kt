package com.radarqr.dating.android.data.model

import com.google.gson.annotations.SerializedName
import com.radarqr.dating.android.utility.instatag.TaggedUser

data class SocialRequest(
    var email: String = "",
    var name: String = "",
    var social_id: String = "",
    var social_type: String = ""
)

data class SocialResponse(
    val `data`: Data,
    val message: String,
    val status: Int
)

data class Data(
    val NewUser: Boolean,
    val StepProgress: Int,
    val profileCompletness: Int,
    val token: String,
    val user: User
)

data class User(
    val country_code: String,
    val name: String,
    val phone_number: String,
    val profile_pic: String,
    val qb_user_id: String
)

data class LikeRequest(var page: Int = 1, var limit: Int = 10, var category: String)

data class SearchByUsername(
    var username: String,
    @SerializedName("pageno") var pageNo: Int,
    var limit: Int
)

data class AcceptInviteRequest(var requester_id: String)
data class SendInviteRequest(var friend_id: String)

data class NearByVenueRequest(
    val lat: String,
    val lng: String,
    val page: Int,
    val limit: Int = 50,
    val distance: Int = 20
)

data class AddTimerRequest(
    val duration: Int,
    val expire_ts: String,
    val start_ts: String,
    val venue_id: String
)

data class VenueImageAddRequest(
    val image: String,
    val tagged_users: ArrayList<TaggedUser>,
    val venue_id: String
)

data class SearchCloseFriendRequest(
    val limit: Int,
    @SerializedName("pageno")
    val pageNo: Int,
    val username: String
)

data class DeleteVenueImageRequest(var venue_id: String, var image_id: String)