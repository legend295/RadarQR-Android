package com.radarqr.dating.android.hotspots.roamingtimer

import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.radarqr.dating.android.base.DataResult
import com.radarqr.dating.android.data.model.AddTimerRequest
import com.radarqr.dating.android.data.model.RoamingTimerStatusResponse
import com.radarqr.dating.android.data.repository.DataRepository
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RoamingTimerViewModel(private val dataRepository: DataRepository) : ViewModel() {

    var roamingTimerResponse: RoamingTimerStatusResponse? = null
    fun addTimer(request: AddTimerRequest) = dataRepository.addTimer(request).asLiveData()
    fun updateRoamingTimer(request: AddTimerRequest) = dataRepository.updateRoamingTimer(request).asLiveData()

    fun getTimerStatus() = dataRepository.getStatus().asLiveData()

    fun deleteRoamingTimer(
        lifeCycleScope: LifecycleCoroutineScope,
        response: suspend (Boolean?, Pair<Int?, String?>?) -> Unit
    ) {
        lifeCycleScope.launchWhenStarted {
            dataRepository.deleteRoamingTimer().collect {
                when (it) {
                    DataResult.Empty -> {}
                    is DataResult.Failure -> {
                        response(false, Pair(it.statusCode, it.message))
                    }

                    DataResult.Loading -> {}
                    is DataResult.Success -> {
                        response(true, Pair(it.statusCode, it.data.message))
                    }
                }
            }
        }
    }

    fun getTimerStatus(
        lifeCycleScope: LifecycleCoroutineScope,
        response: suspend (RoamingTimerStatusResponse?, Pair<Int?, String?>?) -> Unit
    ) {
        lifeCycleScope.launchWhenStarted {
            dataRepository.getStatus().collect {
                when (it) {
                    DataResult.Empty -> {}
                    is DataResult.Failure -> {
                        roamingTimerResponse = null
                        response.invoke(null, Pair(it.statusCode, it.message))
                    }

                    DataResult.Loading -> {}
                    is DataResult.Success -> {
                        roamingTimerResponse = it.data.data
                        response.invoke(it.data.data, null)

                    }
                }
            }
        }
    }

    fun getTimerDifference(): Long {
        roamingTimerResponse?.let {

            val startLong = Date().time
            val endLong = it.expire_ts.toLong() * 1000
            (startLong/1000).getEndTime()
            it.expire_ts.toLong().getEndTime()

            return endLong - startLong
        } ?: return 0
    }

    private fun Long.getEndTime() {
        val cal = Calendar.getInstance()
        cal.timeZone = TimeZone.getTimeZone("UTC")
        cal.time.time = this
        Log.d("time_after", "END TIME = ${cal.time}")
        Log.d("time_after", "END TIME = ${convertLongToTime(this * 1000)}")
    }

    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return format.format(date)
    }

    fun clear(){
        roamingTimerResponse = null
    }
}