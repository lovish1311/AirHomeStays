package com.airhomestays.app.ui.base

import android.util.Log
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.ViewModel
import com.apollographql.apollo3.exception.ApolloNetworkException
import com.airhomestays.app.GetUnReadCountQuery
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.util.CurrencyUtil
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.vo.CurrencyException
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.lang.ref.WeakReference
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit


abstract class BaseViewModel<N>(
    val dataManager: DataManager,
    val baseResourceProvider: ResourceProvider
) : ViewModel() {

    val isLoading = ObservableBoolean(false)

    val compositeDisposable: CompositeDisposable = CompositeDisposable()

    private var mNavigator: WeakReference<N>? = null

    private lateinit var disposable: Disposable

    var navigator: N
        get() = mNavigator!!.get()!!
        set(navigator) {
            this.mNavigator = WeakReference(navigator)
        }

    fun setIsLoading(isLoading: Boolean) {
        this.isLoading.set(isLoading)
    }

    fun getAccessToken(): String {
        try {
            return dataManager.accessToken!!
        } catch (e: KotlinNullPointerException) {
            (navigator as BaseNavigator).openSessionExpire("BaseVM")
        }
        return ""
    }

    fun getUserId(): String {
        try {
            return dataManager.currentUserId!!
        } catch (e: KotlinNullPointerException) {
            (navigator as BaseNavigator).openSessionExpire("BaseVM")
        }
        return ""
    }

    fun getUserCurrency(): String {
        try {
            return dataManager.currentUserCurrency?:"USD"
        } catch (e: KotlinNullPointerException) {
            (navigator as BaseNavigator).openSessionExpire("BaseVM")
        }
        return ""
    }

    fun getCurrencyRates(): String {
        try {
            return dataManager.currencyRates!!
        } catch (e: KotlinNullPointerException) {
            (navigator as BaseNavigator).openSessionExpire("BaseVM")
        }
        return ""
    }

    fun getCurrencyBase(): String {
        try {
            return dataManager.currencyBase!!
        } catch (e: KotlinNullPointerException) {
            (navigator as BaseNavigator).openSessionExpire("BaseVM")
        }
        return ""
    }

    fun getConvertedRate(from: String, amount: Double): Double {
        var rate = 0.0
        try {
            rate = CurrencyUtil.getRate(
                base = getCurrencyBase(),
                to = getUserCurrency(),
                from = from,
                rateStr = getCurrencyRates(),
                amount = amount
            )
        } catch (e: CurrencyException) {
            (navigator as BaseNavigator).openSessionExpire("BaseVM")
            e.printStackTrace()
        } catch (e: Exception) {
            (navigator as BaseNavigator).showError()
            e.printStackTrace()
        }
        return rate
    }

    fun getCurrencySymbol(): String {
        return CurrencyUtil.getCurrencySymbol(getUserCurrency())
    }

    open fun handleException(e: Throwable, showToast: Boolean = false) {
        if (e.cause is SocketTimeoutException) {
            (navigator as BaseNavigator).showToast(baseResourceProvider.getString(R.string.server_error))
        } else if (e is ApolloNetworkException) {
            if (showToast) {
                (navigator as BaseNavigator).showToast(baseResourceProvider.getString(R.string.currently_offline))
            } else {
                (navigator as BaseNavigator).showOffline()
            }
        } else {
            if (showToast) {
                (navigator as BaseNavigator).showToast(baseResourceProvider.getString(R.string.something_went_wrong_action))
            } else {
                (navigator as BaseNavigator).showError()
            }
        }
    }

    fun getUnreadCountAndBanStatus() {
        val query = GetUnReadCountQuery()

        disposable = Observable.interval(5, TimeUnit.SECONDS, Schedulers.io())
            .filter { dataManager.isUserLoggedIn() }
            .switchMap {
                dataManager.getUnreadCount(query)
                    .onErrorResumeNext { _: Throwable -> Observable.empty() }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                try {
                    val result = it.data?.getUnReadCount
                    if (result!!.status == 200) {
                        if (result.results!!.userBanStatus == 1) {
                            (navigator as BaseNavigator).openSessionExpire("BaseVM")
                        } else {
                            dataManager.haveNotification = result.results?.guestCount!! > 0
                        }
                    } else if (result!!.status == 500) {
                        (navigator as BaseNavigator).openSessionExpire("BaseVM")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, { it.printStackTrace() })

        compositeDisposable.add(disposable)
    }

    fun clearUnreadCountApi() {
        if (::disposable.isInitialized) {
            compositeDisposable.remove(disposable)
        }
    }

    fun responseValidation(status: Int, action: () -> Unit, error: String) {
        when (status) {
            200 -> action()
            500 -> ""
            else -> (navigator as BaseNavigator).showToast(error)
        }
    }

    fun checkException() {

    }

    fun catchAll(message: String, action: () -> Unit) {
        try {
            action()
        } catch (t: Throwable) {
            Log.e("Failed to $message. ${t.message}", t.toString())
        }
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }


}
