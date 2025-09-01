package com.radarqr.dating.android.ui.response

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RegisterApiRequest(
    val verification_status: String,
    val firstname: String?,
    val lastname: String?,
    val username: String?,
    val email: String?,
    val gender: Int?,
    val country_code: String,
    val phone_number: String,
    val password: String?,
    val device_info: DeviceInfo
) : Parcelable {

    @Parcelize
    data class DeviceInfo(val device_type: String, val device_token: String) : Parcelable

}

