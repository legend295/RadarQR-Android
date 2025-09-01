package com.radarqr.dating.android.ui.home.main.model

data class LikeDislikeResponse(
    val `data`: Data,
    val message: String,
    val status: Int
)

data class Data(
    val __v: Int,
    val _id: String,
    val action_by: ActionBy,
    val category: String,
    val createdAt: String,
    val custom_params: CustomParams,
    val qb_dialog_id: String?,
    val receiver_id: String,
    val receiver_option_choosed: Boolean,
    val receiver_response_message: Any,
    val sender_id: String,
    val sender_option_choosed: Boolean,
    val updatedAt: String,
    val request_sent: Boolean,
    val request_receive: Boolean,
    val is_match: Boolean,
    val is_unmatch: Boolean,
    val is_decline: Boolean,
)

data class ActionBy(
    val `receiver`: Boolean,
    val sender: Boolean
)

data class CustomParams(
    val category: String,
    val class_name: String,
    val favourite: Boolean
)
