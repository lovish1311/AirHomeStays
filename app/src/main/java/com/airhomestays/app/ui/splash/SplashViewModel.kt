package com.airhomestays.app.ui.splash

import android.content.Intent
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.BuildConfig
import com.airhomestays.app.GetDefaultSettingQuery
import com.airhomestays.app.GetSecureSiteSettingsQuery
import com.airhomestays.app.GetVersionQuery
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class SplashViewModel @Inject constructor(
        dataManager: DataManager,
        val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
) : BaseViewModel<SplashNavigator>(dataManager, resourceProvider) {

    var langauge = ""
    var isHostGuest = ObservableField(0)
    val isStripeReady = MutableLiveData<Int>().apply { value = 0 }

    val showDialog = MutableLiveData<Boolean>()
    val url = MutableLiveData<String?>()

    lateinit var intent: Intent

    init {
        if (dataManager.phoneNoType==null) {
            dataManager.phoneNoType = "2"
        }
    }


    fun defaultSettingsInCache() {
        val request = GetDefaultSettingQuery()

        compositeDisposable.add(dataManager.clearHttpCache()
                .flatMap { dataManager.doGetDefaultSettingApiCall(request).toObservable() }
                .performOnBackOutOnMain(scheduler)
                .subscribe(
                        {
                            setCurrency(it.data?.currency)
                            setSiteName()
                            setLanguage()
                        },
                        {
                            loginStatus()
                        }
                ))
    }
    fun forceUpdate() {
        val request = GetVersionQuery(
            appType = "androidVersion",
            version =BuildConfig.VERSION_NAME
        )


        compositeDisposable.add(dataManager.clearHttpCache()
            .flatMap { dataManager.getVersion(request).toObservable() }
            .performOnBackOutOnMain(scheduler)
            .subscribe(
                {
                   if ( it.data?.getApplicationVersionInfo?.status==200){
                       showDialog.value=false
                       url.value= it.data?.getApplicationVersionInfo?.result?.playStoreUrl
                   }else if (it.data?.getApplicationVersionInfo?.status==400){
                       showDialog.value=true
                   }

                },
                {
                }
            ))
    }

     fun setSiteName() {
        val request = GetSecureSiteSettingsQuery(
            type = "site_settings".toOptional(),
            securityKey = resourceProvider.getString(R.string.security_key)
            )

        compositeDisposable.add(dataManager.clearHttpCache()
            .flatMap { dataManager.doGetSecureSettingApiCall(request).toObservable() }
            .performOnBackOutOnMain(scheduler)
            .subscribe( {
                try {
                    if (it.data?.getSecureSiteSettings?.results?.get(0)?.name == "siteName") {
                        dataManager.siteName = it.data?.getSecureSiteSettings?.results?.get(0)?.name
                    }
                    dataManager.listingApproval = it.data?.getSecureSiteSettings?.results!!.find {
                        it?.name == "listingApproval"
                    }?.value?.toIntOrNull() ?: 0
                    for (item in it.data?.getSecureSiteSettings?.results!!) {
                        if (item?.name == "phoneNumberStatus") {
                            dataManager.phoneNoType = item.value
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }, {
                it.printStackTrace()
               }
        ))
    }

    private fun setCurrency(it: GetDefaultSettingQuery.Currency?) {
        try {
            if (it?.status == 200) {
                if (dataManager.currentUserCurrency == null) {
                    dataManager.currentUserCurrency = it.result!!.base!!
                }
                dataManager.currencyBase = it.result!!.base
                dataManager.currencyRates = it.result!!.rates
            }
            loginStatus()
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
        }
    }

    private fun setLanguage() {
        try {
            if (dataManager.currentUserLanguage == null) {
                dataManager.currentUserLanguage = langauge

            }
        } catch (e: KotlinNullPointerException) {
            e.printStackTrace()
        }
    }

    fun loginStatus() {
        compositeDisposable.add(Observable.just(dataManager.currentUserLoggedInMode)
                .performOnBackOutOnMain(scheduler)
                .subscribe { decideNextActivity(it) }
        )
    }

    private fun decideNextActivity(type: Int) {
        if (intent.hasExtra("content")) {
            navigator.openInboxActivity()
        }
       else if (type == DataManager.LoggedInMode.LOGGED_IN_MODE_LOGGED_OUT.type) {
           if (isHostGuest.get()==3){
               navigator.openLoginActivity()
           }else{
               navigator.openMainActivity()
           }

        } else {
            if (dataManager.isHostOrGuest) {
                navigator.openHostActivity()
            } else {
                navigator.openMainActivity()
            }
        }
    }


}
