package com.airhomestays.app.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.data.remote.paging.NetworkState

private fun getErrorMessage(report: PagingRequestHelper.StatusReport): String {
    return PagingRequestHelper.RequestType.values().mapNotNull {
        report.getErrorFor(it)?.message
    }.first()
}

fun PagingRequestHelper.createStatusLiveData(): LiveData<NetworkState> {
    val liveData = MutableLiveData<NetworkState>()
    addListener { report ->
        when {
            report.hasRunning() -> liveData.postValue(NetworkState.LOADING)
            report.hasError() -> liveData.postValue(NetworkState.error(Throwable()))
            else -> liveData.postValue(NetworkState.LOADED)
        }
    }
    return liveData
}
//getErrorMessage(report))