package com.airhomestays.app.data.remote.paging

enum class Status {
    RUNNING,
    SUCCESS,
    FAILED,
    SUCCESSNODATA,
    RETRY,
    EXPIRED,
    INITIALRETRY
}

@Suppress("DataClassPrivateConstructor")
data class NetworkState private constructor(
    val status: Status,
    val msg: Throwable? = null
) {
    companion object {
        val LOADED = NetworkState(Status.SUCCESS)
        val LOADING = NetworkState(Status.RUNNING)
        val SUCCESSNODATA = NetworkState(Status.SUCCESSNODATA)
        val FAILED = NetworkState(Status.RETRY)
        val EXPIRED = NetworkState(Status.EXPIRED)
        val INITIALFAILED = NetworkState(Status.INITIALRETRY)
        fun error(e: Throwable?) = NetworkState(Status.FAILED, e)
    }
}