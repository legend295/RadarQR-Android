package com.radarqr.dating.android.data.api


import com.radarqr.dating.android.data.model.*
import com.radarqr.dating.android.data.model.accountdetails.AccountDetailsResponse
import com.radarqr.dating.android.data.model.hobbies.Hobbies
import com.radarqr.dating.android.data.model.report.ReportData
import com.radarqr.dating.android.hotspots.model.*
import com.radarqr.dating.android.ui.home.likes.model.LikeResponseModel
import com.radarqr.dating.android.ui.home.main.model.LikeDislikeResponse
import com.radarqr.dating.android.ui.home.main.model.RecommendationResponse
import com.radarqr.dating.android.ui.home.quickBlox.model.MatchUserResponse
import com.radarqr.dating.android.ui.home.quickBlox.model.UserMatchesResponse
import com.radarqr.dating.android.ui.home.settings.prodileModel.SavePrefernceApiResponse
import com.radarqr.dating.android.ui.home.settings.prodileModel.getProfileApiResponse
import com.radarqr.dating.android.ui.welcome.mobileLogin.*
import com.radarqr.dating.android.ui.welcome.otpVerify.VerifyOtpApiResponse
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*

interface ApiHelper {
    /**
     * Register api call to register users number
     */
    @POST("send-otp")
    suspend fun sendOtp(@Body sendOtpApiRequest: SendOtpApiRequest): Response<BaseResponse>

    @POST("verify-otp")
    suspend fun verifyOtp(@Body verifyOtpApiRequest: VerifyOtpApiRequest): Response<VerifyOtpApiResponse>

    @POST("user/get-profile")
    suspend fun getProfile(@Body getRequest: getProfileRequest): Response<getProfileApiResponse>

    @POST("user/match-data")
    suspend fun getOtherProfile(@Body getRequest: getMatchData): Response<MatchUserResponse>


    @PATCH("user/edit-profile")
    suspend fun editProfile(@Body editProfileApiRequest: EditProfileApiRequest): Response<getProfileApiResponse>

    @PATCH("user/save-preferences")
    suspend fun savePreferences(@Body savePreferenceApiRequest: SavePreferenceApiRequest): Response<SavePrefernceApiResponse>

    @GET("user/preferences")
    suspend fun getPreferences(): Response<SavePrefernceApiResponse>

    @POST("user/device-token")
    suspend fun updateToken(@Body updateTokenRequest: updateTokenRequest): Response<BaseResponse>

    @POST("user/logout")
    suspend fun Logout(@Body updateTokenRequest: updateTokenRequest): Response<BaseResponse>

    @POST("user/recommendations")
    suspend fun user_recomendation(@Body request: UserRecommendationRequest): Response<RecommendationResponse>

    @POST("user/accept-request")
    suspend fun acceptRequest(@Body acceptRequest: AcceptRequest): Response<LikeDislikeResponse>

    @POST("user/send-request")
    suspend fun sendRequest(@Body likeDislikeRequest: LikeDislikeRequest): Response<LikeDislikeResponse>

    @HTTP(method = "DELETE", path = "user/delete-image", hasBody = true)
//    @DELETE("user/delete-image")
    suspend fun delete_image(@Body deleteImagesRequest: DeleteImagesRequest): Response<BaseResponse>


    @GET("user/get-likes/{pageno}/{limit}/{category}/")
    suspend fun getLikes(
        @Path("pageno") pageNo: Int,
        @Path("limit") limit: Int,
        @Path("category") category: String
    ): Response<LikeResponseModel>

    @GET("/user/get-matches/{pageno}/{limit}/{platform}")
    suspend fun getMatches(
        @Path("pageno") pageNo: Int,
        @Path("limit") limit: Int,
        @Path("platform") platform: String
    ): Response<MatchesResponse>

    @GET("/user/get-matches/{pageno}/{limit}/{platform}/{dialogidsonly}")
    suspend fun getMatchesDialogsOnly(
        @Path("pageno") pageNo: Int,
        @Path("limit") limit: Int,
        @Path("platform") platform: String,
        @Path("dialogidsonly") dialogIdsOnly: Int = 1
    ): Response<MatchDataDialogsIdsOnly>

    @POST("user/match-data")
    suspend fun getUserMatches(
        @Body request: MatchDataRequest
    ): Response<UserMatchesResponse>


    @GET("user/get-account-settings")
    suspend fun getAccountSettings(): Response<AccountDetailsResponse>

    @PATCH("user/account-settings")
    suspend fun accountSettings(@Body request: AccountSettingsRequest): Response<AccountSettingsRequest>


    @POST("user/un-match")
    suspend fun unMatch(
        @Body request: UnMatchRequest
    ): Response<BaseResponse>

    @POST("user/update-phone-number")
    suspend fun updatePhoneNumber(@Body request: UpdatePhoneNumberRequest): Response<BaseResponse>

    @POST("user/confirm-phone-otp")
    suspend fun confirmPhoneOtp(@Body request: ConfirmOtpRequest): Response<BaseResponse>

    @POST("user/report-user")
    suspend fun reportUser(@Body request: ReportRequest): Response<BaseResponse>

    @GET("user/all-hobbies")
    suspend fun getAllHobbies(): Response<Hobbies>

    @HTTP(method = "DELETE", path = "user/delete-account", hasBody = true)
    suspend fun deleteUser(@Body request: DeleteAccount): Response<BaseResponse>

    @GET("user/report-user-options")
    suspend fun getReportOptions(): Response<ReportData>

    @POST(value = "social-login")
    suspend fun socialLogin(@Body request: SocialRequest): Response<VerifyOtpApiResponse>

    @POST(value = "user/nearYou")
    suspend fun getNearYouUsers(@Body request: NearYouRequest): Response<NearYouData>

    @POST(value = "user/update-lat-lng")
    suspend fun updateCurrentLocation(@Body request: CurrentLocationUpdateRequest): Response<BaseResponse>

    /*Venue Api's*/

    @POST(value = "venue/create")
    suspend fun createVenue(@Body request: CreateVenue): Response<BaseApiResponse<MyVenuesData>>

    @PATCH(value = "venue/update")
    suspend fun updateVenue(@Body request: UpdateVenueRequest): Response<BaseApiResponse<MyVenuesData>>

    @GET("venue/list/{pageno}/{limit}")
    suspend fun getMyVenuesList(
        @Path("pageno") pageNo: Int,
        @Path("limit") limit: Int
    ): Response<BaseApiResponse<VenueResponse>>

    @HTTP(method = "DELETE", path = "venue/delete-venue", hasBody = true)
    suspend fun deleteVenue(@Body request: DeleteVenue): Response<BaseResponse>

    @GET(value = "venue/types")
    suspend fun getVenueTypes(): Response<BaseApiResponse<ArrayList<VenueTypeAndAmbianceResponse>>>

    @GET(value = "venue/ambiance")
    suspend fun getVenueAmbiance(): Response<BaseApiResponse<ArrayList<VenueTypeAndAmbianceResponse>>>

    @PUT(value = "venue/submit-venue")
    suspend fun submitVenue(@Body request: SubmitVenue): Response<BaseApiResponse<MyVenuesData>>

    @GET(value = "venue/{id}/{info}/{status}") // info - send - 0 for full data and 1 for short data , status - 0 for all type or venues and 1 for active/approved venues
    suspend fun getVenueById(
        @Path("id") venueId: String,
        @Path("info") info: Int,
        @Path("status") status: Int
    ): Response<BaseApiResponse<MyVenuesData>>

    @POST(value = "user/search-user")
    suspend fun searchByUsername(@Body request: SearchByUsername): Response<BaseApiResponse<SearchUsernameResponse>>

    @POST(value = "friend/send-invite")
    suspend fun sendInvitation(@Body request: SendInviteRequest): Response<BaseApiResponse<SendInvitationResponse>>

    @PUT(value = "friend/accept-invite")
    suspend fun acceptInvitation(@Body request: AcceptInviteRequest): Response<BaseResponse>

    @GET(value = "friend/all-invites/{pageno}/{limit}")
    suspend fun getAllInvites(
        @Path("pageno") pageNo: Int,
        @Path("limit") limit: Int
    ): Response<BaseApiResponse<AllInvitesResponse>>

    @GET(value = "friend/list/{pageno}/{limit}")
    suspend fun getFriendList(
        @Path("pageno") pageNo: Int,
        @Path("limit") limit: Int
    ): Response<BaseApiResponse<CloseFriendsResponse>>

    @HTTP(method = "DELETE", path = "friend/undo-invite", hasBody = true)
    suspend fun undoInvite(@Body request: SendInviteRequest): Response<BaseApiResponse<JSONObject>>

    @HTTP(method = "DELETE", path = "friend/reject-invite", hasBody = true)
    suspend fun rejectInvite(@Body request: AcceptInviteRequest): Response<BaseApiResponse<JSONObject>>

    @HTTP(method = "DELETE", path = "friend/unfriend", hasBody = true)
    suspend fun removeFriend(@Body request: SendInviteRequest): Response<BaseApiResponse<JSONObject>>

    @POST("venue/nearyou-hotspots")
    suspend fun getNearByVenues(@Body request: NearByVenueRequest): Response<BaseApiResponse<HotspotVenuesData>>

    @POST(value = "roaming/add-timer")
    suspend fun addTimer(@Body request: AddTimerRequest): Response<BaseApiResponse<AddTimerResponse>>

    @PUT(value = "roaming/update-timer")
    suspend fun updateRoamingTimer(@Body request: AddTimerRequest): Response<BaseResponse>

    @GET(value = "roaming/get-status")
    suspend fun getStatus(): Response<BaseApiResponse<RoamingTimerStatusResponse>>

    @HTTP(method = "DELETE", path = "roaming/delete-timer")
    suspend fun deleteRoamingTimer(): Response<BaseResponse>

    @POST(value = "friend/search-close-friend")
    suspend fun searchCloseFriends(@Body request: SearchCloseFriendRequest): Response<BaseApiResponse<SearchCloseFriendResponse>>

    @POST(value = "venue/upload-image")
    suspend fun uploadVenueImages(@Body request: VenueImageAddRequest): Response<BaseApiResponse<VenueImagesResponse>>

    @GET(value = "venue/get-images/{venue_id}/{pageno}/{limit}")
    suspend fun getVenueImages(
        @Path("venue_id") venueId: String,
        @Path("pageno") pageNo: Int,
        @Path("limit") limit: Int
    ): Response<BaseApiResponse<VenuePostsResponse>>

    @HTTP(method = "DELETE", path = "venue/delete-image", hasBody = true)
    suspend fun deleteVenueImages(@Body request: DeleteVenueImageRequest): Response<BaseResponse>

    @GET(value = "roaming/get-singles-profile/{venue_id}/{pageno}/{limit}")
    suspend fun getSinglesForVenue(
        @Path("venue_id") venueId: String,
        @Path("pageno") pageNo: Int,
        @Path("limit") limit: Int
    ): Response<BaseApiResponse<VenueSinglesResponse>>

    @GET(value = "/user/venue-subscription-status")
    suspend fun venueSubscriptionStatus(): Response<BaseApiResponse<SubscriptionStatus>>
}