package com.radarqr.dating.android.data.model

import com.google.gson.annotations.SerializedName
import com.radarqr.dating.android.hotspots.closefriend.adapter.CloseFriendAdapter
import com.radarqr.dating.android.ui.home.settings.prodileModel.ProfileData
import com.radarqr.dating.android.utility.instatag.TaggedUser
import java.io.Serializable

data class NearYouData(
    val `data`: NearYouUsersData, val message: String, val status: Int
)

data class NearYouUsersData(val users: ArrayList<ProfileData>, val total_count: Int)


data class MatchesResponse(
    val `data`: ArrayList<MatchResponseData>, val message: String, val status: Int
)

data class MatchResponseData(
    val __v: Int,
    val _id: String,
    val category: String,
    val createdAt: String,
    val custom_params: CustomParams,
    val qb_dialog_id: String,
    val receiver_id: String,
    val receiver_option_choosed: Boolean,
    val receiver_response_message: String,
    val sender_id: String,
    val sender_option_choosed: Boolean,
    val updatedAt: String
)

data class CustomParams(
    val class_name: String,
    val occupant_id_qb: String,
    val occupant_id_radar: String,
    val occupant_name: String,
    val occupant_profile_pic: String
)

data class MatchDataDialogsIdsOnly(
    val `data`: ArrayList<String>, val message: String, val status: Int
)

data class SearchUsernameResponse(
    val total_count: Int, val users: ArrayList<Users>
)

data class Users(
    val _id: String,
    var friendlist: FriendList?,
    val name: String,
    val profile_pic: String,
    val username: String,
    var requestStatus: CloseFriendAdapter.RequestStatus? = CloseFriendAdapter.RequestStatus.ADD
)

data class FriendList(
    val _id: String? = null,
    val close_friend: Boolean? = false,
    val friend_id: String? = null,
    val request_received: Boolean? = false,
    val request_sent: Boolean? = false,
    val requester_id: String? = null
)

data class SendInvitationResponse(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val friend_action: Any,
    val friend_action_taken_at: Any,
    val friend_id: String,
    val requester_action: Boolean,
    val requester_id: String,
    val updatedAt: String
)

data class AllInvitesResponse(
    val total_count: Int, val users: ArrayList<InvitedUsers>
)

data class InvitedUsers(
    val _id: String,
    val friend_action: Any,
    val friend_id: String,
    val requester_info: RequesterInfo?,
    val requester_action: Boolean,
    val requester_id: String
)

data class RequesterInfo(
    val _id: String, val name: String, val profile_pic: String, val username: String
)

data class CloseFriendsResponse(
    val total_count: Int, val users: ArrayList<CloseFriendUser>
)

data class CloseFriendUser(
    val _id: String,
    val friend_info: FriendInfo?,
    val requester_info: RequesterInfo?,
    val friend_action: Boolean,
    val friend_id: String,
    val requester_action: Boolean,
    val requester_id: String,
    var requestStatus: CloseFriendAdapter.RequestStatus? = CloseFriendAdapter.RequestStatus.ALREADY_ADDED
)

data class FriendInfo(
    val _id: String, val name: String, val profile_pic: String, val username: String
)

data class HotspotVenuesData(
    val total_count: Int, val venues: ArrayList<Venue>
)


data class ContactInfo(
    val address: String,
    val city: String,
    val country: String,
    val id: String,
    @SerializedName("latlon") val latLon: LatLon,
    val locality: String,
    val name: String,
    @SerializedName("phonenumber") val phoneNumber: String,
    val state: String,
    val website: String
)

data class Dist(
    val calculated: Double
)

data class LatLon(
    val coordinates: ArrayList<Double>, val type: String
)

data class AddTimerResponse(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val duration: Int,
    val expire_ts: String,
    val start_ts: String,
    val updatedAt: String,
    val user_id: String,
    val venue_id: String
)

data class RoamingTimerStatusResponse(
    val _id: String,
    val duration: Int,
    val expire_ts: Int,
    val start_ts: Int,
    val status: Int,
    val user_id: String,
    val venue_id: String,
    val venue_detail: VenueDetail
)

data class VenueDetail(
    val __v: Int,
    val _id: String,
    val ambiance: Ambiance,
    val completepercentage: Int,
    val contactinfo: ContactInfo,
    val createdAt: String,
    val description: String,
    val extrainfo: Any,
    val images: ArrayList<VenueImage>,
    val name: String,
    val specialoffer: String,
    val status: Int,
    val type: Type,
    val uniquename: String,
    val updatedAt: String,
    val user_id: String
)

data class Ambiance(
    val name: String, val value: String
)


data class Type(
    val name: String, val value: String
)

data class SearchCloseFriendResponse(
    val total_count: Int, val users: ArrayList<SearchUser>
)

data class SearchUser(
    val _id: String,
    val friend_action: Boolean,
    val friend_id: String,
    val friend_info: FriendInfo,
    val requester_action: Boolean,
    val requester_id: String,
    val requester_info: RequesterInfo
)

data class VenueImagesResponse(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val image: String,
    val tagged_users: ArrayList<TaggedUser>,
    val updatedAt: String,
    val user_id: String,
    val venue_id: String
)


data class VenueSinglesResponse(
    val total_count: Int, val users: ArrayList<SinglesUser>
)

data class SinglesUser(
    val _id: String, val profile_pic: String, val username: String, val name: String
)

data class VenuePostsResponse(
    val total_count: Int, @SerializedName("venue_images") val venueImages: ArrayList<VenueImage>
) : Serializable

data class VenueImage(
    val _id: String,
    val duration: Double,
    var image: String,
    val tagged_users: ArrayList<TaggedUser>,
    val user_id: String,
    val venue_id: String,
    val createdAt: String = "",
    @SerializedName("user_info") val userInfo: UserInfo = UserInfo()
) : Serializable

data class UserInfo(
    val _id: String = "", val name: String = "", val profile_pic: String = ""
) : Serializable

data class UpdateVenueImages(var id: String = "", var name: String = "") : Serializable

data class SubscriptionStatus(
    val subscription: Subscription,
    val subscription_status: SubscriptionStatusResponse
) : Serializable

data class Subscription(
    val purchase_date: String,
    @SerializedName("fullfillment_date") val fulfillment_date: String,
    val expiration_date: String,
    @SerializedName("package") val packageBought: String,
    @SerializedName("venues_count") val venueCount: Int,
    @SerializedName("status") val status: Int
) : Serializable

data class SubscriptionStatusResponse(val status: Int, val message: String) : Serializable




