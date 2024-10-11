package com.airhomestays.app.ui.host.hostReservation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.paging.PagedList
import com.airhomestays.app.GetAllReservationQuery
import com.airhomestays.app.R
import com.airhomestays.app.ReservationStatusMutation
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.data.remote.paging.Listing
import com.airhomestays.app.data.remote.paging.NetworkState
import com.airhomestays.app.ui.base.BaseNavigator
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import javax.inject.Inject

class HostTripsViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<BaseNavigator>(dataManager, resourceProvider) {

    val currencyBase = MutableLiveData<String>()
    val currencyRates = MutableLiveData<String>()

    val tripResult = MutableLiveData<Listing<GetAllReservationQuery.Result>>()
    lateinit var tripList: LiveData<PagedList<GetAllReservationQuery.Result>>
    val networkState: LiveData<NetworkState> = tripResult.switchMap() { it.networkState }
    val refreshState: LiveData<NetworkState> = tripResult.switchMap() { it.refreshState }

    var retryCalled = ""

    fun loadTrips(dateFilters: String): LiveData<PagedList<GetAllReservationQuery.Result>> {
        tripList = MutableLiveData()
        val buildQuery = GetAllReservationQuery(
            dateFilter = "previous".toOptional(),
            userType = "host".toOptional()
            )
        tripResult.value = dataManager.listOfTripsList(buildQuery, 10)
        tripList = tripResult.switchMap() {
            it.pagedList
        }
        return tripList
    }

    fun tripRefresh() {
        tripResult.value?.refresh?.invoke()
    }

    fun tripRetry() {
        tripResult.value?.retry?.invoke()
    }

    val upcomingTripResult = MutableLiveData<Listing<GetAllReservationQuery.Result>>()
    lateinit var upcomingTripList: LiveData<PagedList<GetAllReservationQuery.Result>>
    val upcomingNetworkState: LiveData<NetworkState> =
        upcomingTripResult.switchMap() { it.networkState }

    fun loadUpcomingTrips(dateFilters: String): LiveData<PagedList<GetAllReservationQuery.Result>> {
        upcomingTripList = MutableLiveData()
        val buildQuery = GetAllReservationQuery(
            dateFilter = "upcoming".toOptional(),
            userType = "host".toOptional()
        )
        upcomingTripResult.value = dataManager.listOfTripsList(buildQuery, 10)
        upcomingTripList = upcomingTripResult.switchMap() {
            it.pagedList
        }
        return upcomingTripList
    }

    fun approveReservation(
        threadId: Int,
        content: String,
        type: String,
        startDate: String,
        endDate: String,
        personCapacity: Int,
        reservationId: Int,
        actionType: String
    ) {
        val mutate = ReservationStatusMutation(
            threadId = threadId,
            type = type.toOptional(),
            startDate = startDate.toOptional(),
            endDate = endDate.toOptional(),
            personCapacity = personCapacity.toOptional(),
            reservationId = reservationId.toOptional(),
            actionType = actionType.toOptional()
        )
        compositeDisposable.add(dataManager.getReseravtionStatus(mutate)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe(
                {
                    try {
                        it.data?.let { response ->
                            if (response.reservationStatus?.status == 200) {
                                if (actionType == "approved") {
                                    navigator.showToast(resourceProvider.getString(R.string.reservation_approved))
                                } else if (actionType == "declined") {
                                    navigator.showToast(resourceProvider.getString(R.string.reservation_declined))
                                }
                                upcomingTripResult.value?.refresh?.invoke()
                            } else if (response.reservationStatus?.status == 500) {
                                navigator.openSessionExpire("")
                            } else {
                                navigator.showToast(resourceProvider.getString(R.string.list_not_available))
                            }
                        } ?: navigator.showError()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                },
                { handleException(it) }
            ))
    }

    fun upcomingTripRefresh() {
        upcomingTripResult.value?.refresh?.invoke()
    }

    fun upcomingTripRetry() {
        upcomingTripResult.value?.retry?.invoke()
    }
}
