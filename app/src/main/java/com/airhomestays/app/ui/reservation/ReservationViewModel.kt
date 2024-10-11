package com.airhomestays.app.ui.reservation

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.GetReservationQuery
import com.airhomestays.app.GetSecPaymentQuery
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.Utils
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import com.apollographql.apollo3.exception.ApolloNetworkException
import javax.inject.Inject

class ReservationViewModel @Inject constructor(
    dataManager: DataManager,
    private val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<ReservationNavigator>(dataManager, resourceProvider) {

    var reservationComplete = MutableLiveData<GetReservationQuery.GetReservation?>()
    var reservation = MutableLiveData<GetReservationQuery.Results?>()
    val reservationId = MutableLiveData<Int>()
    val gst = MutableLiveData<String>()
    val panNo = MutableLiveData<String>()
    var type = 1
    var config = 1

    fun getName(): String? {
        return dataManager.currentUserName
    }

    fun getSiteName(): String? {
        return dataManager.siteName
    }

    fun getReservationDetails() {
        val buildQuery = GetReservationQuery(
            reservationId = reservationId.value!!,
            convertCurrency = getUserCurrency().toOptional()
        )
        compositeDisposable.add(dataManager.getReservationDetails(buildQuery)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.getReservation
                    if (data?.status == 200) {
                        try {
                            if (data?.results!!.listData == null) {
                                navigator.show404Page()
                            }
                        } catch (e: Exception) {
                        }
                        reservationComplete.value = response.data!!.getReservation
                        reservation.value = data.results

                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("ReservationVM")
                    } else {
                        navigator.showError()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    navigator.showError()
                }

            }, {
                handleException(it)
            })
        )
    }

    fun getSecPayment() {
        try {
            val query = GetSecPaymentQuery(userId = dataManager.currentUserId!!)

            compositeDisposable.add(dataManager.getSecPayment(query)
                .doOnSubscribe { setIsLoading(true) }
                .doFinally { setIsLoading(false) }
                .performOnBackOutOnMain(scheduler)
                .subscribe({
                    try {
                        if (it.data?.getSecPayment?.status == "success"){
                           gst.value = it.data?.getSecPayment?.result?.gstNumber!!
                           panNo.value = it.data?.getSecPayment?.result?.panNumber!!

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, {
                    if (it is ApolloNetworkException) {
                        navigator.showOffline()
                    } else {
                        navigator.showError()
                        //navigator.finishScreen()
                    }
                })
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun currencyConverter(currency: String, total: Double): String {
        return getCurrencySymbol() + Utils.formatDecimal(getConvertedRate(currency, total))
    }

    fun clearStatusBar(activity: Activity) {
        println("prefTheme:: ${dataManager.prefTheme}")
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> {
                    activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.white)
                }

                Configuration.UI_MODE_NIGHT_NO -> {
                    var flags = activity.window.decorView.systemUiVisibility
                    flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    activity.window.decorView.systemUiVisibility = flags
                    activity.window.statusBarColor = ContextCompat.getColor(activity, R.color.white)
                }
            }

        }

    }
}