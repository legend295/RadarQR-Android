package com.radarqr.dating.android.data.model

data class BaseApiResponse<T>(
    val data: T?,
    val message: String,
    val status: Int
)