package com.radarqr.dating.android.ui.welcome.mobileLogin

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.model.SocialRequest
import com.radarqr.dating.android.data.repository.DataRepository
import com.radarqr.dating.android.ui.welcome.otpVerify.VerifyOtpApiResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException


class SendOtpViewModel constructor(private var dataRepository: DataRepository) :
    ViewModel() {

    var socialRequest: MutableLiveData<SocialRequest> = MutableLiveData(SocialRequest())
    var verifyOtpApiResponse: MutableLiveData<VerifyOtpApiResponse> = MutableLiveData()
    var facebookImage: String = ""

    fun SendOtp(sendOtpApiRequest: SendOtpApiRequest) =
        dataRepository.sendOtp(sendOtpApiRequest).asLiveData()

    fun updatePhoneNumber(request: UpdatePhoneNumberRequest) =
        dataRepository.updatePhoneNumber(request).asLiveData()

    fun confirmPhoneOtp(request: ConfirmOtpRequest) =
        dataRepository.confirmPhoneOtp(request).asLiveData()

    @ExperimentalCoroutinesApi
    suspend fun handleSignInResult(
        completedTask: Task<GoogleSignInAccount>
    ): GoogleSignInAccount =
        suspendCancellableCoroutine { continuation ->
            try {
                continuation.resume(completedTask.getResult(ApiException::class.java)) {}
            } catch (e: ApiException) {
                continuation.resumeWithException(e)
            }
        }


    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun getFacebookToken(facebookCallbackManager: CallbackManager): LoginResult =
        suspendCancellableCoroutine { continuation ->
            LoginManager.getInstance()
                .registerCallback(facebookCallbackManager, object :
                    FacebookCallback<LoginResult> {

                    override fun onSuccess(result: LoginResult) {
                        Log.d("Facebook_login", "$result")
                        continuation.resume(result) { }
                    }

                    override fun onCancel() {
                        // handling cancelled flow (probably don't need anything here)
                        Log.d("Facebook_login", "Login cancel")
                        continuation.cancel()

                    }

                    override fun onError(error: FacebookException) {
                        // Facebook authorization error
                        Log.d("Facebook_login", "error -=- $error")
                        continuation.resumeWithException(error)
                    }
                })
        }


    fun getFacebookData(loginResult: LoginResult, data: () -> Unit) {
        val graphRequest =
            GraphRequest.newMeRequest(loginResult.accessToken) { obj, _ ->
                Log.e("Facebook_login", "completed $obj")
                obj?.let {
                    socialRequest.value?.social_id = it.optString("id")
                    socialRequest.value?.email = it.optString("email")
                    socialRequest.value?.name = "${it["first_name"]} ${it["last_name"]}"
                    socialRequest.value?.social_type = Constants.FACEBOOK
                    facebookImage =
                        "https://graph.facebook.com/${it.optString("id")}/picture?type=large"
                    data.invoke()
                }
            }
        val bundle = Bundle()
        bundle.putString("fields", "id, first_name, last_name,email,picture,gender,location")
        graphRequest.parameters = bundle
        graphRequest.executeAsync()
    }

    fun clear() {
        socialRequest = MutableLiveData(SocialRequest())
        verifyOtpApiResponse = MutableLiveData()
        facebookImage = ""
    }

}





