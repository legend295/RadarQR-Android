package com.radarqr.dating.android.ui.home.likes.model

import com.radarqr.dating.android.ui.home.settings.prodileModel.ImageData
import com.radarqr.dating.android.ui.home.settings.prodileModel.LocationDetail
import java.io.Serializable

data class LikeResponseModel(
    val `data`: LikeData,
    val message: String,
    val status: Int
)

data class LikeData(
    val inperson_count: Int = 0,
    val online_count: Int = 0,
    val total_records: Int = 0,
    val users: ArrayList<UserLikes> = ArrayList()
)

data class UserLikes(
    val _id: String? = null,
    val category: String? = null,
    val qb_dialog_id: String? = null,
    val receiver_id: String? = null,
    val sender_id: String? = null,
    val sender_message: String? = null,
    val user_detail: UserDetail? = null,
    val isHolderVisible: Boolean = false
) : Serializable

data class UserDetail(
    val about_me: String,
    val age: Int,
    val name: String,
    var profile_pic: String,
    var imageData: ImageData = ImageData(),
    var gender: String,
    var birthday: String = "",
    var show_age: Boolean? = false,
    val location: LocationDetail = LocationDetail(),
) : Serializable