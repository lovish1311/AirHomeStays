package com.airhomestays.app.ui.profile.languages

import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.R
import com.airhomestays.app.UserPreferredLanguagesQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseNavigator
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.Event
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.vo.Outcome
import javax.inject.Inject

class LanguagesViewModel @Inject constructor(
    dataManager: DataManager,
    private val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<BaseNavigator>(dataManager, resourceProvider) {

    val postsOutcome = MutableLiveData<Event<Outcome<List<UserPreferredLanguagesQuery.Result?>>>>()
    val preSelectedLanguages = MutableLiveData<String>()

    init {
        getLanguages()
    }

    fun getLanguages() {
        val query = UserPreferredLanguagesQuery()
        compositeDisposable.add(dataManager.doGetLanguagesApiCall(query)
            .doOnSubscribe { postsOutcome.postValue(Event(Outcome.loading(true))) }
            .doFinally { postsOutcome.postValue(Event(Outcome.loading(false))) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.userLanguages
                    if (data?.status == 200) {
                        postsOutcome.value = Event(Outcome.success(data.result!!.toList()))
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("LanguageVM")
                    } else {
                        postsOutcome.value = Event(Outcome.error(Throwable()))
                        if (data?.errorMessage == null)
                            navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                        else navigator.showToast(data.errorMessage.toString())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    postsOutcome.value = Event(Outcome.error(Throwable()))
                    navigator.showToast(resourceProvider.getString(R.string.something_went_wrong))
                }
            }, {
                postsOutcome.value = Event(Outcome.error(Throwable()))
                handleException(it, true)
            })
        )
    }

}