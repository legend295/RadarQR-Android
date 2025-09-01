package com.radarqr.dating.android.ui.welcome.registerScreens.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize


sealed class UserModel {
    class ImagesResponse {
        @SerializedName("Image")
        val profile: ImageClasss? = null

    }

    @Parcelize
    data class ImageClasss(val url: String, val key: String) : Parcelable
}