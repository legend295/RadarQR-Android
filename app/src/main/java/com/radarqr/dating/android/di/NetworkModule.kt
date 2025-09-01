package com.radarqr.dating.android.di

import android.content.Context
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import com.radarqr.dating.android.BuildConfig
import com.radarqr.dating.android.app.RaddarApp
import com.radarqr.dating.android.constant.Constants
import com.radarqr.dating.android.data.api.ApiHelper
import com.radarqr.dating.android.utility.PreferencesHelper
import com.radarqr.dating.android.utility.PreferencesHelper.PreferencesKeys.KEY_AUTH
import com.radarqr.dating.android.utility.Utility.showToast

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


val networkModule = module {
    single {
        provideLoggingInterceptor()
    }
    single {
        networkConnectionInterceptor()
    }
    single {
        provideHttpClient(get(), get(), get())
    }
    single {
        provideApiProvider(get())
    }
    single {
        provideApiService(get())
    }
}

fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    val logging = HttpLoggingInterceptor()
    logging.level = HttpLoggingInterceptor.Level.BODY
    return logging
}

fun networkConnectionInterceptor(): NetworkConnectionInterceptor {
    return NetworkConnectionInterceptor(RaddarApp.appContext!!)
}

var appInterceptor: Interceptor? = null
fun provideHttpClient(
    logging: HttpLoggingInterceptor,
    networkCheckIntercept: NetworkConnectionInterceptor,
    preferencesHelper: PreferencesHelper
): OkHttpClient {
    val httpClient = OkHttpClient.Builder()
    if (appInterceptor == null)
        appInterceptor = RadarIntercept(RaddarApp.appContext!!, preferencesHelper)
    httpClient.addInterceptor(appInterceptor!!)
    httpClient.addInterceptor(networkCheckIntercept)
    httpClient.addInterceptor(logging)
        .connectTimeout(100, TimeUnit.SECONDS)
        .readTimeout(100, TimeUnit.SECONDS)
    return httpClient.build()
}


fun provideApiProvider(okHttpClient: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
}

fun provideApiService(retrofit: Retrofit): ApiHelper {
    return retrofit.create(ApiHelper::class.java)
}

class RadarIntercept(val context: Context, private val preferencesHelper: PreferencesHelper) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var flowValue: String?
        runBlocking(Dispatchers.IO) {
            flowValue = preferencesHelper.getValue(KEY_AUTH).first()
            println("user token: $flowValue")
        }
        var request = chain.request()
        val headers = request.headers.newBuilder()
            .add("Content-Type", "application/json")
            .add("Accept", "application/json")
            .add(
                "Authorization",
                "Bearer $flowValue"
            )
            .build()

        request = request.newBuilder().headers(headers).build()
//        val response = chain.proceed(request)
//        Log.d("MyApp", "Code : " + response.code)
//        return response
        return chain.proceed(request)
    }
}


class NetworkConnectionInterceptor(val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (!isInternetAvailable()) {
            Handler(Looper.getMainLooper()).post {
                context.showToast(
                    Constants.CONNECTION_ERROR
                )


            }

            throw NoConnectivityException()
        }
        return chain.proceed(chain.request())
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.activeNetworkInfo.also { return it != null && it.isConnected }
    }

    class NoConnectivityException : IOException() {
        override val message: String
            get() =
                "You are not connected to the internet. Please check your connection and try again."
    }
}
