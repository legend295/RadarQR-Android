package com.radarqr.dating.android.ui.home.quickBlox.model

import com.radarqr.dating.android.ui.home.settings.prodileModel.ImageData
import java.io.Serializable

data class UserMatchesResponse(
    val `data`: ArrayList<MatchedData>,
    val message: String,
    val status: Int
) : Serializable

data class UserData(
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

data class MatchedData(
    val _id: String,
    var name: String,
    var profile_pic: String,
    val quickblox_user_id: String,
    var imageData: ImageData = ImageData(),
    var matchData: ArrayList<MatchData>
) : Serializable

data class MatchData(
    val _id: String,
    val sender_id: String?,
    val receiver_id: String?,
    val sender_option_choosed: Boolean?,
    val receiver_option_choosed: Boolean?
):Serializable

data class CustomParams(
    val category: String,
    val class_name: String,
    val occupant_id_qb: String,
    val occupant_id_radar: String,
    val occupant_name: String,
    val occupant_profile_pic: String,
    val sender_profile_pic: String
)