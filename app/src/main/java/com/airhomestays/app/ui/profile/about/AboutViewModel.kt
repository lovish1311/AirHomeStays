package com.airhomestays.app.ui.profile.about

import androidx.lifecycle.MutableLiveData
import com.airhomestays.app.GetStaticPageContentQuery
import com.airhomestays.app.GetWhyHostDataQuery
import com.airhomestays.app.data.DataManager
import com.airhomestays.app.ui.base.BaseViewModel
import com.airhomestays.app.util.performOnBackOutOnMain
import com.airhomestays.app.util.resource.ResourceProvider
import com.airhomestays.app.util.rx.Scheduler
import com.airhomestays.app.util.toOptional
import javax.inject.Inject

class AboutViewModel @Inject constructor(
    dataManager: DataManager,
    val scheduler: Scheduler,
    val resourceProvider: ResourceProvider
) : BaseViewModel<AboutNavigator>(dataManager, resourceProvider) {

    var staticContentDetails: MutableLiveData<GetStaticPageContentQuery.Data?> = MutableLiveData()
    var whyHostData: MutableLiveData<GetWhyHostDataQuery.Data?> = MutableLiveData()

    enum class OpenScreen {
        WHY_HOST,
        FINISHED
    }

    fun onClick(openScreen: AboutViewModel.OpenScreen) {
        navigator.navigateScreen(openScreen)
    }


    fun getStaticContent(id: Int) {
        val buildQuery = GetStaticPageContentQuery(id = id.toOptional())
        compositeDisposable.add(dataManager.clearHttpCache()
            .flatMap { dataManager.getStaticPageContent(buildQuery).toObservable() }
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.getStaticPageContent
                    if (data?.status == 200) {
                        staticContentDetails.value = response.data
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("AboutVM")
                    } else {
                        if (data?.errorMessage == null)
                            navigator.showError()
                        else navigator.showToast(data?.errorMessage.toString())
                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                it.printStackTrace()

            })
        )
    }

    fun getWhyHostData() {
        val buildQuery = GetWhyHostDataQuery()
        compositeDisposable.add(dataManager.clearHttpCache()
            .flatMap { dataManager.getWhyHostData(buildQuery).toObservable() }
            .doOnSubscribe { setIsLoading(true) }
            .doFinally { setIsLoading(false) }
            .performOnBackOutOnMain(scheduler)
            .subscribe({ response ->
                try {
                    val data = response.data!!.getWhyHostData
                    if (data?.status == 200) {
                        whyHostData.value = response.data
                    } else if (data?.status == 500) {
                        navigator.openSessionExpire("AboutVM")
                    } else {
                        if (data?.errorMessage == null)
                            navigator.showError()
                        else navigator.showToast(data?.errorMessage.toString())
                    }
                } catch (e: KotlinNullPointerException) {
                    e.printStackTrace()
                    navigator.showError()
                }
            }, {
                it.printStackTrace()

            })
        )
    }
}