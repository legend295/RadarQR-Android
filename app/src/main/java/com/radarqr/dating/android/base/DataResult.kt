package com.radarqr.dating.android.base

sealed class DataResult<out T> {

    object Loading : DataResult<Nothing>()

    data class Failure(
        val statusCode: Int? = 0,
        val message: String? = null,
        val data: String? = ""
    ) :
        DataResult<Nothing>()

    data class Success<out T>(val statusCode: Int? = 0, val data: T) : DataResult<T>()

    object Empty : DataResult<Nothing>() //state flow return default value initially

}

