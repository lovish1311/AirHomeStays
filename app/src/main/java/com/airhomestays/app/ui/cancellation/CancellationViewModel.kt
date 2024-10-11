package com.airhomestays.app.ui.cancellation

import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.CancelReservationMutation
import com.airhomestays.app.CancellationDataQuery
import com.airhomestays.app.R
import com.airhomestays.app.ViewListingDetailsQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import javax.inject.Inject


class CancellationViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<CancellationNavigator>(dataManager, resourceProvider) {

    private lateinit var cancellationDetails: MutableLiveData<CancellationDataQuery.Results?>
    var retryCalled = ""
    lateinit var listingDetails: MutableLiveData<ViewListingDetailsQuery.Results?>
    var bathroomType = "Private Room"
    var blockedDatesArray = ArrayList<String>()
    var ispublish = ObservableBoolean(false)
    val isListingDetailsLoad = MutableLiveData<Boolean>()
    var beds = MutableLiveData<Int>()
    var isPreview = false


    private fun getThreadId(): Int {
        return try {
            cancellationDetails.value!!.threadId!!
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
            0
        }
    }

    fun loadListingDetails(): MutableLiveData<ViewListingDetailsQuery.Results?> {
        if (!::listingDetails.isInitialized) {
            listingDetails = MutableLiveData()
        }
        return listingDetails
    }

    fun loadCancellationDetails(
        resverationId: Int,
        userType: String
    ): MutableLiveData<CancellationDataQuery.Results?> {
        if (!::cancellationDetails.isInitialized) {
            cancellationDetails = MutableLiveData()
            getCancellationDetails(resverationId, userType)
        }
        return cancellationDetails
    }

    fun getListingDetails(resverationId: Int) {
        val buildQuery: ViewListingDetailsQuery = if (isPreview) {
            ViewListingDetailsQuery(listId = resverationId, preview = true.toOptional())
        } else {
            ViewListingDetailsQuery(listId = resverationId)
        }

        compositeDisposable.add(dataManager.doListingDetailsApiCall(buildQuery)
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data?.viewListing
                    if (data!!.status == 200) {
                        val dates = data.results!!.blockedDates
                        if (dates != null) {
                            if (dates.size > 0) {
                                dates.forEachIndexed { index, blockedDate ->
                                    if (blockedDate?.calendarStatus.equals("available").not()) {
                                        val timestamp = blockedDate?.blockedDates!!
                                        blockedDatesArray.add(
                                            Utils.getBlockedDateFormat(
                                                timestamp
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        data.results!!.settingsData!!.forEachIndexed { _, settingsDatum ->
                            if (settingsDatum?.listsettings != null) {
                                if (settingsDatum.listsettings!!.settingsType!!
                                        .typeName == "bathroomType"
                                ) {
                                    bathroomType = settingsDatum.listsettings!!.itemName!!
                                }
                            }
                        }
                        listingDetails.value = data.results
                        beds.value = data!!.results!!.beds!!
                        isListingDetailsLoad.value = true


                    } else {
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, { handleException(it) }
            )
        )
    }

    public fun getCancellationDetails(resverationId: Int, userType: String) {
        val buildQuery = CancellationDataQuery(
            userType = userType,
            currency = getUserCurrency().toOptional(),
            reservationId = resverationId
        )

        compositeDisposable.add(dataManager.getCancellationDetails(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                if (response.data?.cancelReservationData?.results != null) {
                    getListingDetails(
                        response.data?.cancelReservationData?.results?.listId!!
                    )
                    try {
                        val data = response.data?.cancelReservationData
                        if (data!!.status == 200) {
                            cancellationDetails.value = data.results
                        } else if (data.status == 500) {
                            navigator.openSessionExpire("CancellationVM")
                        } else {
                            data.errorMessage?.let {
                                navigator.showToast(it)
                            } ?: navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                } else {
                    val data = response.data?.cancelReservationData
                    if (data!!.status == 400) {
                        data.errorMessage?.let {
                            navigator.showToast(it)
                            navigator.moveBackScreen()
                        }
                    }
                }
            }, {
                handleException(it)
            })
        )
    }

    fun cancelReservation(text: String, reservationId: Int) {
        try {
            val buildMutation = CancelReservationMutation(
                reservationId = reservationId,
                threadId = getThreadId(),
                message = text,
                cancellationPolicy = getCancellationPolicy(),
                checkIn = getCheckIn(),
                checkOut = getCheckOut(),
                refundToGuest = getRefundToGuest(),
                cancelledBy = getCancelledBy(),
                guestServiceFee = getGuestServiceFee(),
                guests = getGuest(),
                total = getTotal(),
                payoutToHost = getPayoutToHost(),
                hostServiceFee = getHostServiceFee(),
                isTaxRefunded = cancellationDetails.value?.isTaxRefunded.toOptional(),
                currency = cancellationDetails.value?.currency!!,
            )

            compositeDisposable.add(dataManager.cancelReservation(buildMutation)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({ response ->
                    try {
                        val data = response.data?.cancelReservation
                        if (data!!.status == 200) {
                            retryCalled = ""
                            navigator.showToast(resourceProvider.getString(R.string.reservation_cancelled))
                            navigator.moveBackScreen()
                        } else if (data.status == 500) {
                            navigator.openSessionExpire("")
                        } else {
                            data.errorMessage?.let {
                                navigator.showToast(it)
                            } ?: navigator.showError()
                        }
                    } catch (e: KotlinNullPointerException) {
                        e.printStackTrace()
                        navigator.showError()
                    }
                }, {
                    handleException(it)
                })
            )
        } catch (e: Exception) {
            e.printStackTrace()
            navigator.showError()
        }
    }

    private fun getCancellationPolicy(): String {
        return try {
            cancellationDetails.value!!.cancellationPolicy!!
        } catch (E: KotlinNullPointerException) {
            ""
        }
    }

    private fun getCheckIn(): String {
        return try {
            cancellationDetails.value!!.checkIn!!
        } catch (E: KotlinNullPointerException) {
            ""
        }
    }

    private fun getCheckOut(): String {
        return try {
            cancellationDetails.value!!.checkOut!!
        } catch (E: KotlinNullPointerException) {
            ""
        }
    }

    private fun getRefundToGuest(): Double {
        return try {
            cancellationDetails.value!!.refundToGuest!!
        } catch (E: KotlinNullPointerException) {
            0.0
        }
    }

    private fun getCancelledBy(): String {
        return try {
            cancellationDetails.value!!.cancelledBy!!
        } catch (E: KotlinNullPointerException) {
            ""
        }
    }

    private fun getGuest(): Int {
        return try {
            cancellationDetails.value!!.guests!!
        } catch (E: KotlinNullPointerException) {
            0
        }
    }

    private fun getTotal(): Double {
        return try {
            cancellationDetails.value!!.total!!
        } catch (E: KotlinNullPointerException) {
            0.0
        }
    }

    private fun getPayoutToHost(): Double {
        return try {
            cancellationDetails.value!!.payoutToHost!!
        } catch (E: KotlinNullPointerException) {
            0.0
        }
    }

    private fun getHostServiceFee(): Double {
        return try {
            cancellationDetails.value!!.hostServiceFee!!
        } catch (E: KotlinNullPointerException) {
            0.0
        }
    }

    private fun getGuestServiceFee(): Double {
        return try {
            cancellationDetails.value!!.guestServiceFee!!
        } catch (E: KotlinNullPointerException) {
            0.0
        }
    }

    fun currencyConverter(currency: String, total: Double): String {
        return getCurrencySymbol() + Utils.formatDecimal(getConvertedRate(currency, total))
    }

}
