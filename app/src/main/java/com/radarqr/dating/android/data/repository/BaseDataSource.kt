package com.radarqr.dating.android.data.repository


import com.radarqr.dating.android.base.BaseActivity
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.utility.BaseUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

abstract class BaseDataSource : BaseActivity() {

    suspend fun <T : Any> safeApiCall(call: suspend () -> Response<T>): DataResult<T> {

        var responseData: DataResult<T>
        try {
            val response = withContext(Dispatchers.IO) {
                call.invoke()
            }

            responseData = if (response.isSuccessful) {
                DataResult.Success(response.code(), response.body()!!)
            } else {
                val responseBody = response.errorBody()!!
                val errorMessage = getErrorMsg(responseBody)
                if (response.code() == Constants.USER_REPORTED) {
                    if (!response.raw().request.url.encodedPath.contains("send-otp")) {
                        BaseUtils.startInitialActivity()
                    }
                }
                DataResult.Failure(
                    statusCode = response.code(),
                    message = if (errorMessage.isNotEmpty()) errorMessage[0] else "",
                    data = if (errorMessage.isNotEmpty() && errorMessage.size == 2) errorMessage[1] else ""
                )


            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                e.printStackTrace()

                responseData = when (e) {
                    is UnknownHostException, is IOException,
                    is ConnectException, is SocketTimeoutException -> {
                        DataResult.Failure(
                            message = Constants.CONNECTION_ERROR
                        )
                    }

                    else -> {
                        DataResult.Failure(
                            message = e.message
                        )
                    }
                }
            }
        }

        return responseData
    }

    private fun getErrorMsg(responseBody: ResponseBody): ArrayList<String> {
        val list = ArrayList<String>()
        return try {
            val jsonObject = JSONObject(responseBody.string())
            list.add(jsonObject.getString("message"))
            list.add(if (jsonObject.has("data")) jsonObject["data"].toString() else "")
            list

        } catch (e: java.lang.Exception) {
            list
        }

    }
}