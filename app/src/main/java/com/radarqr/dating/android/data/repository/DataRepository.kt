package com.radarqr.dating.android.data.repository

import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.data.api.ApiHelper
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject

class DataRepository(private val apiHelper: ApiHelper) : BaseDataSource() {
    /*register repo*/

    fun sendOtp(sendOtpApiRequest: SendOtpApiRequest)
            : Flow<DataResult<BaseResponse>> = flow {
        emit(safeApiCall { apiHelper.sendOtp(sendOtpApiRequest) })
    }

    fun verifyOtp(verifyOtpApiRequest: VerifyOtpApiRequest)
            : Flow<DataResult<VerifyOtpApiResponse>> = flow {
        emit(safeApiCall { apiHelper.verifyOtp(verifyOtpApiRequest) })

    }

    fun getProfileApi(getProfileApiRequest: getProfileRequest)
            : Flow<DataResult<getProfileApiResponse>> = flow {

        emit(safeApiCall { apiHelper.getProfile(getProfileApiRequest) })

    }

    fun getOtherProfileApi(getRequest: getMatchData)
            : Flow<DataResult<MatchUserResponse>> = flow {

        emit(safeApiCall { apiHelper.getOtherProfile(getRequest) })

    }

    fun getLikesApi(pageno: Int, limit: Int, category: String)
            : Flow<DataResult<LikeResponseModel>> = flow {
        emit(safeApiCall { apiHelper.getLikes(pageno, limit, category) })
    }

    fun getMatches(pageNo: Int, limit: Int, platform: String)
            : Flow<DataResult<MatchesResponse>> = flow {
        emit(safeApiCall { apiHelper.getMatches(pageNo, limit, platform) })
    }

    fun getMatchesDialogsOnly(pageNo: Int, limit: Int, platform: String)
            : Flow<DataResult<MatchDataDialogsIdsOnly>> = flow {
        emit(safeApiCall { apiHelper.getMatchesDialogsOnly(pageNo, limit, platform) })
    }

    fun getUserMatchesApi(request: MatchDataRequest)
            : Flow<DataResult<UserMatchesResponse>> = flow {
        emit(safeApiCall { apiHelper.getUserMatches(request) })

    }

    fun unMatcheApi(request: UnMatchRequest)
            : Flow<DataResult<BaseResponse>> = flow {
        emit(safeApiCall { apiHelper.unMatch(request) })

    }

    fun editProfileApi(editProfileApiRequest: EditProfileApiRequest)
            : Flow<DataResult<getProfileApiResponse>> = flow {
        emit(safeApiCall { apiHelper.editProfile(editProfileApiRequest) })

    }

    fun savePreferences(savePreferenceApiRequest: SavePreferenceApiRequest)
            : Flow<DataResult<SavePrefernceApiResponse>> = flow {
        emit(safeApiCall { apiHelper.savePreferences(savePreferenceApiRequest) })

    }

    fun getPreferences()
            : Flow<DataResult<SavePrefernceApiResponse>> = flow {
        emit(safeApiCall { apiHelper.getPreferences() })

    }

    // update token
    fun updateTokenApi(updateTokenRequest: updateTokenRequest)
            : Flow<DataResult<BaseResponse>> = flow {
        emit(safeApiCall { apiHelper.updateToken(updateTokenRequest) })

    }

    fun LogoutApi(updateTokenRequest: updateTokenRequest)
            : Flow<DataResult<BaseResponse>> = flow {
        emit(safeApiCall { apiHelper.Logout(updateTokenRequest) })

    }

    fun userRecomandation(userRecommendationRequest: UserRecommendationRequest)
            : Flow<DataResult<RecommendationResponse>> = flow {
        emit(safeApiCall { apiHelper.user_recomendation(userRecommendationRequest) })
    }

    fun getNearYouUsers(request: NearYouRequest): Flow<DataResult<NearYouData>> = flow {
        emit(safeApiCall { apiHelper.getNearYouUsers(request) })
    }

    fun deleteImage(deleteImagesRequest: DeleteImagesRequest)
            : Flow<DataResult<BaseResponse>> = flow {
        emit(safeApiCall { apiHelper.delete_image(deleteImagesRequest) })

    }

    fun sendRequest(likeDislikeRequest: LikeDislikeRequest)
            : Flow<DataResult<LikeDislikeResponse>> = flow {
        emit(safeApiCall { apiHelper.sendRequest(likeDislikeRequest) })

    }

    fun acceptRequest(acceptRequest: AcceptRequest)
            : Flow<DataResult<LikeDislikeResponse>> = flow {
        emit(safeApiCall { apiHelper.acceptRequest(acceptRequest) })

    }

    fun getAccountDetails(): Flow<DataResult<AccountDetailsResponse>> = flow {
        emit(safeApiCall { apiHelper.getAccountSettings() })
    }

    fun accountSettings(request: AccountSettingsRequest): Flow<DataResult<AccountSettingsRequest>> =
        flow {
            emit(safeApiCall { apiHelper.accountSettings(request) })
        }

    fun unMatchApi(request: UnMatchRequest)
            : Flow<DataResult<BaseResponse>> = flow {
        emit(safeApiCall { apiHelper.unMatch(request) })
    }

    fun updatePhoneNumber(request: UpdatePhoneNumberRequest): Flow<DataResult<BaseResponse>> =
        flow {
            emit(safeApiCall { apiHelper.updatePhoneNumber(request) })
        }

    fun confirmPhoneOtp(request: ConfirmOtpRequest): Flow<DataResult<BaseResponse>> =
        flow {
            emit(safeApiCall { apiHelper.confirmPhoneOtp(request) })
        }

    fun reportUser(request: ReportRequest): Flow<DataResult<BaseResponse>> =
        flow {
            emit(safeApiCall { apiHelper.reportUser(request) })
        }

    fun getAllHobbies(): Flow<DataResult<Hobbies>> =
        flow {
            emit(safeApiCall { apiHelper.getAllHobbies() })
        }

    fun deleteUser(request: DeleteAccount): Flow<DataResult<BaseResponse>> =
        flow {
            emit(safeApiCall { apiHelper.deleteUser(request) })
        }

    fun getReportOptions(): Flow<DataResult<ReportData>> =
        flow {
            emit(safeApiCall { apiHelper.getReportOptions() })
        }

    fun socialLogin(request: SocialRequest): Flow<DataResult<VerifyOtpApiResponse>> =
        flow {
            emit(safeApiCall { apiHelper.socialLogin(request) })
        }

    fun updateCurrentLocation(request: CurrentLocationUpdateRequest): Flow<DataResult<BaseResponse>> =
        flow {
            emit(safeApiCall { apiHelper.updateCurrentLocation(request) })
        }

    /*VENUE API'S*/

    fun createVenue(request: CreateVenue): Flow<DataResult<BaseApiResponse<MyVenuesData>>> =
        flow {
            emit(safeApiCall { apiHelper.createVenue(request) })
        }

    fun updateVenue(request: UpdateVenueRequest): Flow<DataResult<BaseApiResponse<MyVenuesData>>> =
        flow {
            emit(safeApiCall { apiHelper.updateVenue(request) })
        }

    fun getMyVenuesList(
        pageNo: Int,
        limit: Int
    ): Flow<DataResult<BaseApiResponse<VenueResponse>>> =
        flow {
            emit(safeApiCall { apiHelper.getMyVenuesList(pageNo, limit) })
        }

    fun deleteVenue(deleteVenue: DeleteVenue): Flow<DataResult<BaseResponse>> =
        flow { emit(safeApiCall { apiHelper.deleteVenue(request = deleteVenue) }) }

    fun getVenueTypes(): Flow<DataResult<BaseApiResponse<ArrayList<VenueTypeAndAmbianceResponse>>>> =
        flow { emit(safeApiCall { apiHelper.getVenueTypes() }) }

    fun getVenueAmbiance(): Flow<DataResult<BaseApiResponse<ArrayList<VenueTypeAndAmbianceResponse>>>> =
        flow { emit(safeApiCall { apiHelper.getVenueAmbiance() }) }

    fun submitVenue(request: SubmitVenue): Flow<DataResult<BaseApiResponse<MyVenuesData>>> =
        flow {
            emit(safeApiCall { apiHelper.submitVenue(request) })
        }

    fun getVenueById(
        venueId: String,
        info: Int,
        status: Int
    ): Flow<DataResult<BaseApiResponse<MyVenuesData>>> =
        flow {
            emit(safeApiCall { apiHelper.getVenueById(venueId, info, status) })
        }

    fun searchByUsername(request: SearchByUsername): Flow<DataResult<BaseApiResponse<SearchUsernameResponse>>> =
        flow { emit(safeApiCall { apiHelper.searchByUsername(request) }) }

    fun sendInvitation(request: SendInviteRequest): Flow<DataResult<BaseApiResponse<SendInvitationResponse>>> =
        flow { emit(safeApiCall { apiHelper.sendInvitation(request) }) }

    fun acceptInvitation(request: AcceptInviteRequest): Flow<DataResult<BaseResponse>> =
        flow { emit(safeApiCall { apiHelper.acceptInvitation(request) }) }

    fun getAllInvites(
        pageNo: Int,
        limit: Int
    ): Flow<DataResult<BaseApiResponse<AllInvitesResponse>>> =
        flow { emit(safeApiCall { apiHelper.getAllInvites(pageNo, limit) }) }

    fun getFriendList(
        pageNo: Int,
        limit: Int
    ): Flow<DataResult<BaseApiResponse<CloseFriendsResponse>>> =
        flow { emit(safeApiCall { apiHelper.getFriendList(pageNo, limit) }) }

    fun undoInvite(request: SendInviteRequest): Flow<DataResult<BaseApiResponse<JSONObject>>> =
        flow { emit(safeApiCall { apiHelper.undoInvite(request) }) }

    fun removeFriend(request: SendInviteRequest): Flow<DataResult<BaseApiResponse<JSONObject>>> =
        flow { emit(safeApiCall { apiHelper.removeFriend(request) }) }

    fun rejectInvite(request: AcceptInviteRequest): Flow<DataResult<BaseApiResponse<JSONObject>>> =
        flow { emit(safeApiCall { apiHelper.rejectInvite(request) }) }

    fun getNearByVenues(request: NearByVenueRequest): Flow<DataResult<BaseApiResponse<HotspotVenuesData>>> =
        flow { emit(safeApiCall { apiHelper.getNearByVenues(request) }) }

    fun addTimer(request: AddTimerRequest): Flow<DataResult<BaseApiResponse<AddTimerResponse>>> =
        flow { emit(safeApiCall { apiHelper.addTimer(request) }) }

    fun updateRoamingTimer(request: AddTimerRequest): Flow<DataResult<BaseResponse>> =
        flow { emit(safeApiCall { apiHelper.updateRoamingTimer(request) }) }

    fun getStatus(): Flow<DataResult<BaseApiResponse<RoamingTimerStatusResponse>>> =
        flow { emit(safeApiCall { apiHelper.getStatus() }) }

    fun deleteRoamingTimer(): Flow<DataResult<BaseResponse>> =
        flow { emit(safeApiCall { apiHelper.deleteRoamingTimer() }) }

    fun getSinglesForVenue(venueId: String, pageNo: Int, limit: Int) =
        flow { emit(safeApiCall { apiHelper.getSinglesForVenue(venueId, pageNo, limit) }) }

    fun deleteVenueImages(request: DeleteVenueImageRequest) =
        flow { emit(safeApiCall { apiHelper.deleteVenueImages(request) }) }

    fun uploadVenueImages(request: VenueImageAddRequest) =
        flow { emit(safeApiCall { apiHelper.uploadVenueImages(request) }) }

    fun searchCloseFriends(request: SearchCloseFriendRequest) =
        flow { emit(safeApiCall { apiHelper.searchCloseFriends(request) }) }


    fun getVenueImages(venueId: String, pageNo: Int, limit: Int) =
        flow { emit(safeApiCall { apiHelper.getVenueImages(venueId, pageNo, limit) }) }

    fun venueSubscriptionStatus() =
        flow { emit(safeApiCall { apiHelper.venueSubscriptionStatus() }) }

}

