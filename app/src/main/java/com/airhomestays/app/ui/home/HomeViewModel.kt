package com.airhomestays.app.ui.home

import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.GetDefaultSettingQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseNavigator
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.resource.ResourceProvider
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    dataManager: DataManager,
    val resourceProvider: ResourceProvider
) : BaseViewModel<HomeNavigator>(dataManager, resourceProvider) {

    val notification = MutableLiveData<Boolean>()
    var pref: SharedPreferences = dataManager.getPref()

    var loginStatus = 3
    var uiMode: Int? = null

    init {
        loginStatus = dataManager.currentUserLoggedInMode
    }

    fun validateData() {
        if (dataManager.currencyBase == null || dataManager.currencyRates == null) {
            defaultSetting()
        } else {
            setIsLoading(false)
            navigator.initialAdapter()
        }
    }

    fun clearHttpCache() {
        dataManager.clearHttpCache()
    }

    fun disposeObservable() {
        compositeDisposable.clear()
    }

    fun setNotification(notify: Boolean) {
        if (notification.value != notify) {
            notification.value = notify
        }
    }

    fun defaultSetting() {
        val request = GetDefaultSettingQuery()

        compositeDisposable.add(dataManager.doGetDefaultSettingApiCall(request)
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    try {
                        val response = it.data!!
                        if (response.getSearchSettings?.status == 200 &&
                            response.currency?.status == 200
                        ) {
                            if (setCurrency(response.currency)) {
                                navigator.initialAdapter()
                            } else {
                                if (response.currency!!.errorMessage == null)
                                    navigator.showToast(
                                        response.currency!!.errorMessage.toString()
                                    )
                            }
                        } else {
                            if (response.getSearchSettings?.errorMessage == null)
                                (navigator as BaseNavigator).showError()
                            else navigator.showToast(
                                response.getSearchSettings?.errorMessage.toString()
                            )
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        (navigator as BaseNavigator).showError()
                    }
                },
                {
                    handleException(it)
                }
            ))
    }


    private fun setCurrency(it: GetDefaultSettingQuery.Currency?): Boolean {
        try {
            if (it?.status == 200) {
                if (dataManager.currentUserCurrency == null) {
                    dataManager.currentUserCurrency = it.result!!.base!!
                }
                dataManager.currencyBase = it.result!!.base
                dataManager.currencyRates = it.result!!.rates
                return true
            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
            return false
        }
        return false
    }
}