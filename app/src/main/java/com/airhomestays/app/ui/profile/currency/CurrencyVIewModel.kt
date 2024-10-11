package com.airhomestays.app.ui.profile.currency

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.GetCurrenciesListQuery
import com.airhomestays.app.R
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseNavigator
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.Event
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.vo.Outcome
import javax.inject.Inject

@SuppressLint("LogNotTimber")
class CurrencyVIewModel @Inject constructor(
        dataManager: DataManager,
        private val scheduler: Scheduler,
        val resourceProvider: ResourceProvider
): BaseViewModel<BaseNavigator>(dataManager,resourceProvider) {

    val postsOutcome = MutableLiveData<Event<Outcome<List<GetCurrenciesListQuery.Result?>?>>>()
    val preSelectedLanguages = MutableLiveData<String>()

    init {
        getCurrency()
    }

    fun getCurrency() {
        val query = GetCurrenciesListQuery()
        compositeDisposable.add(dataManager.getCurrencyList(query)
                .doOnSubscribe { postsOutcome.postValue(Event(Outcome.loading(true))) }
                .doFinally { postsOutcome.postValue(Event(Outcome.loading(false))) }
                .performOnBackOutOnMain(scheduler)
                .subscribe( { response ->
                    try {
                        val data = response.data!!.getCurrencies
                        if (data?.status == 200) {
                            postsOutcome.value = Event(Outcome.success(data.results!!))
                        } else if(data?.status == 500) {
                            navigator.openSessionExpire("CurrencyVM")
                        } else {
                            postsOutcome.value = Event(Outcome.error(Throwable()))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        postsOutcome.value = Event(Outcome.error(Throwable()))
                        navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                    }
                }, {
                    postsOutcome.value = Event(Outcome.error(Throwable()))
                    handleException(it, true)
                } )
        )
    }


}